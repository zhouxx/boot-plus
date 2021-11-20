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

    /**
     * 判断字典是否存在，不存在则刷新至缓存
     * @param dictKey  字典key
     * @param value    字典的值对应的key
     * @return         字典key在从收集器收集完后收否存在，后续无需再判断key是否存在，减少IO次数
     */
    public boolean existAndRefresh(String dictKey, String value) {
        boolean existFlag = cacheMap.containsKey(dictKey) && cacheMap.get(dictKey).containsKey(value);

        if(existFlag) {
            return true;
        }

        if(!cacheMap.containsKey(dictKey)) {
            logger.warn("dict key: {} is not in cache, and it will reload all dict collectors.", dictKey);
            cacheMap.clear();

            // 字典key是否存在
            boolean exitKey = false;

            for (DictCollector dictCollector : dictCollectors) {
                Map<String, Map<String, Object>> dictAndValues = dictCollector.findDictAndValues();
                cacheMap.putAll(dictAndValues);
                // save mapping
                for (Map.Entry<String, Map<String, Object>> entry : dictAndValues.entrySet()) {
                    String key = entry.getKey();
                    Map<String, Object> valMap = entry.getValue();
                    collectorMapping.put(key, dictCollector);

                    if (!exitKey && key.equals(dictKey) && valMap.containsKey(value)) {
                        exitKey = true;
                    }
                }
            }
            return exitKey;
        }
        // Do not consider the situation of moving from this collector to other collector
        else if(!cacheMap.get(dictKey).containsKey(value)) {
            DictCollector dictCollector = collectorMapping.get(dictKey);
            logger.warn("dict key: {} and value: {} is not in cache, and it will reload with {}.", dictKey, value, dictCollector.getClass());

            Map<String, Map<String, Object>> dictAndValues = dictCollector.findDictAndValues();

            cacheMap.putAll(dictAndValues);
            // save mapping
            for (Map.Entry<String, Map<String, Object>> entry : dictAndValues.entrySet()) {
                String key = entry.getKey();
                Map<String, Object> valMap = entry.getValue();

                if (key.equals(dictKey) && valMap.containsKey(value)) {
                    return true;
                }
            }
        }

        return false;

    }

    public Object getDictValByKey(String dictKey, String value) {
        return cacheMap.get(dictKey).get(value);
    }

}
