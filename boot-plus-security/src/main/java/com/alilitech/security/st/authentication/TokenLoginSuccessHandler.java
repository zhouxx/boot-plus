/*
 *    Copyright 2017-2022 the original author or authors.
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
package com.alilitech.security.st.authentication;

import com.alilitech.security.ExtensibleSecurity;
import com.alilitech.security.authentication.SecurityUser;
import com.alilitech.security.domain.BizUser;
import com.alilitech.security.st.SecurityTokenUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class TokenLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final SecurityTokenUtils securityTokenUtils;

    private final ExtensibleSecurity extensibleSecurity;

    public TokenLoginSuccessHandler(SecurityTokenUtils securityTokenUtils, ExtensibleSecurity extensibleSecurity) {
        this.securityTokenUtils = securityTokenUtils;
        this.extensibleSecurity = extensibleSecurity;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String token = securityTokenUtils.generateToken(authentication);
        BizUser bizUser = ((SecurityUser) authentication.getPrincipal()).getBizUser();
        extensibleSecurity.loginSuccess(request, response, token, bizUser);
    }
}
