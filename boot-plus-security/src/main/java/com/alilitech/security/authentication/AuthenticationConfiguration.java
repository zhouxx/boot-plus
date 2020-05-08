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
package com.alilitech.security.authentication;

import com.alilitech.security.ExtensibleSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@Order(1)
@Configuration
public class AuthenticationConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private AuthenticationSuccessHandler authenticationSuccessHandler;

    @Autowired
    private LoginFailureHandler loginFailureHandler;

    @Autowired
    private LogoutSuccessHandler logoutSuccessHandler;

    @Autowired
    protected ExtensibleSecurity extensibleSecurity;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        HttpSecurity httpSecurity = http.antMatcher("/authentication/*");

        httpSecurity.formLogin()
                .loginProcessingUrl("/authentication/login")
                .successHandler(authenticationSuccessHandler)
                .failureHandler(loginFailureHandler)
                ;
        http.logout().logoutUrl("/authentication/logout").logoutSuccessHandler(logoutSuccessHandler);

        extensibleSecurity.authenticationExtension(http);
    }
}
