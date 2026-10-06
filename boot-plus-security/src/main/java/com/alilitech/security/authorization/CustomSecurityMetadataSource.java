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
import com.alilitech.security.SecurityBizMessageSource;
import com.alilitech.security.SecurityBizProperties;
import com.alilitech.security.authentication.SecurityUser;
import com.alilitech.security.domain.BizResource;
import com.alilitech.security.domain.BizUser;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.lang.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class CustomSecurityMetadataSource {

    protected MessageSourceAccessor messages = SecurityBizMessageSource.getAccessor();

    private final ExtensibleSecurity extensibleSecurity;

    private final SecurityBizProperties securityBizProperties;

    private final LocaleResolver localeResolver;

    private final Map<RequestMatcher, Collection<String>> requestMatchersPermitAllMap = new HashMap<>();

    public CustomSecurityMetadataSource(ExtensibleSecurity extensibleSecurity, SecurityBizProperties securityBizProperties,@Nullable LocaleResolver localeResolver) {
        this.extensibleSecurity = extensibleSecurity;
        this.securityBizProperties = securityBizProperties;
        if(localeResolver == null) {
            this.localeResolver = new AcceptHeaderLocaleResolver();
        } else {
            this.localeResolver = localeResolver;
        }
    }

    @SuppressWarnings("java:S1168")
    public Collection<String> getAttributes(HttpServletRequest request) {
        Map<RequestMatcher, Collection<String>> metadataSource = getMetadataSource(request);
        for (Map.Entry<RequestMatcher, Collection<String>> entry : metadataSource.entrySet()) {
            RequestMatcher requestMatcher = entry.getKey();
            if (requestMatcher.matches(request)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private Map<RequestMatcher, Collection<String>> getMetadataSource(HttpServletRequest request) {

        //拿到用户，判断是否是最大权限
        //从上下文中取得用户对象，只需要解析一次token
        BizUser bizUser = ((SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getBizUser();

        //角色code
        Collection<String> roles = new ArrayList<>();
        //默认uri匹配
        RequestMatcher requestMatcher = PathPatternRequestMatcher.pathPattern(HttpMethod.valueOf(request.getMethod()), requestPath(request));

        //有最大权限的用户
        if(securityBizProperties.getPermitAllUserNames().contains(bizUser.getUsername())) {
            roles.add("ROLE_ALL");   //添加全部资源的角色
        }
        //不需要鉴权的url
        else if(isMatchRequest(request)) {
            return requestMatchersPermitAllMap;
        } else {
            //需要鉴权的
            BizResource bizResource = extensibleSecurity.obtainResource(request);

            if(bizResource == null) {
                throw new AccessDeniedException(messages.getMessage(
                        "Authorization.Failure",
                        new Object[] {request.getRequestURI()},
                        "Authorization failure, make sure you can get roles for resource of {0}!", localeResolver.resolveLocale(request)));
            } else {
                roles = bizResource.getRoles();
                requestMatcher = bizResource.getRequestMatcher();
            }
        }

        //为了强制鉴权
        if(roles.isEmpty()) {
            roles.add(UUID.randomUUID().toString());
        }

        Map<RequestMatcher, Collection<String>> ret = new HashMap<>();
        ret.put(requestMatcher, roles);
        return ret;
    }

    public Map<RequestMatcher, Collection<String>> getRequestMatchersPermitAllMap() {
        if(CollectionUtils.isEmpty(requestMatchersPermitAllMap)) {
            List<RequestMatcher> requestMatchers = securityBizProperties.getPermitAllPatterns().stream()
                    .map(pattern -> (RequestMatcher) PathPatternRequestMatcher.pathPattern(pattern.getMethod(), pattern.getPattern()))
                    .collect(Collectors.toList());
            for (RequestMatcher requestMatcher : requestMatchers) {
                requestMatchersPermitAllMap.put(requestMatcher, Collections.singletonList("ROLE_PUBLIC"));
            }
        }
        return requestMatchersPermitAllMap;
    }

    private boolean isMatchRequest(HttpServletRequest request) {
        Map<RequestMatcher, Collection<String>> requestMatchersPermitAllMapTmp = getRequestMatchersPermitAllMap();
        AtomicBoolean isMatch = new AtomicBoolean(false);
        requestMatchersPermitAllMapTmp.forEach((requestMatcher, configAttributes) -> {
            if(requestMatcher.matches(request)) {
                isMatch.set(true);
            }
        });
        return isMatch.get();
    }

    private String requestPath(HttpServletRequest request) {
        String path = request.getServletPath();
        if (request.getPathInfo() != null) {
            path = path + request.getPathInfo();
        }
        return (path == null || path.isEmpty()) ? "/" : path;
    }
}
