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

package org.wso2.carbon.identity.verification.onfido.connector.constants;

import org.wso2.carbon.identity.verification.onfido.connector.exception.OnfidoClientException;
import org.wso2.carbon.identity.verification.onfido.connector.exception.OnfidoServerException;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_INVALID_ONFIDO_SDK_FLOW_STATUS;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_INVALID_WORKFLOW_RUN_STATUS;

/**
 * This class contains the constants used in the Onfido connector.
 */
public class OnfidoConstants {

    /**
     * General constants used throughout the Onfido connector.
     */
    private static final String IDV_ERROR_PREFIX = "OIDV-";
    public static final String ONFIDO = "ONFIDO";
    public static final String TOKEN = "token";
    public static final String WEBHOOK_TOKEN = "webhook_token";
    public static final String WORKFLOW_ID = "workflow_id";
    public static final String BASE_URL = "base_url";
    public static final String STATUS = "status";
    public static final String APPLICANT_ID = "applicant_id";
    public static final String SDK_TOKEN = "sdk_token";
    public static final String ID = "id";
    public static final String RESULT = "result";
    public static final String OUTPUT = "output";
    public static final String DATA_COMPARISON = "data_comparison";

    /**
     * HTTP request headers for Onfido API calls.
     */
    public static final String TOKEN_HEADER = "Token token=";
    public static final String APPLICATION_JSON = "application/json";

    /**
     * Onfido API endpoint paths.
     */
    public static final String APPLICANTS_ENDPOINT = "/applicants";
    public static final String SDK_TOKEN_ENDPOINT = "/sdk_token";
    public static final String WORKFLOW_RUN_ENDPOINT = "/workflow_runs";
    public static final String STATUS_VERIFY_ENDPOINT = "/workflow_runs/";

    /**
     * Metadata keys for storing onfido verification related details.
     */
    public static final String ONFIDO_APPLICANT_ID = "onfido_applicant_id";
    public static final String ONFIDO_WORKFLOW_RUN_ID = "onfido_workflow_run_id";
    public static final String ONFIDO_WORKFLOW_STATUS = "onfido_workflow_status";
    public static final String ONFIDO_VERIFICATION_STATUS = "onfido_verification_status";
    public static final String ONFIDO_LAST_VERIFIED = "onfido_last_verified";

    /**
     * Mapping of Onfido claim names used during applicant creation to those used in attribute value comparison results.
     */
    public static final Map<String, String> ONFIDO_CLAIM_NAME_MAPPING;
    static {
        Map<String, String> map = new HashMap<>();
        map.put("dob", "date_of_birth");
        ONFIDO_CLAIM_NAME_MAPPING = Collections.unmodifiableMap(map);
    }

    /**
     * Error messages.
     */
    public enum ErrorMessage {

        ERROR_VERIFICATION_FLOW_STATUS_NOT_FOUND("10000",
                "Verification flow status is not defined in the request"),
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
        ERROR_GETTING_ONFIDO_WORKFLOW_STATUS("10015",
                "Error while retrieving the Onfido workflow status."),
        ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_EMPTY("10016",
                "At least one IdVProvider configuration property is empty."),
        ERROR_SIGNATURE_VALIDATION("10017",
                "Signature validation failed due to an invalid request signature."),
        ERROR_SIGNATURE_VALIDATION_PROCESSING("10018","Error occurred during signature validation."),
        ERROR_UNSUPPORTED_RESOURCE_TYPE_OR_ACTION("10019",
                "Unsupported Onfido resource type or action; only 'workflow_run' and 'workflow_run.completed' are supported."),
        ERROR_INVALID_ONFIDO_SDK_FLOW_STATUS("10020",
                "Invalid Onfido SDK flow status provided."),
        ERROR_INVALID_WORKFLOW_RUN_STATUS("10021",
                "Invalid Onfido workflow run status provided."),
        ERROR_RETRIEVING_CLAIMS_AGAINST_WORKFLOW_RUN_ID("10022",
                "No claims found for the provided workflow run ID; the ID may be incorrect."),
        ERROR_UPDATING_IDV_CLAIM_VERIFICATION_STATUS("10023",
                "Error occurred while updating IDV claims verification status."),
        ERROR_INVALID_OR_MISSING_RESOURCE_OUTPUT("10024",
                "Webhook payload request is missing the expected output in the resource field."),
        ERROR_INVALID_OR_MISSING_DATA_COMPARISON("10025",
                "Webhook payload output is missing or contains invalid data_comparison field.");

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
     * Enum representing the various statuses that an SDK flow can transition through.
     */
    public enum OnFidoSdkFlowStatus {

        /**
         * Indicates that the SDK flow has been initiated but not yet completed.
         */
        INITIATED("INITIATED"),

        /**
         * Indicates that the SDK flow has been successfully completed.
         */
        COMPLETED("COMPLETED");

        private final String status;

        OnFidoSdkFlowStatus(String status) {
            this.status = status;
        }

        /**
         * Retrieves the string representation of the SDK flow status.
         *
         * @return The string status value.
         */
        public String getStatus() {
            return status;
        }

