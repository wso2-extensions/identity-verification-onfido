/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.verification.onfido.connector;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.extension.identity.verification.mgt.AbstractIdentityVerifier;
import org.wso2.carbon.extension.identity.verification.mgt.IdentityVerifier;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationClientException;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationException;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationServerException;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVProperty;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdentityVerifierData;
import org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants;
import org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationExceptionMgt;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;
import org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants;
import org.wso2.carbon.identity.verification.onfido.connector.exception.OnfidoClientException;
import org.wso2.carbon.identity.verification.onfido.connector.exception.OnfidoServerException;
import org.wso2.carbon.identity.verification.onfido.connector.internal.OnfidoIDVDataHolder;
import org.wso2.carbon.identity.verification.onfido.connector.web.OnfidoAPIClient;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.UniqueIDUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.ErrorMessage.ERROR_GETTING_USER_STORE;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.APPLICANT_ID;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.BASE_URL;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_CLAIM_VALUE_NOT_EXIST;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_GETTING_ONFIDO_WORKFLOW_STATUS;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_EMPTY;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_INITIATING_ONFIDO_VERIFICATION;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_IDV_PROVIDER_INVALID_OR_DISABLED;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_INVALID_ONFIDO_SDK_FLOW_STATUS;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_VERIFICATION_ALREADY_INITIATED;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_VERIFICATION_FLOW_STATUS_NOT_FOUND;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ID;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ONFIDO_APPLICANT_ID;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ONFIDO_WORKFLOW_RUN_ID;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ONFIDO_WORKFLOW_STATUS;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.SDK_TOKEN;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.STATUS;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.TOKEN;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.WEBHOOK_TOKEN;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.WORKFLOW_ID;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_NON_EXISTING_USER;

/**
 * This class contains the implementation of OnfidoIdentityVerifier.
 */
public class OnfidoIdentityVerifier extends AbstractIdentityVerifier implements IdentityVerifier {

    private static final Log log = LogFactory.getLog(OnfidoIdentityVerifier.class);

    @Override
    public IdentityVerifierData verifyIdentity(String userId, IdentityVerifierData identityVerifierData, int tenantId)
            throws IdentityVerificationException {

        // Get corresponding identity verification provider.
        IdVProvider idVProvider = getIdVProvider(identityVerifierData, tenantId);
        if (idVProvider == null || !idVProvider.isEnabled()) {
            throw new IdentityVerificationClientException(ERROR_IDV_PROVIDER_INVALID_OR_DISABLED.getCode(),
                    ERROR_IDV_PROVIDER_INVALID_OR_DISABLED.getMessage());
        }

        // Get identity verification process related properties that are sent through identity verification request.
        Map<String, String> idVProperties = getIdVPropertyMap(identityVerifierData);

        // Get identity verification provider configurations.
        Map<String, String> idVProviderConfigProperties = getIdVConfigPropertyMap(idVProvider);
        validateIdVProviderConfigProperties(idVProviderConfigProperties);

        try {
            // Get the sdk flow status sent via the verification request
            OnfidoConstants.OnFidoSdkFlowStatus
                    flowStatusEnum = OnfidoConstants.OnFidoSdkFlowStatus.fromString(idVProperties.get(STATUS));

            List<IdVClaim> idVClaims;
            switch (flowStatusEnum) {
                case INITIATED:
                    // Initiate Onfido verification through creating applicant and retrieving sdk token
                    idVClaims = initiateOnfidoVerification(userId, identityVerifierData, tenantId, idVProvider,
                            idVProviderConfigProperties);
                    break;
                case COMPLETED:
                    // Complete the onfido sdk flow by updating the workflow run status.
                    idVClaims = completeOnfidoVerification(userId, identityVerifierData, idVProvider,
                            idVProviderConfigProperties, tenantId);
                    break;
                default:
                    throw new IdentityVerificationClientException(ERROR_INVALID_ONFIDO_SDK_FLOW_STATUS.getCode(),
                            ERROR_INVALID_ONFIDO_SDK_FLOW_STATUS.getMessage());
            }
            identityVerifierData.setIdVClaims(idVClaims);
            return identityVerifierData;

        } catch (OnfidoClientException e) {
            log.error("Invalid Onfido SDK flow status", e);
            throw new IdentityVerificationClientException(ERROR_INVALID_ONFIDO_SDK_FLOW_STATUS.getCode(),
                    ERROR_INVALID_ONFIDO_SDK_FLOW_STATUS.getMessage(), e);
        }
    }

