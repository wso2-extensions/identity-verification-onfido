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

import { useAuthContext } from "@asgardeo/auth-react";
import React, { FunctionComponent, ReactElement, useCallback, useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { isClaimVerified } from "../api";
import { AgeVerificationDrawer, Footer, LoadingSpinner, NavBar, Plans } from "../components";
import { ClaimVerificationStatus, WorkflowStatus } from "../model/identity-verification";
import { useConfig } from "../configContext";

/**
 * Home page for the Sample.
 *
 * @return {React.ReactElement}
 */
export const HomePage: FunctionComponent = (): ReactElement => {
    const { state, signIn } = useAuthContext();
    const navigate = useNavigate();
    const location = useLocation();
    const config = useConfig();

    const [verificationStatus, setVerificationStatus] = useState<ClaimVerificationStatus | null>(null);
    const [isDrawerOpen, setIsDrawerOpen] = useState<boolean>(false);
    const [drawerMessageType, setDrawerMessageType] = useState("info");
    const [drawerMessage, setDrawerMessage] = useState<string>("");
    const [isLoading, setIsLoading] = useState<boolean>(true);

    const checkVerificationStatus = useCallback(() => {
        isClaimVerified("http://wso2.org/claims/dob", config)
            .then((status: ClaimVerificationStatus) => {
                console.log("Verification status:", status);
                setVerificationStatus(status);
                
                if (status.isVerified === true) {
                    setIsDrawerOpen(false);
                } else if (status.isVerified === undefined) {
                    setDrawerMessage("You need to verify your age to upgrade your Guardio Life subscription.");
                    setIsDrawerOpen(true);
                } else {
                    switch(status.workflowStatus) {
                        case WorkflowStatus.AWAITING_INPUT:
                            setDrawerMessage("Your age verification was interrupted. Please resume the verification to continue.");
                            break;
                        case WorkflowStatus.PROCESSING:
                            setDrawerMessage("Your age verification is in progress. Please check back later to complete your upgrade.");
                            break;
                        case WorkflowStatus.APPROVED:
                        case WorkflowStatus.DECLINED:
                        case WorkflowStatus.ABANDONED:
                            setDrawerMessage("Age verification failed. Please contact support for assistance.");
                            break;
                        case WorkflowStatus.REVIEW:
                            setDrawerMessage("Your age verification is under review. Please check back later.");
                            break;
                        case WorkflowStatus.ERROR:
                            setDrawerMessage("An error occurred during age verification. Please try again later or contact support.");
                            break;
                        default:
                            setDrawerMessage("Age verification status unclear. Please contact support for assistance.");
                    }
                    setIsDrawerOpen(true);
                }
            })
            .catch((error) => {
                console.error("Error verifying age:", error);
                navigate('/generic-error', { 
                    state: { 
                        message: "An error occurred while verifying your age. Please try again later or contact support."
                    }
                });
            })
            .then(() => {
                setIsLoading(false);
            });
    }, [config, navigate]);

    useEffect(() => {
        if (!state?.isAuthenticated) {
            signIn()
                .catch((error) => {
                    console.error("Error during sign-in:", error);
                    navigate('/generic-error', { 
                        state: { 
                            message: "An error occurred during sign-in. Please try again later."
                        }
                    });
                });
        } else if (location?.state?.idVerificationInitiated) {
            navigate("/verification-in-progress");
        } else {
            setIsLoading(true);
            checkVerificationStatus();
        }
    }, [state?.isAuthenticated, navigate, checkVerificationStatus, location, signIn]);

    /**
     * This useEffect tracks whether to display the age verification success message after 
     * the user completes age verification. The message will only be shown once to enhance 
     * user experience. If displayed previously, it will not appear again to prevent it 
     * from lingering at the top of the subscription page on subsequent visits.
     */
    useEffect(() => {
        const isAgeVerifiedSuccessMsgShown = localStorage.getItem(state.username + "_isAgeVerifiedSuccessMsgShown") === "true";

        if (!isAgeVerifiedSuccessMsgShown && verificationStatus?.isVerified === true) {
            setIsDrawerOpen(true)
            setDrawerMessageType("success")
            setDrawerMessage("Age verification is successful! You're all set to continue with the subscription.")
            localStorage.setItem(state.username + "_isAgeVerifiedSuccessMsgShown", "true")
        }
    },[verificationStatus])

    const handleVerifyAge = () => {
        if (verificationStatus?.workflowStatus === WorkflowStatus.AWAITING_INPUT) {
            navigate("/verify", { state: { reinitiate: true } });
        } else {
            navigate("/verify");
        }
    };

    if (isLoading) {
        return <div style={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center"}}>
        <LoadingSpinner />
    </div>;
    }

    return (
        <>
            <NavBar />
            <AgeVerificationDrawer
                isOpen={isDrawerOpen}
                setIsOpen={setIsDrawerOpen}
                verifyAge={handleVerifyAge}
                message={drawerMessage}
                type={drawerMessageType}
                showButton={verificationStatus?.isVerified === undefined || verificationStatus?.workflowStatus === WorkflowStatus.AWAITING_INPUT}
            />
            <Plans isAgeVerified={verificationStatus?.isVerified === true} setIsDrawerOpen={setIsDrawerOpen} />
            
            <Footer />
        </>
    );
};