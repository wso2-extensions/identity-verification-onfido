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

package org.wso2.carbon.identity.verification.onfido.api.common;

public class Constants {

    public static final String TENANT_NAME_FROM_CONTEXT = "TenantNameFromContext";
    public static final String CORRELATION_ID_MDC = "Correlation-ID";
    public static final String ERROR_PREFIX = "IDVO-";
    public static final String RESOURCE_WORKFLOW_RUN = "workflow_run";
    public static final String ACTION_WORKFLOW_RUN_COMPLETED = "workflow_run.completed";

    /**
     * Enum for identity verification related errors.
     * Error Code - code to identify the error.
     * Error Message - What went wrong.
     * Error Description - Why it went wrong.
     */
    public enum ErrorMessage {

        // Server errors.
        ERROR_RESOLVING_IDVP("65000",
                "Error resolving identity verification provider.",
                "Error occurred while resolving the identity verification provider: %s."),
        ERROR_RETRIEVING_TENANT("65001",
                "Error retrieving tenant.",
                "Error occurred while retrieving tenant id for the tenant domain: %s."),
        ERROR_CHECK_VERIFICATION("65002",
                "Error performing verification check.",
                "Error occurred while performing verification check."),
        ERROR_RETRIEVING_IDV_CLAIM_BY_METADATA("65003",
                "Error retrieving IDV claims by metadata.",
                "Error occurred while retrieving IDV claims by metadata."),

        //Client errors
        ERROR_UNSUPPORTED_RESOURCE_TYPE_OR_ACTION("65004",
                 "Bad request : Received either an unsupported resource type or action.",
                 "Supported values are resource_type: 'workflow_run' and action: 'workflow_run.completed'.");

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
