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

package org.wso2.carbon.identity.verification.onfido.connector.web;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.wso2.carbon.identity.verification.onfido.connector.exception.OnfidoClientException;
import org.wso2.carbon.identity.verification.onfido.connector.exception.OnfidoServerException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.APPLICANTS_ENDPOINT;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.BASE_URL;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_APPLICANT_ID_NOT_FOUND_IN_ONFIDO;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_BUILDING_ONFIDO_APPLICANT_UPDATE_URI;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_BUILDING_ONFIDO_APPLICANT_URI;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_BUILDING_ONFIDO_SDK_TOKEN_URI;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_BUILDING_WORKFLOW_RUN_GET_URI;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_BUILDING_WORKFLOW_RUN_URI;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_CREATING_ONFIDO_APPLICANT;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_CREATING_RESPONSE;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_CREATING_WORKFLOW_RUN;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_GETTING_ONFIDO_SDK_TOKEN;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_GETTING_ONFIDO_WORKFLOW_STATUS;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_INVALID_TOKEN;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_INVALID_WORKFLOW_ID;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_UPDATING_ONFIDO_APPLICANT;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_WORKFLOW_RUN_ID_NOT_FOUND_IN_ONFIDO;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.SDK_TOKEN_ENDPOINT;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.TOKEN;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.WORKFLOW_ID;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.WORKFLOW_RUN_ENDPOINT;

/**
 * This class contains the implementation of OnfidoAPIClient.
 */
public class OnfidoAPIClient {

