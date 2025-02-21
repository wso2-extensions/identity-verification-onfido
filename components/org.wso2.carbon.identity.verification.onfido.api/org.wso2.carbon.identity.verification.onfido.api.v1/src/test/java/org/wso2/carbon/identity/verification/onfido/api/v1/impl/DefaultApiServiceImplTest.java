/*
 * Copyright (c) 2024-2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.verification.onfido.api.v1.impl;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.internal.OSGiDataHolder;
import org.wso2.carbon.extension.identity.verification.mgt.IdentityVerificationManager;
import org.wso2.carbon.extension.identity.verification.provider.IdVProviderManager;
import org.wso2.carbon.identity.common.testng.WithCarbonHome;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.verification.onfido.api.common.Constants;
import org.wso2.carbon.identity.verification.onfido.api.common.error.APIError;
import org.wso2.carbon.identity.verification.onfido.api.common.error.ErrorDTO;
import org.wso2.carbon.identity.verification.onfido.api.common.error.ErrorResponse;
import org.wso2.carbon.identity.verification.onfido.api.v1.core.OnfidoIdvService;
import org.wso2.carbon.identity.verification.onfido.api.v1.factories.OnfidoIdvServiceFactory;
import org.wso2.carbon.identity.verification.onfido.api.v1.model.VerifyRequest;
import org.wso2.carbon.identity.verification.onfido.api.v1.model.VerifyRequestPayload;
import org.wso2.carbon.identity.verification.onfido.api.v1.model.VerifyRequestPayloadObject;

import java.nio.file.Paths;

import javax.ws.rs.core.Response;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@WithCarbonHome
public class DefaultApiServiceImplTest {

    @Mock
    private OnfidoIdvService onfidoIdvService;
    @Mock
    private IdVProviderManager idVProviderManager;
    @Mock
    private IdentityVerificationManager identityVerificationManager;
    @Mock
    BundleContext bundleContext;
    MockedConstruction<ServiceTracker> mockedConstruction;

    private DefaultApiServiceImpl defaultApiService;
    private VerifyRequest testVerifyRequest;
    private MockedStatic<OnfidoIdvServiceFactory> onfidoIdvServiceFactoryMockedStatic;
    private MockedStatic<IdentityTenantUtil> identityTenantUtil;

    private static final String TEST_X_SHA2_SIGNATURE = "test-signature";
    private static final String TEST_IDVP_ID = "test-idvp-id";
    private static final String TEST_RESOURCE_TYPE = "workflow_run";
    private static final String TEST_ACTION = "workflow_run.completed";
    private static final String TEST_WORKFLOW_RUN_ID = "test_workflow_run_id";
    private static final String TEST_STATUS = "approved";
    private static final String TEST_COMPLETED_AT = "2024-10-05T09:59:34Z";
    private static final String TEST_HREF = "https://api.eu.onfido.com/v3.6/workflow_runs/test_workflow_run_id";

    @BeforeClass
    public void setUpClass() {

        System.setProperty(CarbonBaseConstants.CARBON_HOME, Paths.get(System.getProperty("user.dir"),
                "src", "test", "resources").toString());

    }

    @BeforeMethod
    public void setUp() {

        MockitoAnnotations.openMocks(this);
        testVerifyRequest = createTestVerifyRequest();

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("carbon.super");

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        mockedConstruction = mockConstruction(ServiceTracker.class,
                (mock, context) -> {
                    verify(bundleContext, atLeastOnce()).createFilter(argumentCaptor.capture());
                    if (argumentCaptor.getValue().contains(IdVProviderManager.class.getName())) {
                        when(mock.getServices()).thenReturn(new Object[]{idVProviderManager});
                    }
                    if (argumentCaptor.getValue().contains(IdentityVerificationManager.class.getName())) {
                        when(mock.getServices()).thenReturn(new Object[]{identityVerificationManager});
                    }
                });
        OSGiDataHolder.getInstance().setBundleContext(bundleContext);

        identityTenantUtil = mockStatic(IdentityTenantUtil.class);
        onfidoIdvServiceFactoryMockedStatic = mockStatic(OnfidoIdvServiceFactory.class);
        identityTenantUtil.when(() -> IdentityTenantUtil.getTenantId("carbon.super")).thenReturn(-1234);
        onfidoIdvServiceFactoryMockedStatic.when(OnfidoIdvServiceFactory::getOnfidoIdvService)
                .thenReturn(onfidoIdvService);
        defaultApiService = new DefaultApiServiceImpl();
    }

    @AfterMethod
    public void tearDown() {

        mockedConstruction.close();
        identityTenantUtil.close();
        onfidoIdvServiceFactoryMockedStatic.close();
    }

    @Test
    public void testVerifySuccess() {

        doNothing().when(onfidoIdvService).verify(any(), any(), any());

        Response response = defaultApiService.verify(TEST_X_SHA2_SIGNATURE, TEST_IDVP_ID, testVerifyRequest);

        verify(onfidoIdvService, times(1)).verify(TEST_X_SHA2_SIGNATURE, TEST_IDVP_ID, testVerifyRequest);
        assertEquals(response.getStatus(), Response.Status.OK.getStatusCode(), "Response status should be OK");
    }

    @DataProvider(name = "serverErrorDataProvider")
    public Object[][] serverErrorDataProvider() {

        return new Object[][]{
                {Constants.ErrorMessage.SERVER_ERROR_GENERAL_ERROR},
                {Constants.ErrorMessage.SERVER_ERROR_RESOLVING_IDVP},
                {Constants.ErrorMessage.SERVER_ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_INVALID},
                {Constants.ErrorMessage.SERVER_ERROR_SIGNATURE_VALIDATION_FAILURE},
                {Constants.ErrorMessage.SERVER_ERROR_INVALID_WORKFLOW_RUN_STATUS},
                {Constants.ErrorMessage.SERVER_ERROR_UPDATING_IDV_CLAIM_VERIFICATION_STATUS}
        };
    }

    @Test(dataProvider = "serverErrorDataProvider")
    public void testVerifyServerError(Constants.ErrorMessage errorMessage) {

        ErrorResponse errorResponse = new ErrorResponse.Builder()
                .withCode(errorMessage.getCode())
                .withMessage(errorMessage.getMessage())
                .withDescription(errorMessage.getDescription())
                .build();

        APIError apiError = new APIError(Response.Status.INTERNAL_SERVER_ERROR, errorResponse);

        doThrow(apiError).when(onfidoIdvService).verify(anyString(), anyString(), any(VerifyRequest.class));

        APIError receivedApiError = null;
        try {
            defaultApiService.verify(TEST_X_SHA2_SIGNATURE, TEST_IDVP_ID, testVerifyRequest);
        } catch (APIError e) {
            receivedApiError = e;
        }

        assertNotNull(receivedApiError, "APIError should be thrown");
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                receivedApiError.getStatus().getStatusCode(),
                "Response status should be 500 INTERNAL_SERVER_ERROR");
        ErrorDTO errorDTO = receivedApiError.getResponseEntity();
        assertEquals(errorMessage.getCode(), errorDTO.getCode());
        assertEquals(errorMessage.getMessage(), errorDTO.getMessage());
        assertEquals(errorMessage.getDescription(), errorDTO.getDescription());
    }

    @DataProvider(name = "clientErrorDataProvider")
    public Object[][] clientErrorDataProvider() {

        return new Object[][]{
                {Constants.ErrorMessage.CLIENT_ERROR_SIGNATURE_MISMATCH, Response.Status.UNAUTHORIZED},
                {Constants.ErrorMessage.CLIENT_ERROR_RESOLVING_IDVP, Response.Status.NOT_FOUND},
                {Constants.ErrorMessage.CLIENT_ERROR_UNSUPPORTED_RESOURCE_TYPE_OR_ACTION, Response.Status.BAD_REQUEST},
                {Constants.ErrorMessage.CLIENT_ERROR_INVALID_WORKFLOW_OUTPUT, Response.Status.BAD_REQUEST},
                {Constants.ErrorMessage.CLIENT_ERROR_DATA_COMPARISON_RESULT_NOT_FOUND, Response.Status.BAD_REQUEST},
                {Constants.ErrorMessage.CLIENT_ERROR_DATA_COMPARISON_RESULT_NULL, Response.Status.BAD_REQUEST},
                {Constants.ErrorMessage.CLIENT_ERROR_INVALID_REQUEST, Response.Status.BAD_REQUEST}
        };
    }

    @Test(dataProvider = "clientErrorDataProvider")
    public void testVerifyClientError(Constants.ErrorMessage errorMessage, Response.Status expectedStatus) {

        ErrorResponse errorResponse = new ErrorResponse.Builder()
                .withCode(errorMessage.getCode())
                .withMessage(errorMessage.getMessage())
                .withDescription(errorMessage.getDescription())
                .build();

        APIError apiError = new APIError(expectedStatus, errorResponse);

        doThrow(apiError).when(onfidoIdvService).verify(anyString(), anyString(), any(VerifyRequest.class));

        APIError receivedApiError = null;
        try {
            defaultApiService.verify(TEST_X_SHA2_SIGNATURE, TEST_IDVP_ID, testVerifyRequest);
        } catch (APIError e) {
            receivedApiError = e;
        }

        assertNotNull(receivedApiError, "APIError should be thrown");
        assertEquals(expectedStatus.getStatusCode(), receivedApiError.getStatus().getStatusCode(),
                "Response status should match the expected status");
        ErrorDTO errorDTO = receivedApiError.getResponseEntity();
        assertEquals(errorMessage.getCode(), errorDTO.getCode());
        assertEquals(errorMessage.getMessage(), errorDTO.getMessage());
        assertEquals(errorMessage.getDescription(), errorDTO.getDescription());
    }

    private VerifyRequest createTestVerifyRequest() {

        VerifyRequestPayload verifyRequestPayload = new VerifyRequestPayload();
        verifyRequestPayload.setResourceType(TEST_RESOURCE_TYPE);
        verifyRequestPayload.setAction(TEST_ACTION);

        VerifyRequestPayloadObject object = new VerifyRequestPayloadObject();
        object.setId(TEST_WORKFLOW_RUN_ID);
        object.setStatus(TEST_STATUS);
        object.setCompletedAtIso8601(TEST_COMPLETED_AT);
        object.setHref(TEST_HREF);
        verifyRequestPayload.setObject(object);

        VerifyRequest verifyRequest = new VerifyRequest();
        verifyRequest.setPayload(verifyRequestPayload);

        return verifyRequest;
    }
}
