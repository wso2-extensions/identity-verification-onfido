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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.wso2.carbon.extension.identity.verification.mgt.utils.IdentityVerificationConstants.ErrorMessage.ERROR_GETTING_USER_STORE;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.APPLICANT_ID;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.BASE_URL;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_APPLICANT_ID_NOT_FOUND;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_CLAIM_VALUE_NOT_EXIST;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_GETTING_ONFIDO_WORKFLOW_STATUS;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_EMPTY;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_IDV_PROVIDER_INVALID_OR_DISABLED;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_INITIATING_ONFIDO_VERIFICATION;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_INVALID_ONFIDO_VERIFICATION_FLOW_STATUS;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_ONFIDO_WORKFLOW_RUN_ID_NOT_FOUND;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_REINITIATING_ONFIDO_VERIFICATION;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_REINITIATION_NOT_ALLOWED;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_RETRIEVING_CLAIMS_AGAINST_WORKFLOW_RUN_ID;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_VERIFICATION_ALREADY_INITIATED;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_VERIFICATION_FLOW_STATUS_NOT_FOUND;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_VERIFICATION_REQUIRED_CLAIMS_NOT_FOUND;
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
public class OnfidoIdentityVerifier extends AbstractIdentityVerifier {

    private static final Log log = LogFactory.getLog(OnfidoIdentityVerifier.class);

    @Override
    public IdentityVerifierData verifyIdentity(String userId, IdentityVerifierData identityVerifierData, int tenantId)
            throws IdentityVerificationException {

        // Retrieve identity verification provider.
        IdVProvider idVProvider = getValidatedIdVProvider(identityVerifierData, tenantId);

        // Retrieve identity verification provider's configurations.
        Map<String, String> idVProviderConfigProperties = getValidatedIdVConfigProperties(idVProvider);

        // Extract the verification flow status sent via the verification request.
        OnfidoConstants.VerificationFlowStatus verificationFlowStatus = getVerificationFlowStatus(identityVerifierData);
        List<IdVClaim> idVClaims;

        switch (verificationFlowStatus) {
            case INITIATED:
                // Initiate Onfido verification through creating/updating applicant and retrieving sdk token.
                idVClaims = initiateOnfidoVerification(userId, identityVerifierData, idVProvider,
                        idVProviderConfigProperties, tenantId);
                break;
            case COMPLETED:
                // Complete the onfido verification flow by updating the workflow run status.
                idVClaims = completeOnfidoVerification(userId, identityVerifierData, idVProvider,
                        idVProviderConfigProperties, tenantId);
                break;
            case REINITIATED:
                 // Resends the SDK token for claims with AWAITING_INPUT status.
                 // This reinitiates the Onfido verification flow for incomplete verifications.
                idVClaims = reinitiateOnfidoVerification(userId, identityVerifierData, idVProvider,
                        idVProviderConfigProperties, tenantId);
                break;
            default:
                throw new IdentityVerificationClientException(ERROR_INVALID_ONFIDO_VERIFICATION_FLOW_STATUS.getCode(),
                        ERROR_INVALID_ONFIDO_VERIFICATION_FLOW_STATUS.getMessage());
        }
        identityVerifierData.setIdVClaims(idVClaims);

        return identityVerifierData;
    }

