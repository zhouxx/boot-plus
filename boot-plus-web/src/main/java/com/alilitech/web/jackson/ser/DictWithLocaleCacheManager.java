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
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Zhou Xiaoxiang
 * @since 1.3.10
 */
@ConditionalOnProperty(value = "mvc.json.enableLocale", havingValue = "true")
public class DictWithLocaleCacheManager implements DictCacheManager {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * save all dicts and values
     */
    MessageResourceCollection messageResourceCollection = MessageResourceCollection.MessageResourceCollectionBuilder.newBuilder().build();

    /**
     * key: dict-key, value: {@link DictCollector} object
     */
    private final Map<String, DictCollector> collectorMapping = new ConcurrentHashMap<>();

    private List<DictCollector> dictCollectors = new ArrayList<>();

    private LocaleResolver localeResolver;

    public DictWithLocaleCacheManager(@Nullable List<DictCollector> dictCollectors, LocaleResolver localeResolver) {
        if(dictCollectors != null) {
            this.dictCollectors = dictCollectors;
        }
        this.localeResolver = localeResolver;
        logger.info("use DictWithLocaleCacheManager");
    }

    /**
     * 判断字典是否存在，不存在则刷新至缓存
     * @param dictKey  字典key
     * @param value    字典的值对应的key
     * @return         字典key在从收集器收集完后收否存在，后续无需再判断key是否存在，减少IO次数
     */
    public boolean existAndRefresh(String dictKey, String value) {

        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
        Locale locale = localeResolver.resolveLocale(request);
        boolean existFlag = messageResourceCollection.containsKey(dictKey)
                && messageResourceCollection.getMessage(dictKey, value, null, null, locale) != null;

        if(existFlag) {
            return true;
        }

        if(!messageResourceCollection.containsKey(dictKey)) {
            logger.warn("dict key: {} is not in cache, and it will reload all dict collectors.", dictKey);
            messageResourceCollection.clear();

            // 字典key是否存在
            boolean exitKey = false;

            for (DictCollector dictCollector : dictCollectors) {
                MessageResourceCollection messageResourceCollectionTemp = dictCollector.findLocaleDictAndValues();
                messageResourceCollection.merge(messageResourceCollectionTemp);

                Set<String> keys = messageResourceCollection.getKeys();

                // save mapping
                for (String key : keys) {
                    collectorMapping.put(key, dictCollector);

                    if (!exitKey && messageResourceCollection.getMessage(dictKey, value, null, null, locale) != null) {
                        exitKey = true;
                    }
                }
            }
            return exitKey;
        }
        // Do not consider the situation of moving from this collector to other collector
        else if(messageResourceCollection.getMessage(dictKey, value, null, null, locale) == null) {
            DictCollector dictCollector = collectorMapping.get(dictKey);
            logger.warn("dict key: {} and value: {} is not in cache, and it will reload with {}.", dictKey, value, dictCollector.getClass());

            MessageResourceCollection messageResourceCollectionTemp = dictCollector.findLocaleDictAndValues();
            messageResourceCollection.merge(messageResourceCollectionTemp);

            Set<String> keys = messageResourceCollectionTemp.getKeys();
            for (String key : keys) {
                if (key.equals(dictKey) && messageResourceCollectionTemp.getMessage(dictKey, value, null, null, locale) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public Object getDictValByKey(String dictKey, String value) {
        HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
        Locale locale = localeResolver.resolveLocale(request);
        return messageResourceCollection.getMessage(dictKey, value, null, null, locale);
    }

}
