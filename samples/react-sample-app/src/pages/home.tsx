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

import { Hooks, useAuthContext } from "@asgardeo/auth-react";
import React, { FunctionComponent, ReactElement, useCallback, useEffect, useState } from "react";
import { default as authConfig } from "../config.json";
import { useLocation } from "react-router-dom";
import { LogoutRequestDenied } from "../components/LogoutRequestDenied";
import { USER_DENIED_LOGOUT } from "../constants";
import { useNavigate } from "react-router-dom";
import { handleMissingClientId } from "../util";
import { isClaimVerified } from "../api";
import { AgeVerificationDrawer, Footer, LoadingSpinner, NavBar, Plans } from "../components";

/**
 * Home page for the Sample.
 *
 * @return {React.ReactElement}
 */
export const HomePage: FunctionComponent = (): ReactElement => {

    const {
        state,
        signIn,
        signOut,
        on
    } = useAuthContext();

    const navigate = useNavigate();
    const location = useLocation();

    const [ hasLogoutFailureError, setHasLogoutFailureError ] = useState<boolean>();
    const [ isAgeVerified, setIsAgeVerified ] = useState<boolean>(false);
    const [ isDrawerOpen, setIsDrawerOpen ] = useState<boolean>(false);

    const search = useLocation().search;
    const stateParam = new URLSearchParams(search).get('state');
    const errorDescParam = new URLSearchParams(search).get('error_description');

    useEffect(() => {
        if (state?.isAuthenticated) {
            return;
        }
        handleLogin();
    }, [ state?.isAuthenticated, signIn ]);

    useEffect(() => {
        if (!state?.isAuthenticated) {
            return;
        }
        isClaimVerified("http://wso2.org/claims/dob")
            .then((isVerified) => {
                console.log("isVerified: ", isVerified);
                setIsAgeVerified(isVerified);
                setIsDrawerOpen(!isVerified);
            })
            .catch((error) => {
                console.log(error);
            });

    }, [ state?.isAuthenticated ]);

    useEffect(() => {
        if (stateParam && errorDescParam) {
            if (errorDescParam === "End User denied the logout request") {
                setHasLogoutFailureError(true);
            }
        }
    }, [ stateParam, errorDescParam ]);

    const handleLogin = useCallback(() => {

        setHasLogoutFailureError(false);
        signIn().catch((error) => {
            console.error("Error occurred while signing in.", error);
            navigate("/generic-error", { state: { message: "Error occurred while signing In" } });
        });
    }, [ navigate, signIn ]);

    const handleLogout = () => {
        setHasLogoutFailureError(false);
        signOut().catch(() => setHasLogoutFailureError(true));
    };

    const verifyAge = () => {
        navigate("/verify");
    };

    /**
     * handles the error occurs when the logout consent page is enabled
     * and the user clicks 'NO' at the logout consent page
     */
    useEffect(() => {
        on(Hooks.SignOut, () => {
            setHasLogoutFailureError(false);
        });

        on(Hooks.SignOutFailed, () => {
            if (!errorDescParam) {
                handleLogin();
            }
        })
    }, [ on, handleLogin, errorDescParam ]);


    // If `clientID` is not defined in `config.json`, show a UI warning.
    if (!authConfig?.clientID) {
        handleMissingClientId();
    }

    // Handle log out failure scenarios.
    if (hasLogoutFailureError) {
        return (
            <LogoutRequestDenied
                errorMessage={USER_DENIED_LOGOUT}
                handleLogin={handleLogin}
                handleLogout={handleLogout}
            />
        );
    }

    // If the user is not authenticated, Show a loading spinner until authentication flow is started.
    if (!state?.isAuthenticated) {
        return <LoadingSpinner/>;
    }

    // If the user is authenticated and the ID verification is initiated, show verification in progress page.
    if (location?.state?.idVerificationInitiated) {
        navigate("/verification-in-progress");
    }

    return (
        <>
            <NavBar/>
            <Plans isAgeVerified={isAgeVerified} setIsDrawerOpen={setIsDrawerOpen}/>
            <AgeVerificationDrawer
                isOpen={isDrawerOpen}
                setIsOpen={setIsDrawerOpen}
                verifyAge={verifyAge}
            />
            <Footer/>
        </>
    );
};
