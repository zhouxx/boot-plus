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
package com.alilitech.security.authorization;

import com.alilitech.security.ExtensibleSecurity;
import com.alilitech.security.SecurityBizProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.util.CollectionUtils;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@EnableConfigurationProperties({SecurityBizProperties.class})
public abstract class AuthorizationConfiguration {

    @Autowired
    protected ExtensibleSecurity extensibleSecurity;

    @Autowired
    private CustomAuthorizationManager customAuthorizationManager;

    @Autowired
    private AccessDeniedHandler accessDeniedHandler;

    @Autowired
    protected SecurityBizProperties securityBizProperties;

    @Bean
    protected WebSecurityCustomizer webSecurityCustomizer() {
        return web -> {
            if (!CollectionUtils.isEmpty(securityBizProperties.getIgnorePatterns())) {
                securityBizProperties.getIgnorePatterns().forEach(requestMatcher ->
                        web.ignoring().requestMatchers(requestMatcher.getMethod(), requestMatcher.getPattern()));
            }
        };
    }

    protected void configureAuthorization(HttpSecurity http) throws Exception {
        http.securityMatcher("/**")
                .authorizeHttpRequests(authorize -> authorize.anyRequest().access(customAuthorizationManager))
                .exceptionHandling(exception -> exception.accessDeniedHandler(accessDeniedHandler));
        extensibleSecurity.authorizationExtension(http);
    }
}
