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

import com.haulmont.cuba.core.global.PasswordEncryption;
import com.haulmont.cuba.security.auth.LoginPasswordCredentials;
import com.haulmont.cuba.security.global.LoginException;
import com.haulmont.cuba.web.auth.AuthInfo;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * {@link LoginProvider} that authenticates the user based on provided login and password.
 * In most cases it should be the last called provider.
 * If it's not the last provider, please override the {@link #tryToAuthenticate(AuthInfo)} method.
 */
@Component("cuba_LoginPasswordProvider")
public class LoginPasswordLoginProvider extends AbstractLoginProvider implements Ordered {

    @Inject
    protected PasswordEncryption passwordEncryption;

    @Override
    protected boolean tryToAuthenticate(AuthInfo authInfo) throws LoginException {

        getConnection().login(
                new LoginPasswordCredentials(
                        authInfo.getLogin(),
                        passwordEncryption.getPlainHash(authInfo.getPassword()),
                        authInfo.getLocale()
                )
        );

        return true;
    }

    @Override
    public int getOrder() {
        return HIGHEST_PLATFORM_PRECEDENCE + 30;
    }
}