# Life Guardian - Onfido Connector Usage Example (Single Page Application)

This sample application demonstrates how to perform identity verification with Onfido using WSO2 Identity Server in a Single Page Application (SPA). This application is developed primarily focusing the Forrester demo.

## Getting Started

### Prerequisites
- Ensure you have `Node.js` installed (version 10 or above).

### Registering a Single Page Application in the WSO2 Identity Server Console

Follow the instructions in the [WSO2 Identity Server Documentation](https://is.docs.wso2.com/en/latest/guides/applications/register-single-page-app/) to register your application.

Ensure you add `https://localhost:3000/guardio-life/subscriptions` as a Redirect URL and also include `https://localhost:3000` under Allowed Origins.

### Configuring the Sample Application

1. Navigate to the `identity-verification-onfido-sample-app/samples/react-sample-app/public` directory.
2. Open the `runtime-config.json` file and update it with your registered application and Onfido Identity Verification Provider details:

    ```json
    {
        "clientID": "<CLIENT_ID>",
        "baseUrl": "https://<DOMAIN_NAME>/t/<TENANT_NAME>",
        "signInRedirectURL": "https://localhost:3000/guardio-life/subscriptions",
        "signOutRedirectURL": "https://localhost:3000/guardio-life/subscriptions",
        "userPortalURL": "https://<DOMAIN_NAME>/t/<TENANT_NAME>/myaccount",
        "scope": [ "openid", "profile", "internal_login"],
        "identityVerificationProviderId": "<ONFIDO_IDVP_ID>"
    }
    ```

### Running the Application

Run the following commands to install dependencies and start the application:

```bash
npm install && npm start
```
The application will open at [`https://localhost:3000/guardio-life/subscriptions`](https://localhost:3000/guardio-life/subscriptions).

### Changing the Application's Development Server Port

If you need to run the development server on a different port, follow these steps:

1. Update the `PORT` in the `.env` file located at the root of the application.
2. Update the `signInRedirectURL` and `signOutRedirectURL` in the `src/runtime-config.json` file.
3. Go to the Identity Server/Asgardeo Console and navigate to the **Protocol** tab of your application:
    - Update the **Authorized Redirect URL**.
    - Update the **Allowed Origins**.
