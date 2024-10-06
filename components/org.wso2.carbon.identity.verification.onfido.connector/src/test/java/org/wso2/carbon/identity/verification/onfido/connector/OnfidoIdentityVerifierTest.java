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

package org.wso2.carbon.identity.verification.onfido.connector;

import org.json.JSONObject;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.extension.identity.verification.mgt.IdentityVerificationManager;
import org.wso2.carbon.extension.identity.verification.mgt.exception.IdentityVerificationClientException;
import org.wso2.carbon.extension.identity.verification.mgt.internal.IdentityVerificationDataHolder;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVClaim;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdVProperty;
import org.wso2.carbon.extension.identity.verification.mgt.model.IdentityVerifierData;
import org.wso2.carbon.extension.identity.verification.provider.IdVProviderManager;
import org.wso2.carbon.extension.identity.verification.provider.exception.IdVProviderMgtException;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVConfigProperty;
import org.wso2.carbon.extension.identity.verification.provider.model.IdVProvider;
import org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants;
import org.wso2.carbon.identity.verification.onfido.connector.internal.OnfidoIDVDataHolder;
import org.wso2.carbon.identity.verification.onfido.connector.web.OnfidoAPIClient;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.jdbc.UniqueIDJDBCUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_EMPTY;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_IDV_PROVIDER_INVALID_OR_DISABLED;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_INVALID_ONFIDO_VERIFICATION_FLOW_STATUS;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_VERIFICATION_FLOW_STATUS_NOT_FOUND;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_VERIFICATION_REQUIRED_CLAIMS_NOT_FOUND;

public class OnfidoIdentityVerifierTest {

    @Mock
    private IdVProvider mockIdVProvider;
    @Mock
    private IdentityVerificationDataHolder mockDataHolderInstance;
    @Mock
    private IdVProviderManager mockIdVProviderManager;
    @Mock
    private IdentityVerificationManager mockIdentityVerificationManager;
    @Mock
    private UniqueIDJDBCUserStoreManager mockUniqueIDUserStoreManager;
    @Mock
    private RealmService mockRealmService;
    @Mock
    private UserRealm mockUserRealm;

    @Spy
    @InjectMocks
    private OnfidoIdentityVerifier onfidoIdentityVerifier;

    private static MockedStatic<IdentityVerificationDataHolder> mockedIdVDataHolder;
    private static MockedStatic<OnfidoAPIClient> mockedOnfidoAPIClient;
    private static MockedStatic<OnfidoIDVDataHolder> mockedOnfidoIDVDataHolder;

    private static final String TEST_USER_ID = "test_user_id";
    private static final int TEST_TENANT_ID = 1;
    private static final String TEST_IDV_PROVIDER_ID = "test_idv_provider_id";
    private static final String TEST_WORKFLOW_ID = "test_workflow_id";
    private static final String TEST_BASE_URL = "test_base_url";
    private static final String TEST_WEBHOOK_TOKEN = "test_webhook_token";
    private static final String TEST_API_TOKEN = "test_api_token";
    private static final String TEST_APPLICANT_ID = "test_applicant_id";
    private static final String TEST_WORKFLOW_RUN_ID = "test_workflow_run_id";
    private static final String TEST_SDK_TOKEN = "test_sdk_token";
    private static final String TEST_FIRST_NAME = "Joe";
    private static final String TEST_LAST_NAME = "Doe";

    private static final String CLAIM_URI_FIRST_NAME = "http://wso2.org/claims/givenname";
    private static final String CLAIM_URI_LAST_NAME = "http://wso2.org/claims/lastname";

    private static final String ONFIDO_ATTR_LASTNAME = "last_name";
    private static final String ONFIDO_ATTR_FIRSTNAME = "first_name";
    public static final String ONFIDO_APPLICANT_ID = "onfido_applicant_id";
    public static final String ONFIDO_WORKFLOW_RUN_ID = "onfido_workflow_run_id";
    public static final String ONFIDO_WORKFLOW_STATUS = "onfido_workflow_status";

    private static final String WORKFLOW_ID = "workflow_id";
    private static final String BASE_URL = "base_url";
    private static final String WEBHOOK_TOKEN = "webhook_token";
    private static final String ID = "id";
    private static final String TOKEN = "token";
    private static final String APPLICANT_ID = "applicant_id";
    private static final String STATUS = "status";

