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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.concurrent.TimeUnit;

/**
 * @author Zhou Xiaoxiang
 * @since 1.3.0
 */
public abstract class AbstractCacheTemplate implements CacheTemplate<Object, Object> {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected CacheManager cacheManager;

    protected static final String TTI_TTL_CONFIG_CACHE_NAME = "tti_ttl";

    @Override
    public Object read(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        Cache.ValueWrapper valueWrapper = cache != null ? cache.get(key) : null;
        return valueWrapper != null ? valueWrapper.get() : null;
    }

    @Override
    public Object read(String cacheName, Object key, Class<?> clazz) {
        Cache cache = cacheManager.getCache(cacheName);
        return cache != null ? cache.get(key, clazz) : null;
    }

    @Override
    public Object readAndExpire(String cacheName, Object key, long duration, TimeUnit unit) {
        return this.read(cacheName, key);
    }

    @Override
    public void write(String cacheName, Object key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
        }
    }

    @Override
    public void writeAndExpire(String cacheName, Object key, Object value, long duration, TimeUnit unit) {
        this.write(cacheName,key, value);
    }

    @Override
    public void invalid(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }
}
