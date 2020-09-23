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
package com.alilitech.security;

import com.alilitech.security.authentication.vf.VirtualFilterDefinition;
import com.alilitech.security.domain.BizResource;
import com.alilitech.security.domain.BizUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public interface ExtensibleSecurity {

    String HEADER_NAME = "Authorization";

    //====================token extension===================

    /**
     * validation extension
     * @param bizUser biz user
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws AccessDeniedException when validate error
     */
    default void validTokenExtension(String token, BizUser bizUser, HttpServletRequest request, HttpServletResponse response) throws AccessDeniedException {

    }

    //=======================Authentication===================

    /**
     * config/add {@link VirtualFilterDefinition}s for Authentication
     * @param virtualFilterDefinitions
     */
    default void addVirtualFilterDefinitions(List<VirtualFilterDefinition> virtualFilterDefinitions) {

    }

    /**
     * login success handler
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param token token
     * @throws IOException IOException
     */
    default void loginSuccess(HttpServletRequest request, HttpServletResponse response, String token, BizUser bizUser) throws IOException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().println(token);
    }

    /**
     * login failure handler
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    default void loginFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().println(exception.getMessage());
    }

    /**
     * logout success handler
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @throws IOException IOException
     */
    default void logoutSuccess(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().println("logout success");
    }

    /**
     * load user by username
     * @param username just login key
     * @param maxAuthUser not need authorization user
     * @return biz user
     */
    default <T extends BizUser> T loadUserByUsername(String username, boolean maxAuthUser) {
        List<String> roles = new ArrayList<>(Collections.singletonList("USER"));
        BizUser bizUser = new BizUser(username, username, roles);
        bizUser.setUserId("1");
        return (T) bizUser;
    }


    //=====================Authorization========================

    /**
     * resolve token from request
     * @param request HttpServletRequest
     * @return token
     */
    default String resolveToken(HttpServletRequest request) {
        return request.getHeader(HEADER_NAME);
    }

    /**
     * obtain resource by request, when need(not max authorization user and not permit all uri)
     * @param request HttpServletRequest
     * @return biz resource
     */
    default BizResource obtainResource(HttpServletRequest request) {
        RequestMatcher requestMatcher = new AntPathRequestMatcher("/**", request.getMethod());
        BizResource bizResource = new BizResource(requestMatcher);
        bizResource.setRoles(new ArrayList<>(Collections.singletonList("USER")));
        return bizResource;
    }

    /**
     * authorization failure handler
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param message message
     * @throws IOException IOException
     */
    default void authorizationFailure(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().println(message);
    }

    //=======================original extension===================
    default void authenticationExtension(HttpSecurity http) throws Exception {
        http.cors();
        http.sessionManagement().disable();
        http.csrf().disable();
    }

    //=======================original extension===================
    default void authorizationExtension(HttpSecurity http) throws Exception {
        http.cors();
        http.sessionManagement().disable();
        http.csrf().disable();
        http.logout().disable();
        http.formLogin().disable();
        http.anonymous().disable();
        http.securityContext().disable();
        http.requestCache().disable();
    }

}