    /**
     * Initiates the Onfido verification process for a user.
     *
     * @param userId                      The unique identifier of the user
     * @param identityVerifierData        Data required for identity verification that was passed via the verification request
     * @param tenantId                    The ID of the tenant
     * @param idVProvider                 The identity verification provider
     * @param idVProviderConfigProperties Configuration properties for the identity verification provider
     * @return A list of IdVClaims that have been initiated for verification
     * @throws IdentityVerificationException If there's an error during the verification process
     */
    private List<IdVClaim> initiateOnfidoVerification(String userId, IdentityVerifierData identityVerifierData,
                                                            int tenantId, IdVProvider idVProvider,
                                                            Map<String, String> idVProviderConfigProperties)
            throws IdentityVerificationException {

        // Retrieve the list of WSO2 claims that need to be verified
        List<IdVClaim> verificationRequiredClaims = identityVerifierData.getIdVClaims();

        // The applicants need to be created per user. Hence, if there is already an applicant ID
        // associated with the user, retrieve it. This ID is unique per user in the Onfido system.
        String applicantId = getApplicantId(userId, tenantId, idVProvider);

        // Get the map of Onfido claim names and values for the wso2 claims that haven't been verified yet.
        Map<String, String> idVProviderClaimWithValueMap =
                getIdVProviderClaimWithValueMap(userId, tenantId, idVProvider, verificationRequiredClaims);

        try {
            if (!idVProviderClaimWithValueMap.isEmpty()) {
                JSONObject applicantRequestBody = getApplicantRequestBody(idVProviderClaimWithValueMap);
                // Create a new applicant if no applicant ID exists, otherwise update the existing applicant
                // with new claims to be verified.
                if (StringUtils.isEmpty(applicantId)) {
                    JSONObject onFidoJsonObject = OnfidoAPIClient.
                            createApplicant(idVProviderConfigProperties, applicantRequestBody);

                    applicantId = (String) onFidoJsonObject.get(ID);
                } else {
                    OnfidoAPIClient.updateApplicant(idVProviderConfigProperties, applicantRequestBody);
                }

                // Create a new workflow run for the applicant
                JSONObject workflowRunRequestBody = new JSONObject();
                workflowRunRequestBody.put(WORKFLOW_ID, idVProviderConfigProperties.get(WORKFLOW_ID));
                workflowRunRequestBody.put(APPLICANT_ID, applicantId);
                JSONObject workflowRunJsonObject =
                        OnfidoAPIClient.createWorkflowRun(idVProviderConfigProperties, workflowRunRequestBody);

                // Generate a SDK token for the applicant
                JSONObject sdkTokenRequestBody = new JSONObject();
                sdkTokenRequestBody.put(APPLICANT_ID, applicantId);
                JSONObject sdkTokenJsonObject =
                        OnfidoAPIClient.createSDKToken(idVProviderConfigProperties, sdkTokenRequestBody);

                // Update the metadata of each claim to include the onfido verification process related information.
                Map<String, Object> metadata =
                        getInitiatedVerificationMetadata(applicantId, workflowRunJsonObject.get(ID).toString());
                for (IdVClaim idVClaim : verificationRequiredClaims) {
                    idVClaim.setIsVerified(false);
                    idVClaim.setUserId(userId);
                    idVClaim.setIdVPId(idVProvider.getIdVProviderUuid());
                    idVClaim.setMetadata(metadata);
                }
                // Persist the updated claims in the database
                storeIdVClaims(userId, verificationRequiredClaims, tenantId);

                /* Since storing the SDK token in the database, is not required, it will be added after storing the IDV
                claims. The SDK token will be returned for the verification initiation response in order to render the
                Onfido SDK. */
                metadata.put(SDK_TOKEN, sdkTokenJsonObject.get(TOKEN));
                for (IdVClaim idVClaim : verificationRequiredClaims) {
                    idVClaim.setMetadata(metadata);
                }
            } else {
                throw new IdentityVerificationClientException(ERROR_VERIFICATION_ALREADY_INITIATED.getCode(),
                        ERROR_VERIFICATION_ALREADY_INITIATED.getMessage());
            }
        } catch (OnfidoServerException e) {
            throw new IdentityVerificationServerException(ERROR_INITIATING_ONFIDO_VERIFICATION.getCode(),
                    ERROR_INITIATING_ONFIDO_VERIFICATION.getMessage());
        }
        return verificationRequiredClaims;
    }

