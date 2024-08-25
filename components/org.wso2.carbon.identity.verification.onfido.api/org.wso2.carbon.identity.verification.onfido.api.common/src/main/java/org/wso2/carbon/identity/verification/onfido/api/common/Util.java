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

package org.wso2.carbon.identity.verification.onfido.api.common;

import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.UUID;

/**
 * Util class.
 */
public class Util {

    /**
     * Get correlation id of current thread.
     *
     * @return Correlation-id.
     */
    public static String getCorrelation() {

        if (isCorrelationIDPresent()) {
            return MDC.get(Constants.CORRELATION_ID_MDC);
        }
        return UUID.randomUUID().toString();
    }

    /**
     * Check whether correlation id present in the log MDC.
     *
     * @return whether the correlation id is present.
     */
    public static boolean isCorrelationIDPresent() {

        return MDC.get(Constants.CORRELATION_ID_MDC) != null;
    }

    /**
     * Retrieves the tenant ID from the current context.
     *
     * @return The tenant ID.
     */
    public static int getTenantId() {

        String tenantDomain = IdentityTenantUtil.getTenantDomainFromContext();

        if (StringUtils.isBlank(tenantDomain)) {
            tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        return IdentityTenantUtil.getTenantId(tenantDomain);
    }
}
