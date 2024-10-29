# Configuring the Onfido Identity Verification Connector

To use the Onfido Identity Verification Connector with WSO2 Identity Server, you need to configure the connector within 
the WSO2 Identity Server. Follow the instructions below to set up the Onfido connector using a 
sample application.

To test this setup, your organization must first have an Onfido account. You will then need to create 
a workflow in the Onfido Studio and generate the necessary tokens.

## Prerequisites

1. You need to have an Onfido account for this connector to work. 
Please [contact](https://onfido.com/signup/) the Onfido team and they will be happy to help. Ensure that comparison 
checks are enabled to the account. For more details, refer to the [Onfido Comparison Checks documentation](https://documentation.onfido.com/api/latest/#data_comparison).
2. This version of the connector is tested with WSO2 Identity Server version 7.0 and above. 
Make sure to download and set up the correct version of the
[Identity Server](https://wso2.com/identity-and-access-management) in your environment.
3. By default, the Identity Verification Provider feature is not enabled in 
WSO2 Identity Server version 7.0. To enable it, add the following configuration to the 
deployment.toml file:
   ```toml
   [console.identity_verification_providers]
   enabled = true
   ```

## Setting up and Installing the Onfido Connector

### Step 1: Extracting the Project Artifacts

1. Clone the `identity-verification-onfido` repository.
2. Build the project by running `mvn clean install` in the root directory.

**Note:** The latest project artifacts can also be downloaded from the Connector Store.

### Step 2: Deploying the Onfido Authenticator

1. Navigate to `identity-verification-onfido/components → org.wso2.carbon.identity.verification.onfido.connector → target`.
2. Copy the `org.wso2.carbon.identity.verification.onfido.connector-<version>-SNAPSHOT.jar` file.
3. Navigate to `<IS_HOME>/repository/components/dropins`.
4. Paste the `.jar` file into the dropins directory.
5. Alternatively, you can drag and drop the `.jar` file into the dropins directory.
6. Next, navigate to `identity-verification-onfido/ui-metadata` and copy the `onfido` directory.
7. Paste it into `<IS_HOME>/repository/resources/identity/extensions/identity-verification-providers` directory.

### Step 3: Deploying the Onfido Webhook REST API

1. Navigate to `identity-verification-onfido/components → org.wso2.carbon.identity.verification.onfido.api → org.wso2.carbon.identity.verification.onfido.api.dispatcher → target`.
2. Copy the `idv#onfido.war` file.
3. Navigate to `<IS_HOME>/repository/deployment/server/webapps`.
4. Paste the `.war` file into the webapps directory.
5. Next, navigate to `<IS_HOME>/repository/conf`.
6. Open the `deployment.toml` file.
7. Add the following lines of code:

    ```toml
    [[resource.access_control]]
    context = "(.*)/idv/onfido/v1/(.*)/verify"
    secure = "false"
    http_method = "POST"

    [tenant_context]
    enable_tenant_qualified_urls = "true"
    enable_tenanted_sessions = "true"
    rewrite.custom_webapps = ["/idv/onfido/"]
    ```

## Configuring Onfido Identity Verification Provider in WSO2 Identity Server Console

1. Log in to the WSO2 Identity Server console using your admin credentials.
2. In the WSO2 Identity Server Console, navigate to **Connections** on the left-hand panel.
3. Click **New Connection**.
4. From the displayed templates click on `Create` under the Onfido connector.
5. Enter a name for the Onfido connector and add the necessary configurations for the Onfido connector:
   - **API Token**: The API token generated via the Onfido dashboard.
   - **Workflow ID**: The unique identifier for the Workflow created using Onfido Studio. 
   For more information refer [Onfido Workflow Setup Guide](onfido-setup-guide.md).
   - **Base URL**: The regional base URL for Onfido API calls.
6. Once you have entered the configuration details, click on `Create`.
7. You will be redirected to the Setup Guide for the newly created Onfido connector. Follow the instructions displayed:
   - Log in to your Onfido dashboard and navigate to the Webhook configuration section. Generate a Webhook token by 
   providing the displayed URL and selecting only the `workflow_run.completed` event.
   - Return to the WSO2 console and navigate to the **Settings** tab of the newly created Onfido connector. 
   Enter the obtained token in the Webhook Token field, then click `Update` to finish the setup.
8. Now that you have created a connection for Onfido, ensure that the attributes used in Onfido are correctly mapped to 
the attributes in Identity Server. To do this:
   - Navigate to the **Attributes** tab of the newly created Onfido connector.
   - Verify that first name and last name are already configured as mandatory attributes. 
   - To add other attribute mappings, click **Add Attribute Mapping**. 
   - Enter the attribute name used in Onfido. 
   - Select the corresponding Identity Server Claim URI. 
   - Click **Add Attribute Mapping** and then **Update**.
9. After completing the configuration and attribute mapping, your Onfido connector will be ready for use with WSO2 
Identity Server. You can now integrate Onfido's identity verification process into your applications.

> **Note :**
> 
> In WSO2 Identity Server 7.0, the steps differ slightly. To create a new Identity Verification Provider:
> 1. Log in to the WSO2 Identity Server console using your admin credentials.
> 2. Navigate to Identity Verification Providers in the left-hand menu.
> 3. Click + New Identity Verification Provider.
> 4. Follow steps 4-9 as listed above to complete the setup process.

### Integrating Onfido Identity Verification into Your Application

To integrate Onfido's identity verification into your application use the [Onfido SDK](https://documentation.onfido.com/sdk/). 
The Onfido SDK guides users through the verification process, including capturing and uploading documents or 
photos for biometric checks. The actual verification can then be handled by Identity Server's 
[Identity Verification User APIs](https://github.com/wso2/identity-api-user/blob/master/components/org.wso2.carbon.identity.api.user.idv/org.wso2.carbon.identity.api.user.idv.v1/src/main/resources/idv.yaml).

For a practical example, refer to the [Onfido Sample App - Configuration Guidelines](samples/react-sample-app/README.md).

Follow the steps below to integrate Onfido Identity Verification into your external application:

1. **Integrate Onfido SDK:**
   - Add the Onfido SDK into your external application project. For detailed instructions, refer to the
     [Onfido SDK documentation](https://documentation.onfido.com/sdk/).

2. **Initiate Verification with Onfido:**
   - To initiate verification with Onfido, make a POST request to the `<Base URL>/api/users/v1/me/idv/verify` 
   endpoint with the following payload.

     ```json
     {
         "idVProviderId": "<Onfido identity verification provider's ID>",
         "claims": [
              "http://wso2.org/claims/givenname",
              "http://wso2.org/claims/lastname"
         ],
         "properties": [
             {
                 "key": "status",
                 "value": "INITIATED"
             }
         ]
     }
     ```
   - **Note:**
      - The `idVProviderId` can be found in the Setup Guide page of the created Onfido connector.
      - It is mandatory to include the Claim URIs for first name and last name. Make sure to add any other claims that
        were configured with the Onfido connector for verification.
     
   - The response may look similar to the following:
     ```json
     {
       "idVProviderId": "<Onfido identity verification provider's ID>",
       "claims": [
          {
             "id": "<ID>",
             "uri": "<WSO2 Claim URI>",
             "isVerified": false,
             "claimMetadata": {
                "onfido_applicant_id": "<Onfido applicant ID>",
                "onfido_workflow_run_id": "<Onfido workflow run ID>",
                "sdk_token": "<Onfido SDK token>",
                "onfido_workflow_status": "awaiting_input"
             }
          }
        ]
     }
     ```

3. **Launch the Onfido SDK:**

   - Extract the `sdk_token` and `onfido_workflow_run_id` from the response of the initiation request. 
   Use these to launch the Onfido SDK in your application.

4. **Complete the Verification Process:**

   - After the user completes the document submission and face capture (as defined in the workflow) via the Onfido SDK, 
   make another POST request to the `<Base URL>/api/users/v1/me/idv/verify` endpoint with the following properties in the API request body:

      ```json
      {
          "idVProviderId": "<Onfido identity verification provider's ID>",
          "claims": [
              "http://wso2.org/claims/givenname",
              "http://wso2.org/claims/lastname"
          ],
          "properties": [
              {
                  "key": "status",
                  "value": "COMPLETED"
              }
          ]
      }
   - **Note:** It is mandatory to include the Claim URIs for first name and last name. Make sure to add any other claims that
     were configured with the Onfido connector for verification.

5. **Optional: Reinitiate the Verification Process**

   - In some cases, a user might interrupt the Onfido verification process before completing their 
   document submission or face capture (as defined in the workflow) via the SDK. To allow users to continue the 
   verification process from where they left off, you can reinitiate the process.
   - Reinitiation is only allowed when the workflow status of the claim is `AWAITING_INPUT`. 
   This status indicates that the process is paused, awaiting further input from the user.
   - To reinitiate the verification process, make a POST request to the `<Base URL>/api/users/v1/me/idv/verify` 
   endpoint with the following properties in the API request body:

     ```json
     {
         "idVProviderId": "<Onfido identity verification provider's ID>",
         "claims": [
              "http://wso2.org/claims/givenname",
              "http://wso2.org/claims/lastname"
          ],
         "properties": [
             {
                 "key": "status",
                 "value": "REINITIATED"
             }
         ]
     }
     ```

   - After reinitiating the verification process, follow Step 3 to relaunch the Onfido SDK using the new `sdk_token`, 
   and then proceed to Step 4 to complete the verification process.
   - **Note:** It is mandatory to include the Claim URIs for first name and last name. Make sure to add any other claims that
     were configured with the Onfido connector for verification.


### Configuring Onfido Webhooks

Onfido offers webhooks to notify your system about changes in the status of identity verifications. 
You can learn more about Onfido webhooks by visiting their [official documentation](https://documentation.onfido.com/#about-webhooks).

To integrate Onfido webhooks with WSO2 Identity Server, use the provided Open API endpoint as the webhook URL:

`<Base URL>/idv/onfido/v1/<idvp_id>/verify`

You can find this URL in the console under the **Settings** tab corresponding to the Onfido connector you created. 
By configuring this endpoint, WSO2 Identity Server will automatically update the verification status of users based on 
the notifications received from Onfido.

**Note:** 
- Webhook configuration is mandatory, as the verification status of the user claims won't be updated unless it is configured.
- Additionally, ensure that the workflow is configured to output the data comparison breakdown results. 
For more details, refer to the [Onfido Workflow Setup Guide](onfido-setup-guide.md).
