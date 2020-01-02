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
package com.alilitech.biz.security;

import com.alilitech.biz.security.domain.BizResource;
import com.alilitech.biz.security.domain.BizUser;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public interface ExtensibleSecurity<T> {

    String HEADER_NAME = "Authorization";

    //====================token extension===================

    /**
     * validation extension
     * @param bizUser biz user
     * @param request
     * @param response
     * @return
     * @throws AccessDeniedException when validate error
     */
    default void validateToken(BizUser bizUser, HttpServletRequest request, HttpServletResponse response) throws AccessDeniedException {

    }

    //=======================Authentication===================
    /**
     * login success handler
     * @param request
     * @param response
     * @param token
     * @throws IOException
     */
    default void loginSuccess(HttpServletRequest request, HttpServletResponse response, String token, BizUser bizUser) throws IOException {
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.getWriter().println(token);
    }

    /**
     * login failure handler
     * @param request
     * @param response
     * @param message
     * @throws IOException
     */
    default void loginFailure(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.getWriter().println(message);
    }

    /**
     * logout success handler
     * @param request
     * @param response
     * @throws IOException
     */
    default void logoutSuccess(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().println("logout success");
    }

    /**
     * load user by username
     * @param username just login key
     * @param maxAuthUser not need authorization user
     * @return
     */
    default <T extends BizUser> T loadUserByUsername(String username, boolean maxAuthUser) {
        List<String> roles = new ArrayList<>(Arrays.asList("USER"));
        BizUser bizUser = new BizUser(username, roles);
        bizUser.setUserId("1");
        return (T) bizUser;
    }


    //=====================Authorization========================

    /**
     * resolve token from request
     * @param request
     * @return
     */
    default String resolveToken(HttpServletRequest request) {
        String token = request.getHeader(HEADER_NAME);
        return token;
    }

    /**
     * obtain resource by request, when need(not max authorization user and not permit all uri)
     * @param request
     * @return
     */
    default BizResource obtainResource(HttpServletRequest request) {
        RequestMatcher requestMatcher = new AntPathRequestMatcher("/**", request.getMethod());
        BizResource bizResource = new BizResource(requestMatcher);
        bizResource.setRoles(new ArrayList<>(Arrays.asList("USER")));
        return bizResource;
    }

    /**
     * authorization failure handler
     * @param request
     * @param response
     * @param message
     * @throws IOException
     */
    default void authorizationFailure(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.getWriter().println(message);
    }


}
