/*
 *    Copyright 2017-2026 the original author or authors.
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

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Replaces the removed {@code FilterSecurityInterceptor} + {@code AccessDecisionManager} chain.
 *
 * @author Zhou Xiaoxiang
 * @since 2.2.0
 */
public class CustomAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final CustomSecurityMetadataSource customSecurityMetadataSource;

    private final CustomAccessDecisionManager customAccessDecisionManager;

    public CustomAuthorizationManager(CustomSecurityMetadataSource customSecurityMetadataSource,
                                      CustomAccessDecisionManager customAccessDecisionManager) {
        this.customSecurityMetadataSource = customSecurityMetadataSource;
        this.customAccessDecisionManager = customAccessDecisionManager;
    }

    @Override
    public AuthorizationResult authorize(Supplier<? extends Authentication> authentication, RequestAuthorizationContext context) {
        HttpServletRequest request = context.getRequest();
        Collection<String> attributes = customSecurityMetadataSource.getAttributes(request);
        if (CollectionUtils.isEmpty(attributes)) {
            return new AuthorizationDecision(true);
        }
        customAccessDecisionManager.decide(authentication.get(), request, attributes);
        return new AuthorizationDecision(true);
    }
}
