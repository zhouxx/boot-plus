/*
 *    Copyright 2017-2022 the original author or authors.
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
import com.alilitech.web.support.ResourceBundleCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * since 1.3.10 rename DictCacheManager to DictWithoutLocaleCacheManager
 *
 * @author Zhou Xiaoxiang
 * @since 1.2.6
 */
@ConditionalOnMissingBean(DictCacheManager.class)
public class DictWithoutLocaleCacheManager implements DictCacheManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * save all dicts and values
     */
    ResourceBundleCollection resourceBundleCollection = ResourceBundleCollection.newBuilder().build();
    /**
     * key: dict-key, value: {@link DictCollector} object
     */
    private final Map<String, DictCollector> collectorMapping = new ConcurrentHashMap<>();

    private List<DictCollector> dictCollectors = new ArrayList<>();

    public DictWithoutLocaleCacheManager(@Nullable List<DictCollector> dictCollectors) {
        if(dictCollectors != null) {
            this.dictCollectors = dictCollectors;
        }
        logger.info("use DictWithoutLocaleCacheManager");
    }

    /**
     * 判断字典是否存在，不存在则刷新至缓存
     * @param dictKey  字典key
     * @param value    字典的值对应的key
     * @return         字典key在从收集器收集完后收否存在，后续无需再判断key是否存在，减少IO次数
     */
    @Override
    public boolean existAndRefresh(String dictKey, String value) {
        boolean existFlag = resourceBundleCollection.containsKey(dictKey) && resourceBundleCollection.getResourceBundle(dictKey).containsKey(value);

        if(existFlag) {
            return true;
        }

        if(!resourceBundleCollection.containsKey(dictKey)) {
            logger.warn("dict key: {} is not in cache, and it will reload all dict collectors.", dictKey);
            resourceBundleCollection.clear();

            // 字典key是否存在
            boolean exitKey = false;

            for (DictCollector dictCollector : dictCollectors) {
                ResourceBundleCollection resourceBundleCollectionTemp = dictCollector.findDictAndValues();
                resourceBundleCollection.merge(resourceBundleCollectionTemp);

                Set<String> keys = resourceBundleCollectionTemp.getKeys();

                // save mapping
                for (String key : keys) {
                    collectorMapping.put(key, dictCollector);

                    if (!exitKey && key.equals(dictKey) && resourceBundleCollectionTemp.getResourceBundle(key).containsKey(value)) {
                        exitKey = true;
                    }
                }
            }
            return exitKey;
        }
        // Do not consider the situation of moving from this collector to other collector
        else if(!resourceBundleCollection.getResourceBundle(dictKey).containsKey(value)) {
            DictCollector dictCollector = collectorMapping.get(dictKey);
            logger.warn("dict key: {} and value: {} is not in cache, and it will reload with {}.", dictKey, value, dictCollector.getClass());

            ResourceBundleCollection resourceBundleCollectionTemp = dictCollector.findDictAndValues();

            resourceBundleCollection.merge(resourceBundleCollectionTemp);

            Set<String> keys = resourceBundleCollectionTemp.getKeys();

            for (String key : keys) {
                if (key.equals(dictKey) && resourceBundleCollectionTemp.getResourceBundle(key).containsKey(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Object getDictValByKey(String dictKey, String value) {
        return resourceBundleCollection.getResourceBundle(dictKey).getObject(value);
    }

}
