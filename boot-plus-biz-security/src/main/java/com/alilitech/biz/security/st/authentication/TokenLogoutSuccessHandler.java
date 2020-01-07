/**
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
package com.alilitech.biz.security.st.authentication;

import com.alilitech.biz.security.ExtensibleSecurity;
import com.alilitech.biz.security.st.SecurityTokenUtils;
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
public class TokenLogoutSuccessHandler implements LogoutSuccessHandler {

    private ExtensibleSecurity extensibleSecurity;

    private SecurityTokenUtils securityTokenUtils;

    public TokenLogoutSuccessHandler(SecurityTokenUtils securityTokenUtils, ExtensibleSecurity extensibleSecurity) {
        this.extensibleSecurity = extensibleSecurity;
        this.securityTokenUtils = securityTokenUtils;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String token = extensibleSecurity.resolveToken(request);
        securityTokenUtils.removeToken(token);
        extensibleSecurity.logoutSuccess(request, response);
    }
}