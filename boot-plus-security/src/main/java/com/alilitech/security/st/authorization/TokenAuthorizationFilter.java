/*
 *    Copyright 2017-2021 the original author or authors.
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
package com.alilitech.security.st.authorization;

import com.alilitech.security.ExtensibleSecurity;
import com.alilitech.security.SecurityBizMessageSource;
import com.alilitech.security.authentication.SecurityUser;
import com.alilitech.security.st.SecurityTokenUtils;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * attention: must not managed by Spring.If managed by Spring, ignore url will not take effect!
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class TokenAuthorizationFilter extends OncePerRequestFilter {

    protected MessageSourceAccessor messages = SecurityBizMessageSource.getAccessor();

    private final SecurityTokenUtils securityTokenUtils;

    private final ExtensibleSecurity extensibleSecurity;

    public TokenAuthorizationFilter(SecurityTokenUtils securityTokenUtils, ExtensibleSecurity extensibleSecurity) {
        this.extensibleSecurity = extensibleSecurity;
        this.securityTokenUtils = securityTokenUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        //Token is null
        if(token == null) {
            throw new AccessDeniedException(messages.getMessage(
                    "Token.Empty",
                    new Object[] {request.getRequestURI()},
                    "Token error, please check for {0}!"));
        }


        //token is exist
        if(!securityTokenUtils.exist(token)) {
            throw new AccessDeniedException(messages.getMessage(
                    "Token.Invalid",
                    new Object[] {request.getRequestURI()},
                    "Token error, please check for {0}!"));
        }

        //covert token to authentication
        Authentication authentication = securityTokenUtils.getAuthentication(token);

        //extension validateToken
        extensibleSecurity.validTokenExtension(token, ((SecurityUser) authentication.getPrincipal()).getBizUser(), request, response);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    /**
     * resolve token
     */
    private String resolveToken(HttpServletRequest request){
        return extensibleSecurity.resolveToken(request);
    }
}
