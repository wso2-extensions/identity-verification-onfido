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
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.verification.onfido.api.common.Constants;
import org.wso2.carbon.identity.verification.onfido.api.common.ContextLoader;
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
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.Response;

import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ACTION_WORKFLOW_RUN_COMPLETED;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.CLIENT_ERROR_INVALID_REQUEST;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.CLIENT_ERROR_INVALID_WORKFLOW_OUTPUT;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.CLIENT_ERROR_RESOLVING_IDVP;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.SERVER_ERROR_GENERAL_ERROR;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.SERVER_ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_INVALID;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.SERVER_ERROR_INVALID_WORKFLOW_RUN_STATUS;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.SERVER_ERROR_RESOLVING_IDVP;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.SERVER_ERROR_RETRIEVING_TENANT;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.CLIENT_ERROR_SIGNATURE_MISMATCH;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.CLIENT_ERROR_UNSUPPORTED_RESOURCE_TYPE_OR_ACTION;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.SERVER_ERROR_SIGNATURE_VALIDATION_FAILURE;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.SERVER_ERROR_UPDATING_IDV_CLAIM_VERIFICATION_STATUS;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.HMAC_SHA256_ALGORITHM;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.RESOURCE_WORKFLOW_RUN;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.BASE_URL;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.DATA_COMPARISON;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_EMPTY;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_INVALID_OR_MISSING_DATA_COMPARISON;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_INVALID_OR_MISSING_RESOURCE_OUTPUT;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_INVALID_WORKFLOW_RUN_STATUS;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_RESOLVING_IDV_PROVIDER;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_IDV_PROVIDER_INVALID_OR_DISABLED;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_RETRIEVING_CLAIMS_AGAINST_WORKFLOW_RUN_ID;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_SIGNATURE;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_SIGNATURE_VALIDATION;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_SIGNATURE_VALIDATION_PROCESSING;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_UNSUPPORTED_RESOURCE_TYPE_OR_ACTION;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_UPDATING_IDV_CLAIM_VERIFICATION_STATUS;;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ONFIDO_LAST_VERIFIED;
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
     * It validates the request verifying the signature, and updates the verification claims of the user based on the status.
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
     * Retrieves the tenant ID from the current context.
     *
     * @return The tenant ID.
     * @throws APIError If there is an error retrieving the tenant ID.
     */
    private int getTenantId() {

        String tenantDomain = ContextLoader.getTenantDomainFromContext();
        if (StringUtils.isBlank(tenantDomain)) {
            throw handleError(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    SERVER_ERROR_RETRIEVING_TENANT, tenantDomain);
        }

        return IdentityTenantUtil.getTenantId(tenantDomain);
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
     * @return Config property map of Identity Verification Provider
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
     * Implementation logic extracted from https://github.com/onfido/onfido-java/blob/2466de99b6036a8e72186e52bbd5e66e3779223d/src/main/java/com/onfido/WebhookEventVerifier.java#L81
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
        } catch (Exception ex) {
            throw new OnfidoServerException(ERROR_SIGNATURE_VALIDATION_PROCESSING.getCode(),
                    ERROR_SIGNATURE_VALIDATION_PROCESSING.getMessage(), ex);
        }

        // Compute the HMAC SHA-256 of the raw request body
        String expectedSignature = encodeHexString(sha256Hmac.doFinal(rawRequestBody.getBytes(StandardCharsets.UTF_8)));

        // Perform a time-safe comparison of the signatures
        if (!MessageDigest.isEqual(expectedSignature.getBytes(), xSHA2Signature.getBytes())) {
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
     * Extracts data comparison results from the resource map.
     *
     * @param resource The resource map containing output data
     * @return A map of data comparison results
     * @throws OnfidoClientException if the resource structure is invalid or missing required data
     */
    private Map<String, Object> extractDataComparisonResults(Map<String, Object> resource)
            throws OnfidoClientException {

        if (resource == null || !(resource.get(OUTPUT) instanceof Map)) {
            throw new OnfidoClientException(ERROR_INVALID_OR_MISSING_RESOURCE_OUTPUT.getCode(),
                    ERROR_INVALID_OR_MISSING_RESOURCE_OUTPUT.getMessage());
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> output = (Map<String, Object>) resource.get(OUTPUT);

        Object dataComparisonResults = output.get(DATA_COMPARISON);
        if (!(dataComparisonResults instanceof Map)) {
            throw new OnfidoClientException(ERROR_INVALID_OR_MISSING_DATA_COMPARISON.getCode(),
                    ERROR_INVALID_OR_MISSING_DATA_COMPARISON.getMessage());
        }

        return (Map<String, Object>) dataComparisonResults;
    }

    /**
     * Updates the identity verification claims based on the provided Onfido verification request.
     *
     * @param verifyRequest The Onfido verification request containing the workflow run details and attribute verification results.
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
        Map<String, Object> attributeVerificationResults = extractDataComparisonResults(payload.getResource());

        try {
            // Retrieve the WSO2 identity verification claims associated with the given workflow run ID.
            // Note: The workflow run ID is unique per user, so this should return claims for a single user.
            IdVClaim[] idVClaims = OnfidoIdvServiceHolder.getIdentityVerificationManager()
                    .getIdVClaimsByMetadata(ONFIDO_WORKFLOW_RUN_ID, workflowRunId, idvpId, tenantId);
            if (idVClaims == null || idVClaims.length == 0) {
                throw new OnfidoClientException(ERROR_RETRIEVING_CLAIMS_AGAINST_WORKFLOW_RUN_ID.getCode(),
                        ERROR_RETRIEVING_CLAIMS_AGAINST_WORKFLOW_RUN_ID.getMessage());
            }

            // Get the mapping of WSO2 claim URIs to Onfido claim names
            // (e.g., "http://wso2.org/claims/lastname" maps to "last_name").
            Map<String, String> claimMappings = idVProvider.getClaimMappings();

            for (IdVClaim idVClaim : idVClaims) {

                // Get the Onfido claim name corresponding to the WSO2 claim URI.
                String wso2ClaimUri = idVClaim.getClaimUri();
                String onfidoClaimName = claimMappings.get(wso2ClaimUri);
                if (onfidoClaimName == null) {
                    log.error(String.format("No mapped Onfido claim name identified for the claim URI: %s",
                            wso2ClaimUri));
                    continue;
                }

                // Map the Onfido claim name if necessary.
                // This is needed because some attribute names used during applicant creation (e.g., "dob") differ from
                // those used in the final attribute value comparison results (e.g., "date_of_birth").
                onfidoClaimName =
                        OnfidoConstants.ONFIDO_CLAIM_NAME_MAPPING.getOrDefault(onfidoClaimName, onfidoClaimName);

                Map<String, Object> metadata = idVClaim.getMetadata();
                if (metadata == null) {
                    log.error(String.format("Metadata for the claim URI: %s of user: %s is null", wso2ClaimUri,
                            idVClaim.getUserId()));
                    continue;
                }

                // Update the claim verification status if the workflow run status is "APPROVED"
                // Note:
                //  Even if the overall workflow status is "APPROVED", individual claims may still fail verification.
                //  This discrepancy primarily stems from how the workflow is defined in the Onfido Studio:
                //      1. The Onfido workflow might be configured to approve the overall process even if some claims
                //      don't match exactly.
                //      2. Some claims might be configured optional in the verification process.
                //      Their failure might not affect the overall approval.
                // Therefore, we need to check the verification result (i.e, value comparison results) for each claim
                // separately as we are focusing on the verification of exact claim value.
                if (workflowRunStatus == OnfidoConstants.WorkflowRunStatus.APPROVED) {
                    Map<String, Object> verificationResult =
                            (Map<String, Object>) attributeVerificationResults.get(onfidoClaimName);

                    if (verificationResult == null || verificationResult.get(RESULT) == null) {
                        log.error(String.format("No onfido verification results found for claim: %s of user: %s",
                                wso2ClaimUri, idVClaim.getUserId()));
                    } else {
                        // A claim is considered verified if its verification status is "CLEAR"
                        boolean isVerified = OnfidoConstants.ClaimVerificationStatus.CLEAR.toString()
                                .equals(verificationResult.get(RESULT));
                        idVClaim.setIsVerified(isVerified);

                        // Update metadata with verification status and timestamp
                        metadata.put(ONFIDO_VERIFICATION_STATUS, verificationResult.get(RESULT));
                        metadata.put(ONFIDO_LAST_VERIFIED, completedAt);
                    }
                }

                // Update the workflow status in the metadata
                metadata.put(ONFIDO_WORKFLOW_STATUS, workflowRunStatus.getStatus());
                idVClaim.setMetadata(metadata);

                // Persist the updated claim information in the database
                OnfidoIdvServiceHolder.getIdentityVerificationManager()
                        .updateIdVClaim(idVClaim.getUserId(), idVClaim, tenantId);
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Successfully updated claim verification status of the user: %s, claim: %s",
                            idVClaim.getUserId(), wso2ClaimUri));
                }
            }
        } catch (IdentityVerificationException e) {
            throw new OnfidoServerException(ERROR_UPDATING_IDV_CLAIM_VERIFICATION_STATUS.getCode(),
                    ERROR_UPDATING_IDV_CLAIM_VERIFICATION_STATUS.getMessage(), e);
        }
    }

    /**
     * Handles exceptions related to client errors by mapping error codes to appropriate HTTP statuses.
     *
     * @param e The OnfidoClientException to handle.
     */
    private void handleClientException(OnfidoClientException e) {

        Response.Status status = Response.Status.BAD_REQUEST;;
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
     * Overloaded method to handle cases where there is no exception to pass.
     *
     * @param status    The HTTP status to return.
     * @param errorEnum The error message enumeration.
     * @param data      Additional data for the error message.
     * @return An APIError response.
     */
    private APIError handleError(Response.Status status, Constants.ErrorMessage errorEnum, String... data) {

        return handleException(status, null, errorEnum, data);
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
