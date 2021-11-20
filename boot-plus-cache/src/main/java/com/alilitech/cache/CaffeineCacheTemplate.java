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
package com.alilitech.cache;

import com.alilitech.cache.support.CaffeineCacheManager;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.caffeine.CaffeineCache;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Zhou Xiaoxiang
 * @since 1.3.0
 */
public class CaffeineCacheTemplate extends AbstractCacheTemplate {

    private final Map<String, Caffeine<Object, Object>> caffeineMap = new ConcurrentHashMap<>();

    @Override
    public void setTTLConfig(String cacheName, long timeout, TimeUnit unit) {
        CaffeineCacheManager caffeineCacheManager = (CaffeineCacheManager) this.cacheManager;
        Caffeine<Object, Object> caffeine = null;
        if(caffeineMap.containsKey(cacheName)) {
            caffeine = caffeineMap.get(cacheName).expireAfterWrite(timeout, unit);
        } else {
            caffeine = Caffeine.newBuilder().expireAfterWrite(timeout, unit);
        }
        caffeineCacheManager.setCaffeine(cacheName, caffeine);
    }

    @Override
    public void setTTIConfig(String cacheName, long timeout, TimeUnit unit) {
        CaffeineCacheManager caffeineCacheManager = (CaffeineCacheManager) this.cacheManager;
        Caffeine<Object, Object> caffeine = null;
        if(caffeineMap.containsKey(cacheName)) {
            caffeine = caffeineMap.get(cacheName).expireAfterAccess(timeout, unit);
        } else {
            caffeine = Caffeine.newBuilder().expireAfterAccess(timeout, unit);
        }
        caffeineCacheManager.setCaffeine(cacheName, caffeine);
    }

    @Override
    public void clearTTLConfig(String cacheName) {
        CaffeineCacheManager caffeineCacheManager = (CaffeineCacheManager) this.cacheManager;
        Caffeine<Object, Object> caffeine = null;
        if(caffeineMap.containsKey(cacheName)) {
            caffeine = caffeineMap.get(cacheName).expireAfterWrite(0, TimeUnit.MILLISECONDS);
        } else {
            caffeine = Caffeine.newBuilder().expireAfterWrite(0, TimeUnit.MILLISECONDS);
        }
        caffeineCacheManager.setCaffeine(cacheName, caffeine);
    }

    @Override
    public void clearTTIConfig(String cacheName) {
        CaffeineCacheManager caffeineCacheManager = (CaffeineCacheManager) this.cacheManager;
        Caffeine<Object, Object> caffeine = null;
        if(caffeineMap.containsKey(cacheName)) {
            caffeine = caffeineMap.get(cacheName).expireAfterAccess(0, TimeUnit.MILLISECONDS);
        } else {
            caffeine = Caffeine.newBuilder().expireAfterAccess(0, TimeUnit.MILLISECONDS);
        }
        caffeineCacheManager.setCaffeine(cacheName, caffeine);
    }

    @Override
    public List<String> findKeys(String cacheName) {
        CaffeineCacheManager caffeineCacheManager = (CaffeineCacheManager) this.cacheManager;
        org.springframework.cache.Cache cache = caffeineCacheManager.getCache(cacheName);
        Cache<Object, Object> nativeCache = null;
        if (cache != null) {
            nativeCache = ((CaffeineCache) cache).getNativeCache();
        }
        return nativeCache != null ? nativeCache.asMap().keySet().stream().map(Object::toString).collect(Collectors.toList()) : Collections.emptyList();
    }
}
