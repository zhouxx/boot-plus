/*
 *    Copyright 2017-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.alilitech.security.jwt.authentication;

import com.alilitech.security.jwt.BlackListManager;
import com.alilitech.security.ExtensibleSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class JwtLogoutSuccessHandler implements LogoutSuccessHandler {

    private final ExtensibleSecurity extensibleSecurity;

    private final BlackListManager blackListManager;

    public JwtLogoutSuccessHandler(ExtensibleSecurity extensibleSecurity, BlackListManager blackListManager) {
        this.extensibleSecurity = extensibleSecurity;
        this.blackListManager = blackListManager;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        blackListManager.addAndRefreshBlackList(request);

        extensibleSecurity.logoutSuccess(request, response);
    }
}
