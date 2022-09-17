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

import com.alilitech.web.JsonProperties;
import com.alilitech.web.jackson.anotation.DictFormat;
import com.alilitech.web.jackson.anotation.NumberFormat;
import com.alilitech.web.jackson.anotation.SerializerConvert;
import com.alilitech.web.jackson.anotation.SerializerFormat;
import com.alilitech.web.jackson.ser.converter.*;
import com.alilitech.web.jackson.ser.dict.DictCacheManager;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.introspect.AnnotationMap;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhou Xiaoxiang
 * @since 1.3.6
 */
public class CompositeSerializerModifier extends BeanSerializerModifier {

    private static final Logger logger = LoggerFactory.getLogger(CompositeSerializerModifier.class);

    private final DictCacheManager dictCacheManager;

    private final String targetFiledKeyFormat;

    private static final Class<Annotation>[] effectAnnotationClasses = new Class[]{
            SerializerConvert.class,
            NumberFormat.class, DictFormat.class,
            SerializerFormat.class
    };

    public CompositeSerializerModifier(DictCacheManager dictCacheManager, JsonProperties jsonProperties) {
        this.dictCacheManager = dictCacheManager;
        this.targetFiledKeyFormat = jsonProperties.getTargetFiledKeyFormat();
    }

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDescription, List<BeanPropertyWriter> beanProperties) {
        for (BeanPropertyWriter writer : beanProperties) {
            AnnotationMap annotationMap = writer.getMember().getAllAnnotations();
            List<Annotation> annotations = parseAnnotations(annotationMap);

            if(annotations.isEmpty()) {
                continue;
            }

            List<SerializerConverter> serializerConverters = new ArrayList<>();

            JsonFormatter jsonFormatter = null;
            // 默认的fieldName
            String filedName = writer.getFullName().getSimpleName();
            // 默认的新的target
            String defaultTargetFiledName =  targetFiledKeyFormat.replace("{}", filedName);

            for(Annotation annotation : annotations) {

                if(annotation instanceof SerializerConvert) {
                    Class<? extends SerializerConverter>[] convertClasses = ((SerializerConvert) annotation).convertClasses();

                    for(Class<? extends SerializerConverter> serializerClass : convertClasses) {
                        try {
                            SerializerConverter serializerConverter = serializerClass.newInstance();
                            serializerConverters.add(serializerConverter);
                        } catch (InstantiationException | IllegalAccessException e) {
                            logger.error(e.getMessage());
                        }
                    }
                }
                else if(annotation instanceof NumberFormat) {
                    NumberFormat numberFormat = (NumberFormat) annotation;

                    NumberSerializerConverter numberSerializerConverter = new NumberSerializerConverter(new NumberAnnotationConfig(numberFormat));
                    serializerConverters.add(numberSerializerConverter);

                    String targetFiled = numberFormat.targetFiled().equals("") ? defaultTargetFiledName : numberFormat.targetFiled();

                    jsonFormatter = new JsonFormatter(new FormatConfig(numberFormat.originalValueToString(), numberFormat.newTarget(), targetFiled,
                            numberFormat.pre(), numberFormat.post(), numberFormat.defaultNull(), numberFormat.defaultNullValue()));

                }
                else if(annotation instanceof DictFormat) {
                    DictFormat dictFormat = (DictFormat) annotation;

                    DictSerializerConverter dictSerializerConverter = new DictSerializerConverter(dictCacheManager, new DictAnnotationConfig(dictFormat, filedName));
                    serializerConverters.add(dictSerializerConverter);

                    String targetFiled = dictFormat.targetFiled().equals("") ? defaultTargetFiledName : dictFormat.targetFiled();

                    jsonFormatter = new JsonFormatter(new FormatConfig(dictFormat.originalValueToString(), dictFormat.newTarget(), targetFiled,
                            dictFormat.pre(), dictFormat.post(), dictFormat.defaultNull(), dictFormat.defaultNullValue()));

                } else if(annotation instanceof SerializerFormat) {
                    jsonFormatter = new JsonFormatter(new FormatConfig((SerializerFormat)annotation, defaultTargetFiledName));
                }
            }

            CompositeJsonSerializer compositeJsonSerializer = new CompositeJsonSerializer(jsonFormatter).addAllConvert(serializerConverters);
            writer.assignSerializer(compositeJsonSerializer);
        }

        return beanProperties;
    }

    private List<Annotation> parseAnnotations(AnnotationMap annotationMap) {

        List<Annotation> annotations = new ArrayList<>();

        for(Class<Annotation> clazz : effectAnnotationClasses) {
            if(annotationMap.has(clazz)) {
                annotations.add(annotationMap.get(clazz));
            }
        }

        return annotations;
    }

}
