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
package com.alilitech.security.authentication;

import com.alilitech.security.ExtensibleSecurity;
import com.alilitech.security.SecurityBizProperties;
import com.alilitech.security.authentication.vf.SecurityVirtualFilter;
import com.alilitech.security.authentication.vf.VirtualFilterDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@Order(1)
public class AuthenticationConfiguration extends WebSecurityConfigurerAdapter {

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

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        String prefix = securityBizProperties.getAuthenticationPrefix();

        HttpSecurity httpSecurity = http.antMatcher(prefix + "/**");

        httpSecurity.formLogin()
                .loginProcessingUrl(prefix + "/login")
                .successHandler(authenticationSuccessHandler)
                .failureHandler(loginFailureHandler)
                ;
        http.logout().logoutUrl(prefix + "/logout").logoutSuccessHandler(logoutSuccessHandler);

        List<VirtualFilterDefinition> virtualFilterDefinitions = new ArrayList<>();
        extensibleSecurity.addVirtualFilterDefinitions(virtualFilterDefinitions);

        if(virtualFilterDefinitions.size() > 0) {
            virtualFilterDefinitions.forEach(virtualFilterDefinition -> {
                SecurityVirtualFilter securityVirtualFilter = new SecurityVirtualFilter(virtualFilterDefinition);
                securityVirtualFilter.setAuthenticationFailureHandler(loginFailureHandler);
                securityVirtualFilter.setAuthenticationSuccessHandler(authenticationSuccessHandler);
                http.addFilterBefore(securityVirtualFilter, UsernamePasswordAuthenticationFilter.class);
            });
        }

        extensibleSecurity.authenticationExtension(http);
    }
}