    @BeforeClass
    public void setUpClass() {

        onfidoIdentityVerifier = new OnfidoIdentityVerifier();
        mockedIdVDataHolder = mockStatic(IdentityVerificationDataHolder.class);
        mockedOnfidoAPIClient = mockStatic(OnfidoAPIClient.class);
        mockedOnfidoIDVDataHolder = mockStatic(OnfidoIDVDataHolder.class);
    }

    @AfterClass
    public void tearDownClass() {

        mockedIdVDataHolder.close();
        mockedOnfidoAPIClient.close();
        mockedOnfidoIDVDataHolder.close();
    }

    @BeforeMethod
    public void setUp() throws Exception {

        MockitoAnnotations.openMocks(this);
        setupMocks();
    }

    private void setupMocks() throws Exception {

        mockedOnfidoIDVDataHolder.when(() -> OnfidoIDVDataHolder.getIdentityVerificationManager())
                .thenReturn(mockIdentityVerificationManager);
        mockedIdVDataHolder.when(IdentityVerificationDataHolder::getInstance).thenReturn(mockDataHolderInstance);
        when(mockDataHolderInstance.getIdVProviderManager()).thenReturn(mockIdVProviderManager);
        when(mockIdVProviderManager.getIdVProvider(anyString(), anyInt())).thenReturn(mockIdVProvider);
        setupMockIdVProvider();

        mockedOnfidoIDVDataHolder.when(OnfidoIDVDataHolder::getRealmService).thenReturn(mockRealmService);
        when(mockRealmService.getTenantUserRealm(anyInt())).thenReturn(mockUserRealm);
        when(mockUserRealm.getUserStoreManager()).thenReturn(mockUniqueIDUserStoreManager);
        when(mockUniqueIDUserStoreManager.getUserClaimValueWithID(TEST_USER_ID, CLAIM_URI_FIRST_NAME, null))
                .thenReturn(TEST_FIRST_NAME);
        when(mockUniqueIDUserStoreManager.getUserClaimValueWithID(TEST_USER_ID, CLAIM_URI_LAST_NAME, null))
                .thenReturn(TEST_LAST_NAME);
    }

    private void setupMockIdVProvider() {

        when(mockIdVProvider.getIdVProviderUuid()).thenReturn(TEST_IDV_PROVIDER_ID);
        when(mockIdVProvider.isEnabled()).thenReturn(true);

        IdVConfigProperty[] configProperties = new IdVConfigProperty[4];
        configProperties[0] = createIdVConfigProperty(TOKEN, TEST_API_TOKEN, true);
        configProperties[1] = createIdVConfigProperty(BASE_URL, TEST_BASE_URL, false);
        configProperties[2] = createIdVConfigProperty(WEBHOOK_TOKEN, TEST_WEBHOOK_TOKEN, true);
        configProperties[3] = createIdVConfigProperty(WORKFLOW_ID, TEST_WORKFLOW_ID, false);

        when(mockIdVProvider.getIdVConfigProperties()).thenReturn(configProperties);

        Map<String, String> claimMappings = new HashMap<>();
        claimMappings.put(CLAIM_URI_FIRST_NAME, ONFIDO_ATTR_FIRSTNAME);
        claimMappings.put(CLAIM_URI_LAST_NAME, ONFIDO_ATTR_LASTNAME);

        when(mockIdVProvider.getClaimMappings()).thenReturn(claimMappings);
    }

    private IdentityVerifierData createMockIdentityVerifierData(String status) {

        IdVClaim firstNameClaim = new IdVClaim();
        firstNameClaim.setClaimUri(CLAIM_URI_FIRST_NAME);
        firstNameClaim.setIsVerified(false);

        IdVClaim lastNameClaim = new IdVClaim();
        lastNameClaim.setClaimUri(CLAIM_URI_LAST_NAME);
        lastNameClaim.setIsVerified(false);

        List<IdVClaim> idVClaims = new ArrayList<>();
        idVClaims.add(firstNameClaim);
        idVClaims.add(lastNameClaim);

        IdVProperty statusProperty = new IdVProperty();
        statusProperty.setName(STATUS);
        statusProperty.setValue(status);

        List<IdVProperty> idVProperties = new ArrayList<>();
        idVProperties.add(statusProperty);

        // Create IdentityVerifierData and set its properties
        IdentityVerifierData identityVerifierData = new IdentityVerifierData();
        identityVerifierData.setIdVProviderId(TEST_IDV_PROVIDER_ID);
        identityVerifierData.setIdVClaims(idVClaims);
        identityVerifierData.setIdVProperties(idVProperties);

        return identityVerifierData;
    }

