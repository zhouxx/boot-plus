/**
 *    Copyright 2017-2019 the original author or authors.
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
package com.alili.web.jackson;

import com.alili.web.jackson.anotation.DictFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class DictFormatSerializerModifier extends BeanSerializerModifier {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String, Map<String, Object>> cacheMap = new HashMap<>();

    private List<DictCollector> dictCollectorList = new ArrayList<>();

    public DictFormatSerializerModifier(ObjectProvider<List<DictCollector>> dicServiceProvider) {
        if(dicServiceProvider.getIfAvailable() != null) {
            dictCollectorList = dicServiceProvider.getIfAvailable();
        }
    }

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        for(int i = 0; i < beanProperties.size(); ++i) {
            BeanPropertyWriter writer = beanProperties.get(i);
            DictFormat dictFormat = writer.getAnnotation(DictFormat.class);
            if(dictFormat != null) {
                String sourceFileName = writer.getFullName().getSimpleName();
                String targetFiledName = isEmpty(dictFormat.targetFiled()) ? sourceFileName + "Name" : dictFormat.targetFiled() ;
                String dicKey = isEmpty(dictFormat.dictKey()) ? sourceFileName : dictFormat.dictKey();
                String defaultValue = dictFormat.defaultValue();
                writer.assignSerializer(new DicJsonSerializer(new DictAnnotationConfig(sourceFileName, targetFiledName, dicKey, dictFormat.dictKeyToString(), defaultValue)));
            }
        }

        return beanProperties;
    }

    private static boolean isEmpty(Object str) {
        return str == null || "".equals(str);
    }

    protected class DicJsonSerializer extends JsonSerializer<Object> {

        private DictAnnotationConfig dictAnnotationConfig;

        public DicJsonSerializer(DictAnnotationConfig dictAnnotationConfig) {
            this.dictAnnotationConfig = dictAnnotationConfig;
        }

        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
            //将字典key的原始值转换成String
            if(dictAnnotationConfig.isDictKeyToString()) {
                value = value == null ? null : value.toString();
            }
            //字典key要转换成String，否则不好匹配
            String dictKeyStringValue = value == null ? null : value.toString();
            //如果不存在key,或不存在value，重新加载
            if(!cacheMap.containsKey(dictAnnotationConfig.getDictKey()) || !cacheMap.get(dictAnnotationConfig.getDictKey()).containsKey(dictKeyStringValue)) {
                logger.warn("dict key: {} and value: {} is not in cache, and it will reload all dict collectors.", dictAnnotationConfig.getDictKey(), dictKeyStringValue);
                cacheMap.clear();
                dictCollectorList.forEach(dictCollector -> {
                    cacheMap.putAll(dictCollector.findDictAndValues());
                });
            }
            gen.writeObject(value);
            //写字典值
            gen.writeFieldName(dictAnnotationConfig.getTargetFiledName());
            if(cacheMap.containsKey(dictAnnotationConfig.getDictKey())) {
               Object object = cacheMap.get(dictAnnotationConfig.getDictKey()).get(dictKeyStringValue);
               gen.writeObject(object);
            } else {
               gen.writeObject(dictAnnotationConfig.getDefaultValue());
            }
        }
    }

    protected class DictAnnotationConfig {

        private String sourceFileName;
        private String targetFiledName;

        private String dictKey;
        private boolean dictKeyToString;
        private String defaultValue;

        public DictAnnotationConfig(String sourceFileName, String targetFiledName, String dictKey, boolean dictKeyToString, String defaultValue) {
            this.sourceFileName = sourceFileName;
            this.targetFiledName = targetFiledName;
            this.dictKey = dictKey;
            this.dictKeyToString = dictKeyToString;
            this.defaultValue = defaultValue;
        }

        public String getSourceFileName() {
            return sourceFileName;
        }

        public void setSourceFileName(String sourceFileName) {
            this.sourceFileName = sourceFileName;
        }

        public String getTargetFiledName() {
            return targetFiledName;
        }

        public void setTargetFiledName(String targetFiledName) {
            this.targetFiledName = targetFiledName;
        }

        public String getDictKey() {
            return dictKey;
        }

        public void setDictKey(String dictKey) {
            this.dictKey = dictKey;
        }

        public boolean isDictKeyToString() {
            return dictKeyToString;
        }

        public void setDictKeyToString(boolean dictKeyToString) {
            this.dictKeyToString = dictKeyToString;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }
    }
}
