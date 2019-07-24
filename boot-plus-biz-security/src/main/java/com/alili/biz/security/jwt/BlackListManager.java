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
package com.alili.biz.security.jwt;

import com.alili.biz.security.ExtensibleSecurity;
import org.springframework.cache.CacheManager;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class BlackListManager {

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

        ArrayList cacheSecurity = cacheManager.getCache("cacheSecurity").get(TOKEN_BLACK_LIST, ArrayList.class);

        if(cacheSecurity == null) {
            cacheSecurity = new ArrayList();
        }

        cacheSecurity.add(token);

        //refresh black list
        ArrayList finalCacheSecurity = cacheSecurity;
        cacheSecurity.forEach(o -> {
            String tempToken = (String)o;
            if(!jwtTokenUtils.validateToken(tempToken)) {
                finalCacheSecurity.remove(o);
            };
        });

        cacheManager.getCache("cacheSecurity").put(TOKEN_BLACK_LIST, cacheSecurity);
    }

    /**
     * is in black list
     * @param token
     * @return
     */
    public boolean inBlackList(String token) {
        ArrayList cacheSecurity = cacheManager.getCache("cacheSecurity").get(TOKEN_BLACK_LIST, ArrayList.class);

        if(cacheSecurity == null) {
            return false;
        }

        return cacheSecurity.contains(token);
    }

}
