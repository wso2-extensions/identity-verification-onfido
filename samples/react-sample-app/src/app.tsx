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
import "./app.css";
import { default as authConfig } from "./config.json";
import { ErrorBoundary } from "./error-boundary";
import { HomePage, NotFoundPage, GenericErrorPage, VerificationInProgressPage, SuccessPage, VerifyPage } from "./pages";
import { createTheme, ThemeProvider } from "@mui/material/styles";
import GlobalStyles from "@mui/material/GlobalStyles";
import CssBaseline from "@mui/material/CssBaseline";

const AppContent: FunctionComponent = (): ReactElement => {
    const { error } = useAuthContext();

    return (
        <ErrorBoundary error={error}>
            <Router>
                <Routes>
                    <Route path="/" element={<HomePage/>}/>
                    <Route path="/verify" element={<VerifyPage/>}/>
                    <Route path="/success" element={<SuccessPage/>}/>
                    <Route path="/verification-in-progress" element={<VerificationInProgressPage/>}/>
                    <Route path="/generic-error" element={<GenericErrorPage/>}/>
                    <Route element={<NotFoundPage/>}/>
                </Routes>
            </Router>
        </ErrorBoundary>
    )
};

const defaultTheme = createTheme();
const App = () => (
    <AuthProvider config={authConfig}>
        <ThemeProvider theme={defaultTheme}>
            <GlobalStyles styles={{ ul: { margin: 0, padding: 0, listStyle: 'none' } }}/>
            <CssBaseline/>
            <AppContent/>
        </ThemeProvider>
    </AuthProvider>
);

const root = ReactDOM.createRoot(document.getElementById("root") as HTMLElement);

root.render(<App/>);

