import React from "react";

// TODO: Revamp this to match the general look and feel
export const handleMissingClientId = () => {
    return (
        <div className="content">
            <h2>You need to update the Client ID to proceed.</h2>
            <p>Please open &quot;src/config.json&quot; file using an editor, and update
                the <code>clientID</code> value with the registered application&apos;s client ID.</p>
            <p>Visit repo <a
                href="https://github.com/asgardeo/asgardeo-auth-react-sdk/tree/master/samples/asgardeo-react-app">README</a> for
                more details.</p>
        </div>
    );
}