    private List<IdVClaim> createMockFinalIdVClaims(OnfidoConstants.WorkflowRunStatus workflowRunStatus) {

        IdVClaim firstNameClaim = new IdVClaim();
        firstNameClaim.setClaimUri(CLAIM_URI_FIRST_NAME);
        firstNameClaim.setId("1");

        IdVClaim lastNameClaim = new IdVClaim();
        lastNameClaim.setClaimUri(CLAIM_URI_LAST_NAME);
        firstNameClaim.setId("2");

        List<IdVClaim> idVClaims = new ArrayList<>();
        idVClaims.add(firstNameClaim);
        idVClaims.add(lastNameClaim);

        for (IdVClaim claim : idVClaims) {
            claim.setUserId(TEST_USER_ID);
            claim.setIsVerified(false);
            claim.setMetadata(createClaimMetadata(workflowRunStatus));
        }

        return idVClaims;
    }

    private IdVConfigProperty createIdVConfigProperty(String name, String value, boolean isConfidential) {

        IdVConfigProperty property = new IdVConfigProperty();
        property.setName(name);
        property.setValue(value);
        property.setConfidential(isConfidential);
        return property;
    }

    private Map<String, Object> createClaimMetadata(OnfidoConstants.WorkflowRunStatus workflowRunStatus) {

        Map<String, Object> claimMetadata = new HashMap<>();
        claimMetadata.put(ONFIDO_APPLICANT_ID, TEST_APPLICANT_ID);
        claimMetadata.put(ONFIDO_WORKFLOW_RUN_ID, TEST_WORKFLOW_RUN_ID);
        claimMetadata.put(ONFIDO_WORKFLOW_STATUS, workflowRunStatus.getStatus());
        return claimMetadata;
    }

    @DataProvider(name = "invalidIdVProviderDataProvider")
    public Object[][] invalidIdVProviderDataProvider() {

        return new Object[][]{
                {null, "Null IdV Provider"},
                {mockIdVProvider, "Disabled IdV Provider"}
        };
    }

    @Test(dataProvider = "invalidIdVProviderDataProvider")
    public void testInvalidIdVProvider(IdVProvider idVProvider, String scenario) throws IdVProviderMgtException {

        when(mockIdVProviderManager.getIdVProvider(anyString(), anyInt())).thenReturn(idVProvider);
        if (idVProvider != null) {
            when(idVProvider.isEnabled()).thenReturn(false);
        }

        IdentityVerifierData identityVerifierData = createMockIdentityVerifierData(
                OnfidoConstants.VerificationFlowStatus.INITIATED.getStatus());

        try {
            onfidoIdentityVerifier.verifyIdentity(TEST_USER_ID, identityVerifierData, TEST_TENANT_ID);
            fail("Expected IdentityVerificationClientException was not thrown for " + scenario);
        } catch (IdentityVerificationClientException e) {
            assertEquals(e.getErrorCode(), ERROR_IDV_PROVIDER_INVALID_OR_DISABLED.getCode(),
                    "Unexpected error code for " + scenario);
            assertTrue(e.getMessage().contains(ERROR_IDV_PROVIDER_INVALID_OR_DISABLED.getMessage()),
                    "Unexpected error message for " + scenario);
        } catch (Exception e) {
            fail("Unexpected exception thrown for " + scenario + ": " + e.getMessage());
        }
    }

    @DataProvider(name = "invalidConfigPropertiesDataProvider")
    public Object[][] invalidConfigPropertiesDataProvider() {

        return new Object[][]{
                {null, TEST_BASE_URL, TEST_WEBHOOK_TOKEN, TEST_WORKFLOW_ID, "Null API Token"},
                {TEST_API_TOKEN, null, TEST_WEBHOOK_TOKEN, TEST_WORKFLOW_ID, "Null Base URL"},
                {TEST_API_TOKEN, TEST_BASE_URL, null, TEST_WORKFLOW_ID, "Null Webhook Token"},
                {TEST_API_TOKEN, TEST_BASE_URL, TEST_WEBHOOK_TOKEN, null, "Null Workflow ID"}
        };
    }

