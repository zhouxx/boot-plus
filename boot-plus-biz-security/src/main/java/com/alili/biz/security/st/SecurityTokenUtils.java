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

import com.alili.biz.security.SecurityBizProperties;
import com.alili.biz.security.TokenUtils;
import com.alili.biz.security.authentication.SecurityUser;
import com.alili.biz.security.domain.BizUser;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class SecurityTokenUtils extends TokenUtils {

    private CacheManager cacheManager;

    public SecurityTokenUtils(SecurityBizProperties securityBizProperties, CacheManager cacheManager) {
        super(securityBizProperties);
        this.cacheManager = cacheManager;
    }

    public String generateToken(Authentication authentication) {
        SecurityUser user = (SecurityUser) authentication.getPrincipal();
        BizUser bizUser = user.getBizUser();

        String token = UUID.randomUUID().toString();

        cacheManager.getCache("cacheSecurity").put(token, bizUser);

        return token;
    }

    public Authentication getAuthentication(String token) {
        BizUser bizUser = (BizUser) cacheManager.getCache("cacheSecurity").get(token, bizClass);

        Collection<? extends GrantedAuthority> authorities = Collections.emptyList();

        if(!CollectionUtils.isEmpty(bizUser.getRoles())) {
            authorities = bizUser.getRoles().stream().map(authority -> new SimpleGrantedAuthority(authority)).collect(Collectors.toList());
        };

        SecurityUser user = new SecurityUser(bizUser.getUsername(), bizUser.getPassword(), authorities);
        user.setBizUser(bizUser);

        return new UsernamePasswordAuthenticationToken(user, "", authorities);

    }

    public void removeToken(String token) {
        if(!StringUtils.isEmpty(token)) {
            cacheManager.getCache("cacheSecurity").evict(token);
        }
    }

    public boolean exist(String token) {
        Object security = cacheManager.getCache("cacheSecurity").get(token, bizClass);
        return security != null;
    }
}
