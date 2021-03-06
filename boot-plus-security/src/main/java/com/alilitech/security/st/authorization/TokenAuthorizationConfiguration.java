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

import com.alilitech.security.authorization.AuthorizationConfiguration;
import com.alilitech.security.st.SecurityTokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@ConditionalOnProperty(name="security.token.type", havingValue = "ST")
public class TokenAuthorizationConfiguration extends AuthorizationConfiguration {

    @Autowired
    private SecurityTokenUtils securityTokenUtils;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.addFilterBefore(new TokenAuthorizationFilter(securityTokenUtils, extensibleSecurity), FilterSecurityInterceptor.class);
    }

}
