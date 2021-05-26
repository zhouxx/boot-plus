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
package com.alilitech.web.jackson.ser;

import com.alilitech.web.jackson.DictCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Zhou Xiaoxiang
 * @since 1.2.6
 */
public class DictCacheManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * save all dicts and values
     */
    private final Map<String, Map<String, Object>> cacheMap = new ConcurrentHashMap<>();

    /**
     * key: dict-key, value: {@link DictCollector} object
     */
    private final Map<String, DictCollector> collectorMapping = new ConcurrentHashMap<>();

    private List<DictCollector> dictCollectors = new ArrayList<>();

    public DictCacheManager(@Nullable List<DictCollector> dictCollectors) {
        if(dictCollectors != null) {
            this.dictCollectors = dictCollectors;
        }
    }

    public void existAndRefresh(String dictKey, String value) {
        boolean existFlag = cacheMap.containsKey(dictKey) && cacheMap.get(dictKey).containsKey(value);

        if(existFlag) {
            return;
        }

        if(!cacheMap.containsKey(dictKey)) {
            logger.warn("dict key: {} is not in cache, and it will reload all dict collectors.", dictKey);
            cacheMap.clear();
            dictCollectors.forEach(dictCollector -> {
                Map<String, Map<String, Object>> dictAndValues = dictCollector.findDictAndValues();
                cacheMap.putAll(dictAndValues);
                // save mapping
                dictAndValues.forEach((key, val) -> {
                    collectorMapping.put(key, dictCollector);
                });
            });
        }
        // Do not consider the situation of moving from this collector to other collector
        else if(!cacheMap.get(dictKey).containsKey(value)) {
            logger.warn("dict key: {} and value: {} is not in cache, and it will reload.", dictKey, value);
            DictCollector dictCollector = collectorMapping.get(dictKey);
            cacheMap.putAll(dictCollector.findDictAndValues());
        }

    }

    public boolean exist(String dictKey) {
        return cacheMap.containsKey(dictKey);
    }

    public Object getDictValByKey(String dictKey, String value) {
        return cacheMap.get(dictKey).get(value);
    }

}
