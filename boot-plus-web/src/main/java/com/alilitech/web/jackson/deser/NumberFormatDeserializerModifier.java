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
package com.alilitech.web.jackson.deser;

import com.alilitech.web.jackson.anotation.NumberParse;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.Iterator;

/**
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
public class NumberFormatDeserializerModifier extends BeanDeserializerModifier {

    @Override
    public BeanDeserializerBuilder updateBuilder(DeserializationConfig config, BeanDescription beanDesc, BeanDeserializerBuilder builder) {

        Iterator<SettableBeanProperty> properties = builder.getProperties();

        while (properties.hasNext()) {
            SettableBeanProperty settableBeanProperty = properties.next();

            if(settableBeanProperty.getAnnotation(NumberParse.class) != null && isNumberType(settableBeanProperty.getType())) {

                settableBeanProperty = settableBeanProperty.withValueDeserializer(new NumberJsonDeSerializer(settableBeanProperty.getAnnotation(NumberParse.class), settableBeanProperty.getType()));

                builder.addOrReplaceProperty(settableBeanProperty, true);
            }
        }
        return builder;
    }

    private boolean isNumberType(JavaType type) {
        Class<?> clazz = type.getRawClass();
        return clazz.equals(BigDecimal.class)
                || clazz.equals(byte.class) || clazz.equals(short.class) || clazz.equals(int.class) || clazz.equals(long.class) || clazz.equals(double.class) || clazz.equals(float.class)
                || clazz.equals(Byte.class) || clazz.equals(Integer.class) || clazz.equals(Long.class) || clazz.equals(Double.class) || clazz.equals(Float.class);
    }

    protected static class NumberJsonDeSerializer extends JsonDeserializer<Object> {

        private final NumberParse annotation;

        private final Class<?> targetClass;

        NumberJsonDeSerializer(NumberParse annotation, JavaType javaType) {
            this.annotation = annotation;
            this.targetClass = javaType.getRawClass();
        }

        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            DecimalFormat df = new DecimalFormat(annotation.pattern());
            df.setParseBigDecimal(true);
            BigDecimal bigDecimal = (BigDecimal) df.parse(p.getText(), new ParsePosition(0));

            if(targetClass.equals(BigDecimal.class)) {
                return bigDecimal;
            } else if(targetClass.equals(byte.class) || targetClass.equals(Byte.class)) {
                return bigDecimal.byteValue();
            } else if(targetClass.equals(short.class) || targetClass.equals(Short.class)) {
                return bigDecimal.shortValue();
            } else if(targetClass.equals(int.class) || targetClass.equals(Integer.class)) {
                return bigDecimal.intValue();
            } else if(targetClass.equals(long.class) || targetClass.equals(Long.class)) {
                return bigDecimal.longValue();
            } else if(targetClass.equals(double.class) || targetClass.equals(Double.class)) {
                return bigDecimal.doubleValue();
            } else if(targetClass.equals(float.class) || targetClass.equals(Float.class)) {
                return bigDecimal.floatValue();
            }

            return bigDecimal.doubleValue();
        }
    }

}