    /**
     * Completes the Onfido Verification process for a user, reflecting SDK interactions.
     * This method updates the workflow status according to user activities captured through the SDK interaction.
     *
     * @param userId                      The unique identifier of the user
     * @param identityVerifierData        Contains the data
     * @param idVProvider                 The identity verification provider
     * @param idVProviderConfigProperties Configuration properties for the identity verification provider
     * @param tenantId                    The ID of the tenant
     * @return A list of IdVClaims that have their workflow status updated
     * @throws IdentityVerificationException If there's an error during the updating process
     */
    private List<IdVClaim> completeOnfidoVerification(String userId, IdentityVerifierData identityVerifierData,
                                                      IdVProvider idVProvider,
                                                      Map<String, String> idVProviderConfigProperties, int tenantId)
            throws IdentityVerificationException {

        List<IdVClaim> verificationClaim = new ArrayList<>();
        String workFlowRunId = getWorkFlowRunId(userId, tenantId, idVProvider);
        OnfidoConstants.WorkflowRunStatus
                workflowRunStatus = getOnfidoWorkflowStatus(workFlowRunId, idVProviderConfigProperties);

        // Retrieve the list of WSO2 claims requiring verification and update their workflow statuses in the metadata
        List<IdVClaim> verificationRequiredClaims = identityVerifierData.getIdVClaims();

        for (IdVClaim idVClaim : verificationRequiredClaims) {
            idVClaim = OnfidoIDVDataHolder.getInstance()
                    .getIdentityVerificationManager()
                    .getIdVClaim(userId, idVClaim.getClaimUri(), idVProvider.getIdVProviderUuid(), tenantId);

            if (!idVClaim.isVerified()) {
                updateMetadataWithWorkflowStatus(idVClaim, workflowRunStatus);
                // Persist the updated claim information in the database
                updateIdVClaim(userId, idVClaim, tenantId);
                verificationClaim.add(idVClaim);
            }
        }
        return verificationClaim;
    }

    /**
     * Retrieves the Onfido workflow status for a specified workflow run ID.
     *
     * @param workFlowRunId               The unique identifier for the workflow run whose status is to be fetched
     * @param idVProviderConfigProperties Configuration properties for the identity verification provider
     * @return The current status of the workflow run as defined in OnfidoConstants.WorkflowRunStatus
     * @throws IdentityVerificationException If there's an error in fetching the workflow status from the Onfido API
     */
    private OnfidoConstants.WorkflowRunStatus getOnfidoWorkflowStatus(String workFlowRunId,
                                                                      Map<String, String> idVProviderConfigProperties)
            throws IdentityVerificationException {

        try {
            JSONObject workflowRunStatusJsonObject =
                    OnfidoAPIClient.getWorkflowRunStatus(idVProviderConfigProperties, workFlowRunId);
            return OnfidoConstants.WorkflowRunStatus.fromString(workflowRunStatusJsonObject.getString(STATUS));
        } catch (OnfidoServerException e) {
            throw new IdentityVerificationServerException(ERROR_GETTING_ONFIDO_WORKFLOW_STATUS.getCode(),
                    ERROR_GETTING_ONFIDO_WORKFLOW_STATUS.getMessage());
        }
    }

