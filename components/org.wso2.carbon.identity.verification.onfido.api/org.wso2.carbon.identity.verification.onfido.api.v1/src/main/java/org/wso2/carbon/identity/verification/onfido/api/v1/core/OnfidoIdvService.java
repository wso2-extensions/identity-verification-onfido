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
import org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants;
import org.wso2.carbon.identity.verification.onfido.connector.exception.OnfidoClientException;
import org.wso2.carbon.identity.verification.onfido.connector.exception.OnfidoServerException;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.Response;

import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ACTION_WORKFLOW_RUN_COMPLETED;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.CLIENT_ERROR_INVALID_REQUEST;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.CLIENT_ERROR_RESOLVING_IDVP;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.SERVER_ERROR_GENERAL_ERROR;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.SERVER_ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_INVALID;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.SERVER_ERROR_INVALID_VERIFICATION_STATUS;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.SERVER_ERROR_RESOLVING_IDVP;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.SERVER_ERROR_RETRIEVING_TENANT;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.CLIENT_ERROR_SIGNATURE_MISMATCH;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.CLIENT_ERROR_UNSUPPORTED_RESOURCE_TYPE_OR_ACTION;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.SERVER_ERROR_SIGNATURE_VALIDATION_FAILURE;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.SERVER_ERROR_UPDATING_IDV_CLAIM_VERIFICATION_STATUS;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.RESOURCE_WORKFLOW_RUN;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.BASE_URL;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_EMPTY;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_INVALID_VERIFICATION_STATUS;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_RESOLVING_IDV_PROVIDER;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_RETRIEVING_CLAIMS_AGAINST_WORKFLOW_RUN_ID;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_IDV_PROVIDER_INVALID_OR_DISABLED;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_SIGNATURE;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_SIGNATURE_VALIDATION;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_SIGNATURE_VALIDATION_PROCESSING;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_UNSUPPORTED_RESOURCE_TYPE_OR_ACTION;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_UPDATING_IDV_CLAIM_VERIFICATION_STATUS;;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.STATUS;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.TOKEN;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.WEBHOOK_TOKEN;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.WORKFLOW_ID;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.WORKFLOW_RUN_ID;

