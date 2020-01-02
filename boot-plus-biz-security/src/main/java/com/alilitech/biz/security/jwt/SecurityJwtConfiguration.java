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
package com.alilitech.biz.security.jwt;

import com.alilitech.biz.security.ExtensibleSecurity;
import com.alilitech.biz.security.SecurityBizProperties;
import com.alilitech.biz.security.SecurityConfiguration;
import com.alilitech.biz.security.jwt.authentication.JwtLoginSuccessHandler;
import com.alilitech.biz.security.jwt.authentication.JwtLogoutSuccessHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@ConditionalOnProperty(name="security.token.type", havingValue = "JWT")
@Configuration
@EnableCaching
public class SecurityJwtConfiguration extends SecurityConfiguration {

    //===============global=====================
    @Bean
    public JwtTokenUtils jwtTokenUtils(SecurityBizProperties securityBizProperties) {
        return new JwtTokenUtils(securityBizProperties);
    }

    @Bean
    public BlackListManager blackListManager(CacheManager cacheManager, JwtTokenUtils jwtTokenUtils, ExtensibleSecurity extensibleSecurity) {
        return new BlackListManager(cacheManager, jwtTokenUtils, extensibleSecurity);
    }

    //=============Authentication==========
    @Bean
    public JwtLoginSuccessHandler loginSuccessHandler(JwtTokenUtils jwtTokenUtils, ExtensibleSecurity extensibleSecurity) {
        return new JwtLoginSuccessHandler(jwtTokenUtils, extensibleSecurity);
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler(BlackListManager blackListManager, ExtensibleSecurity extensibleSecurity) {
        return new JwtLogoutSuccessHandler(extensibleSecurity, blackListManager);
    }



}
