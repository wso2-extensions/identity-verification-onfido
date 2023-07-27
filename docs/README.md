# IDV with Onfido - Configuration Guidelines

## Design

![Design Diagram](img/design.png "Design Diagram")

## Prerequisites
1. You need to have Onfido API credentials for this connector to work. Please
   [contact](https://www.evidentid.com/contact-sales/) the Onfido team and they will be happy to help. You will need
    the following credentials:
     - API Token
     - API Base url
     - Webhook token
2. This version of the connector is tested with WSO2 Identity Server version 6.2.0. Make sure to download and set up
   the correct version of the [Identity Server](https://wso2.com/identity-and-access-management) on your environment.

## Installing the Connector
1. Download the connector from [WSO2 Connector Store](https://store.wso2.com/store/assets/isconnector/list).
2. Copy the ```org.wso2.carbon.identity.verification.onfido.connector-x.x.x.jar``` file to
   ```<IS-HOME>/repository/components/dropins``` folder.   
3. Copy the ```onfido#idv.war``` file to ```<IS-HOME>/repository/deployment/server/webapps``` folder.
4. Copy the ```onfido``` folder to 
5. ```<IS_HOME>/repository/resources/identity/extensions/identity-verification-provider``` folder.
6. Restart the server.

## Configuration Steps

1. Log in to the Identity Server management console using your admin credentials.
2. On WSO2 Identity Server Management Console, go to **Main > Identity > Identity Verification Providers**
3. Click **Add** and enter a name and description for the Onfido IDVP and select Onfido from the dropdown.
4. Add configurations of the Onfido identity verification provider.
   - **API Token** - The API token generated via the Onfido dashboard.
   - **Base URL** - The base URL of Onfido server.
   - **Webhook Token** - The webhook token generated via the Onfido dashboard.
5. Map the required local attributes to the attributes from the identity verification provider.
6. Click **Register** to add the identity verification provider.

## Integrate identity verification into the application

1. Use Identity verification user APIs
   (https://github.com/wso2/identity-api-user/blob/master/components/org.wso2.carbon.identity.api.user.idv/org.wso2.carbon.identity.api.user.idv.v1/src/main/resources/idv.yaml) 
   to integrate identity verification for the required application. Please refer [IDV with Onfido - Configuration Guidelines](docs/sample-app) for sample implementation.
   a. import [onfido-sdk-ui](https://github.com/onfido/onfido-sdk-ui)
   b. /{user-id}/idv/verify endpoint with the following properties in the API request body
      ```
        {
            "key": "status",
            "value": "INITIATED"
        }
      ```
   c. Based on the response from the above request extract the sdk_token and prompt onfido sdk.
   d. After completing the verification then again call to /{user-id}/idv/verify endpoint with the following 
      properties in the API request body
      ```
      {
        "key": "status",
        "value": "COMPLETED"
      }
      ```
## Onfido webhook configuration

Onfido provides webhooks to alert the changes in the status of the verification. 
Read more: https://documentation.onfido.com/#about-webhooks

WSO2 Identity Server provides Open API endpoint(/idv/onfido/v1/<idvp_id>>/verify) to add as the webhook URL.
Through this endpoint, WSO2 Identity Server will update the verification status of the user.