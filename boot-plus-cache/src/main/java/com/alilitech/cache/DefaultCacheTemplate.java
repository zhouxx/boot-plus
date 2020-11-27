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
package com.alilitech.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Zhou Xiaoxiang
 * @since 1.3.0
 */
public class DefaultCacheTemplate extends AbstractCacheTemplate {

    @Override
    public void setTTLConfig(String cacheName, long timeout, TimeUnit unit) {

    }

    @Override
    public void setTTIConfig(String cacheName, long timeout, TimeUnit unit) {

    }

    @Override
    public void clearTTLConfig(String cacheName) {

    }

    @Override
    public void clearTTIConfig(String cacheName) {

    }

    @Override
    public List<String> findKeys(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        ConcurrentMap<Object, Object> nativeCache = new ConcurrentHashMap<>();
        if(cache instanceof ConcurrentMapCache) {
            ConcurrentMapCache concurrentMapCache = (ConcurrentMapCache) cache;
            nativeCache = concurrentMapCache.getNativeCache();
        }
        return nativeCache.keySet().stream().map(Object::toString).collect(Collectors.toList());
    }
}
