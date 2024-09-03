# Configuring the Onfido Identity Verification Connector

To use the Onfido Identity Verification Connector with WSO2 Identity Server, you need to configure the connector within 
the WSO2 Identity Server. Follow the instructions below to set up the Onfido connector using a 
sample application.

To test this setup, your organization must first have an Onfido account. You will then need to create 
a workflow in the Onfido Studio and generate the necessary tokens.

## Prerequisites

1. You need to have an Onfido account for this connector to work. 
Please [contact](https://public.support.onfido.com/s/contactsupport) the Onfido team and they will be happy to help.
2. This version of the connector is tested with WSO2 Identity Server version 7.0. 
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
2. In the WSO2 Identity Server Console, navigate to **Identity Verification Providers**.
3. Click **+ New Identity Verification Provider** to create a new Identity Verification Provider (IDVP).
4. You will be redirected to a page displaying a set of available IDVP templates.
5. Click on `Create` under the Onfido IDVP.
6. Enter a name for the Onfido IDVP and add the necessary configurations for the Onfido IDVP:
   - **API Token**: The API token generated via the Onfido dashboard.
   - **Workflow ID**: The unique identifier for the Workflow created using Onfido Studio.
   - **Base URL**: The regional base URL for Onfido API calls.
7. Once you have entered the configuration details, click on `Create`.
8. You will be redirected to the Setup Guide for the newly created Onfido IDVP. Follow the instructions displayed:
   - Log in to your Onfido dashboard and navigate to the Webhook configuration section. Generate a Webhook token by 
   providing the displayed URL and selecting only the `workflow_run.completed` event.
   - Return to the WSO2 console and navigate to the **Settings** tab of the newly created Onfido IDVP. 
   Enter the obtained token in the Webhook Token field, then click `Update` to finish the setup.
   - If you need to provide additional attribute mappings, navigate to the **Attributes** tab and 
   configure the mappings.
9. After completing the configuration and mapping, your Onfido IDVP will be ready for use with WSO2 Identity Server. 
You can now integrate Onfido's identity verification process into your applications.

### Integrating Onfido Identity Verification into Your Application

To integrate Onfido's identity verification into your application, use the Identity Verification User APIs provided 
by WSO2. You can find the API documentation [here](https://github.com/wso2/identity-api-user/blob/master/components/org.wso2.carbon.identity.api.user.idv/org.wso2.carbon.identity.api.user.idv.v1/src/main/resources/idv.yaml).
For a practical example, refer to the [Onfido Sample App - Configuration Guidelines](samples/react-sample-app/README.md).

### Core Steps:

1. **Import Onfido SDK:**
   - Add the [onfido-sdk-ui](https://github.com/onfido/onfido-sdk-ui) to your project.

2. **Initiate Verification with Onfido:**
   - Make a POST request to the `<Base URL>/api/users/v1/me/idv/verify` endpoint with the following properties in the 
   API request body:

     ```json
     {
         "idVProviderId": "<Onfido identity verification provider's ID>",
         "claims": "<List of WSO2 claims that require verification>",
         "properties": [
             {
                 "key": "status",
                 "value": "INITIATED"
             }
         ]
     }
   - **Note:** Ensure that the claims for both first name and last name are included in the claims list. These are mandatory.

3. **Launch the Onfido SDK:**

   - Extract the `sdk_token` and `onfido_workflow_run_id` from the response of the initiation request. 
   Use these to launch the Onfido SDK in your application.

4. **Complete the Verification Process:**

   - After the user completes the document submission and face capture (as defined in the workflow) via the Onfido SDK, 
   make another POST request to the `<Base URL>/api/users/v1/me/idv/verify` endpoint with the following properties in the API request body:

      ```json
      {
          "idVProviderId": "<Onfido identity verification provider's ID>",
          "claims": "<List of WSO2 claims that require verification>",
          "properties": [
              {
                  "key": "status",
                  "value": "COMPLETED"
              }
          ]
      }
   - **Note:** Ensure that the claims for both first name and last name are included in the claims list. These are mandatory.


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
         "claims": "<List of WSO2 claims that require verification>",
         "properties": [
             {
                 "key": "status",
                 "value": "REINITIATED"
             }
         ]
     }
     ```

   - After reinitiating the verification process, follow Step 3 to relaunch the Onfido SDK using the new `sdk_token`, and then proceed to Step 4 to complete the verification process.
   - **Note:** Ensure that the claims for both first name and last name are included in the claims list. These are mandatory.


### Configuring Onfido Webhooks

Onfido offers webhooks to notify your system about changes in the status of identity verifications. 
You can learn more about Onfido webhooks by visiting their [official documentation](https://documentation.onfido.com/#about-webhooks).

To integrate Onfido webhooks with WSO2 Identity Server, use the provided Open API endpoint as the webhook URL:

`<Base URL>/idv/onfido/v1/<idvp_id>/verify`

You can find this URL in the console under the settings tab corresponding to the Onfido IDVP you created. 
By configuring this endpoint, WSO2 Identity Server will automatically update the verification status of users based on the notifications received from Onfido.

**Note:** 
- Webhook configuration is mandatory, as the verification status of the user claims won't be updated unless it is configured.
- Additionally, ensure that the workflow is configured to output the data comparison breakdown results. For more details, refer to the [Onfido Workflow Setup Guide](onfido-setup-guide.md).
