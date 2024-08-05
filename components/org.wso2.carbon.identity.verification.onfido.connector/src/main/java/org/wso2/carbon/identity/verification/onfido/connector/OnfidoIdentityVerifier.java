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
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.COMPLETED;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_CLAIM_VALUE_NOT_EXIST;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_GETTING_ONFIDO_VERIFICATION_STATUS;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_EMPTY;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_INITIATING_ONFIDO_VERIFICATION;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_IDV_PROVIDER_INVALID_OR_DISABLED;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_VERIFICATION_ALREADY_INITIATED;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_VERIFICATION_STATUS_NOT_FOUND;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ID;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.INITIATED;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.SDK_TOKEN;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.STATUS;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.TOKEN;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.WEBHOOK_TOKEN;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.WORKFLOW_ID;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.WORKFLOW_RUN_ID;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_NON_EXISTING_USER;

/**
 * This class contains the implementation of OnfidoIdentityVerifier.
 */
public class OnfidoIdentityVerifier extends AbstractIdentityVerifier implements IdentityVerifier {

    private static final Log log = LogFactory.getLog(OnfidoIdentityVerifier.class);

    @Override
    public IdentityVerifierData verifyIdentity(String userId, IdentityVerifierData identityVerifierData, int tenantId)
            throws IdentityVerificationException {

        // Get corresponding IdV Provider.
        IdVProvider idVProvider = getIdVProvider(identityVerifierData, tenantId);
        if (idVProvider == null || !idVProvider.isEnabled()) {
            throw new IdentityVerificationClientException(ERROR_IDV_PROVIDER_INVALID_OR_DISABLED.getCode(),
                    ERROR_IDV_PROVIDER_INVALID_OR_DISABLED.getMessage());
        }

        // Get IdV Properties that are sent through identity verification request.
        Map<String, String> idVProperties = getIdVPropertyMap(identityVerifierData);

        // Get IdV Provider Config Properties.
        Map<String, String> idVProviderConfigProperties = getIdVConfigPropertyMap(idVProvider);
        validateIdVProviderConfigProperties(idVProviderConfigProperties);

        List<IdVClaim> idVClaims = new ArrayList<>();
        switch (idVProperties.get(STATUS)) {
            case INITIATED:
                // Initiate Onfido verification through creating applicant and retrieving sdk token
                idVClaims = initiateOnfidoVerification(userId, identityVerifierData, tenantId,
                        idVProvider, idVProviderConfigProperties);
                break;
            case COMPLETED:
                // Update the onfido verification status.
                idVClaims = updateOnfidoVerificationStatus(userId, identityVerifierData, idVProvider,
                        idVProviderConfigProperties, tenantId);
                break;
        }
        identityVerifierData.setIdVClaims(idVClaims);
        return identityVerifierData;
    }

