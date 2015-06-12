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

package original.apache.http.impl.cookie;

import java.util.Collection;

import original.apache.http.params.HttpParams;

import original.apache.http.annotation.Immutable;
import original.apache.http.cookie.CookieSpec;
import original.apache.http.cookie.CookieSpecFactory;
import original.apache.http.cookie.CookieSpecProvider;
import original.apache.http.cookie.params.CookieSpecPNames;
import original.apache.http.protocol.HttpContext;

/**
 * {@link CookieSpecProvider} implementation that creates and initializes
 * {@link NetscapeDraftSpec} instances.
 *
 * @since 4.0
 */
@Immutable
@SuppressWarnings("deprecation")
public class NetscapeDraftSpecFactoryHC4 implements CookieSpecFactory, CookieSpecProvider {

    private final String[] datepatterns;

    public NetscapeDraftSpecFactoryHC4(final String[] datepatterns) {
        super();
        this.datepatterns = datepatterns;
    }

    public NetscapeDraftSpecFactoryHC4() {
        this(null);
    }

    public CookieSpec newInstance(final HttpParams params) {
        if (params != null) {

            String[] patterns = null;
            final Collection<?> param = (Collection<?>) params.getParameter(
                    CookieSpecPNames.DATE_PATTERNS);
            if (param != null) {
                patterns = new String[param.size()];
                patterns = param.toArray(patterns);
            }
            return new NetscapeDraftSpecHC4(patterns);
        } else {
            return new NetscapeDraftSpecHC4();
        }
    }

    public CookieSpec create(final HttpContext context) {
        return new NetscapeDraftSpecHC4(this.datepatterns);
    }

}