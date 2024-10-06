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

package org.wso2.carbon.identity.verification.onfido.api.v1.core;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.extension.identity.verification.mgt.IdentityVerificationManager;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;
import org.wso2.carbon.extension.identity.verification.provider.IdVProviderManager;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtException;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVConfigProperty;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;
import org.wso2.carbon.identity.verification.onfido.api.common.OnfidoIdvServiceHolder;
import org.wso2.carbon.identity.verification.onfido.api.common.Util;
import org.wso2.carbon.identity.verification.onfido.api.common.error.APIError;
import org.wso2.carbon.identity.verification.onfido.api.common.error.ErrorDTO;
import org.wso2.carbon.identity.verification.onfido.api.v1.interceptors.RawRequestBodyInterceptor;
import org.wso2.carbon.identity.verification.onfido.api.v1.model.VerifyRequest;
import org.wso2.carbon.identity.verification.onfido.api.v1.model.VerifyRequestPayload;
import org.wso2.carbon.identity.verification.onfido.api.v1.model.VerifyRequestPayloadObject;
import org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.CLIENT_ERROR_RESOLVING_IDVP;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.CLIENT_ERROR_SIGNATURE_MISMATCH;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.CLIENT_ERROR_UNSUPPORTED_RESOURCE_TYPE_OR_ACTION;
import static org.wso2.carbon.identity.verification.onfido.api.common.Constants.ErrorMessage.SERVER_ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_INVALID;

public class OnfidoIdvServiceTest {

    @Mock
    private IdVProviderManager idVProviderManager;

    @Mock
    private IdentityVerificationManager identityVerificationManager;

    @Mock
    private IdVProvider idVProvider;

    @InjectMocks
    private OnfidoIdvService onfidoIdvService;

    private static final String WORKFLOW_ID = "workflow_id";
    private static final String BASE_URL = "base_url";
    private static final String WEBHOOK_TOKEN = "webhook_token";
    private static final String TOKEN = "token";

    private static final String TEST_IDVP_ID = "test-idvp-id";
    private static final int TEST_TENANT_ID = 1;
    private static final String TEST_VALID_RESOURCE_TYPE = "workflow_run";
    private static final String TEST_INVALID_RESOURCE_TYPE = "invalid_resource_type";
    private static final String TEST_VALID_RESOURCE_ACTION = "workflow_run.completed";
    private static final String TEST_INVALID_RESOURCE_ACTION = "invalid_resource_action";
    private static final String TEST_VALID_SIGNATURE =
            "9524f4134c6335b4495284859531b92d3d5c089eb3e7c679e5b9c1debee91f1e";
    private static final String TEST_INVALID_SIGNATURE = "test_invalid_signature";
    private static final String TEST_WORKFLOW_RUN_ID = "test-workflow-run-id";
    private static final String TEST_COMPLETED_AT = "2023-05-01T12:00:00Z";
    private static final String TEST_WORKFLOW_ID = "test_workflow_id";
    private static final String TEST_BASE_URL = "test_base_url";
    private static final String TEST_WEBHOOK_TOKEN = "test_webhook_token";
    private static final String TEST_API_TOKEN = "test_api_token";
    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_APPLICANT_ID = "test_applicant_id";
    private static final String TEST_SUCCESS_VERIFICATION_STATUS = "clear";

    private static final String FIELD_DATE_OF_BIRTH = "date_of_birth";
    private static final String FIELD_FIRST_NAME = "first_name";
    private static final String FIELD_DOCUMENT_NUMBERS = "document_numbers";
    private static final String FIELD_DOCUMENT_TYPE = "document_type";
    private static final String FIELD_LAST_NAME = "last_name";
    private static final String FIELD_DATE_OF_EXPIRY = "date_of_expiry";
    private static final String FIELD_GENDER = "gender";
    private static final String FIELD_ISSUING_COUNTRY = "issuing_country";

    private static final String KEY_RESULT = "result";
    private static final String KEY_PROPERTIES = "properties";
    private static final String VALUE_CLEAR = "clear";
    private static final String VALUE_CONSIDER = "consider";
    private static final String KEY_DATA_COMPARISON = "data_comparison";
    private static final String KEY_OUTPUT = "output";

    private static final String CLAIM_URI_LASTNAME = "http://wso2.org/claims/lastname";
    private static final String CLAIM_URI_FIRSTNAME = "http://wso2.org/claims/firstname";
    private static final String CLAIM_URI_DOB = "http://wso2.org/claims/dob";