    @Test(dataProvider = "invalidConfigPropertiesDataProvider")
    public void testInvalidIdVConfigProperties(String apiToken, String baseUrl, String webhookToken,
                                               String workflowId, String scenario) {

        IdVConfigProperty[] configProperties = new IdVConfigProperty[4];
        configProperties[0] = createIdVConfigProperty(TOKEN, apiToken, true);
        configProperties[1] = createIdVConfigProperty(BASE_URL, baseUrl, false);
        configProperties[2] = createIdVConfigProperty(WEBHOOK_TOKEN, webhookToken, true);
        configProperties[3] = createIdVConfigProperty(WORKFLOW_ID, workflowId, false);
        when(mockIdVProvider.getIdVConfigProperties()).thenReturn(configProperties);

        IdentityVerifierData identityVerifierData = createMockIdentityVerifierData(
                OnfidoConstants.VerificationFlowStatus.INITIATED.getStatus());

        try {
            onfidoIdentityVerifier.verifyIdentity(TEST_USER_ID, identityVerifierData, TEST_TENANT_ID);
            fail("Expected IdentityVerificationClientException was not thrown for " + scenario);
        } catch (IdentityVerificationClientException e) {
            assertEquals(e.getErrorCode(), ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_EMPTY.getCode(),
                    "Unexpected error code for " + scenario);
            assertTrue(e.getMessage().contains(ERROR_IDV_PROVIDER_CONFIG_PROPERTIES_EMPTY.getMessage()),
                    "Unexpected error message for " + scenario);
        } catch (Exception e) {
            fail("Unexpected exception thrown for " + scenario + ": " + e.getMessage());
        }
    }

    @DataProvider(name = "invalidVerificationFlowStatusDataProvider")
    public Object[][] invalidVerificationFlowStatusDataProvider() {

        return new Object[][]{
                {"INVALID_STATUS", ERROR_INVALID_ONFIDO_VERIFICATION_FLOW_STATUS, "Invalid Status"},
                {"", ERROR_VERIFICATION_FLOW_STATUS_NOT_FOUND, "Empty Status"},
                {null, ERROR_VERIFICATION_FLOW_STATUS_NOT_FOUND, "Null Status"}
        };
    }

    @Test(dataProvider = "invalidVerificationFlowStatusDataProvider")
    public void testInvalidVerificationFlowStatus(String status, OnfidoConstants.ErrorMessage expectedError,
                                                  String scenario) {

        IdentityVerifierData identityVerifierData = createMockIdentityVerifierData(status);

        try {
            onfidoIdentityVerifier.verifyIdentity(TEST_USER_ID, identityVerifierData, TEST_TENANT_ID);
            fail("Expected IdentityVerificationClientException was not thrown for " + scenario);
        } catch (IdentityVerificationClientException e) {
            assertEquals(e.getErrorCode(), expectedError.getCode(), "Unexpected error code for " + scenario);
            assertEquals(e.getMessage(), expectedError.getMessage(),
                    "Unexpected error message for " + scenario);
        } catch (Exception e) {
            fail("Unexpected exception thrown for " + scenario + ": " + e.getMessage());
        }
    }

    @Test
    public void testVerificationRequiredClaimsNull() {

        IdVProperty statusProperty = new IdVProperty();
        statusProperty.setName(STATUS);
        statusProperty.setValue(OnfidoConstants.VerificationFlowStatus.INITIATED.getStatus());

        List<IdVProperty> idVProperties = new ArrayList<>();
        idVProperties.add(statusProperty);

        IdentityVerifierData identityVerifierData = new IdentityVerifierData();
        identityVerifierData.setIdVProviderId(TEST_IDV_PROVIDER_ID);
        identityVerifierData.setIdVProperties(idVProperties);

        try {
            onfidoIdentityVerifier.verifyIdentity(TEST_USER_ID, identityVerifierData, TEST_TENANT_ID);
            fail("Expected IdentityVerificationClientException was not thrown");
        } catch (IdentityVerificationClientException e) {
            assertEquals(e.getErrorCode(), ERROR_VERIFICATION_REQUIRED_CLAIMS_NOT_FOUND.getCode(),
                    "Unexpected error code for no verification required claims found.");
            assertEquals(e.getMessage(), ERROR_VERIFICATION_REQUIRED_CLAIMS_NOT_FOUND.getMessage(),
                    "Unexpected error message for no verification required claims found.");
        } catch (Exception e) {
            fail("Unexpected exception thrown for, no verification required claims found : " + e.getMessage());
        }
    }

