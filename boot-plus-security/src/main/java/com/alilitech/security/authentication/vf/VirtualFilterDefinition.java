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
package com.alilitech.security.authentication.vf;

import com.alilitech.security.authentication.SecurityUser;
import com.alilitech.security.domain.BizUser;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * @author Zhou Xiaoxiang
 * @since 1.2.6
 */
public class VirtualFilterDefinition {

    private static volatile AtomicInteger autoIndex = new AtomicInteger(0);

    private String alias;

    private BiPredicate<ServletRequest, ServletResponse> supportedPredicate;

    private P2FunctionWithException<ServletRequest, ServletResponse, Authentication> authenticationFunction;

    private P3Predicate<Authentication, ServletRequest, ServletResponse> endAuthentication;

    public VirtualFilterDefinition(String alias) {
        this.alias = alias;
    }

    public static VirtualFilterDefinition get() {
        return new VirtualFilterDefinition("Filter."+ autoIndex.incrementAndGet());
    }

    public static VirtualFilterDefinition getDefault() {
        VirtualFilterDefinition virtualFilterDefinition = get();

        virtualFilterDefinition.supportedPredicate(((servletRequest, servletResponse) -> true))
                .endAuthentication((authentication, servletRequest, servletResponse) -> true)
                .authenticationFunction((servletRequest, servletResponse) -> {

                    // ... to do valid authentication, valid success then return authentication, other wise throw AuthenticationException

                    // build Authentication
                    String username = servletRequest.getParameter("username");
                    List<String> roles = new ArrayList<>(Arrays.asList("ROLE_PUBLIC"));
                    return VirtualFilterDefinition.buildSimpleAuthentication(username, roles);
        });

        return virtualFilterDefinition;

    }

    public static Authentication buildSimpleAuthentication(String username, List<String> Authorities) {
        BizUser bizUser = new BizUser(username, Authorities);

        //构建UserDetails
        List<GrantedAuthority> authorities = Authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        SecurityUser securityUser = new SecurityUser(username, authorities);
        securityUser.setBizUser(bizUser);

        // 构建AuthenticationToken
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                securityUser, null, authorities);

        return authentication;
    }

    public String getAlias() {
        return alias;
    }

    public VirtualFilterDefinition alias(String alias) {
        this.alias = alias;
        return this;
    }

    public BiPredicate<ServletRequest, ServletResponse> getSupportedPredicate() {
        return supportedPredicate;
    }

    public VirtualFilterDefinition supportedPredicate(BiPredicate<ServletRequest, ServletResponse> supportedPredicate) {
        this.supportedPredicate = supportedPredicate;
        return this;
    }

    public P2FunctionWithException<ServletRequest, ServletResponse, Authentication> getAuthenticationFunction() {
        return authenticationFunction;
    }

    public VirtualFilterDefinition authenticationFunction(P2FunctionWithException<ServletRequest, ServletResponse, Authentication> authenticationFunction) {
        this.authenticationFunction = authenticationFunction;
        return this;
    }

    public P3Predicate<Authentication, ServletRequest, ServletResponse> getEndAuthentication() {
        return endAuthentication;
    }

    public VirtualFilterDefinition endAuthentication(P3Predicate<Authentication, ServletRequest, ServletResponse> endAuthentication) {
        this.endAuthentication = endAuthentication;
        return this;
    }

}
