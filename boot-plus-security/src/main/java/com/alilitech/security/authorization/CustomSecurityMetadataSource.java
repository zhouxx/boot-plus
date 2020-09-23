/*
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
import com.alilitech.security.SecurityBizMessageSource;
import com.alilitech.security.SecurityBizProperties;
import com.alilitech.security.authentication.SecurityUser;
import com.alilitech.security.domain.BizResource;
import com.alilitech.security.domain.BizUser;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class CustomSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {

    protected MessageSourceAccessor messages = SecurityBizMessageSource.getAccessor();

    private final ExtensibleSecurity extensibleSecurity;

    private final SecurityBizProperties securityBizProperties;

    private final Map<RequestMatcher, Collection<ConfigAttribute>> requestMatchersPermitAllMap = new HashMap<>();

    public CustomSecurityMetadataSource(ExtensibleSecurity extensibleSecurity, SecurityBizProperties securityBizProperties) {
        this.extensibleSecurity = extensibleSecurity;
        this.securityBizProperties = securityBizProperties;
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        FilterInvocation fi = (FilterInvocation) object;
        Map<RequestMatcher, Collection<ConfigAttribute>> metadataSource = getMetadataSource(fi.getHttpRequest());
        for (Map.Entry<RequestMatcher, Collection<ConfigAttribute>> entry : metadataSource.entrySet()) {
            RequestMatcher requestMatcher = entry.getKey();
            if (requestMatcher.matches(fi.getHttpRequest())) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return FilterInvocation.class.isAssignableFrom(clazz);
    }

    private Map<RequestMatcher, Collection<ConfigAttribute>> getMetadataSource(HttpServletRequest request) {

        //拿到用户，判断是否是最大权限
        //String token = extensibleSecurity.resolveToken(request);
        //BizUser bizUser = (BizUser) cacheManager.getCache("cacheSecurity").get(token, securityBizProperties.getBizUserClass());
        //从上下文中取得用户对象，只需要解析一次token
        BizUser bizUser = ((SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getBizUser();

        //角色code
        Collection<String> roles = new ArrayList<>();
        //默认uri匹配
        RequestMatcher requestMatcher = new AntPathRequestMatcher(request.getRequestURI(), request.getMethod());

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
                        "Authorization failure, make sure you can get roles for resource of {0}!"));
            } else {
                roles = bizResource.getRoles();
                requestMatcher = bizResource.getRequestMatcher();
            }
        }

        //为了强制鉴权
        if(roles.isEmpty()) {
            roles.add(UUID.randomUUID().toString());
        }

        Collection<ConfigAttribute> configAttributes = new ArrayList<>();
        roles.forEach(roleCode -> {
            ConfigAttribute configAttribute = new SecurityConfig(roleCode);
            configAttributes.add(configAttribute);
        });

        Map<RequestMatcher, Collection<ConfigAttribute>> ret = new HashMap<>();
        ret.put(requestMatcher, configAttributes);
        return ret;
    }

    public Map<RequestMatcher, Collection<ConfigAttribute>> getRequestMatchersPermitAllMap() {
        if(CollectionUtils.isEmpty(requestMatchersPermitAllMap)) {
            List<AntPathRequestMatcher> requestMatchers = securityBizProperties.getPermitAllPatterns().stream().map(requestMatcher -> new AntPathRequestMatcher(requestMatcher.getPattern(), requestMatcher.getMethod().toString())).collect(Collectors.toList());
            for(RequestMatcher requestMatcher : requestMatchers) {
                ConfigAttribute configAttribute = new SecurityConfig("ROLE_PUBLIC");
                requestMatchersPermitAllMap.put(requestMatcher, Collections.singletonList(configAttribute));
            }
        }
        return requestMatchersPermitAllMap;
    }

    private boolean isMatchRequest(HttpServletRequest request) {
        Map<RequestMatcher, Collection<ConfigAttribute>> requestMatchersPermitAllMap = getRequestMatchersPermitAllMap();
        AtomicBoolean isMatch = new AtomicBoolean(false);
        requestMatchersPermitAllMap.forEach((requestMatcher, configAttributes) -> {
            if(requestMatcher.matches(request)) {
                isMatch.set(true);
            }
        });
        return isMatch.get();
    }
}