    private JSONObject createApplicantResponse() {

        return new JSONObject()
                .put(ID, TEST_APPLICANT_ID)
                .put(ONFIDO_ATTR_FIRSTNAME, TEST_FIRST_NAME)
                .put(ONFIDO_ATTR_LASTNAME, TEST_LAST_NAME);
    }

    private JSONObject createWorkflowRunResponse(OnfidoConstants.WorkflowRunStatus workflowRunStatus) {

        return new JSONObject()
                .put(ID, TEST_WORKFLOW_RUN_ID)
                .put(STATUS, workflowRunStatus.getStatus())
                .put(WORKFLOW_ID, TEST_WORKFLOW_ID);
    }

    private JSONObject createSDKTokenResponse() {

        return new JSONObject()
                .put(TOKEN, TEST_SDK_TOKEN)
                .put(APPLICANT_ID, TEST_APPLICANT_ID);
    }

    private void assertCommonClaimProperties(List<IdVClaim> claims, String expectedWorkflowStatus) {

        for (IdVClaim claim : claims) {
            assertFalse(claim.isVerified(), "Claim should not be verified yet");
            Map<String, Object> metadata = claim.getMetadata();
            assertNotNull(metadata, "Claim metadata should not be null");
            assertEquals(metadata.get(ONFIDO_APPLICANT_ID), TEST_APPLICANT_ID, "Applicant ID should match");
            assertEquals(metadata.get(ONFIDO_WORKFLOW_RUN_ID), TEST_WORKFLOW_RUN_ID, "Workflow Run ID should match");
            assertEquals(metadata.get(ONFIDO_WORKFLOW_STATUS), expectedWorkflowStatus,
                    "Workflow status should match expected status");
        }
    }

    @Test
    public void testSuccessfulIdentityVerificationInitiationWithNewApplicant() throws Exception {

        mockedOnfidoAPIClient.when(() -> OnfidoAPIClient.createApplicant(any(), any()))
                .thenReturn(createApplicantResponse());
        mockedOnfidoAPIClient.when(() -> OnfidoAPIClient.createWorkflowRun(any(), any()))
                .thenReturn(createWorkflowRunResponse(
                        OnfidoConstants.WorkflowRunStatus.AWAITING_INPUT));
        mockedOnfidoAPIClient.when(() -> OnfidoAPIClient.createSDKToken(any(), any()))
                .thenReturn(createSDKTokenResponse());

        // Since this is a new applicant no prior idv claims exist
        when(mockIdentityVerificationManager.getIdVClaims(eq(TEST_USER_ID), eq(TEST_IDV_PROVIDER_ID), isNull(),
                eq(TEST_TENANT_ID))).thenReturn(new IdVClaim[0]);

        doReturn(null).when(onfidoIdentityVerifier).storeIdVClaims(anyString(), anyList(), anyInt());

        IdentityVerifierData identityVerifierData = createMockIdentityVerifierData(
                OnfidoConstants.VerificationFlowStatus.INITIATED.getStatus());

        IdentityVerifierData result =
                onfidoIdentityVerifier.verifyIdentity(TEST_USER_ID, identityVerifierData, TEST_TENANT_ID);

        assertNotNull(result, "Result should not be null");
        assertEquals(result.getIdVProviderId(), TEST_IDV_PROVIDER_ID, "IdV Provider ID should match");

        List<IdVClaim> resultClaims = result.getIdVClaims();
        assertNotNull(resultClaims, "Result claims should not be null");
        assertEquals(resultClaims.size(), 2, "Should have two claims");
        assertCommonClaimProperties(resultClaims, OnfidoConstants.WorkflowRunStatus.AWAITING_INPUT.getStatus());

        // Verify updateIdVClaim is not called at all and storeIdVClaims is called once
        verify(onfidoIdentityVerifier, times(0))
                .updateIdVClaim(eq(TEST_USER_ID), any(IdVClaim.class), eq(TEST_TENANT_ID));
        verify(onfidoIdentityVerifier, times(1))
                .storeIdVClaims(eq(TEST_USER_ID), anyList(), eq(TEST_TENANT_ID));

    }

