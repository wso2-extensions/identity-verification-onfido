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

package org.wso2.carbon.identity.verification.onfido.connector.web;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.json.JSONObject;
import org.mockito.MockedStatic;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.identity.verification.onfido.connector.exception.OnfidoServerException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertThrows;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage;

public class OnfidoAPIClientTest {

    private static final String TEST_TOKEN = "test-token";
    public static final String BASE_URL = "base_url";
    public static final String TOKEN = "token";
    private static final String TEST_BASE_URL = "https://api.onfido.com/v3.6";
    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";
    private static final String ID = "id";
    private static final String TEST_FIRST_NAME = "John";
    private static final String TEST_LAST_NAME = "Doe";
    private static final String WORKFLOW_ID = "workflow_id";
    private static final String TEST_WORKFLOW_ID = "test_workflow_id";
    private static final String TEST_WORKFLOW_RUN_ID = "test_workflow_run_id";
    private static final String APPLICANT_ID = "applicant_id";
    private static final String TEST_APPLICANT_ID = "test_applicant_id";
    private static final String TEST_SDK_TOKEN = "test_sdk_token";
    private static final String STATUS = "status";
    private static final String TEST_STATUS_APPROVED = "approved";

    private Map<String, String> idVConfigPropertyMap;

    private static final int[] ERROR_STATUS_CODES =
            {HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_UNAUTHORIZED, HttpStatus.SC_FORBIDDEN, HttpStatus.SC_NOT_FOUND,
                    HttpStatus.SC_INTERNAL_SERVER_ERROR};

    @BeforeMethod
    public void setUp() {

        idVConfigPropertyMap = new HashMap<>();
        idVConfigPropertyMap.put(TOKEN, TEST_TOKEN);
        idVConfigPropertyMap.put(BASE_URL, TEST_BASE_URL);
    }

    private JSONObject createTestIdvClaimsWithValues() {

        JSONObject idvClaimsWithValues = new JSONObject();
        idvClaimsWithValues.put(FIRST_NAME, TEST_FIRST_NAME);
        idvClaimsWithValues.put(LAST_NAME, TEST_LAST_NAME);
        return idvClaimsWithValues;
    }

    private JSONObject createTestWorkflowRunRequestBody() {

        JSONObject workflowRunRequestBody = new JSONObject();
        workflowRunRequestBody.put(WORKFLOW_ID, TEST_WORKFLOW_ID);
        workflowRunRequestBody.put(APPLICANT_ID, TEST_APPLICANT_ID);
        return workflowRunRequestBody;
    }

    private JSONObject createTestSdkTokenRequestBody() {

        JSONObject sdkTokenRequestBody = new JSONObject();
        sdkTokenRequestBody.put(APPLICANT_ID, TEST_APPLICANT_ID);
        return sdkTokenRequestBody;
    }

    @Test
    public void testCreateApplicant() throws Exception {

        JSONObject idvClaimsWithValues = createTestIdvClaimsWithValues();

        JSONObject responseJson = new JSONObject();
        responseJson.put(ID, APPLICANT_ID);
        responseJson.put(FIRST_NAME, TEST_FIRST_NAME);
        responseJson.put(LAST_NAME, TEST_LAST_NAME);

        try (MockedStatic<OnfidoWebUtils> mockedOnfidoWebUtils = mockStatic(OnfidoWebUtils.class)) {
            HttpResponse response = createMockResponse(responseJson, HttpStatus.SC_CREATED);
            mockedOnfidoWebUtils.when(() -> OnfidoWebUtils.httpPost(any(), any(), any())).thenReturn(response);

            JSONObject result = OnfidoAPIClient.createApplicant(idVConfigPropertyMap, idvClaimsWithValues);
            validateApplicantResponse(result, APPLICANT_ID, TEST_FIRST_NAME, TEST_LAST_NAME);
        }
    }

