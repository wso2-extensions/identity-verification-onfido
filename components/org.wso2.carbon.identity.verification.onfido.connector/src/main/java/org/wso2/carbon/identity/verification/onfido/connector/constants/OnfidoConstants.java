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

/**
 * This class contains the constants used in the Onfido connector.
 */
public class OnfidoConstants {

    private static final String IDV_ERROR_PREFIX = "OIDV-";
    public static final String ONFIDO = "ONFIDO";
    public static final String STATUS = "status";
    public static final String APPLICANT_ID = "applicant_id";
    public static final String SDK_TOKEN = "sdk_token";
    public static final String CHECK_ID = "check_id";
    public static final String TOKEN = "token";
    public static final String INITIATED = "INITIATED";
    public static final String COMPLETED = "COMPLETED";
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String BASE_URL = "base_url";
    public static final String ID = "id";
    public static final String TOKEN_HEADER = "Token token=";
    public static final String APPLICATION_JSON = "application/json";
    public static final String REPORT_NAMES = "report_names";
    public static final String APPLICANTS_ENDPOINT = "/applicants";
    public static final String SDK_TOKEN_ENDPOINT = "/sdk_token";
    public static final String CHECKS_ENDPOINT = "/checks";

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
        ERROR_GETTING_ONFIDO_SDK_TOKEN("10007",
                "Getting the Onfido SDK token was failed with the response %s."),
        ERROR_RETRIEVING_IDV_PROVIDER("10008",
                "IdVProvider is not available or not enabled"),
        ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_EMPTY("10009",
                "IdVProvider configuration properties are empty or missing"),
        ERROR_CREATING_HTTP_CLIENT("10010", "Server error encountered while creating http client"),
        ERROR_UPDATING_ONFIDO_APPLICANT("10011",
                "The applicant updating in the Onfido failed with the response %s."),
        ERROR_GETTING_HTTP_CLIENT("10012", "Error preparing http client to publish events.");

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
}
