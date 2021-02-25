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

import com.alilitech.web.jackson.anotation.DictFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class DictFormatSerializerModifier extends BeanSerializerModifier {

    @Autowired
    private DictCacheManager dictCacheManager;

    public DictFormatSerializerModifier() {
    }

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        for (BeanPropertyWriter writer : beanProperties) {
            DictFormat dictFormat = writer.getAnnotation(DictFormat.class);
            if (dictFormat != null) {
                String sourceFileName = writer.getFullName().getSimpleName();
                String targetFiledName = isEmpty(dictFormat.targetFiled()) ? sourceFileName + "Name" : dictFormat.targetFiled();
                String dicKey = isEmpty(dictFormat.dictKey()) ? sourceFileName : dictFormat.dictKey();
                String defaultValue = dictFormat.defaultValue();
                writer.assignSerializer(new DictJsonSerializer(new DictAnnotationConfig(targetFiledName, dicKey, dictFormat.dictKeyToString(), defaultValue, dictFormat.newTarget())));
            }
        }

        return beanProperties;
    }

    private static boolean isEmpty(Object str) {
        return str == null || "".equals(str);
    }

    protected class DictJsonSerializer extends JsonSerializer<Object> {

        private final DictAnnotationConfig dictAnnotationConfig;

        DictJsonSerializer(DictAnnotationConfig dictAnnotationConfig) {
            this.dictAnnotationConfig = dictAnnotationConfig;
        }

        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            // Convert the original value of the dictionary key to String
            if(dictAnnotationConfig.isDictKeyToString()) {
                value = value == null ? null : value.toString();
            }
            // to be matched the cache key, converted the dictionary key string
            String dictKeyStringValue = value == null ? null : value.toString();

            // If there is no key or no value, reload the dictionary collector
            dictCacheManager.existAndRefresh(dictAnnotationConfig.getDictKey(), dictKeyStringValue);

            // 如果是新的目标属性，则先写原始的，再写格式化的
            if(dictAnnotationConfig.isNewTarget()) {
                gen.writeObject(value);
                // write dictionary value
                gen.writeFieldName(dictAnnotationConfig.getTargetFiledName());
            }

            if(dictCacheManager.exist(dictAnnotationConfig.getDictKey())) {
                Object object = dictCacheManager.getDictValByKey(dictAnnotationConfig.getDictKey(), dictKeyStringValue);
                gen.writeObject(object);
            } else {
                gen.writeObject(dictAnnotationConfig.getDefaultValue());
            }

        }
    }

    protected static class DictAnnotationConfig {

        private final String targetFiledName;
        private final String dictKey;
        private final boolean dictKeyToString;
        private final String defaultValue;
        private final boolean newTarget;

        DictAnnotationConfig(String targetFiledName, String dictKey, boolean dictKeyToString, String defaultValue, boolean newTarget) {
            this.targetFiledName = targetFiledName;
            this.dictKey = dictKey;
            this.dictKeyToString = dictKeyToString;
            this.defaultValue = defaultValue;
            this.newTarget = newTarget;
        }

        String getTargetFiledName() {
            return targetFiledName;
        }

        String getDictKey() {
            return dictKey;
        }

        boolean isDictKeyToString() {
            return dictKeyToString;
        }

        String getDefaultValue() {
            return defaultValue;
        }

        public boolean isNewTarget() {
            return newTarget;
        }
    }

}