    private static final String ONFIDO_ATTR_LASTNAME = "last_name";
    private static final String ONFIDO_ATTR_FIRSTNAME = "first_name";
    private static final String ONFIDO_ATTR_DOB = "dob";

    private static final String METADATA_ONFIDO_APPLICANT_ID = "onfido_applicant_id";
    private static final String METADATA_ONFIDO_WORKFLOW_RUN_ID = "onfido_workflow_run_id";
    private static final String METADATA_ONFIDO_WORKFLOW_STATUS = "onfido_workflow_status";
    public static final String METADATA_ONFIDO_VERIFICATION_STATUS = "onfido_verification_status";

    private static final String RAW_REQUEST_BODY = "{\"payload\":{\"resource_type\":\"workflow_run\"," +
            "\"action\":\"workflow_run.completed\",\"object\":{\"id\":\"test-workflow-run-id\"," +
            "\"status\":\"approved\",\"completed_at_iso8601\":\"2024-10-05T09:59:34Z\"},\"resource\":" +
            "{\"output\":{\"data_comparison\":{\"date_of_birth\":{\"result\":\"clear\",\"properties\":{}}," +
            "\"first_name\":{\"result\":\"clear\",\"properties\":{}},\"document_numbers\":{\"result\":\"clear\"," +
            "\"properties\":{}},\"document_type\":{\"result\":\"clear\",\"properties\":{}},\"last_name\":" +
            "{\"result\":\"clear\",\"properties\":{}},\"date_of_expiry\":{\"result\":\"clear\",\"properties\":{}}," +
            "\"gender\":{\"result\":\"clear\",\"properties\":{}},\"issuing_country\":{\"result\":\"clear\"," +
            "\"properties\":{}}}}}}}";

    private static MockedStatic<Util> mockedUtil;
    private static MockedStatic<OnfidoIdvServiceHolder> mockedHolder;
    private static MockedStatic<RawRequestBodyInterceptor> mockedInterceptor;

    @BeforeClass
    public static void setUpClass() {

        mockedUtil = mockStatic(Util.class);
        mockedHolder = mockStatic(OnfidoIdvServiceHolder.class);
        mockedInterceptor = mockStatic(RawRequestBodyInterceptor.class);
    }

    @AfterClass
    public static void tearDownClass() {

        mockedUtil.close();
        mockedHolder.close();
        mockedInterceptor.close();
    }

    @BeforeMethod
    public void setUp() throws Exception {

        MockitoAnnotations.openMocks(this);
        setupMocks();
    }

    private void setupMocks() throws Exception {

        mockedUtil.when(Util::getTenantId).thenReturn(TEST_TENANT_ID);
        mockedHolder.when(OnfidoIdvServiceHolder::getIdVProviderManager).thenReturn(idVProviderManager);
        mockedHolder.when(OnfidoIdvServiceHolder::getIdentityVerificationManager)
                .thenReturn(identityVerificationManager);
        mockedInterceptor.when(RawRequestBodyInterceptor::getRawRequestBody).thenReturn(RAW_REQUEST_BODY);

        when(idVProviderManager.getIdVProvider(eq(TEST_IDVP_ID), eq(TEST_TENANT_ID))).thenReturn(idVProvider);
        when(idVProvider.isEnabled()).thenReturn(true);
        when(idVProvider.getIdVConfigProperties()).thenReturn(createMockConfigProperties());
        when(idVProvider.getClaimMappings()).thenReturn(createMockClaimMappings());
    }

    private Map<String, String> createMockClaimMappings() {

        Map<String, String> claimMappings = new HashMap<>();
        claimMappings.put(CLAIM_URI_LASTNAME, ONFIDO_ATTR_LASTNAME);
        claimMappings.put(CLAIM_URI_FIRSTNAME, ONFIDO_ATTR_FIRSTNAME);
        claimMappings.put(CLAIM_URI_DOB, ONFIDO_ATTR_DOB);
        return claimMappings;
    }

    private IdVConfigProperty[] createMockConfigProperties() {

        IdVConfigProperty[] properties = new IdVConfigProperty[4];

        properties[0] = new IdVConfigProperty();
        properties[0].setName(TOKEN);
        properties[0].setValue(TEST_API_TOKEN);

        properties[1] = new IdVConfigProperty();
        properties[1].setName(BASE_URL);
        properties[1].setValue(TEST_BASE_URL);

        properties[2] = new IdVConfigProperty();
        properties[2].setName(WEBHOOK_TOKEN);
        properties[2].setValue(TEST_WEBHOOK_TOKEN);

        properties[3] = new IdVConfigProperty();
        properties[3].setName(WORKFLOW_ID);
        properties[3].setValue(TEST_WEBHOOK_TOKEN);

        return properties;
    }

