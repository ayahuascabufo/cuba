/*
 * Copyright (c) 2008-2017 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.cuba.web.auth.provider;

import com.haulmont.cuba.security.global.LoginException;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.Connection;
import com.haulmont.cuba.web.auth.AuthInfo;

/**
 * {@link LoginProvider} that implements the "Chain of Responsibility" pattern.
 * It checks if the user is already authenticated or not.
 * If the user is not yet authenticated it checks if it can authenticate him.
 * Regardless of the outcome it passes authorization details to a next LoginProvider.
 *
 * Provider can implement {@link #afterAll(boolean, AuthInfo)} to put there some logic that
 *  has to be called after all providers had their chance to analyze authentication info.
 *
 * Defining and initializing the next provider is a responsibility of a system that uses
 *  login providers.
 */
abstract public class AbstractLoginProvider implements LoginProvider {

    protected LoginProvider nextLoginProvider;

    @Override
    public final boolean process(boolean authenticated, AuthInfo authInfo) throws LoginException {

        boolean result = authenticated;

        before(result, authInfo);

        if (!authenticated) {
            result = tryToAuthenticate(authInfo);
        }

        after(result, authInfo);

        if (nextLoginProvider != null) {
            result = nextLoginProvider.process(result, authInfo);
        }

        afterAll(authenticated, authInfo);

        return result;
    }

    public void setNextLoginProvider(LoginProvider nextLoginProvider) {
        this.nextLoginProvider = nextLoginProvider;
    }

    /**
     * Authenticate the user if possible.
     *
     * @param authInfo          input provided by the user
     * @return                  whether the method is succeeded to authorize the user
     * @throws LoginException   if the input provided by the user is incorrect
     */
    abstract protected boolean tryToAuthenticate(AuthInfo authInfo) throws LoginException;

    protected App getApp() {
        return App.getInstance();
    }

    protected Connection getConnection() {
        return getApp().getConnection();
    }

    protected void before(boolean authenticated, AuthInfo authInfo) {}

    protected void after(boolean authenticated, AuthInfo authInfo) {}

    /**
     * This hook can be used to place there some logic that should be executed
     *  no matter if the user is authorized or not.
     * This method is guaranteed to be called after all Login Providers had a chance to authorize a user.
     */
    protected void afterAll(boolean authenticated, AuthInfo authInfo) {}

}