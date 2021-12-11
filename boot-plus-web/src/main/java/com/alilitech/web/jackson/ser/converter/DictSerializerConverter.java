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
package com.alilitech.web.jackson.ser.converter;


import com.alilitech.web.jackson.ser.DictCacheManager;
import com.alilitech.web.jackson.ser.DictThreadHolder;
import com.alilitech.web.jackson.ser.SerializerConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * dict converter
 * @author Zhou Xiaoxiang
 * @since 1.3.6
 */
public class DictSerializerConverter implements SerializerConverter<Object> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private DictCacheManager dictCacheManager;

    private DictAnnotationConfig dictAnnotationConfig;

    public DictSerializerConverter(DictCacheManager dictCacheManager, DictAnnotationConfig dictAnnotationConfig) {
        this.dictCacheManager = dictCacheManager;
        this.dictAnnotationConfig = dictAnnotationConfig;
    }

    @Override
    public Object doConvert(Object value, Object currentSourceObject) {

        // to be matched the cache key, converted the dictionary key string
        String dictKeyStringValue = value == null ? null : value.toString();

        String holderKey = dictAnnotationConfig.getDictKey() + "." + dictKeyStringValue;
        if(DictThreadHolder.exist(holderKey)) {
            logger.warn("The dictionary '{}' does not exist, the dictionary will not be collected in the same thread", holderKey);
            return null;
        }
        // If there is no key or no value, reload the dictionary collector
        if(dictCacheManager.existAndRefresh(dictAnnotationConfig.getDictKey(), dictKeyStringValue)) {
            return dictCacheManager.getDictValByKey(dictAnnotationConfig.getDictKey(), dictKeyStringValue);
        } else {
            DictThreadHolder.put(holderKey);
            return null;
        }
    }
}
