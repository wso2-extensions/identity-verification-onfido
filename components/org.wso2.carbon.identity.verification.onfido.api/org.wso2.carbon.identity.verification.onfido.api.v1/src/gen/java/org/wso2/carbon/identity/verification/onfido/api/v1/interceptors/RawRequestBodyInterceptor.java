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

package org.wso2.carbon.identity.verification.onfido.api.v1.interceptors;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * An interceptor that reads and stores the raw request body of incoming HTTP requests in a ThreadLocal variable.
 * This allows the raw request body to be accessed later in the processing chain, even after it has been consumed.
 */
public class RawRequestBodyInterceptor extends AbstractPhaseInterceptor<Message> {

    // ThreadLocal variable to store the raw request body for each thread
    private static final ThreadLocal<String> threadLocalRawRequestBody = new ThreadLocal<>();

    public RawRequestBodyInterceptor() {

        super(Phase.READ);
    }

    /**
     * Handles the incoming message by reading the raw request body from the InputStream,
     * storing it in a ThreadLocal variable, and resetting the InputStream for further processing.
     *
     * @param message The CXF message containing the HTTP request.
     * @throws Fault If an error occurs while reading the InputStream or processing the message.
     */
    @Override
    public void handleMessage(Message message) throws Fault {

        InputStream is = message.getContent(InputStream.class);
        if (is != null) {
            try {
                // Convert the InputStream to a String and store it in ThreadLocal
                String rawRequestBody = readInputStream(is);
                threadLocalRawRequestBody.set(rawRequestBody);

                // Reset the InputStream for CXF to process it again
                message.setContent(InputStream.class,
                        new ByteArrayInputStream(rawRequestBody.getBytes(StandardCharsets.UTF_8)));
            } catch (IOException e) {
                throw new Fault(e);
            }
        }
    }

    /**
     * Reads the content of an InputStream and converts it to a String.
     *
     * @param is The InputStream to be read.
     * @return The content of the InputStream as a String.
     * @throws IOException If an error occurs while reading the InputStream.
     */
    private String readInputStream(InputStream is) throws IOException {

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            char[] buffer = new char[1024];
            int length;
            while ((length = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, length);
            }
        }
        return sb.toString();
    }

    /**
     * Retrieves the raw request body stored in the ThreadLocal variable.
     * This method should be called to access the raw request body during processing.
     *
     * @return The raw request body as a String, or null if not set.
     */
    public static String getRawRequestBody() {

        return threadLocalRawRequestBody.get();
    }

    /**
     * Clears the ThreadLocal variable that stores the raw request body.
     * This method should be called after the request has been fully processed to avoid memory leaks.
     */
    public static void clear() {

        threadLocalRawRequestBody.remove();
    }
}
