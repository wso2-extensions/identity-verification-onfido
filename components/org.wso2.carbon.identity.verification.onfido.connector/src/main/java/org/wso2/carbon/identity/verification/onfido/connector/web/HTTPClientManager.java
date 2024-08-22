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

package org.wso2.carbon.identity.verification.onfido.connector.web;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.wso2.carbon.identity.verification.onfido.connector.exception.OnfidoServerException;

import java.io.IOException;

import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_CREATING_HTTP_CLIENT;

/**
 * Manages HTTP client connections for the Onfido connector.
 * This class implements the singleton pattern to ensure only one instance of the HTTP client is created.
 */
public class HTTPClientManager {

    private static final int HTTP_CONNECTION_TIMEOUT = 3000;
    private static final int HTTP_READ_TIMEOUT = 3000;
    private static final int HTTP_CONNECTION_REQUEST_TIMEOUT = 3000;
    private static final int DEFAULT_MAX_CONNECTIONS = 20;
    private static volatile HTTPClientManager httpClientManagerInstance;
    private final CloseableHttpClient httpClient;

    /**
     * Private constructor to prevent direct instantiation.
     *
     * @param httpClient The CloseableHttpClient instance to be managed.
     */
    private HTTPClientManager(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Gets the singleton instance of HTTPClientManager.
     * If the instance doesn't exist, it creates one.
     *
     * @return The singleton instance of HTTPClientManager.
     * @throws OnfidoServerException If there's an error creating the HTTP client.
     */
    public static HTTPClientManager getInstance() throws OnfidoServerException {
        if (httpClientManagerInstance == null) {
            synchronized (HTTPClientManager.class) {
                if (httpClientManagerInstance == null) {
                    httpClientManagerInstance = createInstance();
                }
            }
        }
        return httpClientManagerInstance;
    }

    /**
     * Creates a new instance of HTTPClientManager.
     *
     * @return A new instance of HTTPClientManager.
     * @throws OnfidoServerException If there's an error creating the HTTP client.
     */
    private static HTTPClientManager createInstance() throws OnfidoServerException {
        try {
            PoolingHttpClientConnectionManager connectionManager = createPoolingConnectionManager();
            RequestConfig config = createRequestConfig();
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setDefaultRequestConfig(config)
                    .setConnectionManager(connectionManager)
                    .build();
            return new HTTPClientManager(httpClient);
        } catch (IOException e) {
            throw new OnfidoServerException(ERROR_CREATING_HTTP_CLIENT.getCode(),
                    ERROR_CREATING_HTTP_CLIENT.getMessage(), e);
        }
    }

    /**
     * Gets the managed HTTP client instance.
     *
     * The httpClient is initialized in the private constructor and marked as final,
     * ensuring it's always set when an HTTPClientManager instance is created. The only way to instantiate
     * HTTPClientManager is through the getInstance() method, which internally calls createInstance().
     * If createInstance() fails to create a CloseableHttpClient, it throws an OnfidoServerException,
     * and no HTTPClientManager instance is created. Thus, if an HTTPClientManager instance exists,
     * httpClient is guaranteed to be non-null.
     *
     * @return The CloseableHttpClient instance.
     */
    public CloseableHttpClient getHttpClient() throws OnfidoServerException {
        return httpClient;
    }

    /**
     * Creates a RequestConfig instance with predefined timeout settings.
     *
     * @return A configured RequestConfig instance.
     */
    private static RequestConfig createRequestConfig() {
        return RequestConfig.custom()
                .setConnectTimeout(HTTP_CONNECTION_TIMEOUT)
                .setConnectionRequestTimeout(HTTP_CONNECTION_REQUEST_TIMEOUT)
                .setSocketTimeout(HTTP_READ_TIMEOUT)
                .setRedirectsEnabled(false)
                .setRelativeRedirectsAllowed(false)
                .build();
    }

    /**
     * Creates a PoolingHttpClientConnectionManager with predefined connection limits.
     *
     * @return A configured PoolingHttpClientConnectionManager instance.
     * @throws IOException If there's an error creating the connection manager.
     */
    private static PoolingHttpClientConnectionManager createPoolingConnectionManager() throws IOException {
        PoolingHttpClientConnectionManager poolingHttpClientConnectionMgr = new PoolingHttpClientConnectionManager();
        // Increase max total connection to 20.
        poolingHttpClientConnectionMgr.setMaxTotal(DEFAULT_MAX_CONNECTIONS);
        // Increase default max connection per route to 20.
        poolingHttpClientConnectionMgr.setDefaultMaxPerRoute(DEFAULT_MAX_CONNECTIONS);
        return poolingHttpClientConnectionMgr;
    }
}
