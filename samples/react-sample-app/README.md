# Guardio Life - Onfido Connector Usage Example (Single Page Application)

This sample application demonstrates how to perform identity verification with Onfido using WSO2 Identity Server in a Single Page Application (SPA). The application simulates an insurance company called Guardio Life, where users can browse and select various insurance plans.

To ensure compliance and verify the age of potential customers, Guardio Life implements an identity and age verification process using Onfido before allowing users to select a plan. Here's how it works:

1. Users can view available insurance plans on the homepage.
2. Initially, the buttons for selecting plans are greyed out and not clickable.
3. Users are prompted to complete an identity and age verification process using Onfido.
4. The application integrates with Onfido to perform the verification.
5. Once the verification is successfully completed, the plan selection buttons become active and clickable.
6. Users can then proceed to select their desired insurance plan.

This sample showcases the seamless integration of identity verification in a real-world scenario, enhancing security and compliance for online insurance purchases while providing a clear user experience with visual cues about the verification status.

## Getting Started

### Prerequisites
- Ensure you have `Node.js` installed (version 10 or above).
- Ensure that a user account is created, and the *first name*, *last name*, and *date of birth* are correctly set to match the details on the document that will be used for verification.

### Registering a Single Page Application in the WSO2 Identity Server Console

Follow the instructions in the [WSO2 Identity Server Documentation](https://is.docs.wso2.com/en/latest/guides/applications/register-single-page-app/) to register your application.

Ensure you add `https://localhost:3000` as a Redirect URL and also include `https://localhost:3000` under Allowed Origins.

### Configuring an Onfido Identity Verification Provider in the WSO2 Identity Server Console

Follow the instructions in the [Onfido Connector Documentation](https://github.com/wso2-extensions/identity-verification-onfido/blob/main/docs/config.md#configuring-onfido-identity-verification-provider-in-wso2-identity-server-console)

Note: Ensure that the necessary attribute mappings for the *Guardio Life* application are provided. Navigate to the **Attributes** tab and configure the following attribute mappings:

- first_name -> http://wso2.org/claims/givenname
- last_name -> http://wso2.org/claims/lastname
- dob -> http://wso2.org/claims/dob

These mappings ensure that the user's first name, last name, and date of birth are accurately synchronized between Onfido and WSO2 Identity Server, enabling both user identity verification and attribute value validation.

### Configuring the Sample Application

1. Navigate to the `identity-verification-onfido-sample-app/samples/react-sample-app/public` directory.
2. Open the `runtime-config.json` file and update it with your registered application and Onfido Identity Verification Provider details:

    ```json
    {
        "clientID": "<CLIENT_ID>",
        "baseUrl": "https://<DOMAIN_NAME>/t/<TENANT_NAME>",
        "signInRedirectURL": "https://localhost:3000",
        "signOutRedirectURL": "https://localhost:3000",
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
The application will open at [`https://localhost:3000`](https://localhost:3000).

### Changing the Application's Development Server Port

If you need to run the development server on a different port, follow these steps:

1. Update the `PORT` in the `.env` file located at the root of the application.
2. Update the `signInRedirectURL` and `signOutRedirectURL` in the `src/runtime-config.json` file.
3. Go to the Identity Server/Asgardeo Console and navigate to the **Protocol** tab of your application:
    - Update the **Authorized Redirect URL**.
    - Update the **Allowed Origins**.
