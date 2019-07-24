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
package com.alili.biz.security;

import com.alili.biz.security.authentication.LoginFailureHandler;
import com.alili.biz.security.authentication.UserAuthenticationService;
import com.alili.biz.security.authorization.CustomAccessDecisionManager;
import com.alili.biz.security.authorization.CustomSecurityMetadataSource;
import com.alili.biz.security.authorization.TokenAccessDeniedHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class SecurityConfiguration {

    //===============global=====================
    @Bean
    @ConditionalOnMissingBean
    public ExtensibleSecurity extensibleSecurity() {
        return new DefaultExtensibleSecurity();
    }

    //=============Authentication==========
    @Bean
    public LoginFailureHandler loginFailureHandler(ExtensibleSecurity extensibleSecurity) {
        return new LoginFailureHandler(extensibleSecurity);
    }

    @Bean
    public UserDetailsService userDetailsService(ExtensibleSecurity extensibleSecurity, SecurityBizProperties securityBizProperties) {
        return new UserAuthenticationService(extensibleSecurity, securityBizProperties);
    }

    //=============Authorization==========
    @Bean
    public AccessDeniedHandler accessDeniedHandler(ExtensibleSecurity extensibleSecurity) {
        return new TokenAccessDeniedHandler(extensibleSecurity);
    }

    @Bean
    public CustomAccessDecisionManager customAccessDecisionManager() {
        return new CustomAccessDecisionManager();
    }

    @Bean
    public CustomSecurityMetadataSource customSecurityMetadataSource(ExtensibleSecurity extensibleSecurity, CacheManager cacheManager, SecurityBizProperties securityBizProperties) {
        return new CustomSecurityMetadataSource(extensibleSecurity, cacheManager, securityBizProperties);
    }

}
