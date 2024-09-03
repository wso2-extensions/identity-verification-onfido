/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.verification.onfido.api.v1.core;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationException;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtException;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVConfigProperty;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.verification.onfido.api.common.Constants;
import org.wso2.carbon.identity.verification.onfido.api.common.OnfidoIdvServiceHolder;
import org.wso2.carbon.identity.verification.onfido.api.common.error.APIError;
import org.wso2.carbon.identity.verification.onfido.api.common.error.ErrorResponse;
import org.wso2.carbon.identity.verification.onfido.api.v1.interceptors.RawRequestBodyInterceptor;
import org.wso2.carbon.identity.verification.onfido.api.v1.model.VerifyRequest;
import org.wso2.carbon.identity.verification.onfido.api.v1.model.VerifyRequestPayload;
import org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants;
import org.wso2.carbon.identity.verification.onfido.connector.exception.OnfidoClientException;
import org.wso2.carbon.identity.verification.onfido.connector.exception.OnfidoServerException;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.Response;

import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ACTION_WORKFLOW_RUN_COMPLETED;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.CLIENT_ERROR_DATA_COMPARISON_RESULT_NOT_FOUND;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.CLIENT_ERROR_DATA_COMPARISON_RESULT_NULL;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.CLIENT_ERROR_INVALID_REQUEST;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.CLIENT_ERROR_INVALID_WORKFLOW_OUTPUT;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.CLIENT_ERROR_RESOLVING_IDVP;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.CLIENT_ERROR_SIGNATURE_MISMATCH;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.CLIENT_ERROR_UNSUPPORTED_RESOURCE_TYPE_OR_ACTION;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.SERVER_ERROR_GENERAL_ERROR;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.SERVER_ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_INVALID;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.SERVER_ERROR_INVALID_WORKFLOW_RUN_STATUS;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.SERVER_ERROR_RESOLVING_IDVP;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.SERVER_ERROR_SIGNATURE_VALIDATION_FAILURE;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.SERVER_ERROR_UPDATING_IDV_CLAIM_VERIFICATION_STATUS;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.HMAC_SHA256_ALGORITHM;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.RESOURCE_WORKFLOW_RUN;
import static org.wso2.carbon.identity.verification.onfido.api.common.Util.getTenantId;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.BASE_URL;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.DATA_COMPARISON;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_DATA_COMPARISON_BREAKDOWN_CLAIM_NOT_FOUND;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_DATA_COMPARISON_BREAKDOWN_CLAIM_VERIFICATION_RESULT_NULL;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_EMPTY;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_IDV_PROVIDER_INVALID_OR_DISABLED;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_INVALID_OR_MISSING_DATA_COMPARISON;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_INVALID_OR_MISSING_RESOURCE_OUTPUT;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_INVALID_WORKFLOW_RUN_STATUS;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_RESOLVING_IDV_PROVIDER;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_RETRIEVING_CLAIMS_AGAINST_WORKFLOW_RUN_ID;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_SIGNATURE;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_SIGNATURE_VALIDATION;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_SIGNATURE_VALIDATION_PROCESSING;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_UNSUPPORTED_RESOURCE_TYPE_OR_ACTION;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_UPDATING_IDV_CLAIM_VERIFICATION_STATUS;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ONFIDO_COMPLETED_AT;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ONFIDO_VERIFICATION_STATUS;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ONFIDO_WORKFLOW_RUN_ID;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ONFIDO_WORKFLOW_STATUS;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.OUTPUT;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.RESULT;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.TOKEN;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.WEBHOOK_TOKEN;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.WORKFLOW_ID;

/**
 * Onfido Identity Verification Service implementation to be notified when the verification is completed.
 */
public class OnfidoIdvService {

    private static final Log log = LogFactory.getLog(OnfidoIdvService.class);