    /**
     * Updates the metadata of an identity verification claim based on the given workflow run status.
     * This method ensures that the workflow status in the metadata is not prematurely set to an ending status.
     * Instead, ending statuses are managed through a webhook to ensure they accurately reflect the actual
     * completion of the verification process.
     *
     * The method modifies the claim's metadata directly, setting the workflow status to 'PROCESSING' if
     * the current status is an ending one. Otherwise, it updates the metadata with the actual current workflow status.
     *
     * @param idVClaim          The identity verification claim whose metadata is being updated.
     * @param workflowRunStatus The current status of the workflow which dictates how the metadata is updated.
     */
    private static void updateMetadataWithWorkflowStatus(IdVClaim idVClaim,
                                                         OnfidoConstants.WorkflowRunStatus workflowRunStatus) {

        Map<String, Object> metadata = idVClaim.getMetadata();
        if (workflowRunStatus.isEndingStatus()) {
            metadata.put(ONFIDO_WORKFLOW_STATUS, OnfidoConstants.WorkflowRunStatus.PROCESSING.getStatus());
        } else {
            metadata.put(ONFIDO_WORKFLOW_STATUS, workflowRunStatus.getStatus());
        }
        idVClaim.setMetadata(metadata);
    }

    /**
     * Retrieves a map of Onfido claim names to their corresponding values for WSO2 claims that have not yet been verified.
     * This method filters out claims that have already been associated with an Onfido applicant ID, ensuring that only
     * unverified claims are processed. It queries the user store to fetch the values of these claims for a specified user.
     *
     * @param userId                   The unique identifier of the user
     * @param tenantId                 The ID of the tenant
     * @param idVProvider              The identity verification provider
     * @param verificationRequiredClaims List of claims that require verification
     * @return A map where each key is an IDV provider claim URI and each value is the corresponding user claim value
     * @throws IdentityVerificationException if there is an error retrieving the claim values or mappings
     */
    private Map<String, String> getIdVProviderClaimWithValueMap(String userId, int tenantId, IdVProvider idVProvider,
                                                                List<IdVClaim> verificationRequiredClaims)
            throws IdentityVerificationException {

        Map<String, String> idVProviderClaimWithValueMap = new HashMap<>();
        try {
            Map<String, String> idVClaimMap = idVProvider.getClaimMappings();
            UniqueIDUserStoreManager uniqueIDUserStoreManager = getUniqueIdEnabledUserStoreManager(tenantId);

            for (IdVClaim idVClaim : verificationRequiredClaims) {
                String claimUri = idVClaim.getClaimUri();
                idVClaim = OnfidoIDVDataHolder.getInstance().
                        getIdentityVerificationManager().getIdVClaim(userId, idVClaim.getClaimUri(),
                                idVProvider.getIdVProviderUuid(), tenantId);

                if (idVClaim == null || idVClaim.getMetadata() == null ||
                        idVClaim.getMetadata().get(ONFIDO_APPLICANT_ID) == null) {
                    String claimValue = uniqueIDUserStoreManager.getUserClaimValueWithID(userId, claimUri, null);
                    if (StringUtils.isEmpty(claimValue)) {
                        throw new IdentityVerificationClientException(ERROR_CLAIM_VALUE_NOT_EXIST.getCode(),
                                String.format(ERROR_CLAIM_VALUE_NOT_EXIST.getMessage(), claimUri));
                    }
                    idVProviderClaimWithValueMap.put(idVClaimMap.get(claimUri), claimValue);
                }
            }
        } catch (UserStoreException e) {
            if (StringUtils.isNotBlank(e.getMessage()) &&
                    e.getMessage().contains(ERROR_CODE_NON_EXISTING_USER.getCode())) {
                if (log.isDebugEnabled()) {
                    log.debug("User does not exist with the given user id: " + userId);
                }
            }
            throw IdentityVerificationExceptionMgt.handleServerException(
                    IdentityVerificationConstants.ErrorMessage.ERROR_RETRIEVING_IDV_CLAIM_MAPPINGS, userId, e);
        }
        return idVProviderClaimWithValueMap;
    }