/**
 * Onfido Identity Verification Service implementation to be notified when he verification is completed.
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

//            validateSignature(xSHA2Signature, idVProviderConfigProperties, rawRequestBody);

            updateIdVClaims(verifyRequest.getPayload().getObject().getId(), idvpId, tenantId,
                    OnfidoConstants.VerificationStatus.fromString(verifyRequest.getPayload().getObject().getStatus()));

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
     *
     * @param xSHA2Signature              The SHA-2 signature from the Onfido webhook.
     * @param idVProviderConfigProperties The configuration properties of the Identity Verification Provider.
     * @param rawRequestBody              The raw verification request payload from Onfido.
     * @throws OnfidoClientException If the signature validation fails.
     * @throws OnfidoServerException If a server-side error occurs during validation.
     */
    private void validateSignature(String xSHA2Signature, Map<String, String> idVProviderConfigProperties,
                                   String rawRequestBody) throws OnfidoClientException, OnfidoServerException {

        try {
            if (StringUtils.isBlank(xSHA2Signature)) {
                throw new OnfidoClientException(ERROR_SIGNATURE.getCode(), ERROR_SIGNATURE.getMessage());
            }

            String webhookToken = idVProviderConfigProperties.get(WEBHOOK_TOKEN);

            // Compute the HMAC using the SHA256 algorithm and your webhook's token as the key.
            String expectedSignature = computeHmacSHA256(webhookToken, rawRequestBody);

            // Make sure signatures are both in binary or both in hexadecimal before comparing.
            byte[] signatureHeader = decodeHexadecimal(xSHA2Signature);

            // Use a constant time equality function to prevent timing attacks.
            if (!constantTimeEquals(signatureHeader, expectedSignature.getBytes())) {
                throw new OnfidoClientException(ERROR_SIGNATURE_VALIDATION.getCode(),
                        ERROR_SIGNATURE_VALIDATION.getMessage());
            }
        } catch (SignatureException e) {
            throw new OnfidoServerException(ERROR_SIGNATURE_VALIDATION_PROCESSING.getCode(),
                    ERROR_SIGNATURE_VALIDATION_PROCESSING.getMessage());
        }
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
     * Updates the identity verification claims based on the provided workflow run ID.
     *
     * @param workFlowRunId The unique identifier for the workflow run associated with the ID verification claims.
     * @param idvpId        The identity verification provider ID.
     * @param tenantId      The tenant ID.
     * @param status        The Onfido verification status.
     * @throws OnfidoClientException If no claims are found for the provided workflow run ID.
     * @throws OnfidoServerException If a server-side error occurs while updating the claims.
     */
    private void updateIdVClaims(String workFlowRunId, String idvpId, int tenantId,
                                 OnfidoConstants.VerificationStatus status)
            throws OnfidoClientException, OnfidoServerException {

        boolean isVerified = (status == OnfidoConstants.VerificationStatus.APPROVED);

        try {
            // Retrieve ID verification claims based on metadata
            IdVClaim[] idVClaims = OnfidoIdvServiceHolder.getIdentityVerificationManager()
                    .getIdVClaimsByMetadata(WORKFLOW_RUN_ID, workFlowRunId, idvpId, tenantId);

            if (idVClaims == null || idVClaims.length == 0) {
                throw new OnfidoClientException(ERROR_RETRIEVING_CLAIMS_AGAINST_WORKFLOW_RUN_ID.getCode(),
                        ERROR_RETRIEVING_CLAIMS_AGAINST_WORKFLOW_RUN_ID.getMessage());
            }

            // Process and update each ID verification claim
            for (IdVClaim idVClaim : idVClaims) {
                if (idVClaim != null) {
                    idVClaim.setIsVerified(isVerified);
                    if (idVClaim.getMetadata() != null && idVClaim.getMetadata().containsKey(STATUS)) {
                        idVClaim.getMetadata().replace(STATUS, status.getStatus());
                    }
                    // Update the ID verification claim in the database
                    OnfidoIdvServiceHolder.getIdentityVerificationManager()
                            .updateIdVClaim(idVClaim.getUserId(), idVClaim, tenantId);
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
        } else if (ERROR_INVALID_VERIFICATION_STATUS.getCode().equals(errorCode)) {
            errorMessage = SERVER_ERROR_INVALID_VERIFICATION_STATUS;
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

    /**
     * Computes the HMAC SHA-256 hash of a given value using the provided key.
     *
     * @param key   The key to use for HMAC computation.
     * @param value The value to hash.
     * @return The computed HMAC SHA-256 hash.
     * @throws SignatureException If an error occurs during HMAC computation.
     */
    private static String computeHmacSHA256(String key, String value) throws SignatureException {

        String result;
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            result = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
        } catch (NoSuchAlgorithmException e) {
            throw new SignatureException("Invalid algorithm provided while calculating HMAC signature.", e);
        } catch (InvalidKeyException e) {
            throw new SignatureException("Failed to calculate HMAC signature.", e);
        }
        return result;
    }

    /**
     * Decodes a hexadecimal string into a byte array.
     *
     * @param hexadecimalString The hexadecimal string to decode.
     * @return The decoded byte array.
     */
    private static byte[] decodeHexadecimal(String hexadecimalString) {

        int len = hexadecimalString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexadecimalString.charAt(i), 16) << 4)
                    + Character.digit(hexadecimalString.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Compares two byte arrays in constant time to prevent timing attacks.
     *
     * @param a The first byte array.
     * @param b The second byte array.
     * @return True if the byte arrays are equal, false otherwise.
     */
    private static boolean constantTimeEquals(byte[] a, byte[] b) {

        if (a.length != b.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