    /**
     * Initiates the Onfido verification process for a user.
     *
     * @param userId                      The unique identifier of the user.
     * @param identityVerifierData        Data required for identity verification that was passed via the
     *                                    verification request.
     * @param tenantId                    The ID of the tenant.
     * @param idVProvider                 The identity verification provider.
     * @param idVProviderConfigProperties Configuration properties for the identity verification provider.
     * @return A list of IdVClaims that have been initiated for verification.
     * @throws IdentityVerificationException If there's an error during the verification process.
     */
    private List<IdVClaim> initiateOnfidoVerification(String userId, IdentityVerifierData identityVerifierData,
                                                      IdVProvider idVProvider,
                                                      Map<String, String> idVProviderConfigProperties, int tenantId)
            throws IdentityVerificationException {

        // Retrieve the list of WSO2 claims that need to be verified.
        List<IdVClaim> verificationRequiredClaims = getVerificationRequiredClaims(identityVerifierData);

        List<IdVClaim> claimsToUpdate = new ArrayList<>();

        // The applicant need to be created per user. Hence, if there is already an applicant ID
        // associated with the user, retrieve it. This ID is unique per user in the Onfido system.
        String applicantId = getApplicantId(userId, tenantId, idVProvider);

        // Get the map of Onfido claim names and values for the wso2 claims that haven't initiated verification yet.
        Map<String, String> unverifiedOnfidoClaimsWithValueMap =
                getUnverifiedOnfidoClaimsWithValueMap(userId, tenantId, idVProvider, verificationRequiredClaims,
                        claimsToUpdate);
        if (unverifiedOnfidoClaimsWithValueMap.isEmpty()) {
            throw new IdentityVerificationClientException(ERROR_VERIFICATION_ALREADY_INITIATED.getCode(),
                    ERROR_VERIFICATION_ALREADY_INITIATED.getMessage());
        }

        try {
            // Create new applicant or update existing one with new claims to be verified.
            applicantId = createOrUpdateApplicant(idVProviderConfigProperties, unverifiedOnfidoClaimsWithValueMap,
                    applicantId);

            // Create a new workflow run and generate an SDK token for the applicant.
            String workflowRunId = createWorkflowRun(idVProviderConfigProperties, applicantId);
            String sdkToken = createSdkToken(idVProviderConfigProperties, applicantId);

            // Update the metadata of each claim to include the Onfido verification process information and
            // persist the changes in the database.
            Map<String, Object> metadata = getInitiatedVerificationMetadata(applicantId, workflowRunId);
            updateAndStoreClaims(userId, tenantId, idVProvider, verificationRequiredClaims, claimsToUpdate, metadata);

            /* Since storing the SDK token in the database, is not required, it will be added after storing the IDV
            claims. The SDK token will be returned for the verification initiation response in order to render the
            Onfido SDK. */
            metadata.put(SDK_TOKEN, sdkToken);
            for (IdVClaim idVClaim : verificationRequiredClaims) {
                idVClaim.setMetadata(metadata);
            }
        } catch (OnfidoServerException e) {
            throw new IdentityVerificationServerException(ERROR_INITIATING_ONFIDO_VERIFICATION.getCode(),
                    String.format(ERROR_INITIATING_ONFIDO_VERIFICATION.getMessage(), userId), e);
        }
        return verificationRequiredClaims;
    }

    /**
     * Completes the Onfido Verification process for a user, reflecting SDK interactions.
     * This method updates the workflow status according to user activities captured through the SDK interaction.
     *
     * @param userId                      The unique identifier of the user.
     * @param identityVerifierData        Contains the data.
     * @param idVProvider                 The identity verification provider.
     * @param idVProviderConfigProperties Configuration properties for the identity verification provider.
     * @param tenantId                    The ID of the tenant.
     * @return A list of IdVClaims that have their workflow status updated.
     * @throws IdentityVerificationException If there's an error during the updating process.
     */
    private List<IdVClaim> completeOnfidoVerification(String userId, IdentityVerifierData identityVerifierData,
                                                      IdVProvider idVProvider,
                                                      Map<String, String> idVProviderConfigProperties, int tenantId)
            throws IdentityVerificationException {

        // Extract workflow run ID and check the status.
        String workflowRunId = getWorkflowRunId(userId, tenantId, idVProvider, identityVerifierData);
        OnfidoConstants.WorkflowRunStatus
                workflowRunStatus = getWorkflowRunStatusFromAPI(workflowRunId, idVProviderConfigProperties);

        // Retrieve IdVClaims associated with the workflow run ID.
        List<IdVClaim> idVClaims = getIdVClaimsByWorkflowRunId(workflowRunId, idVProvider.getIdVProviderUuid(),
                tenantId);

        // Update the workflow run status.
        for (IdVClaim idVClaim : idVClaims) {
            if (!idVClaim.isVerified()) {
                updateMetadataWithWorkflowStatus(idVClaim, workflowRunStatus);
                // Persist the updated claim information in the database.
                updateIdVClaim(userId, idVClaim, tenantId);
            }
        }
        return idVClaims;
    }

