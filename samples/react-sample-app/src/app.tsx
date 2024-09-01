/**
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com). All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import { AuthProvider, useAuthContext } from "@asgardeo/auth-react";
import React, { FunctionComponent, ReactElement } from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter as Router, Route, Routes } from "react-router-dom";
import { ThemeProvider } from "@oxygen-ui/react";
import { default as authConfig } from "./config.json";
import { ErrorBoundary } from "./error-boundary";
import { HomePage, NotFoundPage, GenericErrorPage, VerificationInProgressPage, SuccessPage, VerifyPage } from "./pages";
import { LoadingSpinner } from "./components";
import Theme from './styles/guardioTheme';

const AppContent: FunctionComponent = (): ReactElement => {
    const { error, state } = useAuthContext();

    if (state?.isLoading) {
        return <LoadingSpinner />;
    }

    return (
        <ErrorBoundary error={error}>
            <Router basename={process.env.REACT_APP_BASE_URL}>
                <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/verify" element={<VerifyPage />} />
                    <Route path="/success" element={<SuccessPage />} />
                    <Route path="/verification-in-progress" element={<VerificationInProgressPage />} />
                    <Route path="/generic-error" element={<GenericErrorPage />} />
                    <Route path="*" element={<NotFoundPage />} />
                </Routes>
            </Router>
        </ErrorBoundary>
    )
};

const App = () => (
    <AuthProvider config={authConfig}>
        <ThemeProvider theme={Theme} defaultMode="light">
            <AppContent/>
        </ThemeProvider>
    </AuthProvider>
);

const root = ReactDOM.createRoot(document.getElementById("root") as HTMLElement);

root.render(<App/>);