    /**
     * Creates an applicant in Onfido with the given verification claim data of a user.
     *
     * @param idVConfigPropertyMap The map containing the configuration properties of the IdV Provider.
     * @param idvClaimsWithValues  A JSONObject containing the user's claim data to be sent to Onfido.
     * @return A JSONObject containing the created applicant's details as returned by Onfido.
     * @throws OnfidoServerException If an error occurs during the applicant creation process, including
     *                               URI building errors or unexpected responses from the Onfido API.
     */
    public static JSONObject createApplicant(Map<String, String> idVConfigPropertyMap,
                                             JSONObject idvClaimsWithValues)
            throws OnfidoServerException, OnfidoClientException {

        String apiToken = idVConfigPropertyMap.get(TOKEN);
        String baseUrl = idVConfigPropertyMap.get(BASE_URL);

        try {
            URI uri = buildUri(baseUrl, APPLICANTS_ENDPOINT);
            HttpResponse response = OnfidoWebUtils.httpPost(apiToken, uri.toString(), idvClaimsWithValues.toString());

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_CREATED) {
                return getJsonObject(response);
            } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                throw new OnfidoClientException(ERROR_INVALID_TOKEN.getCode(), ERROR_INVALID_TOKEN.getMessage());
            } else {
                throw new OnfidoServerException(ERROR_CREATING_ONFIDO_APPLICANT.getCode(),
                        String.format(ERROR_CREATING_ONFIDO_APPLICANT.getMessage(), statusCode));
            }
        } catch (URISyntaxException e) {
            throw new OnfidoServerException(ERROR_BUILDING_ONFIDO_APPLICANT_URI.getCode(),
                    ERROR_BUILDING_ONFIDO_APPLICANT_URI.getMessage(), e);
        }
    }

    /**
     * Creates a workflow run in Onfido for a specific applicant.
     *
     * @param idVConfigPropertyMap   The map containing the configuration properties of the IdV Provider.
     * @param workflowRunRequestBody A JSONObject containing the necessary data to create a workflow run,
     *                               typically including the workflow ID and applicant ID.
     * @return A JSONObject containing the details of the created workflow run as returned by Onfido.
     * @throws OnfidoServerException If an error occurs during the workflow run creation process, including
     *                               URI building errors, network issues, or unexpected responses from the Onfido API.
     */
    public static JSONObject createWorkflowRun(Map<String, String> idVConfigPropertyMap,
                                               JSONObject workflowRunRequestBody)
            throws OnfidoServerException, OnfidoClientException {

        String apiToken = idVConfigPropertyMap.get(TOKEN);
        String baseUrl = idVConfigPropertyMap.get(BASE_URL);

        try {
            URI uri = buildUri(baseUrl, WORKFLOW_RUN_ENDPOINT);
            HttpResponse response =
                    OnfidoWebUtils.httpPost(apiToken, uri.toString(), workflowRunRequestBody.toString());

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_CREATED) {
                return getJsonObject(response);
            } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                throw new OnfidoClientException(ERROR_INVALID_TOKEN.getCode(), ERROR_INVALID_TOKEN.getMessage());
            } else if (statusCode == HttpStatus.SC_UNPROCESSABLE_ENTITY) {
                throw new OnfidoClientException(ERROR_INVALID_WORKFLOW_ID.getCode(),
                        String.format(ERROR_INVALID_WORKFLOW_ID.getMessage(), workflowRunRequestBody.get(WORKFLOW_ID)));
            } else {
                throw new OnfidoServerException(ERROR_CREATING_WORKFLOW_RUN.getCode(),
                        String.format(ERROR_CREATING_WORKFLOW_RUN.getMessage(), statusCode));
            }
        } catch (URISyntaxException e) {
            throw new OnfidoServerException(ERROR_BUILDING_WORKFLOW_RUN_URI.getCode(),
                    ERROR_BUILDING_WORKFLOW_RUN_URI.getMessage(), e);
        }
    }

    /**
     * Retrieves a SDK token from Onfido for an already created applicant.
     *
     * @param idVConfigPropertyMap The map containing the configuration properties of the IdV Provider.
     * @param sdkTokenRequestBody  A JSONObject containing the necessary data to request an SDK token.
     * @return A JSONObject containing the SDK token as returned by Onfido.
     * @throws OnfidoServerException If an error occurs during the SDK token request process, including
     *                               URI building errors or unexpected responses from the Onfido API.
     */
    public static JSONObject createSDKToken(Map<String, String> idVConfigPropertyMap, JSONObject sdkTokenRequestBody)
            throws OnfidoServerException, OnfidoClientException {

        String apiToken = idVConfigPropertyMap.get(TOKEN);
        String baseUrl = idVConfigPropertyMap.get(BASE_URL);

        try {
            URI uri = buildUri(baseUrl, SDK_TOKEN_ENDPOINT);
            HttpResponse response = OnfidoWebUtils.httpPost(apiToken, uri.toString(), sdkTokenRequestBody.toString());

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                return getJsonObject(response);
            } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                throw new OnfidoClientException(ERROR_INVALID_TOKEN.getCode(), ERROR_INVALID_TOKEN.getMessage());
            } else {
                throw new OnfidoServerException(ERROR_GETTING_ONFIDO_SDK_TOKEN.getCode(),
                        String.format(ERROR_GETTING_ONFIDO_SDK_TOKEN.getMessage(), statusCode));
            }
        } catch (URISyntaxException e) {
            throw new OnfidoServerException(ERROR_BUILDING_ONFIDO_SDK_TOKEN_URI.getCode(),
                    ERROR_BUILDING_ONFIDO_SDK_TOKEN_URI.getMessage(), e);
        }
    }

    /**
     * Updates an applicant in Onfido with the provided verification claim data of a user.
     *
     * @param idVConfigPropertyMap The map containing the configuration properties of the IdV Provider.
     * @param idvClaimsWithValues  A JSONObject containing the user's claim data to be updated in Onfido.
     * @param applicantId          The unique identifier of the applicant to be updated in Onfido.
     * @return A JSONObject containing the response from Onfido after the update request.
     * @throws OnfidoServerException If an error occurs during the applicant update process, including
     *                               URI building errors or unexpected responses from the Onfido API.
     */
    public static JSONObject updateApplicant(Map<String, String> idVConfigPropertyMap,
                                             JSONObject idvClaimsWithValues, String applicantId)
            throws OnfidoServerException, OnfidoClientException {

        String apiToken = idVConfigPropertyMap.get(TOKEN);
        String baseUrl = idVConfigPropertyMap.get(BASE_URL);

        try {
            URI uri = buildUri(baseUrl, APPLICANTS_ENDPOINT + "/" + applicantId);
            HttpResponse response = OnfidoWebUtils.httpPut(apiToken, uri.toString(), idvClaimsWithValues.toString());

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                return getJsonObject(response);
            } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                throw new OnfidoClientException(ERROR_INVALID_TOKEN.getCode(), ERROR_INVALID_TOKEN.getMessage());
            } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
                throw new OnfidoServerException(ERROR_APPLICANT_ID_NOT_FOUND_IN_ONFIDO.getCode(),
                        String.format(ERROR_APPLICANT_ID_NOT_FOUND_IN_ONFIDO.getMessage(), applicantId));
            } else {
                throw new OnfidoServerException(ERROR_UPDATING_ONFIDO_APPLICANT.getCode(),
                        String.format(ERROR_UPDATING_ONFIDO_APPLICANT.getMessage(), statusCode));
            }
        } catch (URISyntaxException e) {
            throw new OnfidoServerException(ERROR_BUILDING_ONFIDO_APPLICANT_UPDATE_URI.getCode(),
                    ERROR_BUILDING_ONFIDO_APPLICANT_UPDATE_URI.getMessage(), e);
        }
    }

    /**
     * Retrieves the status of a workflow run in Onfido.
     *
     * @param idVConfigPropertyMap The map containing the configuration properties of the IdV Provider.
     * @param workflowRunId        The ID of the workflow run whose status is to be retrieved.
     * @return A JSONObject containing the status of the workflow run as returned by Onfido.
     * @throws OnfidoServerException If an error occurs while retrieving the workflow run status, including
     *                               URI building errors, encoding issues, or unexpected responses from the Onfido API.
     */
    public static JSONObject getWorkflowRunStatus(Map<String, String> idVConfigPropertyMap, String workflowRunId)
            throws OnfidoServerException, OnfidoClientException {

        String apiToken = idVConfigPropertyMap.get(TOKEN);
        String baseUrl = idVConfigPropertyMap.get(BASE_URL);

        try {
            URI uri = buildUri(baseUrl, WORKFLOW_RUN_ENDPOINT + "/" + workflowRunId);
            HttpResponse response = OnfidoWebUtils.httpGet(apiToken, uri.toString());

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                return getJsonObject(response);
            } else if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
                throw new OnfidoClientException(ERROR_INVALID_TOKEN.getCode(), ERROR_INVALID_TOKEN.getMessage());
            } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
                throw new OnfidoServerException(ERROR_WORKFLOW_RUN_ID_NOT_FOUND_IN_ONFIDO.getCode(),
                        String.format(ERROR_WORKFLOW_RUN_ID_NOT_FOUND_IN_ONFIDO.getMessage(), workflowRunId));
            } else {
                throw new OnfidoServerException(ERROR_GETTING_ONFIDO_WORKFLOW_STATUS.getCode(),
                        String.format(ERROR_GETTING_ONFIDO_WORKFLOW_STATUS.getMessage(), statusCode));
            }
        } catch (URISyntaxException e) {
            throw new OnfidoServerException(ERROR_BUILDING_WORKFLOW_RUN_GET_URI.getCode(),
                    ERROR_BUILDING_WORKFLOW_RUN_GET_URI.getMessage(), e);
        }
    }

    private static JSONObject getJsonObject(HttpResponse response) throws OnfidoServerException {

        try {
            HttpEntity entity = response.getEntity();
            String jsonResponse = EntityUtils.toString(entity);
            return new JSONObject(jsonResponse);
        } catch (IOException e) {
            throw new OnfidoServerException(ERROR_CREATING_RESPONSE.getCode(),
                    ERROR_CREATING_RESPONSE.getMessage());
        }
    }

    /**
     * Constructs a URI by combining the base URL with the given endpoint.
     *
     * This method addresses the issue where simply using setPath() would overwrite
     * the existing path (including the version number, e.g., /v3.6) in the base URL.
     * Instead, it appends the endpoint to the existing path, preserving the version
     * information.
     *
     * For example:
     * - If baseUrl is "https://api.eu.onfido.com/v3.6" and endpoint is "/applicants",
     *   the resulting URI will be "https://api.eu.onfido.com/v3.6/applicants".
     * - Without this approach, setPath("/applicants") would have resulted in
     *   "https://api.eu.onfido.com/applicants", omitting the version number.
     *
     * @param baseUrl  The base URL, including the scheme, host, and version path.
     * @param endpoint The endpoint to be appended to the base URL's path.
     * @return A URI combining the base URL and the endpoint.
     * @throws URISyntaxException If the resulting URI is not valid.
     */
    private static URI buildUri(String baseUrl, String endpoint) throws URISyntaxException {

        URIBuilder builder = new URIBuilder(baseUrl);
        return builder.setPath(builder.getPath() + endpoint).build();
    }
}
