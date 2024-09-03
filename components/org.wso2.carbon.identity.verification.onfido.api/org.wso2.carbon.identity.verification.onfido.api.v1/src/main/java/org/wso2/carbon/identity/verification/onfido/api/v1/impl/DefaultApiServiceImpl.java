/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.identity.verification.onfido.api.v1.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.wso2.carbon.identity.verification.onfido.api.v1.DefaultApiService;
import org.wso2.carbon.identity.verification.onfido.api.v1.core.OnfidoIdvService;
import org.wso2.carbon.identity.verification.onfido.api.v1.model.VerifyRequest;

import javax.ws.rs.core.Response;

/**
 * This class implements the default api service.
 */
public class DefaultApiServiceImpl implements DefaultApiService {

    @Autowired
    OnfidoIdvService onfidoIdvService;

    @Override
    public Response verify(String xSHA2Signature, String idvpId, VerifyRequest verifyRequest) {

        onfidoIdvService.verify(xSHA2Signature, idvpId, verifyRequest);
        return Response.ok().build();
    }
}