        /**
         * Converts a string value to the corresponding SdkFlowStatus enum.
         * If the value does not match any known statuses, an exception is thrown.
         *
         * @param status The string representation of the SDK flow status.
         * @return The corresponding SdkFlowStatus enum value.
         * @throws OnfidoClientException If the status is invalid or not recognized.
         */
        public static OnFidoSdkFlowStatus fromString(String status)
                throws OnfidoClientException {
            for (OnFidoSdkFlowStatus flowStatus : OnFidoSdkFlowStatus.values()) {
                if (flowStatus.status.equalsIgnoreCase(status)) {
                    return flowStatus;
                }
            }
            throw new OnfidoClientException(ERROR_INVALID_ONFIDO_SDK_FLOW_STATUS.getCode(),
                    ERROR_INVALID_ONFIDO_SDK_FLOW_STATUS.getMessage());
        }

        @Override
        public String toString() {
            return this.status;
        }
    }

    /**
     * Represents the various statuses that a workflow run can transition through as an applicant progresses
     * through tasks.
     * For more information, refer to the Onfido API documentation:
     * https://documentation.onfido.com/api/latest/#workflow-run-status.
     */
    public enum WorkflowRunStatus {

        /**
         * Indicates that the workflow is currently processing non-interactive tasks.
         */
        PROCESSING("processing"),

        /**
         * Indicates that the workflow is waiting for the applicant to complete a Smart Capture SDK interactive task.
         */
        AWAITING_INPUT("awaiting_input"),

        /**
         * Indicates that the workflow run has reached an end task where the applicant has been approved.
         */
        APPROVED("approved"),

        /**
         * Indicates that the workflow run has reached an end task where the applicant has been declined.
         */
        DECLINED("declined"),

        /**
         * Indicates that the workflow run has reached an end task where the applicant requires further review.
         */
        REVIEW("review"),

        /**
         * Indicates that the workflow has been abandoned due to an interactive task timing out.
         */
        ABANDONED("abandoned"),

        /**
         * Indicates that the workflow ended due to a technical issue during runtime.
         */
        ERROR("error");

        private final String status;
        private static final Set<WorkflowRunStatus> ENDING_STATUSES = Collections.unmodifiableSet(
                EnumSet.of(APPROVED, DECLINED, REVIEW)
                                                                                                 );
        WorkflowRunStatus(String status) {
            this.status = status;
        }

        /**
         * Retrieves the string representation of the workflow run status.
         *
         * @return The string status value.
         */
        public String getStatus() {
            return status;
        }

        /**
         * Converts a string value to the corresponding WorkflowRunStatus enum.
         * If the value does not match any known statuses, an exception is thrown.
         *
         * @param status The string representation of the workflow run status.
         * @return The corresponding WorkflowRunStatus enum value.
         * @throws OnfidoServerException If the status is invalid or not recognized.
         */
        public static WorkflowRunStatus fromString(String status) throws OnfidoServerException {
            for (WorkflowRunStatus vs : WorkflowRunStatus.values()) {
                if (vs.status.equalsIgnoreCase(status)) {
                    return vs;
                }
            }
            throw new OnfidoServerException(ERROR_INVALID_WORKFLOW_RUN_STATUS.getCode(),
                    ERROR_INVALID_WORKFLOW_RUN_STATUS.getMessage());
        }

        /**
         * Checks if the current status is one of the workflow run ending statuses.
         *
         * @return true if it is one of the ending statuses, false otherwise.
         */
        public boolean isEndingStatus() {
            return ENDING_STATUSES.contains(this);
        }
    }


    /**
     * Enum representing the possible verification statuses for identity claims.
     */
    public enum ClaimVerificationStatus {

        /**
         * Indicates that the claim has been verified and matches the provided information.
         * No issues or discrepancies were found during the verification process.
         */
        CLEAR("clear"),

        /**
         * Indicates that the claim requires further review or investigation.
         * There may be minor discrepancies or potential issues that need to be addressed.
         * This status suggests that the verification did not fully pass, and additional checks may be necessary.
         */
        CONSIDER("consider"),

        /**
         * Indicates that the claim status is unknown or not provided.
         * This may occur when data comparison is disabled, resulting in a null value or the value comparison for the
         * attribute is not supported.
         */
        UNKNOWN(null);

        private final String value;

        ClaimVerificationStatus(String value) {
            this.value = value;
        }

        /**
         * Retrieves the string value associated with the verification status.
         *
         * @return The string representation of the verification status.
         */
        public String getValue() {
            return value;
        }

        /**
         * Converts a string value to the corresponding ClaimVerificationStatus enum.
         * If the value does not match any known statuses, UNKNOWN is returned.
         *
         * @param text The string representation of the verification status.
         * @return The corresponding ClaimVerificationStatus enum value.
         */
        public static ClaimVerificationStatus fromString(String text) {
            for (ClaimVerificationStatus status : ClaimVerificationStatus.values()) {
                if (status.value != null && status.value.equalsIgnoreCase(text)) {
                    return status;
                }
            }
            return UNKNOWN;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }
}
