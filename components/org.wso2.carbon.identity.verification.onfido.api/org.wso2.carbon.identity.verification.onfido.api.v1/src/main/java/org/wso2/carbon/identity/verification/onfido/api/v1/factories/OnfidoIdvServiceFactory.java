/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.identity.verification.onfido.api.v1.factories;

import org.wso2.carbon.extension.identity.verification.mgt.IdentityVerificationManager;
import org.wso2.carbon.extension.identity.verification.provider.IdVProviderManager;
import org.wso2.carbon.identity.verification.onfido.api.common.OnfidoIdvServiceHolder;
import org.wso2.carbon.identity.verification.onfido.api.v1.core.OnfidoIdvService;

/**
 * Factory class for OnfidoIdvService.
 */
public class OnfidoIdvServiceFactory {

    private static final OnfidoIdvService SERVICE;

    static {
        IdVProviderManager idvProviderManager = OnfidoIdvServiceHolder.getIdVProviderManager();
        IdentityVerificationManager
                identityVerificationManager = OnfidoIdvServiceHolder.getIdentityVerificationManager();

        if (idvProviderManager == null) {
            throw new IllegalStateException("IdVProviderManager is not available from OSGi context.");
        }

        if (identityVerificationManager == null) {
            throw new IllegalStateException("IdentityVerificationManager is not available from OSGi context.");
        }

        SERVICE = new OnfidoIdvService(idvProviderManager, identityVerificationManager);
    }

    /**
     * Get OnfidoIdvService.
     *
     * @return OnfidoIdvService.
     */
    public static OnfidoIdvService getOnfidoIdvService() {

        return SERVICE;
    }
}