    /**
     * Creates a metadata map for tracking the Onfido verification process. The metadata is used to monitor
     * the status and progression of identity verification claims within the system.
     *
     * @param applicantId   The unique identifier of the applicant in the Onfido system
     * @param workflowRunId The identifier of the workflow run associated with the applicant's verification process
     * @return A map containing key-value pairs of metadata related to the Onfido verification process
     */
    private Map<String, Object> getInitiatedVerificationMetadata(String applicantId, String workflowRunId) {

        Map<String, Object> metadata = new HashMap<>();
        metadata.put(ONFIDO_APPLICANT_ID, applicantId);
        metadata.put(ONFIDO_WORKFLOW_RUN_ID, workflowRunId);
        metadata.put(ONFIDO_WORKFLOW_STATUS, OnfidoConstants.WorkflowRunStatus.AWAITING_INPUT.getStatus());
        return metadata;
    }

    /**
     * Retrieves the applicant ID from the metadata of an existing identity verification claim associated
     * with the given user and identity verification provider.
     *
     * @param userId     The unique identifier of the user whose applicant ID is being retrieved.
     * @param tenantId   The ID of the tenant.
     * @param idVProvider The identity verification provider.
     * @return The applicant ID if found, otherwise returns null.
     * @throws IdentityVerificationException If there is an error accessing the claims.
     */
    private static String getApplicantId(String userId, int tenantId, IdVProvider idVProvider)
            throws IdentityVerificationException {

        String applicantId = null;
        IdVClaim[] idVClaims = OnfidoIDVDataHolder.getInstance().getIdentityVerificationManager().
                getIdVClaims(userId, idVProvider.getIdVProviderUuid(), null, tenantId);
        for (IdVClaim idVClaim : idVClaims) {
            if (idVClaim != null && idVClaim.getMetadata() != null &&
                    idVClaim.getMetadata().get(ONFIDO_APPLICANT_ID) != null && !idVClaim.isVerified()) {
                applicantId = (String) idVClaim.getMetadata().get(ONFIDO_APPLICANT_ID);
                break;
            }
        }
        return applicantId;
    }

    /**
     * Retrieves the workflow run ID from the metadata of an existing identity verification claim associated
     * with the given user and identity verification provider.
     *
     * @param userId     The unique identifier of the user whose applicant ID is being retrieved.
     * @param tenantId   The ID of the tenant.
     * @param idVProvider The identity verification provider.
     * @return The applicant ID if found, otherwise returns null.
     * @throws IdentityVerificationException If there is an error accessing the claims.
     */
    private static String getWorkFlowRunId(String userId, int tenantId, IdVProvider idVProvider)
            throws IdentityVerificationException {

        String workFlowRunId = null;
        IdVClaim[] idVClaims = OnfidoIDVDataHolder.getInstance().getIdentityVerificationManager().
                getIdVClaims(userId, idVProvider.getIdVProviderUuid(), null, tenantId);
        for (IdVClaim idVClaim : idVClaims) {
            if (idVClaim != null && idVClaim.getMetadata() != null &&
                    idVClaim.getMetadata().get(ONFIDO_WORKFLOW_RUN_ID) != null && !idVClaim.isVerified()) {
                workFlowRunId = (String) idVClaim.getMetadata().get(ONFIDO_WORKFLOW_RUN_ID);
                break;
            }
        }
        return workFlowRunId;
    }