    /**
     * Reinitiates the Onfido Verification process, when a previous verification attempt is in an 'AWAITING_INPUT'
     * state and needs to be restarted. It generates a new SDK token, allowing the user to
     * continue an incomplete verification process.
     *
     * @param identityVerifierData        Data required for identity verification that was passed via the
     *                                    verification request.
     * @param idVProvider                 The identity verification provider.
     * @param idVProviderConfigProperties Configuration properties for the identity verification provider.
     * @param tenantId                    The ID of the tenant.
     * @return A list of IdVClaims updated with the new SDK token.
     * @throws IdentityVerificationException If there's an error during the reinitiation process or if
     *                                       the current state is not 'AWAITING_INPUT'.
     */
    private List<IdVClaim> reinitiateOnfidoVerification(String userId, IdentityVerifierData identityVerifierData,
                                                        IdVProvider idVProvider,
                                                        Map<String, String> idVProviderConfigProperties, int tenantId)
            throws IdentityVerificationException {

        // Extract workflow run ID.
        String workflowRunId = getWorkflowRunId(userId, tenantId, idVProvider, identityVerifierData);

        // Retrieve IdVClaims associated with the workflow run ID.
        List<IdVClaim> idVClaims = getIdVClaimsByWorkflowRunId(workflowRunId, idVProvider.getIdVProviderUuid(),
                tenantId);

        try {
            // Extract the workflow run status from claim metadata.
            OnfidoConstants.WorkflowRunStatus workflowRunStatus = getWorkflowRunStatusFromClaims(idVClaims);

            if (workflowRunStatus != OnfidoConstants.WorkflowRunStatus.AWAITING_INPUT) {
                throw new IdentityVerificationClientException(ERROR_REINITIATION_NOT_ALLOWED.getCode(),
                        ERROR_REINITIATION_NOT_ALLOWED.getMessage());
            }

            // Extract applicant ID from claim metadata.
            String applicantId = getApplicantIdFromClaims(idVClaims);

            // Create a new SDK token.
            String sdkToken = createSdkToken(idVProviderConfigProperties, applicantId);

            // Update all claims with the new SDK token.
            idVClaims.forEach(claim -> claim.getMetadata().put(SDK_TOKEN, sdkToken));
        } catch (OnfidoServerException e) {
            throw new IdentityVerificationServerException(ERROR_REINITIATING_ONFIDO_VERIFICATION.getCode(),
                    ERROR_REINITIATING_ONFIDO_VERIFICATION.getMessage(), e);
        }
        return idVClaims;
    }

    /**
     * Retrieves and validates the Identity Verification Provider.
     *
     * @param identityVerifierData Data required for identity verification that was passed via the verification request.
     * @param tenantId             The ID of the tenant.
     * @return A validated IdVProvider object.
     * @throws IdentityVerificationClientException If the idv provider is null, disabled, or invalid.
     */
    private IdVProvider getValidatedIdVProvider(IdentityVerifierData identityVerifierData, int tenantId)
            throws IdentityVerificationException {

        IdVProvider idVProvider = getIdVProvider(identityVerifierData, tenantId);
        if (idVProvider == null || !idVProvider.isEnabled()) {
            throw new IdentityVerificationClientException(ERROR_IDV_PROVIDER_INVALID_OR_DISABLED.getCode(),
                    ERROR_IDV_PROVIDER_INVALID_OR_DISABLED.getMessage());
        }
        return idVProvider;
    }