    private VerifyRequest createVerifyRequest(OnfidoConstants.WorkflowRunStatus workflowStatus) {

        VerifyRequest verifyRequest = new VerifyRequest();
        VerifyRequestPayload payload = new VerifyRequestPayload();
        payload.setResourceType(TEST_VALID_RESOURCE_TYPE);
        payload.setAction(TEST_VALID_RESOURCE_ACTION);

        VerifyRequestPayloadObject object = new VerifyRequestPayloadObject();
        object.setId(TEST_WORKFLOW_RUN_ID);
        object.setCompletedAtIso8601(TEST_COMPLETED_AT);
        object.setStatus(workflowStatus.getStatus());
        payload.setObject(object);

        Map<String, Object> resource = new HashMap<>();
        Map<String, Object> output = new HashMap<>();
        Map<String, Object> dataComparison = new HashMap<>();

        String[] fields = {FIELD_DATE_OF_BIRTH, FIELD_FIRST_NAME, FIELD_DOCUMENT_NUMBERS, FIELD_DOCUMENT_TYPE,
                FIELD_LAST_NAME, FIELD_DATE_OF_EXPIRY, FIELD_GENDER, FIELD_ISSUING_COUNTRY};

        String resultValue =
                (workflowStatus == OnfidoConstants.WorkflowRunStatus.APPROVED) ? VALUE_CLEAR : VALUE_CONSIDER;

        for (String field : fields) {
            Map<String, Object> fieldData = new HashMap<>();
            fieldData.put(KEY_RESULT, resultValue);
            fieldData.put(KEY_PROPERTIES, new HashMap<>());
            dataComparison.put(field, fieldData);
        }

        output.put(KEY_DATA_COMPARISON, dataComparison);
        resource.put(KEY_OUTPUT, output);

        payload.setResource(resource);
        verifyRequest.setPayload(payload);
        return verifyRequest;
    }

    private IdVClaim[] createMockIdVClaimsBeforeVerificationStatusUpdate() {
        
        String[] claimUris = {CLAIM_URI_FIRSTNAME, CLAIM_URI_LASTNAME, CLAIM_URI_DOB};
        IdVClaim[] claims = new IdVClaim[claimUris.length];

        Map<String, Object> metadata = new HashMap<>();
        metadata.put(METADATA_ONFIDO_APPLICANT_ID, TEST_APPLICANT_ID);
        metadata.put(METADATA_ONFIDO_WORKFLOW_RUN_ID, TEST_WORKFLOW_RUN_ID);
        metadata.put(METADATA_ONFIDO_WORKFLOW_STATUS, OnfidoConstants.WorkflowRunStatus.PROCESSING.getStatus());

        for (int i = 0; i < claimUris.length; i++) {
            claims[i] = new IdVClaim();
            claims[i].setClaimUri(claimUris[i]);
            claims[i].setUserId(TEST_USER_ID);
            claims[i].setIsVerified(false);
            claims[i].setMetadata(new HashMap<>(metadata));
        }

        return claims;
    }

    @DataProvider(name = "invalidResourceTypeAndActionDataProvider")
    public Object[][] invalidResourceTypeAndActionDataProvider() {

        return new Object[][]{
                {TEST_INVALID_RESOURCE_TYPE, TEST_VALID_RESOURCE_ACTION},
                {TEST_VALID_RESOURCE_TYPE, TEST_INVALID_RESOURCE_ACTION},
                {TEST_INVALID_RESOURCE_TYPE, TEST_INVALID_RESOURCE_ACTION}
        };
    }

    @Test(dataProvider = "invalidResourceTypeAndActionDataProvider")
    public void testInvalidResourceTypeAndAction(String resourceType, String action) {

        OnfidoIdvService service = new OnfidoIdvService();

        VerifyRequest verifyRequest = new VerifyRequest();
        VerifyRequestPayload payload = new VerifyRequestPayload();
        payload.setResourceType(resourceType);
        payload.setAction(action);
        verifyRequest.setPayload(payload);

        try {
            service.verify(TEST_VALID_SIGNATURE, TEST_IDVP_ID, verifyRequest);
            fail("Should have thrown an APIError for invalid resource type or action");
        } catch (APIError e) {
            assertEquals(e.getStatus().getStatusCode(), Response.Status.BAD_REQUEST.getStatusCode());
            ErrorDTO errorDTO = e.getResponseEntity();
            assertEquals(errorDTO.getCode(), CLIENT_ERROR_UNSUPPORTED_RESOURCE_TYPE_OR_ACTION.getCode());
            assertEquals(errorDTO.getMessage(), CLIENT_ERROR_UNSUPPORTED_RESOURCE_TYPE_OR_ACTION.getMessage());
            assertEquals(errorDTO.getDescription(), CLIENT_ERROR_UNSUPPORTED_RESOURCE_TYPE_OR_ACTION.getDescription());
        }
    }