    @Test
    public void testSuccessfulIdentityVerificationInitiationWithExistingApplicant() throws Exception {

        mockedOnfidoAPIClient.when(() -> OnfidoAPIClient.updateApplicant(any(), any(), any()))
                .thenReturn(createApplicantResponse());
        mockedOnfidoAPIClient.when(() -> OnfidoAPIClient.createWorkflowRun(any(), any()))
                .thenReturn(createWorkflowRunResponse(
                        OnfidoConstants.WorkflowRunStatus.AWAITING_INPUT));
        mockedOnfidoAPIClient.when(() -> OnfidoAPIClient.createSDKToken(any(), any()))
                .thenReturn(createSDKTokenResponse());

        IdVClaim firstNameClaim = new IdVClaim();
        firstNameClaim.setClaimUri(CLAIM_URI_FIRST_NAME);
        firstNameClaim.setIsVerified(false);
        firstNameClaim.setMetadata(createClaimMetadata(OnfidoConstants.WorkflowRunStatus.DECLINED));

        // Since the applicant already exists and has prior identity verification claims, for simplicity,
        // we'll consider only the first name-related identity verification claim as available.
        when(mockIdentityVerificationManager.getIdVClaims(eq(TEST_USER_ID), eq(TEST_IDV_PROVIDER_ID), isNull(),
                eq(TEST_TENANT_ID)))
                .thenReturn(new IdVClaim[]{firstNameClaim});
        when(mockIdentityVerificationManager.getIdVClaim(eq(TEST_USER_ID), eq(CLAIM_URI_FIRST_NAME),
                eq(TEST_IDV_PROVIDER_ID), eq(TEST_TENANT_ID)))
                .thenReturn(firstNameClaim);

        doReturn(null).when(onfidoIdentityVerifier)
                .updateIdVClaim(anyString(), any(IdVClaim.class), anyInt());
        doReturn(null).when(onfidoIdentityVerifier).storeIdVClaims(anyString(), anyList(), anyInt());

        IdentityVerifierData identityVerifierData = createMockIdentityVerifierData(
                OnfidoConstants.VerificationFlowStatus.INITIATED.getStatus());

        IdentityVerifierData result =
                onfidoIdentityVerifier.verifyIdentity(TEST_USER_ID, identityVerifierData, TEST_TENANT_ID);

        assertNotNull(result, "Result should not be null");
        assertEquals(result.getIdVProviderId(), TEST_IDV_PROVIDER_ID, "IdV Provider ID should match");

        List<IdVClaim> resultClaims = result.getIdVClaims();
        assertNotNull(resultClaims, "Result claims should not be null");
        assertEquals(resultClaims.size(), 2, "Should have two claims");
        assertCommonClaimProperties(resultClaims, OnfidoConstants.WorkflowRunStatus.AWAITING_INPUT.getStatus());

        // Verify updateIdVClaim and storeIdVClaims are called once each 
        verify(onfidoIdentityVerifier, times(1))
                .updateIdVClaim(eq(TEST_USER_ID), any(IdVClaim.class), eq(TEST_TENANT_ID));
        verify(onfidoIdentityVerifier, times(1))
                .storeIdVClaims(eq(TEST_USER_ID), anyList(), eq(TEST_TENANT_ID));
    }

    @DataProvider(name = "workflowStatusDataProvider")
    public Object[][] workflowStatusDataProvider() {

        return new Object[][]{
                {OnfidoConstants.WorkflowRunStatus.AWAITING_INPUT},
                {OnfidoConstants.WorkflowRunStatus.REVIEW},
                {OnfidoConstants.WorkflowRunStatus.APPROVED},
                {OnfidoConstants.WorkflowRunStatus.DECLINED},
                {OnfidoConstants.WorkflowRunStatus.ABANDONED},
                {OnfidoConstants.WorkflowRunStatus.ERROR}
        };
    }

