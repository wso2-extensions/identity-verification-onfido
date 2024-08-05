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

package org.wso2.carbon.identity.verification.onfido.connector.constants;

import org.wso2.carbon.identity.verification.onfido.connector.exception.OnfidoServerException;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_INVALID_VERIFICATION_STATUS;

/**
 * This class contains the constants used in the Onfido connector.
 */
public class OnfidoConstants {

    private static final String IDV_ERROR_PREFIX = "OIDV-";
    public static final String ONFIDO = "ONFIDO";
    public static final String STATUS = "status";
    public static final String APPLICANT_ID = "applicant_id";
    public static final String WORKFLOW_ID = "workflow_id";
    public static final String WORKFLOW_RUN_ID = "workflow_run_id";
    public static final String SDK_TOKEN = "sdk_token";
    public static final String TOKEN = "token";
    public static final String WEBHOOK_TOKEN = "webhook_token";
    public static final String INITIATED = "INITIATED";
    public static final String COMPLETED = "COMPLETED";
    public static final String BASE_URL = "base_url";
    public static final String ID = "id";
    public static final String TOKEN_HEADER = "Token token=";
    public static final String APPLICATION_JSON = "application/json";
    public static final String APPLICANTS_ENDPOINT = "/applicants";
    public static final String SDK_TOKEN_ENDPOINT = "/sdk_token";
    public static final String WORKFLOW_RUN_ENDPOINT = "/workflow_runs";
    public static final String STATUS_VERIFY_ENDPOINT = "/workflow_runs/";

    /**
     * Error messages.
     */
    public enum ErrorMessage {

        ERROR_VERIFICATION_STATUS_NOT_FOUND("10000",
                "Verification status is not defined in the request"),
        ERROR_IDENTITY_VERIFICATION("10001",
                "Error while verifying the user identity through Onfido."),
        ERROR_CLAIM_VALUE_NOT_EXIST("10002",
                "Required identity verification claim value does not exist."),
        ERROR_CREATING_RESPONSE("10003", "Error while creating the response."),
        ERROR_VERIFICATION_ALREADY_INITIATED("10004", "Verification already initiated"),
        ERROR_CREATING_ONFIDO_APPLICANT("10005",
                "The applicant creation in the Onfido failed with the response %s."),
        ERROR_INITIATING_ONFIDO_VERIFICATION("10006",
                "Initiating the verification in Onfido failed with the response %s."),
        ERROR_CREATING_WORKFLOW_RUN("10007",
                "Creating the Onfido workflow run was failed with the response %s."),
        ERROR_GETTING_ONFIDO_SDK_TOKEN("10008",
                "Getting the Onfido SDK token was failed with the response %s."),
        ERROR_IDV_PROVIDER_INVALID_OR_DISABLED("10009",
                "IdVProvider is not available or not enabled"),
        ERROR_RESOLVING_IDV_PROVIDER("10010",
                "Error encountered while retrieving the identity verification provider."),
        ERROR_SIGNATURE("10011", "Signature is null"),
        ERROR_CREATING_HTTP_CLIENT("10012", "Server error encountered while creating http client"),
        ERROR_UPDATING_ONFIDO_APPLICANT("10013",
                "The applicant updating in the Onfido failed with the response %s."),
        ERROR_GETTING_HTTP_CLIENT("10014", "Error preparing http client to publish events."),
        ERROR_GETTING_ONFIDO_VERIFICATION_STATUS("10015",
                "Error while retrieving the Onfido verification status."),
        ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_EMPTY("10016",
                "At least one IdVProvider configuration property is empty."),
        ERROR_SIGNATURE_VALIDATION("10017",
                "Signature validation failed due to an invalid request signature."),
        ERROR_SIGNATURE_VALIDATION_PROCESSING("10018","Error occurred during signature validation."),
        ERROR_UNSUPPORTED_RESOURCE_TYPE_OR_ACTION("10019",
                "Unsupported Onfido resource type or action; only 'workflow_run' and 'workflow_run.completed' are supported."),
        ERROR_INVALID_VERIFICATION_STATUS("10020",
                "Invalid Onfido verification status provided."),
        ERROR_RETRIEVING_CLAIMS_AGAINST_WORKFLOW_RUN_ID("10021",
                "No claims found for the provided workflow run ID; the ID may be incorrect."),
        ERROR_UPDATING_IDV_CLAIM_VERIFICATION_STATUS("10022",
                "Error occurred while updating IDV claims verification status.");

        private final String code;
        private final String message;

        ErrorMessage(String code, String message) {

            this.code = code;
            this.message = message;
        }

        public String getCode() {

            return IDV_ERROR_PREFIX + code;
        }

        public String getMessage() {

            return message;
        }

        @Override
        public String toString() {

            return code + ":" + message;
        }
    }

    /**
     * Statuses of an Onfido verification process.
     */
    public enum VerificationStatus {
        PROCESSING("processing"),
        AWAITING_INPUT("awaiting_input"),
        APPROVED("approved"),
        DECLINED("declined"),
        REVIEW("review"),
        ABANDONED("abandoned"),
        ERROR("error");

        private final String status;

        VerificationStatus(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public static VerificationStatus fromString(String status) throws OnfidoServerException {
            for (VerificationStatus vs : VerificationStatus.values()) {
                if (vs.status.equalsIgnoreCase(status)) {
                    return vs;
                }
            }
            throw new OnfidoServerException(ERROR_INVALID_VERIFICATION_STATUS.getCode(),
                    ERROR_INVALID_VERIFICATION_STATUS.getMessage());
        }
    }

}
