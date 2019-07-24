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
package com.alili.biz.security.authorization;

import com.alili.biz.security.SecurityBizMessageSource;
import org.apache.commons.codec.binary.StringUtils;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.FilterInvocation;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class CustomAccessDecisionManager implements AccessDecisionManager {

    protected MessageSourceAccessor messages = SecurityBizMessageSource.getAccessor();

    @Override
    public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes) throws AccessDeniedException, InsufficientAuthenticationException {

        FilterInvocation fi = (FilterInvocation) object;
        String requestURI = fi.getHttpRequest().getRequestURI();

        Iterator<ConfigAttribute> iterator = configAttributes.iterator();
        while (iterator.hasNext()) {
            if (authentication == null) {
                throw new AccessDeniedException(messages.getMessage(
                        "Authorization.NotAllowed",
                        new Object[] { requestURI },
                        "Authorization is not allowed for {0}!"));
            }
            ConfigAttribute configAttribute = iterator.next();
            String needCode = configAttribute.getAttribute();
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            for (GrantedAuthority authority : authorities) {
                if (StringUtils.equals(authority.getAuthority(),  needCode)) {
                    return;
                }
            }
        }
        throw new AccessDeniedException(messages.getMessage(
                "Authorization.NotAllowed",
                new Object[] { requestURI },
                "Authorization is not allowed for {0}!"));
    }

    public boolean supports(ConfigAttribute attribute) {
        return true;
    }

    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }
}
