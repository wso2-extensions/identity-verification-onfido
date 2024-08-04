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
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.ERROR_CHECK_VERIFICATION;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.ERROR_INVALID_REQUEST;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.ERROR_RESOLVING_IDVP;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.ERROR_UPDATING_IDV_CLAIM_VERIFICATION_STATUS;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.RESOURCE_WORKFLOW_RUN;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.BASE_URL;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_EMPTY;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_RETRIEVING_IDV_PROVIDER;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_SIGNATURE;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_SIGNATURE_VALIDATION;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_UNSUPPORTED_RESOURCE_TYPE_OR_ACTION;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.STATUS;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.TOKEN;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.WEBHOOK_TOKEN;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.WORKFLOW_ID;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.WORKFLOW_RUN_ID;
import org.wso2.carbon.identity.verification.onfido.api.v1.interceptors.RawRequestBodyInterceptor;

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

            // Access the raw request body
            String rawRequestBody = RawRequestBodyInterceptor.getRawRequestBody();

            String resourceType = verifyRequest.getPayload().getResourceType();
            String action = verifyRequest.getPayload().getAction();
            validateResourceTypeAndAction(resourceType, action);

            IdVProvider idVProvider =
                    OnfidoIdvServiceHolder.getIdVProviderManager().getIdVProvider(idvpId, tenantId);
            if (idVProvider == null || !idVProvider.isEnabled()) {
                throw new OnfidoClientException(ERROR_RETRIEVING_IDV_PROVIDER.getCode(),
                        ERROR_RETRIEVING_IDV_PROVIDER.getMessage());
            }
            Map<String, String> idVProviderConfigProperties = getIdVConfigPropertyMap(idVProvider);
            validateIdVProviderConfigProperties(idVProviderConfigProperties);

            // Validate the signature available in the header with the webhook token.
            validateSignature(xSHA2Signature, idVProviderConfigProperties, rawRequestBody);

            String workFlowRunId = verifyRequest.getPayload().getObject().getId();
            OnfidoConstants.VerificationStatus status =
                    OnfidoConstants.VerificationStatus.fromString(verifyRequest.getPayload().getObject().getStatus());

            updateIdVClaims(workFlowRunId, idvpId, tenantId, status);

        } catch (IdVProviderMgtException e) {
            throw handleIdVException(e, ERROR_RESOLVING_IDVP, idvpId);
        } catch (OnfidoServerException e) {
            throw handleIdVException(e, ERROR_CHECK_VERIFICATION, idvpId);
        }catch (OnfidoClientException e) {
            throw handleIdVException(e, ERROR_INVALID_REQUEST);
        } catch (IdentityVerificationException e) {
            throw handleIdVException(e, ERROR_UPDATING_IDV_CLAIM_VERIFICATION_STATUS, idvpId);
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
            throw handleException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    Constants.ErrorMessage.ERROR_RETRIEVING_TENANT, tenantDomain);
        }

        return IdentityTenantUtil.getTenantId(tenantDomain);
    }

    /**
     * Get config property map of Identity Verification Provider.
     *
     * @param idVProvider Identity Verification Provider.
     * @return Config property map of Identity Verification Provider
     */
    private Map<String, String> getIdVConfigPropertyMap(IdVProvider idVProvider) {

        IdVConfigProperty[] idVConfigProperties = idVProvider.getIdVConfigProperties();
        Map<String, String> configPropertyMap = new HashMap<>();
        for (IdVConfigProperty idVConfigProperty : idVConfigProperties) {
            configPropertyMap.put(idVConfigProperty.getName(), idVConfigProperty.getValue());
        }
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
     * Validates the signature available in the header with the webhook token.
     *
     * @param xSHA2Signature                The SHA-2 signature from the Onfido webhook.
     * @param idVProviderConfigProperties   The configuration properties of the Identity Verification Provider.
     * @param rawRequestBody                 The raw verification request payload from Onfido.
     * @throws OnfidoServerException If the signature validation fails.
     */
    private void validateSignature(String xSHA2Signature, Map<String, String> idVProviderConfigProperties,
                                   String rawRequestBody) throws OnfidoServerException {

        try {
            if (StringUtils.isBlank(xSHA2Signature)) {
                throw new OnfidoServerException(ERROR_SIGNATURE.getCode(), ERROR_SIGNATURE.getMessage());
            }

            String webhookToken = idVProviderConfigProperties.get(WEBHOOK_TOKEN);

            // Compute the HMAC using the SHA256 algorithm and your webhook's token as the key.
            String expectedSignature = computeHmacSHA256(webhookToken, rawRequestBody);

            // Make sure signatures are both in binary or both in hexadecimal before comparing.
            byte[] signatureHeader = decodeHexadecimal(xSHA2Signature);

            // Use a constant time equality function to prevent timing attacks.
            if (!constantTimeEquals(signatureHeader, expectedSignature.getBytes())) {
                throw new OnfidoServerException(ERROR_SIGNATURE_VALIDATION.getCode(),
                        ERROR_SIGNATURE_VALIDATION.getMessage());
            }
        } catch (SignatureException e) {
            throw new OnfidoServerException(ERROR_SIGNATURE_VALIDATION.getCode(),
                    ERROR_SIGNATURE_VALIDATION.getMessage());
        }
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

    /**
     * Handles identity verification exceptions and constructs an APIError response.
     *
     * @param e          The identity verification exception.
     * @param errorEnum  The error message enumeration.
     * @param data       Additional data for the error message.
     * @return An APIError response.
     */
    public APIError handleIdVException(IdentityException e, Constants.ErrorMessage errorEnum, String... data) {

        ErrorResponse errorResponse;
        Response.Status status;
        if (e instanceof OnfidoClientException) {
            status = Response.Status.BAD_REQUEST;
            errorResponse = getErrorBuilder(e, errorEnum, data)
                    .build(log, e, buildErrorDescription(errorEnum.getDescription(), data), true);
        } else {
            status = Response.Status.INTERNAL_SERVER_ERROR;
            errorResponse = getErrorBuilder(e, errorEnum, data)
                    .build(log, e, buildErrorDescription(errorEnum.getDescription(), data), false);
        }
        return new APIError(status, errorResponse);
    }

    /**
     * Constructs an error response builder for identity verification exceptions.
     *
     * @param exception  The identity verification exception.
     * @param errorEnum  The error message enumeration.
     * @param data       Additional data for the error message.
     * @return An ErrorResponse.Builder instance.
     */
    private ErrorResponse.Builder getErrorBuilder(IdentityException exception,
                                                  Constants.ErrorMessage errorEnum, String... data) {

        String errorCode = (StringUtils.isBlank(exception.getErrorCode())) ?
                errorEnum.getCode() : exception.getErrorCode();
        String description = (StringUtils.isBlank(exception.getMessage())) ?
                errorEnum.getDescription() : exception.getMessage();
        return new ErrorResponse.Builder()
                .withCode(errorCode)
                .withMessage(errorEnum.getMessage())
                .withDescription(buildErrorDescription(description, data));
    }

    /**
     * Constructs an error description string using the provided description and data.
     *
     * @param description The error description template.
     * @param data        Additional data for the error description.
     * @return The constructed error description string.
     */
    private String buildErrorDescription(String description, String... data) {

        if (ArrayUtils.isNotEmpty(data)) {
            return String.format(description, (Object[]) data);
        }
        return description;
    }

    /**
     * Handle exceptions generated in API.
     *
     * @param status HTTP Status.
     * @param error  Error Message information.
     * @return APIError.
     */
    private APIError handleException(Response.Status status, Constants.ErrorMessage error, String data) {

        return new APIError(status, getErrorBuilder(error, data).build());
    }

    /**
     * Constructs an error response builder for API errors.
     *
     * @param errorMsg The error message enumeration.
     * @param data     Additional context data.
     * @return An ErrorResponse.Builder instance.
     */
    private ErrorResponse.Builder getErrorBuilder(Constants.ErrorMessage errorMsg, String data) {

        return new ErrorResponse.Builder()
                .withCode(errorMsg.getCode())
                .withMessage(errorMsg.getMessage())
                .withDescription(includeData(errorMsg, data));
    }

    /**
     * Includes context data in the error message description.
     *
     * @param error The error message enumeration.
     * @param data  The context data.
     * @return The formatted error message description.
     */
    private String includeData(Constants.ErrorMessage error, String data) {

        if (StringUtils.isNotBlank(data)) {
            return String.format(error.getDescription(), data);
        } else {
            return error.getDescription();
        }
    }

    /**
     * Validates the resource type and action received in the verification request.
     * Throws an OnfidoClientException if the resource type or action is unsupported.
     *
     * @param resourceType The type of the resource to be verified.
     * @param action       The action associated with the verification request.
     * @throws OnfidoClientException if the resource type or action is unsupported.
     */
    private void validateResourceTypeAndAction(String resourceType, String action) throws OnfidoClientException {

        if (!RESOURCE_WORKFLOW_RUN.equals(resourceType) || !ACTION_WORKFLOW_RUN_COMPLETED.equals(action)) {
            throw new OnfidoClientException(ERROR_UNSUPPORTED_RESOURCE_TYPE_OR_ACTION.getCode(),
                    ERROR_UNSUPPORTED_RESOURCE_TYPE_OR_ACTION.getMessage());
        }
    }

    /**
     * Retrieves, processes, and updates identity verification claims based on the provided workflow run ID.
     *
     * @param workFlowRunId The unique identifier for the workflow run associated with the ID verification claims.
     * @param idvpId        The identity verification provider ID.
     * @param tenantId      The tenant ID.
     * @param status        The Onfido verification status.
     * @throws IdentityVerificationException If an error occurs while processing or updating the ID verification claims.
     */
    private void updateIdVClaims(String workFlowRunId, String idvpId, int tenantId, OnfidoConstants.VerificationStatus status)
            throws IdentityVerificationException {

        boolean isVerified = (status == OnfidoConstants.VerificationStatus.APPROVED);

        // Retrieve ID verification claims based on metadata
        IdVClaim[] idVClaims = OnfidoIdvServiceHolder.getIdentityVerificationManager()
                .getIdVClaimsByMetadata(WORKFLOW_RUN_ID, workFlowRunId, idvpId, tenantId);

        // Process and update each ID verification claim
        for (IdVClaim idVClaim : idVClaims) {
            if (idVClaim != null) {
                idVClaim.setIsVerified(isVerified);
                if (idVClaim.getMetadata() != null && idVClaim.getMetadata().containsKey(STATUS)) {
                    idVClaim.getMetadata().replace(STATUS, status.getStatus());
                }
                // Update the ID verification claim in the database
                OnfidoIdvServiceHolder.getIdentityVerificationManager().updateIdVClaim(idVClaim.getUserId(), idVClaim, tenantId);
            }
        }
    }
}