    /**
     * Retrieves and validates the configuration properties for the Identity Verification Provider.
     *
     * @param idVProvider The Identity Verification Provider object.
     * @return A map of validated configuration properties.
     * @throws IdentityVerificationClientException If the configuration properties are null, empty,
     *                                       or missing required fields.
     */
    private Map<String, String> getValidatedIdVConfigProperties(IdVProvider idVProvider)
            throws IdentityVerificationClientException {

        Map<String, String> idVProviderConfigProperties = getIdVConfigPropertyMap(idVProvider);
        if (idVProviderConfigProperties == null || idVProviderConfigProperties.isEmpty() ||
                StringUtils.isBlank(idVProviderConfigProperties.get(TOKEN)) ||
                StringUtils.isBlank(idVProviderConfigProperties.get(BASE_URL)) ||
                StringUtils.isBlank(idVProviderConfigProperties.get(WEBHOOK_TOKEN)) ||
                StringUtils.isBlank(idVProviderConfigProperties.get(WORKFLOW_ID))) {
            throw new IdentityVerificationClientException(ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_EMPTY.getCode(),
                    ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_EMPTY.getMessage());
        }
        return idVProviderConfigProperties;
    }

    /**
     * Retrieves and validates the verification required claims.
     *
     * @param identityVerifierData Data required for identity verification that was passed via the verification request.
     * @return @return A list of IdVClaim objects that require verification.
     * @throws IdentityVerificationClientException If the list of verification-required claims is null or empty.
     */
    private static List<IdVClaim> getVerificationRequiredClaims(IdentityVerifierData identityVerifierData)
            throws IdentityVerificationClientException {

        List<IdVClaim> verificationRequiredClaims = identityVerifierData.getIdVClaims();
        if (verificationRequiredClaims == null || verificationRequiredClaims.isEmpty()) {
            throw new IdentityVerificationClientException(ERROR_VERIFICATION_REQUIRED_CLAIMS_NOT_FOUND.getCode(),
                    ERROR_VERIFICATION_REQUIRED_CLAIMS_NOT_FOUND.getMessage());
        }
        return verificationRequiredClaims;
    }

    /**
     * Creates a new applicant if no applicant ID exists, otherwise updates the existing applicant
     * with new claims to be verified. This method also handles the creation of the applicant request body.
     *
     * @param idVProviderConfigProperties  The configuration properties for the IdV provider.
     * @param idVProviderClaimWithValueMap A map of IdV provider claims and their values.
     * @param applicantId                  The existing applicant ID, if any.
     * @return The applicant ID (either newly created or existing).
     * @throws OnfidoServerException If there's an error in creating or updating the applicant.
     */
    private String createOrUpdateApplicant(Map<String, String> idVProviderConfigProperties,
                                           Map<String, String> idVProviderClaimWithValueMap,
                                           String applicantId) throws OnfidoServerException {

        JSONObject applicantRequestBody = getApplicantRequestBody(idVProviderClaimWithValueMap);

        if (StringUtils.isEmpty(applicantId)) {
            JSONObject createdApplicantJsonObject =
                    OnfidoAPIClient.createApplicant(idVProviderConfigProperties, applicantRequestBody);
            return createdApplicantJsonObject.getString(ID);
        } else {
            OnfidoAPIClient.updateApplicant(idVProviderConfigProperties, applicantRequestBody, applicantId);
            return applicantId;
        }
    }

    /**
     * Creates a new workflow run in the Onfido system for the given applicant.
     *
     * @param idVProviderConfigProperties A map containing configuration properties for the IDV provider.
     * @param applicantId                 The unique identifier of the applicant in the Onfido system.
     * @return The ID of the newly created workflow run.
     * @throws OnfidoServerException If there's an error in creating the workflow run or processing the response.
     */
    private String createWorkflowRun(Map<String, String> idVProviderConfigProperties, String applicantId)
            throws OnfidoServerException {

        JSONObject workflowRunRequestBody = new JSONObject()
                .put(WORKFLOW_ID, idVProviderConfigProperties.get(WORKFLOW_ID))
                .put(APPLICANT_ID, applicantId);
        JSONObject workflowRunJsonObject =
                OnfidoAPIClient.createWorkflowRun(idVProviderConfigProperties, workflowRunRequestBody);
        return workflowRunJsonObject.getString(ID);
    }