    private List<IdVClaim> initiateOnfidoVerification(String userId, IdentityVerifierData identityVerifierData,
                                                            int tenantId, IdVProvider idVProvider,
                                                            Map<String, String> idVProviderConfigProperties)
            throws IdentityVerificationException {

        List<IdVClaim> verificationRequiredClaims = identityVerifierData.getIdVClaims();
        String applicantId = getApplicantId(userId, tenantId, idVProvider);
        Map<String, String> idVProviderClaimWithValueMap =
                getIdVProviderClaimWithValueMap(userId, tenantId, idVProvider, verificationRequiredClaims);

        try {
            if (!idVProviderClaimWithValueMap.isEmpty()) {
                // The idVProviderClaimWithValueMap will contain the claims that need to be initiated the verification.
                JSONObject applicantRequestBody = getApplicantRequestBody(idVProviderClaimWithValueMap);
                if (StringUtils.isEmpty(applicantId)) {
                    JSONObject onFidoJsonObject = OnfidoAPIClient.
                            createApplicant(idVProviderConfigProperties, applicantRequestBody);

                    applicantId = (String) onFidoJsonObject.get(ID);
                } else {
                    OnfidoAPIClient.updateApplicant(idVProviderConfigProperties, applicantRequestBody);
                }

                // Create a workflow run
                JSONObject workflowRunRequestBody = new JSONObject();
                workflowRunRequestBody.put(WORKFLOW_ID, idVProviderConfigProperties.get(WORKFLOW_ID));
                workflowRunRequestBody.put(APPLICANT_ID, applicantId);

                JSONObject workflowRunJsonObject =
                        OnfidoAPIClient.createWorkflowRun(idVProviderConfigProperties, workflowRunRequestBody);

                // Generate a SDK token
                JSONObject sdkTokenRequestBody = new JSONObject();
                sdkTokenRequestBody.put(APPLICANT_ID, applicantId);
                JSONObject sdkTokenJsonObject =
                        OnfidoAPIClient.createSDKToken(idVProviderConfigProperties, sdkTokenRequestBody);

                Map<String, Object> metadata =
                        getInitiatedVerificationMetadata(applicantId, workflowRunJsonObject.get(ID).toString());
                for (IdVClaim idVClaim : verificationRequiredClaims) {
                    idVClaim.setIsVerified(false);
                    idVClaim.setUserId(userId);
                    idVClaim.setIdVPId(idVProvider.getIdVProviderUuid());
                    idVClaim.setMetadata(metadata);
                }
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

    private List<IdVClaim> updateOnfidoVerificationStatus(String userId, IdentityVerifierData identityVerifierData,
                                                          IdVProvider idVProvider,
                                                          Map<String, String> idVProviderConfigProperties, int tenantId)
            throws IdentityVerificationException {

        List<IdVClaim> verificationClaim = new ArrayList<>();
        String workFlowRunId = getWorkFlowRunId(userId, tenantId, idVProvider);
        OnfidoConstants.VerificationStatus
                verificationStatus = getOnfidoVerificationStatus(workFlowRunId, idVProviderConfigProperties);
        List<IdVClaim> verificationRequiredClaims = identityVerifierData.getIdVClaims();
        for (IdVClaim idVClaim : verificationRequiredClaims) {
            idVClaim = OnfidoIDVDataHolder.getInstance()
                    .getIdentityVerificationManager()
                    .getIdVClaim(userId, idVClaim.getClaimUri(), idVProvider.getIdVProviderUuid(), tenantId);
            if (!idVClaim.isVerified()) {
                Map<String, Object> metadata = idVClaim.getMetadata();
                metadata.put(STATUS, verificationStatus.getStatus());
                idVClaim.setMetadata(metadata);
                updateIdVClaim(userId, idVClaim, tenantId);
                verificationClaim.add(idVClaim);
            }
        }
        return verificationClaim;
    }

    private OnfidoConstants.VerificationStatus getOnfidoVerificationStatus(String workFlowRunId,
                                                                           Map<String, String> idVProviderConfigProperties)
            throws IdentityVerificationException {

        try{
            JSONObject verificationStatusJsonObject =
                    OnfidoAPIClient.getVerificationStatus(idVProviderConfigProperties, workFlowRunId);
            return OnfidoConstants.VerificationStatus.fromString(verificationStatusJsonObject.getString(STATUS));
        } catch (OnfidoServerException e) {
            throw new IdentityVerificationServerException(ERROR_GETTING_ONFIDO_VERIFICATION_STATUS.getCode(),
                    ERROR_GETTING_ONFIDO_VERIFICATION_STATUS.getMessage());
        }
    }

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
                        idVClaim.getMetadata().get(APPLICANT_ID) == null) {
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

    private Map<String, Object> getInitiatedVerificationMetadata(String applicantId, String workflowRunId) {

        Map<String, Object> metadata = new HashMap<>();
        metadata.put(APPLICANT_ID, applicantId);
        metadata.put(WORKFLOW_RUN_ID, workflowRunId);
        metadata.put(STATUS, OnfidoConstants.VerificationStatus.AWAITING_INPUT.getStatus());
        return metadata;
    }

    private static String getApplicantId(String userId, int tenantId, IdVProvider idVProvider)
            throws IdentityVerificationException {

        String applicantId = null;
        IdVClaim[] idVClaims = OnfidoIDVDataHolder.getInstance().getIdentityVerificationManager().
                getIdVClaims(userId, idVProvider.getIdVProviderUuid(), null, tenantId);
        for (IdVClaim idVClaim : idVClaims) {
            if (idVClaim != null && idVClaim.getMetadata() != null &&
                    idVClaim.getMetadata().get(APPLICANT_ID) != null && !idVClaim.isVerified()) {
                applicantId = (String) idVClaim.getMetadata().get(APPLICANT_ID);
                break;
            }
        }
        return applicantId;
    }

    private static String getWorkFlowRunId(String userId, int tenantId, IdVProvider idVProvider)
            throws IdentityVerificationException {

        String workFlowRunId = null;
        IdVClaim[] idVClaims = OnfidoIDVDataHolder.getInstance().getIdentityVerificationManager().
                getIdVClaims(userId, idVProvider.getIdVProviderUuid(), null, tenantId);
        for (IdVClaim idVClaim : idVClaims) {
            if (idVClaim != null && idVClaim.getMetadata() != null &&
                    idVClaim.getMetadata().get(WORKFLOW_RUN_ID) != null && !idVClaim.isVerified()) {
                workFlowRunId = (String) idVClaim.getMetadata().get(WORKFLOW_RUN_ID);
                break;
            }
        }
        return workFlowRunId;
    }

    private Map<String, String> getIdVPropertyMap(IdentityVerifierData identityVerifierData)
            throws IdentityVerificationClientException {

        List<IdVProperty> identityVerificationProperties = identityVerifierData.getIdVProperties();
        if (identityVerificationProperties == null || identityVerificationProperties.isEmpty()) {
            throw new IdentityVerificationClientException(ERROR_VERIFICATION_STATUS_NOT_FOUND.getCode(),
                    ERROR_VERIFICATION_STATUS_NOT_FOUND.getMessage());
        }
        Map<String, String> idVPropertyMap = new HashMap<>();
        for (IdVProperty idVProperty : identityVerificationProperties) {
            if (StringUtils.equals(idVProperty.getName(), STATUS) && StringUtils.isNotBlank(idVProperty.getValue())) {
                idVPropertyMap.put(idVProperty.getName(), idVProperty.getValue());
            } else {
                throw new IdentityVerificationClientException(ERROR_VERIFICATION_STATUS_NOT_FOUND.getCode(),
                        ERROR_VERIFICATION_STATUS_NOT_FOUND.getMessage());
            }
        }
        return idVPropertyMap;
    }

    private JSONObject getApplicantRequestBody(Map<String, String> idVClaimsWithValues) {

        JSONObject idVClaimRequestBody = new JSONObject();
        for (Map.Entry<String, String> idVClaim : idVClaimsWithValues.entrySet()) {
            idVClaimRequestBody.put(idVClaim.getKey(), idVClaim.getValue());
        }
        return idVClaimRequestBody;
    }

    private UniqueIDUserStoreManager getUniqueIdEnabledUserStoreManager(int tenantId)
            throws IdentityVerificationServerException, UserStoreException {

        RealmService realmService = OnfidoIDVDataHolder.getInstance().getRealmService();
        UserStoreManager userStoreManager = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
        if (!(userStoreManager instanceof UniqueIDUserStoreManager)) {
            throw IdentityVerificationExceptionMgt.handleServerException(ERROR_GETTING_USER_STORE);
        }
        return (UniqueIDUserStoreManager) userStoreManager;
    }

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
