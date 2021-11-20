/*
 *    Copyright 2017-2021 the original author or authors.
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
package com.alilitech.security.jwt;

import com.alilitech.security.ExtensibleSecurity;
import com.alilitech.security.st.SecurityTokenUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class BlackListManager {

    protected final Log logger = LogFactory.getLog(getClass());

    public static final String TOKEN_BLACK_LIST = "black_list";

    private CacheManager cacheManager;

    private JwtTokenUtils jwtTokenUtils;

    private ExtensibleSecurity extensibleSecurity;

    public BlackListManager(CacheManager cacheManager, JwtTokenUtils jwtTokenUtils, ExtensibleSecurity extensibleSecurity) {
        this.cacheManager = cacheManager;
        this.jwtTokenUtils = jwtTokenUtils;
        this.extensibleSecurity = extensibleSecurity;
    }

    /**
     * add and refresh black list
     * @param request
     */
    public void addAndRefreshBlackList(HttpServletRequest request) {
        //add to black list
        String token = extensibleSecurity.resolveToken(request);

        Cache cache = cacheManager.getCache(SecurityTokenUtils.SECURITY_CACHE_NAME);

        if(cache == null) {
            logger.error(SecurityTokenUtils.SECURITY_CACHE_NAME + " cache does not exist!");
            return;
        }

        ArrayList<String> cacheSecurity = cache.get(TOKEN_BLACK_LIST, ArrayList.class);

        if(cacheSecurity == null) {
            cacheSecurity = new ArrayList<>();
        }

        cacheSecurity.add(token);

        //refresh black list
        ArrayList<String> finalCacheSecurity = cacheSecurity;
        cacheSecurity.forEach(o -> {
            if(!jwtTokenUtils.validateToken(o)) {
                finalCacheSecurity.remove(o);
            }
        });

        cache.put(TOKEN_BLACK_LIST, cacheSecurity);
    }

    /**
     * is in black list
     * @param token
     * @return
     */
    public boolean inBlackList(String token) {

        Cache cache = cacheManager.getCache(SecurityTokenUtils.SECURITY_CACHE_NAME);

        if(cache == null) {
            logger.error(SecurityTokenUtils.SECURITY_CACHE_NAME + " cache does not exist!");
            return false;
        }

        ArrayList<String> cacheSecurity = cache.get(TOKEN_BLACK_LIST, ArrayList.class);

        if(cacheSecurity == null) {
            return false;
        }

        return cacheSecurity.contains(token);
    }

}
