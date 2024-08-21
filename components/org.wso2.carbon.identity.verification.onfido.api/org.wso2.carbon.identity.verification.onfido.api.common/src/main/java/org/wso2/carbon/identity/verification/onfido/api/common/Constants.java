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

package org.wso2.carbon.identity.verification.onfido.api.common;

public class Constants {

    public static final String TENANT_NAME_FROM_CONTEXT = "TenantNameFromContext";
    public static final String CORRELATION_ID_MDC = "Correlation-ID";
    public static final String ERROR_PREFIX = "OIDV-";
    public static final String RESOURCE_WORKFLOW_RUN = "workflow_run";
    public static final String ACTION_WORKFLOW_RUN_COMPLETED = "workflow_run.completed";
    public static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

    /**
     * Enum for identity verification related errors.
     * Error Code - code to identify the error.
     * Error Message - What went wrong.
     * Error Description - Why it went wrong.
     */
    public enum ErrorMessage {

        // Server errors
        SERVER_ERROR_RESOLVING_IDVP("65001",
                "Identity verification provider retrieval failed.",
                "An error occurred while attempting to resolve the identity verification provider."),
        SERVER_ERROR_RETRIEVING_TENANT("65002",
                "Tenant retrieval failed.",
                "The system encountered an error while retrieving the tenant ID for the tenant domain: %s."),
        SERVER_ERROR_UPDATING_IDV_CLAIM_VERIFICATION_STATUS("65003",
                "Updating identity verification claims failed.",
                "An error occurred while updating the identity verification claims status."),
        SERVER_ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_INVALID("65004",
                "Invalid OnFido configuration properties.",
                "One or more OnFido identity verification provider configuration properties are invalid or missing."),
        SERVER_ERROR_SIGNATURE_VALIDATION_FAILURE("65005",
                "Error during signature validation.",
                "An error occurred while validating the request signature."),
        SERVER_ERROR_GENERAL_ERROR("65006",
                "Internal server error.",
                "An unexpected error occurred while processing the request."),
        SERVER_ERROR_INVALID_WORKFLOW_RUN_STATUS("65007",
                "Invalid Onfido workflow run status provided.",
                "An error occurred due to an invalid Onfido workflow run status being provided in the request."),


        // Client errors
        CLIENT_ERROR_RESOLVING_IDVP("60001",
                "Identity verification provider retrieval failed.",
                "The identity verification provider ID in the URL could not be resolved. It may be unavailable or disabled."),
        CLIENT_ERROR_SIGNATURE_MISMATCH("60002",
                "Invalid request signature.",
                "The request contains an invalid signature, indicating potential unauthorized access or data tampering."),
        CLIENT_ERROR_UNSUPPORTED_RESOURCE_TYPE_OR_ACTION("60003",
                "Unsupported resource type or action.",
                "The request contains an unsupported resource type or action. Only 'workflow_run' resource type and 'workflow_run.completed' action are supported."),
        CLIENT_ERROR_INVALID_WORKFLOW_OUTPUT("60004",
                "Invalid workflow output.",
                "The request contains an invalid workflow output format. Ensure that the workflow is configured to include the document report's data comparison results in the output."),
        CLIENT_ERROR_INVALID_REQUEST("60005",
                "Invalid request payload.",
                "The request payload contains invalid input, such as an invalid workflow run ID.");

        private final String code;
        private final String message;
        private final String description;

        ErrorMessage(String code, String message, String description) {

            this.code = code;
            this.message = message;
            this.description = description;
        }

        public String getCode() {

            return ERROR_PREFIX + code;
        }

        public String getMessage() {

            return message;
        }

        public String getDescription() {

            return description;
        }

        @Override
        public String toString() {

            return code + " | " + message;
        }
    }
}
