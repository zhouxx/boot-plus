/*
 *    Copyright 2017-present the original author or authors.
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
package com.alilitech.web.jackson.ser.dict;

import com.alilitech.web.jackson.DictCollector;
import com.alilitech.web.support.MessageResourceCollection;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Zhou Xiaoxiang
 * @since 2.0.2
 */
public class DictLocaleCache {

    /**
     * save all dicts and values
     */
    private static final MessageResourceCollection CACHED = MessageResourceCollection.newBuilder().build();

    /**
     * key: dict-key, value: {@link DictCollector} object
     */
    private static final Map<String, DictCollector> KEY_COLLECTOR_MAPPINGS = new ConcurrentHashMap<>();

    private static final Map<DictCollector, Set<String>> COLLECTOR_KEYS_MAPPINGS = new HashMap<>();

    private static final ReentrantReadWriteLock READ_WRITE_LOCK = new ReentrantReadWriteLock();

    public static void refreshAll(MessageResourceCollection messageResourceCollectionTemp, Map<String, DictCollector> keyCollectorMappings, Map<DictCollector, Set<String>> collectorKeysMappings) {
        READ_WRITE_LOCK.writeLock().lock();
        try {
            CACHED.clear();
            CACHED.merge(messageResourceCollectionTemp);

            KEY_COLLECTOR_MAPPINGS.clear();
            KEY_COLLECTOR_MAPPINGS.putAll(keyCollectorMappings);

            COLLECTOR_KEYS_MAPPINGS.clear();
            COLLECTOR_KEYS_MAPPINGS.putAll(collectorKeysMappings);

        } finally {
            READ_WRITE_LOCK.writeLock().unlock();
        }
    }

    public static void refreshByCollector(DictCollector dictCollector, MessageResourceCollection messageResourceCollectionTemp) {
        READ_WRITE_LOCK.writeLock().lock();
        try {
            Set<String> keys = messageResourceCollectionTemp.getKeys();

            CACHED.removeByKeys(keys);
            CACHED.merge(messageResourceCollectionTemp);

            Set<String> originalKeys = COLLECTOR_KEYS_MAPPINGS.get(dictCollector);
            for(String key : originalKeys) {
                KEY_COLLECTOR_MAPPINGS.remove(key);
            }

            COLLECTOR_KEYS_MAPPINGS.put(dictCollector, keys);

            for(String key : keys) {
                KEY_COLLECTOR_MAPPINGS.put(key, dictCollector);
            }

        } finally {
            READ_WRITE_LOCK.writeLock().unlock();
        }
    }

    public static boolean containsWithNoLock(String dictKey) {
        return CACHED.containsKey(dictKey);
    }

    public static Object getDictValByKey(String dictKey, String value, Locale locale) {
        READ_WRITE_LOCK.readLock().lock();
        try {
            if(!CACHED.containsKey(dictKey)) {
                return null;
            }
            return CACHED.getMessage(dictKey, value, null, null, locale);
        } finally {
            READ_WRITE_LOCK.readLock().unlock();
        }
    }

}