    @Test
    public void testInvalidGetIdVProvider() throws IdVProviderMgtException {

        IdVProviderManager mockIdVProviderManager = mock(IdVProviderManager.class);
        mockedHolder.when(OnfidoIdvServiceHolder::getIdVProviderManager).thenReturn(mockIdVProviderManager);
        when(mockIdVProviderManager.getIdVProvider(anyString(), anyInt())).thenReturn(null);

        VerifyRequest verifyRequest = createVerifyRequest(OnfidoConstants.WorkflowRunStatus.APPROVED);

        try {
            onfidoIdvService.verify(TEST_VALID_SIGNATURE, TEST_IDVP_ID, verifyRequest);
            fail("Expected APIError to be thrown");
        } catch (APIError e) {
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), e.getStatus().getStatusCode());
            ErrorDTO errorDTO = e.getResponseEntity();
            assertEquals(CLIENT_ERROR_RESOLVING_IDVP.getCode(), errorDTO.getCode());
            assertEquals(CLIENT_ERROR_RESOLVING_IDVP.getMessage(), errorDTO.getMessage());
            assertEquals(CLIENT_ERROR_RESOLVING_IDVP.getDescription(), errorDTO.getDescription());
        }
    }

    @DataProvider(name = "invalidConfigPropertiesDataProvider")
    public Object[][] invalidConfigPropertiesDataProvider() {

        return new Object[][]{
                {null, TEST_BASE_URL, TEST_WEBHOOK_TOKEN, TEST_WORKFLOW_ID},
                {TEST_API_TOKEN, null, TEST_WEBHOOK_TOKEN, TEST_WORKFLOW_ID},
                {TEST_API_TOKEN, TEST_BASE_URL, null, TEST_WORKFLOW_ID},
                {TEST_API_TOKEN, TEST_BASE_URL, TEST_WEBHOOK_TOKEN, null}
        };
    }

    @Test(dataProvider = "invalidConfigPropertiesDataProvider")
    public void testInvalidIdVProviderConfigProperties(String token, String baseUrl, String webhookToken,
                                                       String workflowId) {

        IdVConfigProperty[] idVProviderConfigProperties = new IdVConfigProperty[4];

        idVProviderConfigProperties[0] = new IdVConfigProperty();
        idVProviderConfigProperties[0].setName(TOKEN);
        idVProviderConfigProperties[0].setValue(token);

        idVProviderConfigProperties[1] = new IdVConfigProperty();
        idVProviderConfigProperties[1].setName(BASE_URL);
        idVProviderConfigProperties[1].setValue(baseUrl);

        idVProviderConfigProperties[2] = new IdVConfigProperty();
        idVProviderConfigProperties[2].setName(WEBHOOK_TOKEN);
        idVProviderConfigProperties[2].setValue(webhookToken);

        idVProviderConfigProperties[3] = new IdVConfigProperty();
        idVProviderConfigProperties[3].setName(WORKFLOW_ID);
        idVProviderConfigProperties[3].setValue(workflowId);

        when(idVProvider.getIdVConfigProperties()).thenReturn(idVProviderConfigProperties);

        VerifyRequest verifyRequest = createVerifyRequest(OnfidoConstants.WorkflowRunStatus.APPROVED);

        try {
            onfidoIdvService.verify(TEST_VALID_SIGNATURE, TEST_IDVP_ID, verifyRequest);
            fail("Expected APIError to be thrown for invalid config properties");
        } catch (APIError e) {
            assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), e.getStatus().getStatusCode());
            ErrorDTO errorDTO = e.getResponseEntity();
            assertEquals(SERVER_ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_INVALID.getCode(), errorDTO.getCode());
            assertEquals(SERVER_ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_INVALID.getMessage(), errorDTO.getMessage());
            assertEquals(SERVER_ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_INVALID.getDescription(),
                    errorDTO.getDescription());
        }
    }

    @DataProvider(name = "invalidSignatureDataProvider")
    public Object[][] invalidSignatureDataProvider() {

        return new Object[][]{
                {TEST_INVALID_SIGNATURE},
                {null}
        };
    }

    @Test(dataProvider = "invalidSignatureDataProvider")
    public void testInvalidSignature(String signature) {

        VerifyRequest verifyRequest = createVerifyRequest(OnfidoConstants.WorkflowRunStatus.APPROVED);
        APIError receivedApiError = null;
        try {
            onfidoIdvService.verify(signature, TEST_IDVP_ID, verifyRequest);
        } catch (APIError e) {
            receivedApiError = e;
        }

        assertNotNull(receivedApiError, "APIError should be thrown");
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(),
                receivedApiError.getStatus().getStatusCode(),
                "Response status should be 401 UNAUTHORIZED");
        ErrorDTO errorDTO = receivedApiError.getResponseEntity();
        assertEquals(CLIENT_ERROR_SIGNATURE_MISMATCH.getCode(), errorDTO.getCode());
        assertEquals(CLIENT_ERROR_SIGNATURE_MISMATCH.getMessage(), errorDTO.getMessage());
        assertEquals(CLIENT_ERROR_SIGNATURE_MISMATCH.getDescription(), errorDTO.getDescription());
    }

    @Test
    public void testVerifySuccessWithWorkflowRunStatusApproved() throws Exception {

        testVerifyWithWorkflowStatus(OnfidoConstants.WorkflowRunStatus.APPROVED, true);
    }

    @DataProvider(name = "unApprovedWorkflowStatusProvider")
    public Object[][] unApprovedWorkflowStatusProvider() {

        return new Object[][]{
                {OnfidoConstants.WorkflowRunStatus.PROCESSING},
                {OnfidoConstants.WorkflowRunStatus.AWAITING_INPUT},
                {OnfidoConstants.WorkflowRunStatus.DECLINED},
                {OnfidoConstants.WorkflowRunStatus.REVIEW},
                {OnfidoConstants.WorkflowRunStatus.ABANDONED},
                {OnfidoConstants.WorkflowRunStatus.ERROR}
        };
    }

    @Test(dataProvider = "unApprovedWorkflowStatusProvider")
    public void testVerifySuccessWithUnApprovedWorkflowStatus(OnfidoConstants.WorkflowRunStatus workflowStatus)
            throws Exception {

        testVerifyWithWorkflowStatus(workflowStatus, false);
    }

    private void testVerifyWithWorkflowStatus(OnfidoConstants.WorkflowRunStatus workflowStatus, boolean isApproved)
            throws Exception {

        VerifyRequest verifyRequest = createVerifyRequest(workflowStatus);

        when(identityVerificationManager.getIdVClaimsByMetadata(
                eq(METADATA_ONFIDO_WORKFLOW_RUN_ID),
                eq(TEST_WORKFLOW_RUN_ID),
                eq(TEST_IDVP_ID),
                eq(TEST_TENANT_ID)
                                                               )).thenReturn(
                createMockIdVClaimsBeforeVerificationStatusUpdate());

        ArgumentCaptor<IdVClaim> idVClaimCaptor = ArgumentCaptor.forClass(IdVClaim.class);
        doReturn(null).when(identityVerificationManager)
                .updateIdVClaim(anyString(), idVClaimCaptor.capture(), anyInt());

        onfidoIdvService.verify(TEST_VALID_SIGNATURE, TEST_IDVP_ID, verifyRequest);

        List<IdVClaim> capturedClaims = idVClaimCaptor.getAllValues();
        assertEquals(capturedClaims.size(), 3, "Expected 3 IdVClaim updates");

        for (IdVClaim claim : capturedClaims) {
            assertNotNull(claim, "Captured IdVClaim should not be null");
            assertEquals(claim.isVerified(), isApproved, "IdVClaim verification status mismatch");

            Map<String, Object> metadata = claim.getMetadata();
            assertNotNull(metadata, "Metadata should not be null");
            assertEquals(workflowStatus.getStatus(), metadata.get(METADATA_ONFIDO_WORKFLOW_STATUS),
                    "Incorrect workflow status being set.");

            if (isApproved) {
                assertEquals(TEST_SUCCESS_VERIFICATION_STATUS, metadata.get(METADATA_ONFIDO_VERIFICATION_STATUS),
                        "Verification status in metadata should match 'clear'");
            } else {
                assertFalse(metadata.containsKey(METADATA_ONFIDO_VERIFICATION_STATUS),
                        "METADATA_ONFIDO_VERIFICATION_STATUS should not be present for unapproved workflow status");
            }
        }
    }
}
