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
import com.alilitech.web.support.MessageResourceCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author Zhou Xiaoxiang
 * @since 1.3.10
 */
@ConditionalOnProperty(value = "mvc.json.enableLocale", havingValue = "true")
public class DictWithLocaleCacheManager implements DictCacheManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<DictCollector> dictCollectors = new ArrayList<>();

    private LocaleResolver localeResolver;

    public DictWithLocaleCacheManager(@Nullable List<DictCollector> dictCollectors, LocaleResolver localeResolver) {
        if(dictCollectors != null) {
            this.dictCollectors = dictCollectors;
        }
        this.localeResolver = localeResolver;
        logger.debug("use DictWithLocaleCacheManager");
    }

    /**
     * 判断字典是否存在，不存在则刷新至缓存
     * @param dictKey  字典key
     * @param value    字典的值对应的key
     * @return         字典key在从收集器收集完后收否存在，后续无需再判断key是否存在，减少IO次数
     */
    @Override
    public Object getAndRefresh(String dictKey, String value) {

        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
        Locale locale = localeResolver.resolveLocale(request);

        Object val = DictLocaleCache.getDictValByKey(dictKey, value, locale);

        if(val != null) {
            return val;
        }

        if(!DictLocaleCache.containsWithNoLock(dictKey)) {
            synchronized (this) {
                if (!DictCache.containsWithNoLock(dictKey)) {
                    this.onApplicationEvent(new DictCacheEvent(this, null));
                }
            }

        }

        return DictLocaleCache.getDictValByKey(dictKey, value, locale);
    }

    @Override
    public void onApplicationEvent(DictCacheEvent dictEvent) {
        DictCollector dictCollector = dictEvent.getDictCollector();
        if(dictCollector == null) {
            logger.info("reload all dict collectors.");
            MessageResourceCollection messageResourceCollection = MessageResourceCollection.newBuilder().build();
            Map<String, DictCollector> keyCollectorMappings = new HashMap<>();
            Map<DictCollector, Set<String>> collectorKeysMappings = new HashMap<>();
            for (DictCollector collector : dictCollectors) {
                MessageResourceCollection messageResourceCollectionTemp = collector.findLocaleDictAndValues();
                messageResourceCollection.merge(messageResourceCollectionTemp);

                Set<String> keys = messageResourceCollectionTemp.getKeys();
                collectorKeysMappings.put(collector, keys);

                // save mapping
                for (String key : keys) {
                    keyCollectorMappings.put(key, collector);
                }
            }

            DictLocaleCache.refreshAll(messageResourceCollection, keyCollectorMappings, collectorKeysMappings);
            return;
        }

        MessageResourceCollection resourceBundleCollection = dictCollector.findLocaleDictAndValues();
        DictLocaleCache.refreshByCollector(dictCollector, resourceBundleCollection);
    }
}