    /**
     * Creates an SDK token in the Onfido system for a specific applicant.
     *
     * @param idVProviderConfigProperties A map containing configuration properties for the IDV provider.
     * @param applicantId                 The unique identifier of the applicant in the Onfido system.
     * @return The value of the created SDK token.
     * @throws OnfidoServerException If there's an error in creating the SDK token or processing the response.
     */
    private String createSdkToken(Map<String, String> idVProviderConfigProperties, String applicantId)
            throws OnfidoServerException {

        JSONObject sdkTokenRequestBody = new JSONObject().put(APPLICANT_ID, applicantId);
        JSONObject sdkTokenJsonObject =
                OnfidoAPIClient.createSDKToken(idVProviderConfigProperties, sdkTokenRequestBody);
        return sdkTokenJsonObject.getString(TOKEN);
    }

    /**
     * Retrieves the Onfido workflow status for a specified workflow run ID.
     *
     * @param workFlowRunId               The unique identifier for the workflow run whose status is to be fetched.
     * @param idVProviderConfigProperties Configuration properties for the identity verification provider.
     * @return The current status of the workflow run as defined in OnfidoConstants.WorkflowRunStatus.
     * @throws IdentityVerificationException If there's an error in fetching the workflow status from the Onfido API.
     */
    private OnfidoConstants.WorkflowRunStatus getWorkflowRunStatusFromAPI(
            String workFlowRunId, Map<String, String> idVProviderConfigProperties)
            throws IdentityVerificationException {

        try {
            JSONObject workflowRunStatusJsonObject =
                    OnfidoAPIClient.getWorkflowRunStatus(idVProviderConfigProperties, workFlowRunId);
            return OnfidoConstants.WorkflowRunStatus.fromString(workflowRunStatusJsonObject.getString(STATUS));
        } catch (OnfidoServerException e) {
            throw new IdentityVerificationServerException(ERROR_GETTING_ONFIDO_WORKFLOW_STATUS.getCode(),
                    ERROR_GETTING_ONFIDO_WORKFLOW_STATUS.getMessage(), e);
        }
    }

    /**
     * Retrieves a list of IdVClaims associated with a specific workflow run ID.
     *
     * @param workflowRunId   The unique identifier of the Onfido workflow run.
     * @param idVProviderUuid The UUID of the identity verification provider.
     * @param tenantId        The ID of the tenant.
     * @return A list of IdVClaims associated with the given workflow run ID.
     * @throws IdentityVerificationClientException If no claims are found for the given workflow run ID.
     * @throws IdentityVerificationException       If there's an error during the retrieval process.
     */
    private List<IdVClaim> getIdVClaimsByWorkflowRunId(String workflowRunId, String idVProviderUuid, int tenantId)
            throws IdentityVerificationException {

        IdVClaim[] idVClaimArray = OnfidoIDVDataHolder.getIdentityVerificationManager()
                .getIdVClaimsByMetadata(ONFIDO_WORKFLOW_RUN_ID, workflowRunId, idVProviderUuid, tenantId);
        List<IdVClaim> idVClaims = new ArrayList<>(Arrays.asList(idVClaimArray));
        if (idVClaims.isEmpty()) {
            throw new IdentityVerificationClientException(ERROR_RETRIEVING_CLAIMS_AGAINST_WORKFLOW_RUN_ID.getCode(),
                    ERROR_RETRIEVING_CLAIMS_AGAINST_WORKFLOW_RUN_ID.getMessage());
        }
        return idVClaims;
    }

    /**
     * Retrieves the workflow run status from the metadata of the IdVClaim.
     *
     * @param idVClaims A list of IdVClaims associated with a specific workflow run.
     * @return The WorkflowRunStatus extracted from the claim's metadata.
     * @throws OnfidoServerException If there's an error parsing the workflow status string.
     */
    private OnfidoConstants.WorkflowRunStatus getWorkflowRunStatusFromClaims(List<IdVClaim> idVClaims)
            throws OnfidoServerException {

        return OnfidoConstants.WorkflowRunStatus.fromString(
                (String) idVClaims.get(0).getMetadata().get(ONFIDO_WORKFLOW_STATUS));
    }

