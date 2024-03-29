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
package com.alilitech.security.jwt.authorization;

import com.alilitech.security.jwt.BlackListManager;
import com.alilitech.security.ExtensibleSecurity;
import com.alilitech.security.jwt.JwtTokenUtils;
import com.alilitech.security.authentication.SecurityUser;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.SpringSecurityMessageSource;
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
public class JwtTokenAuthorizationFilter extends OncePerRequestFilter {

    private static final String TOKEN_ERROR = "Token.Error";

    private static final String TOKEN_ERROR_MSG = "Token error, please check for {0}!";

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    private ExtensibleSecurity extensibleSecurity;

    private JwtTokenUtils jwtTokenUtils;

    private BlackListManager blackListManager;

    public JwtTokenAuthorizationFilter(JwtTokenUtils jwtTokenUtils, ExtensibleSecurity extensibleSecurity, BlackListManager blackListManager) {
        this.extensibleSecurity = extensibleSecurity;
        this.jwtTokenUtils = jwtTokenUtils;
        this.blackListManager = blackListManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        //Token is null
        if(token == null) {
            throw new AccessDeniedException(messages.getMessage(
                    TOKEN_ERROR,
                    new Object[] {request.getRequestURI()},
                    TOKEN_ERROR_MSG));
        }

        //token is in black list
        if(blackListManager.inBlackList(token)) {
            throw new AccessDeniedException(messages.getMessage(
                    TOKEN_ERROR,
                    new Object[] {request.getRequestURI()},
                    TOKEN_ERROR_MSG));
        }

        //validate token
        if(jwtTokenUtils.validateToken(token) ) {

            //covert token to authentication
            Authentication authentication = jwtTokenUtils.getAuthentication(token);

            //extension validateToken
            extensibleSecurity.validTokenExtension(token, ((SecurityUser) authentication.getPrincipal()).getBizUser(), request, response);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            //refresh token
            if(jwtTokenUtils.compareExpireTime(token)) {
                String newToken = jwtTokenUtils.refreshToken(token);
                response.addHeader(ExtensibleSecurity.HEADER_NAME, newToken);
            }

        } else {
            throw new AccessDeniedException(messages.getMessage(
                    TOKEN_ERROR,
                    new Object[] {request.getRequestURI()},
                    TOKEN_ERROR_MSG));
        }

        filterChain.doFilter(request, response);

    }

    /**
     * resolve token
     * @param request
     * @return token
     */
    private String resolveToken(HttpServletRequest request){
        return extensibleSecurity.resolveToken(request);
    }
}