    /**
     * Handles the Onfido webhook verification status update.
     * This method is invoked when Onfido sends a verification status update via webhook.
     * It validates the request verifying the signature, and updates the verification claims of the user
     * based on the status.
     *
     * @param xSHA2Signature The SHA-2 signature from the Onfido webhook header for validation.
     * @param idvpId         The identity verification provider ID.
     * @param verifyRequest  The verification request payload from Onfido.
     */
    public void verify(String xSHA2Signature, String idvpId, VerifyRequest verifyRequest) {

        int tenantId = getTenantId();
        try {
            String rawRequestBody = RawRequestBodyInterceptor.getRawRequestBody();

            validateResourceTypeAndAction(verifyRequest);

            IdVProvider idVProvider = getIdVProvider(idvpId, tenantId);
            Map<String, String> idVProviderConfigProperties = getIdVConfigPropertyMap(idVProvider);

            validateSignature(xSHA2Signature, idVProviderConfigProperties, rawRequestBody);

            updateIdVClaims(verifyRequest, idvpId, tenantId, idVProvider);
        } catch (OnfidoClientException e) {
            handleClientException(e);
        } catch (OnfidoServerException e) {
            handleServerException(e);
        } finally {
            RawRequestBodyInterceptor.clear();
        }
    }

    /**
     * Retrieves the Identity Verification Provider (IdVProvider) for the given tenant.
     *
     * @param idvpId   The identity verification provider ID.
     * @param tenantId The tenant ID.
     * @return The IdVProvider object.
     * @throws OnfidoClientException   If the provider is not found or is disabled.
     */
    private IdVProvider getIdVProvider(String idvpId, int tenantId) throws OnfidoClientException,
            OnfidoServerException {

        try {
            IdVProvider idVProvider = OnfidoIdvServiceHolder.getIdVProviderManager().getIdVProvider(idvpId, tenantId);
            if (idVProvider == null || !idVProvider.isEnabled()) {
                throw new OnfidoClientException(ERROR_IDV_PROVIDER_INVALID_OR_DISABLED.getCode(),
                        ERROR_IDV_PROVIDER_INVALID_OR_DISABLED.getMessage());
            }
            return idVProvider;
        } catch (IdVProviderMgtException e) {
            throw new OnfidoServerException(ERROR_RESOLVING_IDV_PROVIDER.getCode(),
                    ERROR_RESOLVING_IDV_PROVIDER.getMessage(), e);
        }
    }

    /**
     * Retrieves and validates the configuration properties of the Identity Verification Provider.
     *
     * @param idVProvider Identity Verification Provider.
     * @return Config property map of Identity Verification Provider.
     */
    private Map<String, String> getIdVConfigPropertyMap(IdVProvider idVProvider) throws OnfidoServerException {

        Map<String, String> configPropertyMap = new HashMap<>();
        for (IdVConfigProperty idVConfigProperty : idVProvider.getIdVConfigProperties()) {
            configPropertyMap.put(idVConfigProperty.getName(), idVConfigProperty.getValue());
        }
        validateIdVProviderConfigProperties(configPropertyMap);
        return configPropertyMap;
    }

    /**
     * Validates the configuration properties of the Identity Verification Provider.
     *
     * @param idVProviderConfigProperties The configuration properties of the Identity Verification Provider.
     * @throws OnfidoServerException If any required configuration property is missing or invalid.
     */
    private void validateIdVProviderConfigProperties(Map<String, String> idVProviderConfigProperties)
            throws OnfidoServerException {

        if (idVProviderConfigProperties == null || idVProviderConfigProperties.isEmpty() ||
                StringUtils.isBlank(idVProviderConfigProperties.get(TOKEN)) ||
                StringUtils.isBlank(idVProviderConfigProperties.get(BASE_URL)) ||
                StringUtils.isBlank(idVProviderConfigProperties.get(WEBHOOK_TOKEN)) ||
                StringUtils.isBlank(idVProviderConfigProperties.get(WORKFLOW_ID))) {

            throw new OnfidoServerException(ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_EMPTY.getCode(),
                    ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_EMPTY.getMessage());
        }
    }

