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
package com.alilitech.security.jwt.authentication;

import com.alilitech.security.authentication.SecurityUser;
import com.alilitech.security.domain.BizUser;
import com.alilitech.security.jwt.BlackListManager;
import com.alilitech.security.ExtensibleSecurity;
import com.alilitech.security.jwt.JwtTokenUtils;
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

    private final JwtTokenUtils jwtTokenUtils;

    public JwtLogoutSuccessHandler(ExtensibleSecurity extensibleSecurity, BlackListManager blackListManager, JwtTokenUtils jwtTokenUtils) {
        this.extensibleSecurity = extensibleSecurity;
        this.blackListManager = blackListManager;
        this.jwtTokenUtils = jwtTokenUtils;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        blackListManager.addAndRefreshBlackList(request);

        String token = extensibleSecurity.resolveToken(request);
        if(authentication == null) {
            authentication = jwtTokenUtils.getAuthentication(token);
        }
        BizUser bizUser = ((SecurityUser) authentication.getPrincipal()).getBizUser();
        extensibleSecurity.logoutSuccess(request, response, bizUser);
    }
}
