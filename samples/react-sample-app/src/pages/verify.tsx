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
import { useNavigate } from "react-router-dom";
import { completeVerification, initiateVerification } from "../api";
import { IdVResponseInterface } from "../model/identity-verification";
import { Footer, LoadingSpinner, NavBar } from "../components";

interface VerifyPageProps {
    setVerificationInitiated?: (isInitiated: boolean) => void;
    setShowVerificationWidget?: (show: boolean) => void;
}

export const VerifyPage: FunctionComponent<VerifyPageProps> = () => {

    const navigate = useNavigate();

    const [ loading, setLoading ] = useState<boolean>(false);
    const [ onfidoInstance, setOnfidoInstance ] = useState<OnfidoServ.SdkHandle>(null);

    const initIdentityVerification = async () => {
        setLoading(true);
        const response: IdVResponseInterface = await initiateVerification();
        const token = response?.claims?.[0]?.claimMetadata?.sdk_token;

        if (!token) {
            console.error("Token not found in response: ", response);
            navigate(
                '/generic-error',
                { state: { message: "Token not found in the init response from the Onfido server" } }
            );
        }

        const instance = OnfidoServ.init({
            useModal: false,
            token,
            onComplete: (data) => {
                completeVerification()
                // callback for when everything is complete
                console.log('Verification completed', data);
                navigate('/', { state: { idVerificationInitiated: true } });
            },
            steps: [
                {
                    type: 'welcome',
                    options: {
                        title: 'Verify your age',
                    },
                },
                'welcome',
                'document',
                'face',
                'complete',
            ],
        });

        setOnfidoInstance(instance);
        setLoading(false);
    }

    useEffect(() => {
        if (loading) {
            return;
        }

        initIdentityVerification()
            .catch((err) => {
                console.error('err:', err.message, err.request);
                navigate('/generic-error', { state: { message: err.message } });
            });

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
