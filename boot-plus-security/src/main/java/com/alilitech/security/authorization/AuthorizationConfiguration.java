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
package com.alilitech.security.authorization;

import com.alilitech.security.ExtensibleSecurity;
import com.alilitech.security.SecurityBizProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.util.StringUtils;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@EnableConfigurationProperties({SecurityBizProperties.class})
public abstract class AuthorizationConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private CustomSecurityMetadataSource customSecurityMetadataSource;

    @Autowired
    protected ExtensibleSecurity extensibleSecurity;

    @Autowired
    private CustomAccessDecisionManager customAccessDecisionManager;

    @Autowired
    private AccessDeniedHandler accessDeniedHandler;

    @Autowired
    protected SecurityBizProperties securityBizProperties;

    @Override
    public void configure(WebSecurity web) {
        //ignore urls
        if(!StringUtils.isEmpty(securityBizProperties.getIgnorePatterns())) {
            securityBizProperties.getIgnorePatterns().forEach(requestMatcher -> web.ignoring().antMatchers(requestMatcher.getMethod(), requestMatcher.getPattern()));
        }
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.antMatcher("/**").authorizeRequests()
                .anyRequest().authenticated()
                .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
                    public <O extends FilterSecurityInterceptor> O postProcess(
                            O fsi) {
                        fsi.setSecurityMetadataSource(customSecurityMetadataSource);
                        fsi.setAccessDecisionManager(customAccessDecisionManager);
                        return fsi;
                    }
                });

        http.exceptionHandling().accessDeniedHandler(accessDeniedHandler);

        extensibleSecurity.authorizationExtension(http);
    }
}