    @Test
    public void testCreateWorkflowRun() throws Exception {

        JSONObject workflowRunRequestBody = createTestWorkflowRunRequestBody();

        JSONObject responseJson = new JSONObject();
        responseJson.put(ID, TEST_WORKFLOW_RUN_ID);
        responseJson.put(WORKFLOW_ID, TEST_WORKFLOW_ID);
        responseJson.put(APPLICANT_ID, TEST_APPLICANT_ID);

        try (MockedStatic<OnfidoWebUtils> mockedOnfidoWebUtils = mockStatic(OnfidoWebUtils.class)) {
            HttpResponse response = createMockResponse(responseJson, HttpStatus.SC_CREATED);
            mockedOnfidoWebUtils.when(() -> OnfidoWebUtils.httpPost(any(), any(), any())).thenReturn(response);

            JSONObject result = OnfidoAPIClient.createWorkflowRun(idVConfigPropertyMap, workflowRunRequestBody);

            assertNotNull(result, "Result JSON object should not be null");
            assertEquals(result.getString(ID), TEST_WORKFLOW_RUN_ID, "ID mismatch");
            assertEquals(result.getString(WORKFLOW_ID), TEST_WORKFLOW_ID, "Workflow ID mismatch");
            assertEquals(result.getString(APPLICANT_ID), TEST_APPLICANT_ID, "Applicant ID mismatch");
        }
    }

    @Test
    public void testCreateSDKToken() throws Exception {

        JSONObject sdkTokenRequestBody = createTestSdkTokenRequestBody();

        JSONObject responseJson = new JSONObject();
        responseJson.put(TOKEN, TEST_SDK_TOKEN);

        try (MockedStatic<OnfidoWebUtils> mockedOnfidoWebUtils = mockStatic(OnfidoWebUtils.class)) {
            HttpResponse response = createMockResponse(responseJson, HttpStatus.SC_OK);
            mockedOnfidoWebUtils.when(() -> OnfidoWebUtils.httpPost(any(), any(), any())).thenReturn(response);

            JSONObject result = OnfidoAPIClient.createSDKToken(idVConfigPropertyMap, sdkTokenRequestBody);

            assertNotNull(result, "Result JSON object should not be null");
            assertEquals(result.getString(TOKEN), TEST_SDK_TOKEN, "SDK token mismatch");
        }
    }

    @Test
    public void testUpdateApplicant() throws Exception {

        JSONObject idvClaimsWithValues = createTestIdvClaimsWithValues();

        JSONObject responseJson = new JSONObject();
        responseJson.put(ID, TEST_APPLICANT_ID);
        responseJson.put(FIRST_NAME, TEST_FIRST_NAME);
        responseJson.put(LAST_NAME, TEST_LAST_NAME);

        try (MockedStatic<OnfidoWebUtils> mockedOnfidoWebUtils = mockStatic(OnfidoWebUtils.class)) {
            HttpResponse response = createMockResponse(responseJson, HttpStatus.SC_OK);
            mockedOnfidoWebUtils.when(() -> OnfidoWebUtils.httpPut(any(), any(), any())).thenReturn(response);

            JSONObject result =
                    OnfidoAPIClient.updateApplicant(idVConfigPropertyMap, idvClaimsWithValues, TEST_APPLICANT_ID);

            validateApplicantResponse(result, TEST_APPLICANT_ID, TEST_FIRST_NAME, TEST_LAST_NAME);
        }
    }

    @Test
    public void testGetWorkflowRunStatus() throws Exception {

        JSONObject responseJson = new JSONObject();
        responseJson.put(ID, TEST_WORKFLOW_RUN_ID);
        responseJson.put(APPLICANT_ID, TEST_APPLICANT_ID);
        responseJson.put(WORKFLOW_ID, TEST_WORKFLOW_ID);
        responseJson.put(STATUS, TEST_STATUS_APPROVED);

        try (MockedStatic<OnfidoWebUtils> mockedOnfidoWebUtils = mockStatic(OnfidoWebUtils.class)) {
            HttpResponse response = createMockResponse(responseJson, HttpStatus.SC_OK);
            mockedOnfidoWebUtils.when(() -> OnfidoWebUtils.httpGet(any(), any())).thenReturn(response);

            JSONObject result = OnfidoAPIClient.getWorkflowRunStatus(idVConfigPropertyMap, TEST_WORKFLOW_RUN_ID);

            assertNotNull(result, "Result JSON object should not be null");
            assertEquals(result.getString(ID), TEST_WORKFLOW_RUN_ID, "Workflow run ID mismatch");
            assertEquals(result.getString(APPLICANT_ID), TEST_APPLICANT_ID, "Applicant ID mismatch");
            assertEquals(result.getString(WORKFLOW_ID), TEST_WORKFLOW_ID, "Workflow ID mismatch");
            assertEquals(result.getString(STATUS), TEST_STATUS_APPROVED, "Status mismatch");
        }
    }

