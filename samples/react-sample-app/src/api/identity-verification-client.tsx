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

import { HttpRequestConfig, HttpResponse } from "@asgardeo/auth-react";
import { IdVClaim, SdkFlowStatus, IdVResponseInterface, ClaimVerificationStatus } from "../model/identity-verification";
import { IdVConstants } from "../constants";
import { default as authConfig } from "../config.json";
import { AsgardeoSPAClient } from "@asgardeo/auth-react";

const httpClient = AsgardeoSPAClient.getInstance();

export const initiateVerification = async (): Promise<IdVResponseInterface> => {
    return changeVerificationStatus(SdkFlowStatus.INITIATED)
}

export const completeVerification = async (): Promise<IdVResponseInterface> => {
    return changeVerificationStatus(SdkFlowStatus.COMPLETED);
}

const changeVerificationStatus = async (status: SdkFlowStatus): Promise<IdVResponseInterface> => {

    const requestConfig: HttpRequestConfig = {
        url: `${authConfig?.baseUrl}/api/users/v1/me/idv/verify`,
        method: IdVConstants.HTTP_POST,
        headers: {
            'Content-Type': IdVConstants.APPLICATION_JSON,
            'Accept': IdVConstants.APPLICATION_JSON
        },
        data: {
            "idVProviderId": authConfig?.identityVerificationProviderId,
            "claims": [
                "http://wso2.org/claims/dob",
                "http://wso2.org/claims/givenname",
                "http://wso2.org/claims/lastname"
            ],
            "properties": [
                {
                    "key": "status",
                    "value": status
                }
            ]
        }
    }

    return httpClient.httpRequest(requestConfig)
        .then((response: HttpResponse<IdVResponseInterface>) => {
            return response.data;
        })
        .catch((error) => {
            throw error;
        });
}

export const isClaimVerified = async (claimToVerify: string): Promise<ClaimVerificationStatus> => {

    const requestConfig: HttpRequestConfig = {
        url: `${authConfig?.baseUrl}/api/users/v1/me/idv/claims/`,
        method: IdVConstants.HTTP_GET,
        headers: {
            'Content-Type': IdVConstants.URL_ENCODED,
            'Accept': IdVConstants.APPLICATION_JSON
        },
        params: {
            "idVProviderId": authConfig?.identityVerificationProviderId ?? "",
        }
    }

    return httpClient.httpRequest(requestConfig)
        .then((response: HttpResponse<IdVClaim[]>) => {
            const claims = response.data as IdVClaim[];
            const claim = claims.find((claim) => claim.uri === claimToVerify);
            console.log("Found claim : ", claim );
            return getClaimVerificationStatus(claim);
        })
        .catch((error) => {
            throw error;
        });
}

function getClaimVerificationStatus(claim: IdVClaim | undefined): ClaimVerificationStatus {
    if (!claim) {
        return { isVerified: undefined, workflowStatus: undefined };
    }
    return {
        isVerified: claim.isVerified,
        workflowStatus: claim.claimMetadata.onfido_workflow_status
    };
}
