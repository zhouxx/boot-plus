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

import com.alilitech.security.SecurityBizMessageSource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Collection;
import java.util.Objects;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class CustomAccessDecisionManager {

    protected MessageSourceAccessor messages = SecurityBizMessageSource.getAccessor();

    private final LocaleResolver localeResolver;

    public CustomAccessDecisionManager(@Nullable LocaleResolver localeResolver) {
        if(localeResolver == null) {
            this.localeResolver = new AcceptHeaderLocaleResolver();
        } else {
            this.localeResolver = localeResolver;
        }
    }

    public void decide(Authentication authentication, HttpServletRequest request, Collection<String> requiredAuthorities) {
        String requestURI = request.getRequestURI();

        for (String needCode : requiredAuthorities) {
            if (authentication == null) {
                throw new AccessDeniedException(messages.getMessage(
                        "Authorization.NotAllowed",
                        new Object[]{requestURI},
                        "Authorization is not allowed for {0}!", localeResolver.resolveLocale(request)));
            }
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            for (GrantedAuthority authority : authorities) {
                if (Objects.equals(authority.getAuthority(), needCode)) {
                    return;
                }
            }
        }
        throw new AccessDeniedException(messages.getMessage(
                "Authorization.NotAllowed",
                new Object[] { requestURI },
                "Authorization is not allowed for {0}!", localeResolver.resolveLocale(request)));
    }
}
