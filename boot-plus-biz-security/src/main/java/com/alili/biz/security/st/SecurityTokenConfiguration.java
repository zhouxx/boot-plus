/**
 *    Copyright 2017-2019 the original author or authors.
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
package com.alili.biz.security.st;

import com.alili.biz.security.ExtensibleSecurity;
import com.alili.biz.security.SecurityBizProperties;
import com.alili.biz.security.SecurityConfiguration;
import com.alili.biz.security.st.authentication.TokenLoginSuccessHandler;
import com.alili.biz.security.st.authentication.TokenLogoutSuccessHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@ConditionalOnProperty(name="security.token.type", havingValue = "ST")
@Configuration
@EnableCaching
public class SecurityTokenConfiguration extends SecurityConfiguration {

    //===============global=====================
    @Bean
    public SecurityTokenUtils tokenUtils(SecurityBizProperties securityTokenProperties, CacheManager cacheManager) {
        return new SecurityTokenUtils(securityTokenProperties, cacheManager);
    }

    //=============Authentication==========
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler(SecurityTokenUtils securityTokenUtils, ExtensibleSecurity extensibleSecurity) {
        return new TokenLoginSuccessHandler(securityTokenUtils, extensibleSecurity);
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler(SecurityTokenUtils securityTokenUtils, ExtensibleSecurity extensibleSecurity) {
        return new TokenLogoutSuccessHandler(securityTokenUtils, extensibleSecurity);
    }



}
