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
package com.alilitech.web.jackson.ser.dict;

import com.alilitech.web.jackson.DictCollector;
import com.alilitech.web.support.ResourceBundleCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.lang.Nullable;

import java.util.*;

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

    private List<DictCollector> dictCollectors = new ArrayList<>();

    public DictWithoutLocaleCacheManager(@Nullable List<DictCollector> dictCollectors) {
        if(dictCollectors != null) {
            this.dictCollectors = dictCollectors;
        }
        logger.debug("use DictWithoutLocaleCacheManager");
    }

    /**
     * 判断字典是否存在，不存在则刷新至缓存
     * @param dictKey  字典key
     * @param valueKey    字典的值对应的key
     * @return         字典key在从收集器收集完后收否存在，后续无需再判断key是否存在，减少IO次数
     */
    @Override
    public Object getAndRefresh(String dictKey, String valueKey) {

        Object val = DictCache.getDictValByKey(dictKey, valueKey);

        if(val != null) {
            return val;
        }

        if(!DictCache.containsWithNoLock(dictKey)) {
            synchronized (this) {
                if (!DictCache.containsWithNoLock(dictKey)) {
                    this.onApplicationEvent(new DictCacheEvent(this, null));
                }
            }
        }

        return DictCache.getDictValByKey(dictKey, valueKey);
    }

    @Override
    public void onApplicationEvent(DictCacheEvent dictEvent) {
        DictCollector dictCollector = dictEvent.getDictCollector();
        if(dictCollector == null) {
            logger.info("reload all dict collectors.");
            ResourceBundleCollection resourceBundleCollection = ResourceBundleCollection.newBuilder().build();
            Map<String, DictCollector> keyCollectorMappings = new HashMap<>();
            Map<DictCollector, Set<String>> collectorKeysMappings = new HashMap<>();
            for (DictCollector collector : dictCollectors) {
                ResourceBundleCollection resourceBundleCollectionTemp = collector.findDictAndValues();
                resourceBundleCollection.merge(resourceBundleCollectionTemp);

                Set<String> keys = resourceBundleCollectionTemp.getKeys();
                collectorKeysMappings.put(collector, keys);

                // save mapping
                for (String key : keys) {
                    keyCollectorMappings.put(key, collector);
                }
            }

            DictCache.refreshAll(resourceBundleCollection, keyCollectorMappings, collectorKeysMappings);
            return;
        }

        ResourceBundleCollection resourceBundleCollection = dictCollector.findDictAndValues();
        DictCache.refreshByCollector(dictCollector, resourceBundleCollection);
    }
}
