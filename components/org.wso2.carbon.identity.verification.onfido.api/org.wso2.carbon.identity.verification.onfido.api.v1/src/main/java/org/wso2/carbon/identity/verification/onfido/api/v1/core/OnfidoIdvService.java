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
import org.json.JSONObject;
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
import org.wso2.carbon.identity.verification.onfido.connector.exception.OnfidoClientException;
import org.wso2.carbon.identity.verification.onfido.connector.exception.OnfidoServerException;
import org.wso2.carbon.identity.verification.onfido.connector.web.OnfidoAPIClient;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.core.Response;

import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.ERROR_CHECK_VERIFICATION;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.ERROR_RESOLVING_IDVP;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.ERROR_RETRIEVING_IDV_CLAIM_BY_METADATA;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.BASE_URL;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_EMPTY;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_SIGNATURE;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_SIGNATURE_VALIDATION;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.TOKEN;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.WEBHOOK_TOKEN;

/**
 * Onfido Identity Verification Service implementation to be notified when he verification is completed.
 */
public class OnfidoIdvService {

    private static final Log log = LogFactory.getLog(OnfidoIdvService.class);

    public void verify(String xSHA2Signature, String idvpId, VerifyRequest verifyRequest) {

        int tenantId = getTenantId();
        try {
            String checkId = verifyRequest.getPayload().getObject().getId();
            IdVProvider idVProvider =
                    OnfidoIdvServiceHolder.getIdVProviderManager().getIdVProvider(idvpId, tenantId);
            Map<String, String> idVProviderConfigProperties = getIdVConfigPropertyMap(idVProvider);
            validateIdVProviderConfigProperties(idVProviderConfigProperties);

            // Validate the signature available in the header with the webhook token.
            validateSignature(xSHA2Signature, idVProviderConfigProperties, verifyRequest);

            JSONObject checkJsonObject =
                    OnfidoAPIClient.verificationCheckGet(idVProviderConfigProperties, checkId);
            String verificationStatus = checkJsonObject.get("result").toString();
            String applicantIdValue = checkJsonObject.get("applicant_id").toString();
            IdVClaim[] idVClaims = OnfidoIdvServiceHolder.getIdentityVerificationManager().
                    getIdVClaimsByMetadata("applicant_id", applicantIdValue, idvpId, tenantId);
            boolean isVerified;
            String idvStatus;
            if (StringUtils.equals(verificationStatus, "clear")) {
                isVerified = true;
                idvStatus = "VERIFIED";
            } else {
                isVerified = false;
                idvStatus = "REJECTED";
            }
            for (IdVClaim idVClaim : idVClaims) {
                if (idVClaim != null) {
                    idVClaim.setIsVerified(isVerified);
                }
                String status = "status";
                if (idVClaim != null && idVClaim.getMetadata() != null &&
                        idVClaim.getMetadata().containsKey(status)) {
                    idVClaim.getMetadata().replace(status, idvStatus);
                }
            }
        } catch (IdVProviderMgtException e) {
            throw handleIdVException(e, ERROR_RESOLVING_IDVP, idvpId);
        } catch (OnfidoServerException e) {
            throw handleIdVException(e, ERROR_CHECK_VERIFICATION, idvpId);
        } catch (IdentityVerificationException e) {
            throw handleIdVException(e, ERROR_RETRIEVING_IDV_CLAIM_BY_METADATA, idvpId);
        }
    }

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

    private void validateIdVProviderConfigProperties(Map<String, String> idVProviderConfigProperties)
            throws OnfidoServerException {

        if (idVProviderConfigProperties == null || idVProviderConfigProperties.isEmpty() ||
                StringUtils.isBlank(idVProviderConfigProperties.get(TOKEN)) ||
                StringUtils.isBlank(idVProviderConfigProperties.get(BASE_URL)) ||
                StringUtils.isBlank(idVProviderConfigProperties.get(WEBHOOK_TOKEN))) {

            throw new OnfidoServerException(ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_EMPTY.getCode(),
                    ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_EMPTY.getMessage());
        }
    }

    private void validateSignature(String xSHA2Signature, Map<String, String> idVProviderConfigProperties,
                                   VerifyRequest verifyRequest) throws OnfidoServerException {

        try {
            if (StringUtils.isBlank(xSHA2Signature)) {
                throw new OnfidoServerException(ERROR_SIGNATURE.getCode(), ERROR_SIGNATURE.getMessage());
            }

            String webhookToken = idVProviderConfigProperties.get(WEBHOOK_TOKEN);
            String verificationRequest = verifyRequest.toString();

            // Compute the HMAC using the SHA256 algorithm and your webhook's token as the key.
            String expectedSignature = computeHmacSHA256(webhookToken, verificationRequest);

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

    private static byte[] decodeHexadecimal(String hexadecimalString) {

        int len = hexadecimalString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexadecimalString.charAt(i), 16) << 4)
                    + Character.digit(hexadecimalString.charAt(i + 1), 16));
        }
        return data;
    }

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

    public APIError handleIdVException(IdentityException e, Constants.ErrorMessage errorEnum, String... data) {

        ErrorResponse errorResponse;
        Response.Status status;
        // todo
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
     * Return error builder.
     *
     * @param errorMsg Error Message information.
     * @return ErrorResponse.Builder.
     */
    private ErrorResponse.Builder getErrorBuilder(Constants.ErrorMessage errorMsg, String data) {

        return new ErrorResponse.Builder()
                .withCode(errorMsg.getCode())
                .withMessage(errorMsg.getMessage())
                .withDescription(includeData(errorMsg, data));
    }

    /**
     * Include context data to error message.
     *
     * @param error Error message.
     * @param data  Context data.
     * @return Formatted error message.
     */
    private String includeData(Constants.ErrorMessage error, String data) {

        if (StringUtils.isNotBlank(data)) {
            return String.format(error.getDescription(), data);
        } else {
            return error.getDescription();
        }
    }
}
