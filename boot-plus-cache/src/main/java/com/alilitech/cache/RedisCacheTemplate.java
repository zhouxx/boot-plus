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

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
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
public class RedisCacheTemplate extends AbstractCacheTemplate implements InitializingBean {

    @Autowired
    protected RedisTemplate<Object, Object> redisTemplate;

    @Override
    public Object read(String cacheName, Object key) {

        Map<String, Duration> ttiCacheMap = getTtiOrTtlCacheMap("tti");

        if(ttiCacheMap.containsKey(cacheName)) {
            Duration duration = ttiCacheMap.get(cacheName);
            return this.readAndExpire(cacheName, key, duration.toMillis(), TimeUnit.MILLISECONDS);
        } else {
            return super.read(cacheName, key);
        }
    }

    @Override
    public Object read(String cacheName, Object key, Class<?> clazz) {

        Map<String, Duration> ttiCacheMap = getTtiOrTtlCacheMap("tti");

        if(ttiCacheMap.containsKey(cacheName)) {
            RedisCache cache = (RedisCache) cacheManager.getCache(cacheName);
            Duration duration = ttiCacheMap.get(cacheName);
            Object ret = cache != null ? cache.get(key, clazz) : null;
            if(ret != null) {
                this.expire(cache.getCacheConfiguration().getKeyPrefixFor(cacheName) + key, duration.toMillis(), TimeUnit.MILLISECONDS);
            }
            return ret;
        } else {
            return super.read(cacheName, key, clazz);
        }
    }

    @Override
    public void write(String cacheName, Object key, Object value) {

        Map<String, Duration> ttlCacheMap = getTtiOrTtlCacheMap("ttl");
        Map<String, Duration> ttiCacheMap = getTtiOrTtlCacheMap("tti");

        if(ttlCacheMap.containsKey(cacheName)) {
            Duration duration = ttlCacheMap.get(cacheName);
            this.writeAndExpire(cacheName, key, value, duration.toMillis(), TimeUnit.MILLISECONDS);
        } else if(ttiCacheMap.containsKey(cacheName)) {
            Duration duration = ttiCacheMap.get(cacheName);
            this.writeAndExpire(cacheName, key, value, duration.toMillis(), TimeUnit.MILLISECONDS);
        }else {
            super.write(cacheName, key, value);
        }
    }

    @Override
    public Object readAndExpire(String cacheName, Object key, long timeout, TimeUnit unit) {
        RedisCache cache = (RedisCache) cacheManager.getCache(cacheName);
        Object ret = super.read(cacheName, key);
        if(ret != null && cache != null) {
            this.expire(cache.getCacheConfiguration().getKeyPrefixFor(cacheName) + key, timeout, unit);
        }
        return ret;
    }

    @Override
    public void writeAndExpire(String cacheName, Object key, Object value, long timeout, TimeUnit unit) {
        RedisCache cache = (RedisCache) cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
            this.expire(cache.getCacheConfiguration().getKeyPrefixFor(cacheName) + key, timeout, unit);
        }
    }

    @Override
    public void setTTLConfig(String cacheName, long timeout, TimeUnit unit) {
        long millis = TimeoutUtils.toMillis(timeout, unit);
        Duration duration = Duration.ofMillis(millis);

        Map<String, Duration> ttlCacheMap = getTtiOrTtlCacheMap("ttl");
        ttlCacheMap.put(cacheName, duration);
        putTtiOrTtlCacheMap("ttl", ttlCacheMap);
    }

    @Override
    public void setTTIConfig(String cacheName, long timeout, TimeUnit unit) {
        long millis = TimeoutUtils.toMillis(timeout, unit);
        Duration duration = Duration.ofMillis(millis);
        Map<String, Duration> ttiCacheMap = getTtiOrTtlCacheMap("tti");
        ttiCacheMap.put(cacheName, duration);
        putTtiOrTtlCacheMap("tti", ttiCacheMap);
    }

    private void expire(Object key, long timeout, TimeUnit unit) {
        boolean expire = redisTemplate.expire(key, timeout, unit);
        if(!expire) {
            logger.warn("redisTemplate failed to set TTL, make sure RedisSerializer is correct.");
        }
    }

    @Override
    public void clearTTLConfig(String cacheName) {
        Map<String, Duration> ttlCacheMap = getTtiOrTtlCacheMap("ttl");
        if(!CollectionUtils.isEmpty(ttlCacheMap)) {
            ttlCacheMap.remove(cacheName);
            putTtiOrTtlCacheMap("ttl", ttlCacheMap);
        }
    }

    @Override
    public void clearTTIConfig(String cacheName) {
        Map<String, Duration> ttiCacheMap = getTtiOrTtlCacheMap("tti");
        if(!CollectionUtils.isEmpty(ttiCacheMap)) {
            ttiCacheMap.remove(cacheName);
            putTtiOrTtlCacheMap("tti", ttiCacheMap);
        }
    }

    @Override
    public List<String> findKeys(String cacheName) {
        RedisCache cache = (RedisCache) cacheManager.getCache(cacheName);
        if (cache != null) {
            String prefix = cache.getCacheConfiguration().getKeyPrefixFor(cacheName);
            return redisTemplate.keys(prefix.concat("*")).stream().map(s -> s.toString().replaceFirst(prefix, "")).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        redisTemplate.setKeySerializer(new StringRedisSerializer());
    }

    /**
     * 基于redis的tti,ttl配置，都需要存在redis服务里，否则应用之间无法生效
     * 而且这个配置不能依赖于cacheManager,因为这个不是正常的业务
     * @param postfix
     * @return
     */
    private Map<String, Duration> getTtiOrTtlCacheMap(String postfix) {
        Map<String, Duration> cacheMap = (Map<String, Duration>)redisTemplate.opsForValue().get(TTI_TTL_CONFIG_CACHE_NAME + "::" + postfix);
        if(cacheMap == null) {
            cacheMap = new ConcurrentHashMap<>();
        }
        return cacheMap;
    }

    private void putTtiOrTtlCacheMap(String postfix, Map<String, Duration> cacheMap) {
        redisTemplate.opsForValue().set(TTI_TTL_CONFIG_CACHE_NAME + "::" + postfix, cacheMap);
    }
}
