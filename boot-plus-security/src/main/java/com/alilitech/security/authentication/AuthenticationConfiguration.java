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
package com.alilitech.security.authentication;

import com.alilitech.security.ExtensibleSecurity;
import com.alilitech.security.SecurityBizProperties;
import com.alilitech.security.authentication.vf.SecurityVirtualFilter;
import com.alilitech.security.authentication.vf.VirtualFilterDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */

public class AuthenticationConfiguration {

    @Autowired
    private AuthenticationSuccessHandler authenticationSuccessHandler;

    @Autowired
    private LoginFailureHandler loginFailureHandler;

    @Autowired
    private LogoutSuccessHandler logoutSuccessHandler;

    @Autowired
    protected ExtensibleSecurity extensibleSecurity;

    @Autowired
    protected SecurityBizProperties securityBizProperties;

    @Order(1)
    @Bean
    public SecurityFilterChain authenticationSecurityFilterChain(HttpSecurity http) throws Exception {
        String prefix = securityBizProperties.getAuthenticationPrefix();

        http.securityMatcher(prefix + "/**")
                .formLogin(form -> form
                        .loginProcessingUrl(prefix + "/login")
                        .successHandler(authenticationSuccessHandler)
                        .failureHandler(loginFailureHandler))
                .logout(logout -> logout
                        .logoutUrl(prefix + "/logout")
                        .logoutSuccessHandler(logoutSuccessHandler));

        List<VirtualFilterDefinition> virtualFilterDefinitions = new ArrayList<>();
        boolean overrideUsernamePasswordAuthenticationFilter = extensibleSecurity.addVirtualFilterDefinitions(virtualFilterDefinitions);

        if(!virtualFilterDefinitions.isEmpty()) {
            virtualFilterDefinitions.forEach(virtualFilterDefinition -> {
                SecurityVirtualFilter securityVirtualFilter = new SecurityVirtualFilter(virtualFilterDefinition);
                securityVirtualFilter.setAuthenticationFailureHandler(loginFailureHandler);
                securityVirtualFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
                http.addFilterBefore(securityVirtualFilter, UsernamePasswordAuthenticationFilter.class);
            });
        }

        // since 1.3.1
        // 返回true时，则替换默认的filter : UsernamePasswordAuthenticationFilter.class
        // 如果是false，一定要覆写认证和鉴权方法
        if(overrideUsernamePasswordAuthenticationFilter) {
            VirtualFilterDefinition virtualFilterDefinition = VirtualFilterDefinition.get().supportedPredicate((servletRequest, servletResponse) -> {
                // 此filter是否支持验证判断
                RequestMatcher requestMatcher = PathPatternRequestMatcher.pathPattern(HttpMethod.POST, prefix + "/login");
                return requestMatcher.matches((HttpServletRequest) servletRequest);
            }).authenticationFunction((servletRequest, servletResponse) -> {
                // 如何验证，验证失败时可以抛出AuthenticationException
                throw new AuthenticationServiceException("refuse");
            }).endAuthentication((authentication, servletRequest, servletResponse) -> {
                // 此filter认证成功后，是否结束整个流程的认证
                return true;
            }).alias("refused filter");

            http.addFilterAt(new SecurityVirtualFilter(virtualFilterDefinition), UsernamePasswordAuthenticationFilter.class);
        }

        extensibleSecurity.authenticationExtension(http);

        return http.build();
    }
}
