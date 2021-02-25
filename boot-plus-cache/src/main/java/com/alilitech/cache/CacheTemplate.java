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

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Zhou Xiaoxiang
 * @since 1.3.0
 */
public interface CacheTemplate<K, V> {

    /**
     * read value
     * @param cacheName
     * @param key
     * @return
     */
    V read(String cacheName, K key);


    /**
     * read value
     * @param cacheName
     * @param key
     * @param clazz
     * @return
     */
    V read(String cacheName, K key, Class<?> clazz);

    /**
     * read and expire cache
     * @param cacheName
     * @param key
     * @return
     */
    V readAndExpire(String cacheName, K key, long duration, TimeUnit unit);

    /**
     * write value
     * @param cacheName
     * @param key
     * @param value
     */
    void write(String cacheName, K key, V value);

    /**
     * write/set and expire cache
     * @param cacheName
     * @param key
     * @param value
     */
    void writeAndExpire(String cacheName, K key, V value, long timeout, TimeUnit unit);

    /**
     * invalid cache
     * @param cacheName
     * @param key
     */
    void invalid(String cacheName, K key);

    /**
     * set ttl policy on cache
     * @param cacheName
     * @param timeout
     * @param unit
     */
    void setTTLConfig(String cacheName, long timeout, TimeUnit unit);

    /**
     * set tti policy on cache
     * @param cacheName
     * @param timeout
     * @param unit
     */
    void setTTIConfig(String cacheName, long timeout, TimeUnit unit);

    /**
     * clear ttl policy
     * @param cacheName
     */
    void clearTTLConfig(String cacheName);

    /**
     * clear tti policy
     * @param cacheName
     */
    void clearTTIConfig(String cacheName);

    /**
     * get the keys by cache name
     * @param cacheName
     * @return
     */
    List<String> findKeys(String cacheName);

}
