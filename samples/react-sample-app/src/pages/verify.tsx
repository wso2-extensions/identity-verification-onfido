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

import React, { FunctionComponent, useEffect, useState } from "react";
import * as OnfidoServ from 'onfido-sdk-ui'
import { useNavigate, useLocation } from "react-router-dom";
import { completeVerification, initiateVerification, reinitiateVerification } from "../api";
import { IdVResponseInterface } from "../model/identity-verification";
import { Footer, LoadingSpinner, NavBar } from "../components";

interface VerifyPageProps {
    setVerificationInitiated?: (isInitiated: boolean) => void;
    setShowVerificationWidget?: (show: boolean) => void;
}

export const VerifyPage: FunctionComponent<VerifyPageProps> = () => {
    const navigate = useNavigate();
    const [loading, setLoading] = useState<boolean>(true);
    const [onfidoInstance, setOnfidoInstance] = useState<OnfidoServ.SdkHandle | null>(null);

    const location = useLocation();
    const reinitiate = location.state?.reinitiate === true;

    const initIdentityVerification = async () => {
        try {

            let response: IdVResponseInterface;

            if (reinitiate) {
                console.log("Reinitiating verification");
                response = await reinitiateVerification();
            } else {
                console.log("Initiating verification");
                response = await initiateVerification();
            }

            console.log("Verification response:", response);

            const token = response?.claims?.[0]?.claimMetadata?.sdk_token;
            const workflowRunId = response?.claims?.[0]?.claimMetadata?.onfido_workflow_run_id;

            if (!token || !workflowRunId) {
                const missingItem = !token ? "SDK token" : "Workflow run ID";
                throw new Error(`${missingItem} not found in the identity verification initiation response from the Identity server`);
            }

            const instance = OnfidoServ.init({
                useModal: false,
                token,
                onComplete: (data) => {
                    completeVerification();
                    console.log('Verification completed', data);
                    navigate('/', { state: { idVerificationInitiated: true } });
                },
                workflowRunId
            });
            setOnfidoInstance(instance);
        } catch (error) {
            let ErrorMessage;
            if (error.response?.status === 400 && error.response?.data?.code === "OIDV-10002") {
                ErrorMessage = `The initiation of the identity verification encountered an issue. Please proceed with the following steps to resolve it:
                    1. Log in to your WSO2 MyAccount.
                    2. Check if the following required attributes are set and have valid values:
                    - Date of Birth
                    - First Name
                    - Last Name
                    3. If any of these attributes are missing or incorrect, please update them in your WSO2 MyAccount.
                    If you've confirmed all attributes are correct and the issue persists, please contact support for further assistance.`;
            } else {
                ErrorMessage = error.response?.data?.description || error.message || "An unexpected error occurred during the initiation of the identity verification. Please try again later or contact support.";
            }
            navigate('/generic-error', { state: { message: ErrorMessage } });
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        initIdentityVerification();

        // method to clean up the onfido instance
        return () => {
            onfidoInstance && onfidoInstance.tearDown()
        }
    }, [])

    return (
        <>
            <NavBar/>
            <div id="onfido-mount">
                {loading && <LoadingSpinner/>}
            </div>
            <Footer/>
        </>
    );
}
