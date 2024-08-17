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
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationServerException;
import org.wso2.carbon.identity.verification.onfido.connector.exception.OnfidoServerException;

import java.io.IOException;
import java.util.Map;

import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.APPLICANTS_ENDPOINT;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.BASE_URL;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_CREATING_ONFIDO_APPLICANT;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_CREATING_RESPONSE;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_CREATING_WORKFLOW_RUN;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_GETTING_ONFIDO_SDK_TOKEN;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_GETTING_ONFIDO_WORKFLOW_STATUS;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_UPDATING_ONFIDO_APPLICANT;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.SDK_TOKEN_ENDPOINT;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.STATUS_VERIFY_ENDPOINT;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.TOKEN;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.WORKFLOW_RUN_ENDPOINT;

/**
 * This class contains the implementation of OnfidoAPIClient.
 */
public class OnfidoAPIClient {

    /**
     * This method is used to create applicant in Onfido with the given verification claim data of a user.
     *
     * @param idVConfigPropertyMap The map of the configuration properties.
     * @return The SDK token.
     * @throws OnfidoServerException Identity verification server exception.
     */
    public static JSONObject createApplicant(Map<String, String> idVConfigPropertyMap,
                                             JSONObject idvClaimsWithValues)
            throws OnfidoServerException {

        String apiToken = idVConfigPropertyMap.get(TOKEN);
        String uri = idVConfigPropertyMap.get(BASE_URL) + APPLICANTS_ENDPOINT;
        HttpResponse response = OnfidoWebUtils.httpPost(apiToken, uri, idvClaimsWithValues.toString());

        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
            return getJsonObject(response);
        } else {
            throw new OnfidoServerException(ERROR_CREATING_ONFIDO_APPLICANT.getCode(),
                    String.format(ERROR_CREATING_ONFIDO_APPLICANT.getMessage(),
                            response.getStatusLine().getStatusCode()));
        }
    }

    /**
     * This method is used to create a workflow run in Onfido.
     *
     * @param idVConfigPropertyMap The map of the configuration properties.
     * @param workflowRunRequestBody The request body for creating the workflow run in JSON format.
     * @return The created workflow run details as a JSONObject.
     * @throws OnfidoServerException If an error occurs while creating the workflow run on the Onfido server.
     */
    public static JSONObject createWorkflowRun(Map<String, String> idVConfigPropertyMap,
                                               JSONObject workflowRunRequestBody)
            throws OnfidoServerException {

        String apiToken = idVConfigPropertyMap.get(TOKEN);
        String uri = idVConfigPropertyMap.get(BASE_URL) + WORKFLOW_RUN_ENDPOINT;

        HttpResponse response = OnfidoWebUtils.httpPost(apiToken, uri, workflowRunRequestBody.toString());
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_CREATED) {
            return getJsonObject(response);
        } else {
            throw new OnfidoServerException(ERROR_CREATING_WORKFLOW_RUN.getCode(),
                    String.format(ERROR_CREATING_WORKFLOW_RUN.getMessage(),
                            response.getStatusLine().getStatusCode()));
        }
    }

    /**
     * This method is used to get the SDK token from Onfido by calling the Onfido SDK token endpoint.
     *
     * @param idVConfigPropertyMap Config property map of the Onfido IDV connector.
     * @param sdkTokenRequestBody  Request body of the SDK token request.
     * @return SDK token response.
     * @throws IdentityVerificationServerException Identity verification server exception.
     */
    public static JSONObject createSDKToken(Map<String, String> idVConfigPropertyMap, JSONObject sdkTokenRequestBody)
            throws OnfidoServerException {

        String apiToken = idVConfigPropertyMap.get(TOKEN);
        String uri = idVConfigPropertyMap.get(BASE_URL) + SDK_TOKEN_ENDPOINT;

        HttpResponse response = OnfidoWebUtils.httpPost(apiToken, uri, sdkTokenRequestBody.toString());
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            return getJsonObject(response);
        } else {
            throw new OnfidoServerException(ERROR_GETTING_ONFIDO_SDK_TOKEN.getCode(),
                    String.format(ERROR_GETTING_ONFIDO_SDK_TOKEN.getMessage(),
                            response.getStatusLine().getStatusCode()));
        }
    }

    /**
     * This method is used to create applicant in Onfido with the given verification claim data of a user.
     *
     * @param idVConfigPropertyMap The map of the configuration properties.
     * @param idvClaimsWithValues  Idv claims with values.
     * @return response for the update applicant request.
     * @throws OnfidoServerException Identity verification server exception.
     */
    public static JSONObject updateApplicant(Map<String, String> idVConfigPropertyMap,
                                             JSONObject idvClaimsWithValues)
            throws OnfidoServerException {

        String apiToken = idVConfigPropertyMap.get(TOKEN);
        String uri = idVConfigPropertyMap.get(BASE_URL) + APPLICANTS_ENDPOINT;
        HttpResponse response = OnfidoWebUtils.httpPut(apiToken, uri, idvClaimsWithValues.toString());

        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            return getJsonObject(response);
        } else {
            throw new OnfidoServerException(ERROR_UPDATING_ONFIDO_APPLICANT.getCode(),
                    String.format(ERROR_UPDATING_ONFIDO_APPLICANT.getMessage(),
                            response.getStatusLine().getStatusCode()));
        }
    }

    /**
     * This method is used to retrieve the status of a workflow run in Onfido.
     *
     * @param idVConfigPropertyMap The map of the configuration properties.
     * @param workflowRunId The ID of the workflow run whose verification status is to be retrieved.
     * @return The verification status of the workflow run as a JSONObject.
     * @throws OnfidoServerException If an error occurs while retrieving the verification status from the Onfido server.
     */
    public static JSONObject getWorkflowRunStatus(Map<String, String> idVConfigPropertyMap , String workflowRunId)
            throws OnfidoServerException {

        String apiToken = idVConfigPropertyMap.get(TOKEN);
        String uri = idVConfigPropertyMap.get(BASE_URL) + STATUS_VERIFY_ENDPOINT + workflowRunId;

        HttpResponse response = OnfidoWebUtils.httpGet(apiToken, uri);
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            return getJsonObject(response);
        } else {
            throw new OnfidoServerException(ERROR_GETTING_ONFIDO_WORKFLOW_STATUS.getCode(),
                    String.format(ERROR_GETTING_ONFIDO_WORKFLOW_STATUS.getMessage(),
                            response.getStatusLine().getStatusCode()));
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
}