    /**
     * Extracts the Onfido applicant ID from the metadata of the IdVClaim.
     *
     * @param idVClaims A list of IdVClaims associated with a specific applicant.
     * @return The Onfido applicant ID extracted from the claim's metadata.
     * @throws OnfidoServerException If the applicant ID is not found or is empty.
     */
    private String getApplicantIdFromClaims(List<IdVClaim> idVClaims) throws OnfidoServerException {

        String applicantId = (String) idVClaims.get(0).getMetadata().get(ONFIDO_APPLICANT_ID);
        if (StringUtils.isEmpty(applicantId)) {
            throw new OnfidoServerException(ERROR_APPLICANT_ID_NOT_FOUND.getCode(),
                    ERROR_APPLICANT_ID_NOT_FOUND.getMessage());
        }
        return applicantId;
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
     * Retrieves unverified Onfido claims with their values for a specified user.
     * <p>
     * This method handles a list of verification-required claims for a given user. It performs the following tasks:
     * 1. Filters out claims already associated with an Onfido applicant ID. For the remaining claims,
     * it fetches their values from the user store.
     * 2. Identifies existing claims that need to be updated.
     * 3. Creates and return a map of Onfido claim names to their corresponding user store values.
     *
     * @param userId                     The unique identifier of the user.
     * @param tenantId                   The ID of the tenant.
     * @param idVProvider                The identity verification provider.
     * @param verificationRequiredClaims List of claims that require verification.
     * @param claimsToUpdate             Output parameter: List to be populated with claims that need updating.
     * @return A map where keys are Onfido claim names and values are the corresponding claim values
     * from the user store.
     * @throws IdentityVerificationException if there's an error retrieving claim values, mappings,
     *                                       or processing claims.
     */
    private Map<String, String> getUnverifiedOnfidoClaimsWithValueMap(String userId, int tenantId,
                                                                      IdVProvider idVProvider,
                                                                      List<IdVClaim> verificationRequiredClaims,
                                                                      List<IdVClaim> claimsToUpdate)
            throws IdentityVerificationException {

        Map<String, String> idVProviderClaimWithValueMap = new HashMap<>();
        try {
            Map<String, String> idVClaimMap = idVProvider.getClaimMappings();
            UniqueIDUserStoreManager uniqueIDUserStoreManager = getUniqueIdEnabledUserStoreManager(tenantId);

            for (IdVClaim idVClaim : verificationRequiredClaims) {
                String claimUri = idVClaim.getClaimUri();
                IdVClaim existingIdVClaim = OnfidoIDVDataHolder.getIdentityVerificationManager()
                        .getIdVClaim(userId, idVClaim.getClaimUri(), idVProvider.getIdVProviderUuid(), tenantId);

                if (existingIdVClaim == null || existingIdVClaim.getMetadata() == null ||
                        existingIdVClaim.getMetadata().get(ONFIDO_APPLICANT_ID) == null) {
                    String claimValue = uniqueIDUserStoreManager.getUserClaimValueWithID(userId, claimUri, null);
                    if (StringUtils.isEmpty(claimValue)) {
                        throw new IdentityVerificationClientException(ERROR_CLAIM_VALUE_NOT_EXIST.getCode(),
                                String.format(ERROR_CLAIM_VALUE_NOT_EXIST.getMessage(), claimUri));
                    }
                    idVProviderClaimWithValueMap.put(idVClaimMap.get(claimUri), claimValue);
                }
                if (existingIdVClaim != null) {
                    claimsToUpdate.add(existingIdVClaim);
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
     * @param applicantId   The unique identifier of the applicant in the Onfido system.
     * @param workflowRunId The identifier of the workflow run associated with the applicant's verification process.
     * @return A map containing key-value pairs of metadata related to the Onfido verification process.
     */
    private Map<String, Object> getInitiatedVerificationMetadata(String applicantId, String workflowRunId) {

        Map<String, Object> metadata = new HashMap<>();
        metadata.put(ONFIDO_APPLICANT_ID, applicantId);
        metadata.put(ONFIDO_WORKFLOW_RUN_ID, workflowRunId);
        metadata.put(ONFIDO_WORKFLOW_STATUS, OnfidoConstants.WorkflowRunStatus.AWAITING_INPUT.getStatus());
        return metadata;
    }

    /**
     * Updates existing claims and stores new claims for identity verification.
     *
     * @param userId                     The unique identifier of the user.
     * @param tenantId                   The ID of the tenant.
     * @param idVProvider                The identity verification provider.
     * @param verificationRequiredClaims List of all claims that require verification.
     * @param claimsToUpdate             List of existing claims that need to be updated.
     * @param metadata                   Additional metadata to be associated with the claims specific to Onfido.
     * @throws IdentityVerificationException if there's an error updating or storing claims.
     */
    private void updateAndStoreClaims(String userId, int tenantId, IdVProvider idVProvider,
                                      List<IdVClaim> verificationRequiredClaims, List<IdVClaim> claimsToUpdate,
                                      Map<String, Object> metadata) throws IdentityVerificationException {

        // Create a set of claim URIs of the IdV claims that already existing in the DB.
        Set<String> updateClaimUris = claimsToUpdate.stream()
                .map(IdVClaim::getClaimUri)
                .collect(Collectors.toSet());

        // Update metadata for existing IdV claims
        for (IdVClaim claim : claimsToUpdate) {
            claim.setIsVerified(false);
            claim.setMetadata(metadata);
            updateIdVClaim(userId, claim, tenantId);
        }

        List<IdVClaim> claimsToStore = new ArrayList<>();

        for (IdVClaim claim : verificationRequiredClaims) {
            if (!updateClaimUris.contains(claim.getClaimUri())) {
                claim.setIsVerified(false);
                claim.setUserId(userId);
                claim.setIdVPId(idVProvider.getIdVProviderUuid());
                claim.setMetadata(metadata);
                claimsToStore.add(claim);
            }
        }

        // Store new IdV claims
        if (!claimsToStore.isEmpty()) {
            storeIdVClaims(userId, claimsToStore, tenantId);
        }
    }

    /**
     * Retrieves the applicant ID from the metadata of an existing identity verification claim associated
     * with the given user and identity verification provider.
     *
     * @param userId      The unique identifier of the user whose applicant ID is being retrieved.
     * @param tenantId    The ID of the tenant.
     * @param idVProvider The identity verification provider.
     * @return The applicant ID if found, otherwise returns null.
     * @throws IdentityVerificationException If there is an error accessing the claims.
     */
    private static String getApplicantId(String userId, int tenantId, IdVProvider idVProvider)
            throws IdentityVerificationException {

        String applicantId = null;
        IdVClaim[] idVClaims = OnfidoIDVDataHolder.getIdentityVerificationManager()
                .getIdVClaims(userId, idVProvider.getIdVProviderUuid(), null, tenantId);
        for (IdVClaim idVClaim : idVClaims) {
            if (idVClaim != null && idVClaim.getMetadata() != null &&
                    idVClaim.getMetadata().get(ONFIDO_APPLICANT_ID) != null) {
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
     * @param userId               The unique identifier of the user whose applicant ID is being retrieved.
     * @param tenantId             The ID of the tenant.
     * @param idVProvider          The identity verification provider.
     * @param identityVerifierData Data required for identity verification that was passed via the verification request.
     * @return The applicant ID if found, otherwise returns null.
     * @throws IdentityVerificationException If there is an error accessing the claims.
     */
    private static String getWorkflowRunId(String userId, int tenantId, IdVProvider idVProvider,
                                           IdentityVerifierData identityVerifierData)
            throws IdentityVerificationException {

        // Retrieve the claim URIs of the wso2 claims that need to be verified.
        List<IdVClaim> verificationRequiredClaims = getVerificationRequiredClaims(identityVerifierData);
        Set<String> verificationRequiredClaimsUri = verificationRequiredClaims.stream()
                .map(IdVClaim::getClaimUri)
                .collect(Collectors.toSet());

        String workflowRunId = null;
        IdVClaim[] idVClaims = OnfidoIDVDataHolder.getIdentityVerificationManager()
                .getIdVClaims(userId, idVProvider.getIdVProviderUuid(), null, tenantId);
        for (IdVClaim idVClaim : idVClaims) {
            if (idVClaim != null && idVClaim.getMetadata() != null &&
                    idVClaim.getMetadata().get(ONFIDO_WORKFLOW_RUN_ID) != null &&
                            verificationRequiredClaimsUri.contains(idVClaim.getClaimUri())) {

                workflowRunId = (String) idVClaim.getMetadata().get(ONFIDO_WORKFLOW_RUN_ID);
                break;
            }
        }

        if (workflowRunId == null) {
            throw new IdentityVerificationClientException(ERROR_ONFIDO_WORKFLOW_RUN_ID_NOT_FOUND.getCode(),
                    ERROR_ONFIDO_WORKFLOW_RUN_ID_NOT_FOUND.getMessage());
        }
        return workflowRunId;
    }

    /**
     * Retrieves the verification flow status from the identity verifier data.
     *
     * @param identityVerifierData Data required for identity verification that was passed via the verification request.
     * @return The Onfido verification flow status.
     * @throws IdentityVerificationClientException If the flow status is not found or invalid.
     */
    private OnfidoConstants.VerificationFlowStatus getVerificationFlowStatus(IdentityVerifierData identityVerifierData)
            throws IdentityVerificationClientException {

        String statusValue = getPropertyValue(identityVerifierData, STATUS, ERROR_VERIFICATION_FLOW_STATUS_NOT_FOUND);
        try {
            return OnfidoConstants.VerificationFlowStatus.fromString(statusValue);
        } catch (OnfidoClientException e) {
            throw new IdentityVerificationClientException(ERROR_INVALID_ONFIDO_VERIFICATION_FLOW_STATUS.getCode(),
                    ERROR_INVALID_ONFIDO_VERIFICATION_FLOW_STATUS.getMessage());
        }
    }

    /**
     * Helper method to retrieve a property value from identity verifier data.
     *
     * @param identityVerifierData Data required for identity verification that was passed via the verification request.
     * @param propertyName         The name of the property to retrieve.
     * @param errorMessage         The error message to show if the property is not found.
     * @return The value of the property.
     * @throws IdentityVerificationClientException If the property is not found.
     */
    private String getPropertyValue(IdentityVerifierData identityVerifierData, String propertyName,
                                    OnfidoConstants.ErrorMessage errorMessage)
            throws IdentityVerificationClientException {

        List<IdVProperty> properties = identityVerifierData.getIdVProperties();

        if (properties == null || properties.isEmpty()) {
            throw new IdentityVerificationClientException(errorMessage.getCode(), errorMessage.getMessage());
        }

        for (IdVProperty property : properties) {
            if (StringUtils.equals(property.getName(), propertyName) && StringUtils.isNotBlank(property.getValue())) {
                return property.getValue();
            }
        }
        throw new IdentityVerificationClientException(errorMessage.getCode(), errorMessage.getMessage());
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
     * @throws IdentityVerificationServerException If the UserStoreManager is not an instance of
     *                                             UniqueIDUserStoreManager.
     * @throws UserStoreException                  If there is a failure in retrieving the UserStoreManager.
     */
    private UniqueIDUserStoreManager getUniqueIdEnabledUserStoreManager(int tenantId)
            throws IdentityVerificationServerException, UserStoreException {

        RealmService realmService = OnfidoIDVDataHolder.getRealmService();
        UserStoreManager userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
        if (!(userStoreManager instanceof UniqueIDUserStoreManager)) {
            throw IdentityVerificationExceptionMgt.handleServerException(ERROR_GETTING_USER_STORE);
        }
        return (UniqueIDUserStoreManager) userStoreManager;
    }
}
