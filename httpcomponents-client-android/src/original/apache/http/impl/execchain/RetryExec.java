/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package original.apache.http.impl.execchain;

import java.io.IOException;

import org.kman.apache.http.logging.Logger;
import original.apache.http.Header;
import original.apache.http.HttpException;
import original.apache.http.NoHttpResponseException;
import original.apache.http.annotation.Immutable;
import original.apache.http.client.HttpRequestRetryHandler;
import original.apache.http.client.NonRepeatableRequestException;
import original.apache.http.client.methods.CloseableHttpResponse;
import original.apache.http.client.methods.HttpExecutionAware;
import original.apache.http.client.methods.HttpRequestWrapper;
import original.apache.http.client.protocol.HttpClientContext;
import original.apache.http.conn.routing.HttpRoute;
import original.apache.http.util.Args;

/**
 * Request executor in the request execution chain that is responsible
 * for making a decision whether a request failed due to an I/O error
 * should be re-executed.
 * <p/>
 * Further responsibilities such as communication with the opposite
 * endpoint is delegated to the next executor in the request execution
 * chain.
 *
 * @since 4.3
 */
@Immutable
public class RetryExec implements ClientExecChain {

    private final static String TAG = "HttpClient";

    private final ClientExecChain requestExecutor;
    private final HttpRequestRetryHandler retryHandler;

    public RetryExec(
            final ClientExecChain requestExecutor,
            final HttpRequestRetryHandler retryHandler) {
        Args.notNull(requestExecutor, "HTTP request executor");
        Args.notNull(retryHandler, "HTTP request retry handler");
        this.requestExecutor = requestExecutor;
        this.retryHandler = retryHandler;
    }

    public CloseableHttpResponse execute(
            final HttpRoute route,
            final HttpRequestWrapper request,
            final HttpClientContext context,
            final HttpExecutionAware execAware) throws IOException, HttpException {
        Args.notNull(route, "HTTP route");
        Args.notNull(request, "HTTP request");
        Args.notNull(context, "HTTP context");
        final Header[] origheaders = request.getAllHeaders();
        for (int execCount = 1;; execCount++) {
            try {
                return this.requestExecutor.execute(route, request, context, execAware);
            } catch (final IOException ex) {
                if (execAware != null && execAware.isAborted()) {
                    if (Logger.isLoggable(TAG, Logger.DEBUG)) {
                        Logger.d(TAG, "Request has been aborted");
                    }
                    throw ex;
                }
                if (retryHandler.retryRequest(ex, execCount, context)) {
                    if (Logger.isLoggable(TAG, Logger.INFO)) {
                        Logger.i(TAG, "I/O exception ("+ ex.getClass().getName() +
                                ") caught when processing request to "
                                + route +
                                ": "
                                + ex.getMessage());
                    }
                    if (Logger.isLoggable(TAG, Logger.DEBUG)) {
                        Logger.d(TAG, ex.getMessage(), ex);
                    }
                    if (!RequestEntityProxy.isRepeatable(request)) {
                        if (Logger.isLoggable(TAG, Logger.DEBUG)) {
                            Logger.d(TAG, "Cannot retry non-repeatable request");
                        }
                        final NonRepeatableRequestException nreex = new NonRepeatableRequestException(
                                "Cannot retry request with a non-repeatable request entity");
                        nreex.initCause(ex);
                    }
                    request.setHeaders(origheaders);
                    if (Logger.isLoggable(TAG, Logger.INFO)) {
                        Logger.i(TAG, "Retrying request to " + route);
                    }
                } else {
                    if (ex instanceof NoHttpResponseException) {
                        final NoHttpResponseException updatedex = new NoHttpResponseException(
                                route.getTargetHost().toHostString() + " failed to respond");
                        updatedex.setStackTrace(ex.getStackTrace());
                        throw updatedex;
                    } else {
                        throw ex;
                    }
                }
            }
        }
    }

}