    @Test(dataProvider = "workflowStatusDataProvider")
    public void testSuccessfulIdentityVerificationCompletion(OnfidoConstants.WorkflowRunStatus workflowRunStatus)
            throws Exception {

        IdentityVerifierData identityVerifierData = createMockIdentityVerifierData(
                OnfidoConstants.VerificationFlowStatus.COMPLETED.getStatus());

        // Mock existing IdVClaims associated with the user
        List<IdVClaim> existingClaimsList = createMockFinalIdVClaims(OnfidoConstants.WorkflowRunStatus.AWAITING_INPUT);
        IdVClaim[] existingClaims = existingClaimsList.toArray(new IdVClaim[0]);
        when(mockIdentityVerificationManager.getIdVClaims(anyString(), anyString(), any(), anyInt())).thenReturn(
                existingClaims);
        when(mockIdentityVerificationManager.getIdVClaimsByMetadata(anyString(), anyString(), anyString(),
                anyInt())).thenReturn(existingClaims);

        mockedOnfidoAPIClient.when(() -> OnfidoAPIClient.getWorkflowRunStatus(any(), any()))
                .thenReturn(createWorkflowRunResponse(workflowRunStatus));

        doReturn(null).when(onfidoIdentityVerifier)
                .updateIdVClaim(anyString(), any(IdVClaim.class), anyInt());

        IdentityVerifierData result =
                onfidoIdentityVerifier.verifyIdentity(TEST_USER_ID, identityVerifierData, TEST_TENANT_ID);

        assertNotNull(result, "Result should not be null");
        assertEquals(result.getIdVProviderId(), TEST_IDV_PROVIDER_ID, "IdV Provider ID should match");

        List<IdVClaim> resultClaims = result.getIdVClaims();
        assertNotNull(resultClaims, "Result claims should not be null");
        assertEquals(resultClaims.size(), 2, "Should have two claims");

        String expectedWorkflowStatus = workflowRunStatus.isEndingStatus() ?
                OnfidoConstants.WorkflowRunStatus.PROCESSING.getStatus() :
                workflowRunStatus.getStatus();
        assertCommonClaimProperties(resultClaims, expectedWorkflowStatus);
        verify(onfidoIdentityVerifier, times(2))
                .updateIdVClaim(eq(TEST_USER_ID), any(IdVClaim.class), eq(TEST_TENANT_ID));
    }

    @Test
    public void testSuccessfulIdentityVerificationReinitiation() throws Exception {

        IdentityVerifierData identityVerifierData = createMockIdentityVerifierData(
                OnfidoConstants.VerificationFlowStatus.REINITIATED.getStatus());

        // Mock existing IdVClaims associated with the user
        List<IdVClaim> existingClaimsList = createMockFinalIdVClaims(OnfidoConstants.WorkflowRunStatus.AWAITING_INPUT);
        IdVClaim[] existingClaims = existingClaimsList.toArray(new IdVClaim[0]);
        when(mockIdentityVerificationManager.getIdVClaims(eq(TEST_USER_ID), eq(TEST_IDV_PROVIDER_ID), isNull(),
                eq(TEST_TENANT_ID)))
                .thenReturn(existingClaims);
        when(mockIdentityVerificationManager.getIdVClaimsByMetadata(eq(ONFIDO_WORKFLOW_RUN_ID),
                eq(TEST_WORKFLOW_RUN_ID), eq(TEST_IDV_PROVIDER_ID), eq(TEST_TENANT_ID)))
                .thenReturn(existingClaims);

        JSONObject sdkTokenResponse = createSDKTokenResponse();
        mockedOnfidoAPIClient.when(() -> OnfidoAPIClient.createSDKToken(any(), any())).thenReturn(sdkTokenResponse);

        IdentityVerifierData result =
                onfidoIdentityVerifier.verifyIdentity(TEST_USER_ID, identityVerifierData, TEST_TENANT_ID);

        assertNotNull(result, "Result should not be null");
        assertEquals(result.getIdVProviderId(), TEST_IDV_PROVIDER_ID, "IdV Provider ID should match");

        List<IdVClaim> resultClaims = result.getIdVClaims();
        assertNotNull(resultClaims, "Result claims should not be null");
        assertEquals(resultClaims.size(), 2, "Should have two claims");
        assertCommonClaimProperties(resultClaims, OnfidoConstants.WorkflowRunStatus.AWAITING_INPUT.getStatus());
    }
}
