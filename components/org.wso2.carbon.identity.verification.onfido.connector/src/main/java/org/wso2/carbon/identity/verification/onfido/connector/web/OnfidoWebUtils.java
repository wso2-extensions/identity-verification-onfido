/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
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

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.wso2.carbon.identity.verification.onfido.connector.exception.OnfidoClientException;
import org.wso2.carbon.identity.verification.onfido.connector.exception.OnfidoServerException;

import java.io.IOException;
import java.net.UnknownHostException;

import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.APPLICATION_JSON;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_IDENTITY_VERIFICATION;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.ErrorMessage.ERROR_INVALID_BASE_URL;
import static org.wso2.carbon.identity.verification.onfido.connector.constants.OnfidoConstants.TOKEN_HEADER;

/**
 * The OnfidoWebUtils class contains all the general helper functions required by the Onfido connector.
 */
public class OnfidoWebUtils {

    private OnfidoWebUtils() {

    }

    /**
     * Send an HTTP POST request.
     *
     * @param apiToken    API token provided by Onfido.
     * @param requestURL  The URL to which the POST request should be sent.
     * @param requestBody A hashmap that includes the parameters to be sent through the request.
     * @return httpResponse         The response received from the HTTP call.
     * @throws OnfidoServerException Exception thrown when an error occurred with the
     *                               HTTP client connection.
     */
    public static HttpResponse httpPost(String apiToken, String requestURL, String requestBody)
            throws OnfidoServerException, OnfidoClientException {

        HttpPost request = new HttpPost(requestURL);
        request.addHeader(HttpHeaders.AUTHORIZATION, TOKEN_HEADER + apiToken);
        request.addHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
        request.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

        CloseableHttpClient client = HTTPClientManager.getInstance().getHttpClient();
        try (CloseableHttpResponse response = client.execute(request)) {
            return toHttpResponse(response);
        } catch (UnknownHostException e) {
            throw new OnfidoClientException(ERROR_INVALID_BASE_URL.getCode(),
                    ERROR_INVALID_BASE_URL.getMessage(), e);
        } catch (IOException e) {
            throw new OnfidoServerException(ERROR_IDENTITY_VERIFICATION.getCode(),
                    ERROR_IDENTITY_VERIFICATION.getMessage(), e);
        }
    }

    /**
     * Send an HTTP PUT request.
     *
     * @param apiToken    API token provided by Onfido.
     * @param requestURL  The URL to which the POST request should be sent.
     * @param requestBody A hashmap that includes the parameters to be sent through the request.
     * @return httpResponse         The response received from the HTTP call.
     * @throws OnfidoServerException Exception thrown when an error occurred with the HTTP
     *                               client connection.
     */
    public static HttpResponse httpPut(String apiToken, String requestURL, String requestBody)
            throws OnfidoServerException, OnfidoClientException {

        HttpPut request = new HttpPut(requestURL);
        request.addHeader(HttpHeaders.AUTHORIZATION, TOKEN_HEADER + apiToken);
        request.addHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
        request.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

        CloseableHttpClient client = HTTPClientManager.getInstance().getHttpClient();
        try (CloseableHttpResponse response = client.execute(request)) {
            return toHttpResponse(response);
        } catch (UnknownHostException e) {
            throw new OnfidoClientException(ERROR_INVALID_BASE_URL.getCode(),
                    ERROR_INVALID_BASE_URL.getMessage(), e);
        } catch (IOException e) {
            throw new OnfidoServerException(ERROR_IDENTITY_VERIFICATION.getCode(),
                    ERROR_IDENTITY_VERIFICATION.getMessage(), e);
        }
    }

    /**
     * Send an HTTP PUT request.
     *
     * @param apiToken   API token provided by Onfido.
     * @param requestURL The URL to which the POST request should be sent.
     * @return httpResponse         The response received from the HTTP call.
     * @throws OnfidoServerException Exception thrown when an error occurred with the HTTP
     *                               client connection.
     */
    public static HttpResponse httpGet(String apiToken, String requestURL)
            throws OnfidoServerException, OnfidoClientException {

        HttpGet request = new HttpGet(requestURL);
        request.addHeader(HttpHeaders.AUTHORIZATION, TOKEN_HEADER + apiToken);
        request.addHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);

        CloseableHttpClient client = HTTPClientManager.getInstance().getHttpClient();
        try (CloseableHttpResponse response = client.execute(request)) {
            return toHttpResponse(response);
        } catch (UnknownHostException e) {
            throw new OnfidoClientException(ERROR_INVALID_BASE_URL.getCode(),
                    ERROR_INVALID_BASE_URL.getMessage(), e);
        } catch (IOException e) {
            throw new OnfidoServerException(ERROR_IDENTITY_VERIFICATION.getCode(),
                    ERROR_IDENTITY_VERIFICATION.getMessage(), e);
        }
    }

    private static HttpResponse toHttpResponse(CloseableHttpResponse response) throws IOException {

        HttpResponse result = new BasicHttpResponse(response.getStatusLine());
        if (response.getEntity() != null) {
            result.setEntity(new BufferedHttpEntity(response.getEntity()));
        }
        return result;
    }
}
