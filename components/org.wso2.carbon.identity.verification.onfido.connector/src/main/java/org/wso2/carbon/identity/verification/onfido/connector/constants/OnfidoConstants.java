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

import org.wso2.carbon.identity.verification.onfido.connector.exception.OnfidoClientException;
import org.wso2.carbon.identity.verification.onfido.connector.exception.OnfidoServerException;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_INVALID_ONFIDO_VERIFICATION_FLOW_STATUS;
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
    public static final String DOB = "dob";
    public static final String DATE_OF_BIRTH = "date_of_birth";

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

    /**
     * Metadata keys for storing onfido verification related details.
     */
    public static final String ONFIDO_APPLICANT_ID = "onfido_applicant_id";
    public static final String ONFIDO_WORKFLOW_RUN_ID = "onfido_workflow_run_id";
    public static final String ONFIDO_WORKFLOW_STATUS = "onfido_workflow_status";
    public static final String ONFIDO_VERIFICATION_STATUS = "onfido_verification_status";
    public static final String ONFIDO_COMPLETED_AT = "onfido_completed_at";

    /**
     * Mapping of Onfido claim names used during applicant creation to those used in attribute value comparison results.
     */
    public static final Map<String, String> ONFIDO_CLAIM_NAME_MAPPING;
    static {
        Map<String, String> map = new HashMap<>();
        map.put(DOB, DATE_OF_BIRTH);
        ONFIDO_CLAIM_NAME_MAPPING = Collections.unmodifiableMap(map);
    }

    /**
     * Error messages.
     */
    public enum ErrorMessage {

        ERROR_VERIFICATION_FLOW_STATUS_NOT_FOUND("10000",
                "Verification flow status is missing or undefined in the request"),
        ERROR_IDENTITY_VERIFICATION("10001",
                "Error while verifying the user identity through Onfido."),
        ERROR_CLAIM_VALUE_NOT_EXIST("10002",
                "Required identity verification claim value does not exist."),
        ERROR_CREATING_RESPONSE("10003", "Error while creating the response."),
        ERROR_VERIFICATION_ALREADY_INITIATED("10004", "Verification already initiated"),
        ERROR_CREATING_ONFIDO_APPLICANT("10005",
                "The applicant creation in the Onfido failed with the response %s."),
        ERROR_INITIATING_ONFIDO_VERIFICATION("10006",
                "Error occured while initiating the verification in Onfido for the user : %s."),
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
        ERROR_ONFIDO_WORKFLOW_RUN_ID_NOT_FOUND("10014", "No associated Onfido workflow run found. " +
                "Ensure that the verification process has been initiated before attempting to complete " +
                "or reinitiate it."),
        ERROR_GETTING_ONFIDO_WORKFLOW_STATUS("10015",
                "Error while retrieving the Onfido workflow status."),
        ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_EMPTY("10016",
                "At least one IdVProvider configuration property is empty."),
        ERROR_SIGNATURE_VALIDATION("10017",
                "Signature validation failed due to an invalid request signature."),
        ERROR_SIGNATURE_VALIDATION_PROCESSING("10018", "Error occurred during signature validation."),
        ERROR_UNSUPPORTED_RESOURCE_TYPE_OR_ACTION("10019",
                "Unsupported Onfido resource type or action; only 'workflow_run' and " +
                        "'workflow_run.completed' are supported."),
        ERROR_INVALID_ONFIDO_VERIFICATION_FLOW_STATUS("10020",
                "Invalid Onfido Verification flow status provided."),
        ERROR_INVALID_WORKFLOW_RUN_STATUS("10021",
                "Invalid Onfido workflow run status provided."),
        ERROR_RETRIEVING_CLAIMS_AGAINST_WORKFLOW_RUN_ID("10022",
                "No claims found for the provided workflow run ID; the ID may be incorrect."),
        ERROR_UPDATING_IDV_CLAIM_VERIFICATION_STATUS("10023",
                "Error occurred while updating IDV claims verification status."),
        ERROR_INVALID_OR_MISSING_RESOURCE_OUTPUT("10024",
                "Webhook payload request is missing the expected output in the resource field."),
        ERROR_INVALID_OR_MISSING_DATA_COMPARISON("10025",
                "Webhook payload output is missing or contains invalid data_comparison field."),
        ERROR_BUILDING_ONFIDO_APPLICANT_URI("10026", "Error occurred while building URI for " +
                "Onfido applicant creation."),
        ERROR_BUILDING_WORKFLOW_RUN_URI("10027", "Error occurred while building URI for  " +
                "Onfido workflow run creation."),
        ERROR_BUILDING_ONFIDO_SDK_TOKEN_URI("10028", "Error occurred while building URI for " +
                "Onfido SDK token request."),
        ERROR_BUILDING_ONFIDO_APPLICANT_UPDATE_URI("10029", "Error occurred while building URI for " +
                "updating Onfido applicant."),
        ERROR_BUILDING_WORKFLOW_RUN_GET_URI("10030", "Error occurred while building URI for " +
                "retrieving Onfido workflow run status."),
        ERROR_APPLICANT_ID_NOT_FOUND("10031", "Applicant ID not found in claim metadata."),
        ERROR_REINITIATING_ONFIDO_VERIFICATION("10032", "An error occurred while reinitiating" +
                " the verification. This could be due to issues with retrieving necessary data " +
                "(such as SDK token, workflow run ID, or applicant ID)."),
        ERROR_REINITIATION_NOT_ALLOWED("10033", "Reinitiation not allowed. Current workflow status" +
                " is not 'AWAITING_INPUT'. Reinitiation is only permitted for claims with 'AWAITING_INPUT' status."),
        ERROR_VERIFICATION_REQUIRED_CLAIMS_NOT_FOUND("10034", "Verification requested claims list " +
                "cannot be empty. Make sure to provide the claims that need to be verified."),
        ERROR_DATA_COMPARISON_BREAKDOWN_CLAIM_NOT_FOUND("10035", "No Onfido data comparison results " +
                "found for claim: %s of user: %s."),
        ERROR_DATA_COMPARISON_BREAKDOWN_CLAIM_VERIFICATION_RESULT_NULL("10036", "The Onfido data " +
                "comparison result returned null for claim: %s of user: %s. This could be due to Comparison Checks " +
                "not being enabled for your account."),
        ERROR_CLAIM_MAPPING_NOT_FOUND("10037", "No Onfido claim mapping found for the claim URI: %s."),
        ERROR_INVALID_TOKEN("10038", "Invalid or expired Onfido token provided."),
        ERROR_INVALID_BASE_URL("10039", "Invalid Onfido base URL provided."),
        ERROR_APPLICANT_ID_NOT_FOUND_IN_ONFIDO("10040",
                "No Onfido user found associated with the provided applicant ID: %s."),
        ERROR_WORKFLOW_RUN_ID_NOT_FOUND_IN_ONFIDO("10041",
                "No Onfido workflow run found for the provided workflow run ID: %s."),
        ERROR_INVALID_WORKFLOW_ID("10042", "Invalid Onfido workflow ID : %s provided.");

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
     * Enum representing the various statuses that a verification flow can transition through.
     */
    public enum VerificationFlowStatus {

        /**
         * Indicates that the verification flow has been initiated.
         */
        INITIATED("INITIATED"),

        /**
         * Indicates that the verification flow has been successfully completed.
         */
        COMPLETED("COMPLETED"),

        /**
         * Indicates that the verification flow has been initiated previously and now requires reinitiation
         * to obtain the SDK token.
         */
        REINITIATED("REINITIATED");

        private final String status;

        VerificationFlowStatus(String status) {
            this.status = status;
        }

        /**
         * Retrieves the string representation of the verification flow status.
         *
         * @return The string status value.
         */
        public String getStatus() {
            return status;
        }

        /**
         * Converts a string value to the corresponding VerificationFlowStatus enum.
         * If the value does not match any known statuses, an exception is thrown.
         *
         * @param status The string representation of the verification flow status.
         * @return The corresponding VerificationFlowStatus enum value.
         * @throws OnfidoClientException If the status is invalid or not recognized.
         */
        public static VerificationFlowStatus fromString(String status)
                throws OnfidoClientException {
            for (VerificationFlowStatus flowStatus : VerificationFlowStatus.values()) {
                if (flowStatus.status.equalsIgnoreCase(status)) {
                    return flowStatus;
                }
            }
            throw new OnfidoClientException(ERROR_INVALID_ONFIDO_VERIFICATION_FLOW_STATUS.getCode(),
                    ERROR_INVALID_ONFIDO_VERIFICATION_FLOW_STATUS.getMessage());
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