    @Test
    public void testCreateApplicantWithErrorResponse() throws Exception {

        JSONObject idvClaimsWithValues = createTestIdvClaimsWithValues();

        testErrorResponse(() -> OnfidoAPIClient.createApplicant(idVConfigPropertyMap, idvClaimsWithValues),
                ErrorMessage.ERROR_CREATING_ONFIDO_APPLICANT);
    }

    @Test
    public void testCreateWorkflowRunWithErrorResponse() throws Exception {

        JSONObject workflowRunRequestBody = createTestWorkflowRunRequestBody();

        testErrorResponse(() -> OnfidoAPIClient.createWorkflowRun(idVConfigPropertyMap, workflowRunRequestBody),
                ErrorMessage.ERROR_CREATING_WORKFLOW_RUN);
    }

    @Test
    public void testCreateSDKTokenWithErrorResponse() throws Exception {

        JSONObject sdkTokenRequestBody = createTestSdkTokenRequestBody();

        testErrorResponse(() -> OnfidoAPIClient.createSDKToken(idVConfigPropertyMap, sdkTokenRequestBody),
                ErrorMessage.ERROR_GETTING_ONFIDO_SDK_TOKEN);
    }

    @Test
    public void testUpdateApplicantWithErrorResponse() throws Exception {

        JSONObject idvClaimsWithValues = createTestIdvClaimsWithValues();

        testErrorResponse(
                () -> OnfidoAPIClient.updateApplicant(idVConfigPropertyMap, idvClaimsWithValues, TEST_APPLICANT_ID),
                ErrorMessage.ERROR_UPDATING_ONFIDO_APPLICANT);
    }

    @Test
    public void testGetWorkflowRunStatusWithErrorResponse() throws Exception {

        testErrorResponse(() -> OnfidoAPIClient.getWorkflowRunStatus(idVConfigPropertyMap, TEST_WORKFLOW_RUN_ID),
                ErrorMessage.ERROR_GETTING_ONFIDO_WORKFLOW_STATUS);
    }

    private void testErrorResponse(Callable<JSONObject> apiCall, ErrorMessage expectedError) throws Exception {

        for (int statusCode : ERROR_STATUS_CODES) {
            try (MockedStatic<OnfidoWebUtils> mockedOnfidoWebUtils = mockStatic(OnfidoWebUtils.class)) {
                HttpResponse response = createMockResponse(new JSONObject(), statusCode);
                mockedOnfidoWebUtils.when(() -> OnfidoWebUtils.httpPost(any(), any(), any())).thenReturn(response);
                mockedOnfidoWebUtils.when(() -> OnfidoWebUtils.httpPut(any(), any(), any())).thenReturn(response);
                mockedOnfidoWebUtils.when(() -> OnfidoWebUtils.httpGet(any(), any())).thenReturn(response);

                assertThrows(OnfidoServerException.class, apiCall::call);
                try {
                    apiCall.call();
                } catch (OnfidoServerException e) {
                    assertEquals(e.getErrorCode(), expectedError.getCode());
                    assertEquals(e.getMessage(), String.format(expectedError.getMessage(), statusCode));
                }
            }
        }
    }

    private HttpResponse createMockResponse(JSONObject responseJson, int statusCode) throws Exception {

        HttpResponse response = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, statusCode, null));
        response.setEntity(new StringEntity(responseJson.toString()));
        return response;
    }

    private void validateApplicantResponse(JSONObject result, String id, String firstName, String lastName) {

        assertNotNull(result, "Result JSON object should not be null");
        assertEquals(result.getString(ID), id, "ID mismatch");
        assertEquals(result.getString(FIRST_NAME), firstName, "First name mismatch");
        assertEquals(result.getString(LAST_NAME), lastName, "Last name mismatch");
    }
}