    /**
     * Extracts the 'status' property from the identity verification request payload. This method filters out all
     * properties except 'status', ensuring it is present and valid. If 'status' is absent
     * or the property list is empty, it throws an exception.
     *
     * @param identityVerifierData Data required for identity verification that was passed via the verification request
     * @return A map with the 'status' property key and value.
     * @throws IdentityVerificationClientException If 'status' is missing or the property list is empty.
     */
    private Map<String, String> getIdVPropertyMap(IdentityVerifierData identityVerifierData)
            throws IdentityVerificationClientException {

        List<IdVProperty> identityVerificationProperties = identityVerifierData.getIdVProperties();
        if (identityVerificationProperties == null || identityVerificationProperties.isEmpty()) {
            throw new IdentityVerificationClientException(ERROR_VERIFICATION_FLOW_STATUS_NOT_FOUND.getCode(),
                    ERROR_VERIFICATION_FLOW_STATUS_NOT_FOUND.getMessage());
        }

        Map<String, String> idVPropertyMap = new HashMap<>();
        boolean statusFound = false;
        for (IdVProperty idVProperty : identityVerificationProperties) {
            if (StringUtils.equals(idVProperty.getName(), STATUS) && StringUtils.isNotBlank(idVProperty.getValue())) {
                idVPropertyMap.put(idVProperty.getName(), idVProperty.getValue());
                statusFound = true;
                break;
            }
        }

        if (!statusFound) {
            throw new IdentityVerificationClientException(ERROR_VERIFICATION_FLOW_STATUS_NOT_FOUND.getCode(),
                    ERROR_VERIFICATION_FLOW_STATUS_NOT_FOUND.getMessage());
        }

        return idVPropertyMap;
    }

    /**
     * Constructs a JSON object to represent an applicant's identity verification claims.
     *
     * @param idVClaimsWithValues A map containing claim keys and their corresponding values.
     * @return A JSON object containing the mapped claim data.
     */
    private JSONObject getApplicantRequestBody(Map<String, String> idVClaimsWithValues) {

        JSONObject idVClaimRequestBody = new JSONObject();
        for (Map.Entry<String, String> idVClaim : idVClaimsWithValues.entrySet()) {
            idVClaimRequestBody.put(idVClaim.getKey(), idVClaim.getValue());
        }
        return idVClaimRequestBody;
    }

    /**
     * Retrieves an instance of UniqueIDUserStoreManager for a specified tenant.
     *
     * @param tenantId The ID of the tenant.
     * @return An instance of UniqueIDUserStoreManager if the user store manager is of the correct type.
     * @throws IdentityVerificationServerException If the UserStoreManager is not an instance of UniqueIDUserStoreManager.
     * @throws UserStoreException                  If there is a failure in retrieving the UserStoreManager.
     */
    private UniqueIDUserStoreManager getUniqueIdEnabledUserStoreManager(int tenantId)
            throws IdentityVerificationServerException, UserStoreException {

        RealmService realmService = OnfidoIDVDataHolder.getInstance().getRealmService();
        UserStoreManager userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
        if (!(userStoreManager instanceof UniqueIDUserStoreManager)) {
            throw IdentityVerificationExceptionMgt.handleServerException(ERROR_GETTING_USER_STORE);
        }
        return (UniqueIDUserStoreManager) userStoreManager;
    }

    /**
     * Validates the configuration properties for an identity verification provider.
     *
     * @param idVProviderConfigProperties A map containing the configuration properties for the identity verification provider.
     * @throws IdentityVerificationServerException If the configuration properties are incomplete or invalid.
     */
    private void validateIdVProviderConfigProperties(Map<String, String> idVProviderConfigProperties)
            throws IdentityVerificationServerException {

        if (idVProviderConfigProperties == null || idVProviderConfigProperties.isEmpty() ||
                StringUtils.isBlank(idVProviderConfigProperties.get(TOKEN)) ||
                StringUtils.isBlank(idVProviderConfigProperties.get(BASE_URL)) ||
                StringUtils.isBlank(idVProviderConfigProperties.get(WEBHOOK_TOKEN)) ||
                StringUtils.isBlank(idVProviderConfigProperties.get(WORKFLOW_ID))) {

            throw new IdentityVerificationServerException(ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_EMPTY.getCode(),
                    ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_EMPTY.getMessage());
        }
    }
}