    /**
     * Validates the signature provided in the webhook request against the expected signature.
     * Implementation logic extracted from
     * https://github.com/onfido/onfido-java/blob/master/src/main/java/com/onfido/WebhookEventVerifier.java#L81
     *
     * @param xSHA2Signature              The SHA-2 signature from the Onfido webhook.
     * @param idVProviderConfigProperties The configuration properties of the Identity Verification Provider.
     * @param rawRequestBody              The raw verification request payload from Onfido.
     * @throws OnfidoClientException If the signature validation fails.
     * @throws OnfidoServerException If a server-side error occurs during validation.
     */
    private void validateSignature(String xSHA2Signature, Map<String, String> idVProviderConfigProperties,
                                   String rawRequestBody) throws OnfidoClientException, OnfidoServerException {

        if (StringUtils.isBlank(xSHA2Signature)) {
            throw new OnfidoClientException(ERROR_SIGNATURE.getCode(), ERROR_SIGNATURE.getMessage());
        }

        String webhookToken = idVProviderConfigProperties.get(WEBHOOK_TOKEN);

        Mac sha256Hmac;
        SecretKeySpec secretKey;
        try {
            sha256Hmac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            secretKey = new SecretKeySpec(webhookToken.getBytes(StandardCharsets.UTF_8), HMAC_SHA256_ALGORITHM);
            sha256Hmac.init(secretKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new OnfidoServerException(ERROR_SIGNATURE_VALIDATION_PROCESSING.getCode(),
                    ERROR_SIGNATURE_VALIDATION_PROCESSING.getMessage(), e);
        }

        // Compute the HMAC SHA-256 of the raw request body.
        String expectedSignature = encodeHexString(sha256Hmac.doFinal(rawRequestBody.getBytes(StandardCharsets.UTF_8)));

        // Perform a time-safe comparison of the signatures.
        if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8),
                xSHA2Signature.getBytes(StandardCharsets.UTF_8))) {
            throw new OnfidoClientException(ERROR_SIGNATURE_VALIDATION.getCode(),
                    ERROR_SIGNATURE_VALIDATION.getMessage());
        }
    }

    /**
     * Encodes a byte array into a hexadecimal string.
     *
     * @param byteArray The byte array to encode.
     * @return The hexadecimal string representation of the byte array.
     */
    private String encodeHexString(byte[] byteArray) {

        StringBuilder hexStringBuffer = new StringBuilder();
        for (byte b : byteArray) {
            hexStringBuffer.append(byteToHex(b));
        }
        return hexStringBuffer.toString();
    }

    /**
     * Converts a byte into a hexadecimal string.
     *
     * @param num The byte to convert.
     * @return The hexadecimal string representation of the byte.
     */
    private String byteToHex(byte num) {

        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }

    /**
     * Validates the resource type and action from the verification request.
     *
     * @param verifyRequest The verification request payload from Onfido.
     * @throws OnfidoClientException If the resource type or action is unsupported.
     */
    private void validateResourceTypeAndAction(VerifyRequest verifyRequest) throws OnfidoClientException {

        String resourceType = verifyRequest.getPayload().getResourceType();
        String action = verifyRequest.getPayload().getAction();

        if (!RESOURCE_WORKFLOW_RUN.equals(resourceType) || !ACTION_WORKFLOW_RUN_COMPLETED.equals(action)) {
            throw new OnfidoClientException(ERROR_UNSUPPORTED_RESOURCE_TYPE_OR_ACTION.getCode(),
                    ERROR_UNSUPPORTED_RESOURCE_TYPE_OR_ACTION.getMessage());
        }
    }

    /**
     * Updates the identity verification claims based on the provided Onfido verification request.
     *
     * @param verifyRequest The Onfido verification request containing the workflow run details and attribute
     *                      verification results.
     * @param idvpId        The identity verification provider ID.
     * @param tenantId      The tenant ID.
     * @param idVProvider   The identity verification provider.
     * @throws OnfidoClientException If the payload structure is invalid or required data is missing.
     * @throws OnfidoServerException If a server-side error occurs while updating the claims.
     */
    private void updateIdVClaims(VerifyRequest verifyRequest, String idvpId, int tenantId, IdVProvider idVProvider)
            throws OnfidoClientException, OnfidoServerException {

        VerifyRequestPayload payload = verifyRequest.getPayload();
        String workflowRunId = payload.getObject().getId();
        String completedAt = payload.getObject().getCompletedAtIso8601();
        OnfidoConstants.WorkflowRunStatus workflowRunStatus =
                OnfidoConstants.WorkflowRunStatus.fromString(payload.getObject().getStatus());
        try {
            /*
             * Retrieve the WSO2 identity verification claims associated with the given workflow run ID
             * and update the workflow status.
             * Note: The workflow run ID is unique per user, so this should return claims for a single user.
             */
            IdVClaim[] idVClaims = getIdVClaimsByWorkflowRunId(workflowRunId, idvpId, tenantId);
            updateIdvClaimsWorkflowStatus(idVClaims, workflowRunStatus, completedAt);

            /*
             * Update the claim verification status if the workflow run status is "APPROVED".
             *
             * Note:
             *  Even if the overall workflow status is "APPROVED", individual claims may still fail verification.
             *  This discrepancy primarily stems from how the workflow is defined in the Onfido Studio:
             *      1. The Onfido workflow might be configured to approve the overall process even if some claims
             *         don't match exactly.
             *      2. Some claims might be configured as optional in the verification process.
             *         Their failure might not affect the overall approval.
             *  Therefore, we need to check the verification result (i.e., value comparison results) for each claim
             *  separately as we are focusing on the verification of exact claim value.
             */
            if (workflowRunStatus == OnfidoConstants.WorkflowRunStatus.APPROVED) {
                Map<String, Object> dataComparisonResults = extractDataComparisonResults(payload.getResource());
                updateIdvClaimsVerificationResults(dataComparisonResults, idVClaims, idVProvider);
            }

            // Persist the updated claim information in the database.
            persistUpdatedClaims(idVClaims, tenantId);

        } catch (IdentityVerificationException e) {
            throw new OnfidoServerException(ERROR_UPDATING_IDV_CLAIM_VERIFICATION_STATUS.getCode(),
                    ERROR_UPDATING_IDV_CLAIM_VERIFICATION_STATUS.getMessage(), e);
        }
    }

    /**
     * Retrieves the identity verification claims associated with a specific workflow run ID.
     *
     * @param workflowRunId The ID of the workflow run.
     * @param idvpId        The identity verification provider ID.
     * @param tenantId      The tenant ID.
     * @return An array of IdVClaim objects associated with the workflow run.
     * @throws OnfidoClientException If no claims are found for the given workflow run ID.
     * @throws IdentityVerificationException If there's an error retrieving the claims.
     */
    private IdVClaim[] getIdVClaimsByWorkflowRunId(String workflowRunId, String idvpId, int tenantId)
            throws OnfidoClientException, IdentityVerificationException {

        IdVClaim[] idVClaims = OnfidoIdvServiceHolder.getIdentityVerificationManager()
                .getIdVClaimsByMetadata(ONFIDO_WORKFLOW_RUN_ID, workflowRunId, idvpId, tenantId);
        if (idVClaims == null || idVClaims.length == 0) {
            throw new OnfidoClientException(ERROR_RETRIEVING_CLAIMS_AGAINST_WORKFLOW_RUN_ID.getCode(),
                    ERROR_RETRIEVING_CLAIMS_AGAINST_WORKFLOW_RUN_ID.getMessage());
        }
        return idVClaims;
    }

    /**
     * Updates the workflow status and completion time for a set of identity verification claims.
     *
     * @param idVClaims         The array of identity verification claims to update.
     * @param workflowRunStatus The new workflow run status.
     * @param completedAt       The completion timestamp.
     */
    private void updateIdvClaimsWorkflowStatus(IdVClaim[] idVClaims,
                                               OnfidoConstants.WorkflowRunStatus workflowRunStatus,
                                               String completedAt) {

        for (IdVClaim idVClaim : idVClaims) {
            Map<String, Object> metadata = idVClaim.getMetadata();
            if (metadata != null) {
                metadata.put(ONFIDO_WORKFLOW_STATUS, workflowRunStatus.getStatus());
                metadata.put(ONFIDO_COMPLETED_AT, completedAt);
                idVClaim.setMetadata(metadata);
            } else {
                log.error(String.format("Metadata for the claim URI: %s of user: %s is null", idVClaim.getClaimUri(),
                        idVClaim.getUserId()));
            }
        }
    }

    /**
     * Extracts data comparison results from the resource map.
     *
     * @param resource The resource map containing output data.
     * @return A map of data comparison results.
     * @throws OnfidoClientException if the resource structure is invalid or missing required data.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> extractDataComparisonResults(Map<String, Object> resource)
            throws OnfidoClientException {

        if (resource == null || !(resource.get(OUTPUT) instanceof Map)) {
            throw new OnfidoClientException(ERROR_INVALID_OR_MISSING_RESOURCE_OUTPUT.getCode(),
                    ERROR_INVALID_OR_MISSING_RESOURCE_OUTPUT.getMessage());
        }

        Map<String, Object> output = (Map<String, Object>) resource.get(OUTPUT);

        Object dataComparisonResults = output.get(DATA_COMPARISON);
        if (!(dataComparisonResults instanceof Map)) {
            throw new OnfidoClientException(ERROR_INVALID_OR_MISSING_DATA_COMPARISON.getCode(),
                    ERROR_INVALID_OR_MISSING_DATA_COMPARISON.getMessage());
        }

        return (Map<String, Object>) dataComparisonResults;
    }

    /**
     * Updates the verification results for a set of identity verification claims.
     *
     * @param dataComparisonResults The data comparison results from Onfido.
     * @param idVClaims             The array of identity verification claims to update.
     * @param idVProvider           The identity verification provider.
     * @throws OnfidoClientException If there's an error processing the verification results.
     */
    @SuppressWarnings("unchecked")
    private void updateIdvClaimsVerificationResults(Map<String, Object> dataComparisonResults, IdVClaim[] idVClaims,
                                                    IdVProvider idVProvider) throws OnfidoClientException {
        /*
         * Get the mapping of WSO2 claim URIs to Onfido claim names
         * (e.g., "http://wso2.org/claims/lastname" maps to "last_name").
         */
        Map<String, String> claimMappings = idVProvider.getClaimMappings();

        for (IdVClaim idVClaim : idVClaims) {
            String wso2ClaimUri = idVClaim.getClaimUri();
            String onfidoClaimName = getOnfidoClaimName(wso2ClaimUri, claimMappings);
            if (onfidoClaimName == null) {
                log.error(String.format("No mapped Onfido claim name identified for the claim URI: %s for the " +
                        "IDV provider: %s.", wso2ClaimUri, idVProvider.getIdVProviderUuid()));
                continue;
            }
            Map<String, Object> metadata = idVClaim.getMetadata();
            Map<String, Object> verificationResult = (Map<String, Object>) dataComparisonResults.get(onfidoClaimName);
            validateVerificationResult(verificationResult, idVClaim);

            // A claim is considered verified if its verification status is "CLEAR".
            boolean isVerified = OnfidoConstants.ClaimVerificationStatus.CLEAR.toString()
                    .equals(verificationResult.get(RESULT));
            idVClaim.setIsVerified(isVerified);

            // Update metadata with verification status.
            metadata.put(ONFIDO_VERIFICATION_STATUS, verificationResult.get(RESULT));
            idVClaim.setMetadata(metadata);
        }
    }

    /**
     * Retrieves the Onfido claim name corresponding to a WSO2 claim URI.
     *
     * This method performs a two-step mapping process:
     * 1. It first maps the WSO2 claim URI to an initial Onfido claim name.
     * 2. It then applies a second mapping to handle differences between attribute names
     *    used during applicant creation and those used in the final data comparison results.
     *
     * This second mapping is necessary because some attribute names differ between these two stages.
     * For example, "dob" used during applicant creation might be referred to as "date_of_birth"
     * in the final attribute value comparison results.
     *
     * @param wso2ClaimUri   The WSO2 claim URI.
     * @param claimMappings  The map of WSO2 claim URIs to initial Onfido claim names.
     * @return The corresponding final Onfido claim name, or null if not found.
     */
    private String getOnfidoClaimName(String wso2ClaimUri, Map<String, String> claimMappings) {

        String onfidoClaimName = claimMappings.get(wso2ClaimUri);
        return OnfidoConstants.ONFIDO_CLAIM_NAME_MAPPING.getOrDefault(onfidoClaimName, onfidoClaimName);
    }

    /**
     * Validates the verification result for a single claim.
     *
     * @param verificationResult The verification result from Onfido.
     * @param idVClaim           The identity verification claim being validated.
     * @throws OnfidoClientException If the verification result is missing or invalid.
     */
    private void validateVerificationResult(Map<String, Object> verificationResult, IdVClaim idVClaim)
            throws OnfidoClientException {

        if (verificationResult == null) {
            throw new OnfidoClientException(
                    ERROR_DATA_COMPARISON_BREAKDOWN_CLAIM_NOT_FOUND.getCode(),
                    String.format(ERROR_DATA_COMPARISON_BREAKDOWN_CLAIM_NOT_FOUND.getMessage(), idVClaim.getClaimUri(),
                            idVClaim.getUserId()));
        } else if (verificationResult.get(RESULT) == null) {
            throw new OnfidoClientException(
                    ERROR_DATA_COMPARISON_BREAKDOWN_CLAIM_VERIFICATION_RESULT_NULL.getCode(),
                    String.format(ERROR_DATA_COMPARISON_BREAKDOWN_CLAIM_VERIFICATION_RESULT_NULL.getMessage(),
                            idVClaim.getClaimUri(), idVClaim.getUserId()));
        }
    }

    /**
     * Persists the updated identity verification claims to the database.
     *
     * @param idVClaims The array of updated identity verification claims.
     * @param tenantId  The tenant ID.
     * @throws IdentityVerificationException If there's an error updating the claims in the database.
     */
    private void persistUpdatedClaims(IdVClaim[] idVClaims, int tenantId) throws IdentityVerificationException {

        for (IdVClaim idVClaim : idVClaims) {
            OnfidoIdvServiceHolder.getIdentityVerificationManager()
                    .updateIdVClaim(idVClaim.getUserId(), idVClaim, tenantId);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Successfully updated claim verification status of the user: %s, claim: %s.",
                        idVClaim.getUserId(), idVClaim.getClaimUri()));
            }
        }
    }

    /**
     * Handles exceptions related to client errors by mapping error codes to appropriate HTTP statuses.
     *
     * @param e The OnfidoClientException to handle.
     */
    private void handleClientException(OnfidoClientException e) {

        Response.Status status = Response.Status.BAD_REQUEST;
        Constants.ErrorMessage errorMessage = CLIENT_ERROR_INVALID_REQUEST;
        String errorCode = e.getErrorCode();

        if (ERROR_SIGNATURE_VALIDATION.getCode().equals(errorCode) || ERROR_SIGNATURE.getCode().equals(errorCode)) {
            status = Response.Status.UNAUTHORIZED;
            errorMessage = CLIENT_ERROR_SIGNATURE_MISMATCH;
        } else if (ERROR_IDV_PROVIDER_INVALID_OR_DISABLED.getCode().equals(errorCode)) {
            status = Response.Status.NOT_FOUND;
            errorMessage = CLIENT_ERROR_RESOLVING_IDVP;
        } else if (ERROR_UNSUPPORTED_RESOURCE_TYPE_OR_ACTION.getCode().equals(errorCode)) {
            errorMessage = CLIENT_ERROR_UNSUPPORTED_RESOURCE_TYPE_OR_ACTION;
        } else if (ERROR_INVALID_OR_MISSING_RESOURCE_OUTPUT.getCode().equals(errorCode) ||
                ERROR_INVALID_OR_MISSING_DATA_COMPARISON.getCode().equals(errorCode)) {
            errorMessage = CLIENT_ERROR_INVALID_WORKFLOW_OUTPUT;
        } else if (ERROR_DATA_COMPARISON_BREAKDOWN_CLAIM_NOT_FOUND.getCode().equals(errorCode)) {
            errorMessage = CLIENT_ERROR_DATA_COMPARISON_RESULT_NOT_FOUND;
        } else if (ERROR_DATA_COMPARISON_BREAKDOWN_CLAIM_VERIFICATION_RESULT_NULL.getCode().equals(errorCode)) {
            errorMessage = CLIENT_ERROR_DATA_COMPARISON_RESULT_NULL;
        }

        throw handleException(status, e, errorMessage, StringUtils.EMPTY);
    }

    /**
     * Handles exceptions related to server errors by mapping error codes to appropriate HTTP statuses.
     *
     * @param e The OnfidoServerException to handle.
     */
    private void handleServerException(OnfidoServerException e) {

        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
        Constants.ErrorMessage errorMessage = SERVER_ERROR_GENERAL_ERROR;
        String errorCode = e.getErrorCode();

        if (ERROR_RESOLVING_IDV_PROVIDER.getCode().equals(errorCode)) {
            errorMessage = SERVER_ERROR_RESOLVING_IDVP;
        } else if (ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_EMPTY.getCode().equals(errorCode)) {
            errorMessage = SERVER_ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_INVALID;
        } else if (ERROR_SIGNATURE_VALIDATION_PROCESSING.getCode().equals(errorCode)) {
            errorMessage = SERVER_ERROR_SIGNATURE_VALIDATION_FAILURE;
        } else if (ERROR_INVALID_WORKFLOW_RUN_STATUS.getCode().equals(errorCode)) {
            errorMessage = SERVER_ERROR_INVALID_WORKFLOW_RUN_STATUS;
        } else if (ERROR_UPDATING_IDV_CLAIM_VERIFICATION_STATUS.getCode().equals(errorCode)) {
            errorMessage = SERVER_ERROR_UPDATING_IDV_CLAIM_VERIFICATION_STATUS;
        }

        throw handleException(status, e, errorMessage, StringUtils.EMPTY);
    }

    /**
     * Constructs an APIError response based on the provided exception and error message.
     *
     * @param status    The HTTP status to return.
     * @param e         The IdentityException that occurred.
     * @param errorEnum The error message enumeration.
     * @param data      Additional data for the error message.
     * @return An APIError response.
     */
    private APIError handleException(Response.Status status, IdentityException e, Constants.ErrorMessage errorEnum,
                                     String... data) {

        boolean isClientError = status.getStatusCode() >= 400 && status.getStatusCode() < 500;
        ErrorResponse errorResponse = getErrorBuilder(e, errorEnum, data)
                .build(log, e, buildErrorDescription(errorEnum.getDescription(), data), isClientError);

        return new APIError(status, errorResponse);
    }

    /**
     * Constructs an error response builder for identity verification exceptions.
     *
     * @param exception The identity verification exception.
     * @param errorEnum The error message enumeration.
     * @param data      Additional data for the error message.
     * @return An ErrorResponse.Builder instance.
     */
    private ErrorResponse.Builder getErrorBuilder(IdentityException exception,
                                                  Constants.ErrorMessage errorEnum, String... data) {

        String errorCode = (errorEnum != null) ? errorEnum.getCode() : exception.getErrorCode();
        String message = (errorEnum != null) ? errorEnum.getMessage() : exception.getMessage();
        String description =
                (errorEnum != null) ? buildErrorDescription(errorEnum.getDescription(), data) : exception.getMessage();
        return new ErrorResponse.Builder()
                .withCode(errorCode)
                .withMessage(message)
                .withDescription(description);
    }

    /**
     * Constructs an error description string using the provided description and data.
     *
     * @param description The error description template.
     * @param data        Additional data for the error description.
     * @return The constructed error description string.
     */
    private String buildErrorDescription(String description, String... data) {

        return ArrayUtils.isNotEmpty(data) ? String.format(description, (Object[]) data) : description;
    }
}
