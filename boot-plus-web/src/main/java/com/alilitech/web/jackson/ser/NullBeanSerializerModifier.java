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

import com.alilitech.web.jackson.anotation.NullFormat;
import com.alilitech.web.jackson.ser.support.*;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class NullBeanSerializerModifier extends BeanSerializerModifier {
    /**
     *
     */
    private boolean defaultNull;
    private String defaultNullValue;

    private final JsonSerializer<Object> _nullArrayJsonSerializer = new NullArrayJsonSerializer();
    private final JsonSerializer<Object> _nullStringJsonSerializer = new NullStringJsonSerializer();
    private final JsonSerializer<Object> _nullMapJsonSerializer = new NullMapJsonSerializer();
    private final JsonSerializer<Object> _nullDoubleJsonSerializer = new NullDoubleJsonSerializer();
    private final JsonSerializer<Object> _nullIntegerJsonSerializer = new NullIntegerJsonSerializer();
    private final JsonSerializer<Object> _nullDateJsonSerializer = new NullDateJsonSerializer();
    private final JsonSerializer<Object> _nullObjectJsonSerializer = new NullObjectJsonSerializer();
    private final JsonSerializer<Object> _nullBigDecimalJsonSerializer = new NullBigDecimalJsonSerializer();

    public NullBeanSerializerModifier() {
    }

    public NullBeanSerializerModifier(boolean defaultNull, String defaultNullValue) {
        this.defaultNull = defaultNull;
        this.defaultNullValue = defaultNullValue;
    }

    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {

        //如果类上定义了，则在取类定义的值，如果类没定义，则取全局
        String classDefaultNullValue = defaultNullValue;

        NullFormat nullFormat = beanDesc.getClassAnnotations().get(NullFormat.class);

        if(nullFormat != null) {
            if(!nullFormat.defaultNull()) {
                return beanProperties;
            } else {
                classDefaultNullValue = nullFormat.defaultNullValue();
            }
        }

        for (BeanPropertyWriter writer : beanProperties) {
            String propertyDefaultNullValue = classDefaultNullValue;

            //属性指定的默认值
            NullFormat propertyNullFormat = writer.getAnnotation(NullFormat.class);

            if (propertyNullFormat != null) {
                if (!propertyNullFormat.defaultNull()) {
                    propertyDefaultNullValue = null;
                } else {
                    propertyDefaultNullValue = propertyNullFormat.defaultNullValue();
                }
            }

            //如果提供了空值的默认值
            if (propertyDefaultNullValue != null) {
                writer.assignNullSerializer(new NullDefaultValueJsonSerializer(propertyDefaultNullValue));
            } else if (defaultNull) {  //没有提供则提供不同类型的
                if (this.isArrayType(writer)) {
                    writer.assignNullSerializer(this.defaultNullArrayJsonSerializer());
                } else if (this.isStringType(writer)) {
                    writer.assignNullSerializer(this.defaultNullStringJsonSerializer());
                } else if (this.isMapType(writer)) {
                    writer.assignNullSerializer(this.defaultNullMapJsonSerializer());
                } else if (this.isDoubleType(writer)) {
                    writer.assignNullSerializer(this.defaultNullDoubleJsonSerializer());
                } else if (this.isBigDecimalType(writer)) {
                    writer.assignNullSerializer(this.defaultNullBigDecimalJsonSerializer());
                } else if (this.isIntegerType(writer)) {
                    writer.assignNullSerializer(this.defaultNullIntegerJsonSerializer());
                } else if (this.isDateType(writer)) {
                    writer.assignNullSerializer(this.defaultNullDateJsonSerializer());
                } else {
                    writer.assignNullSerializer(this.defaultNullObjectJsonSerializer());
                }
            }
        }

        return beanProperties;
    }

    private boolean isArrayType(BeanPropertyWriter writer) {
        //Class clazz = writer.getPropertyType();
        Class<?> clazz = writer.getType().getRawClass();
        return clazz.isArray() || clazz.equals(List.class) || clazz.equals(Set.class);
    }

    private boolean isStringType(BeanPropertyWriter writer) {
        Class<?> clazz = writer.getType().getRawClass();
        return clazz.equals(String.class);
    }

    private boolean isMapType(BeanPropertyWriter writer) {
        Class<?> clazz = writer.getType().getRawClass();
        return clazz.equals(Map.class);
    }

    private boolean isBigDecimalType(BeanPropertyWriter writer) {
        Class<?> clazz = writer.getType().getRawClass();
        return clazz.equals(BigDecimal.class);
    }

    private boolean isDoubleType(BeanPropertyWriter writer) {
        Class<?> clazz = writer.getType().getRawClass();
        return clazz.equals(Double.class);
    }

    private boolean isIntegerType(BeanPropertyWriter writer) {
        Class<?> clazz = writer.getType().getRawClass();
        return clazz.equals(Integer.class) || clazz.equals(Long.class) || clazz.equals(Short.class);
    }

    private boolean isDateType(BeanPropertyWriter writer) {
        Class<?> clazz = writer.getType().getRawClass();
        return clazz.equals(Date.class) || clazz.equals(java.sql.Date.class);
    }

    private JsonSerializer<Object> defaultNullArrayJsonSerializer() {
        return this._nullArrayJsonSerializer;
    }

    private JsonSerializer<Object> defaultNullStringJsonSerializer() {
        return this._nullStringJsonSerializer;
    }

    private JsonSerializer<Object> defaultNullMapJsonSerializer() {
        return this._nullMapJsonSerializer;
    }

    private JsonSerializer<Object> defaultNullDoubleJsonSerializer() {
        return this._nullDoubleJsonSerializer;
    }

    private JsonSerializer<Object> defaultNullIntegerJsonSerializer() {
        return this._nullIntegerJsonSerializer;
    }

    private JsonSerializer<Object> defaultNullDateJsonSerializer() {
        return this._nullDateJsonSerializer;
    }

    private JsonSerializer<Object> defaultNullObjectJsonSerializer() {
        return this._nullObjectJsonSerializer;
    }

    private JsonSerializer<Object> defaultNullBigDecimalJsonSerializer() {
        return this._nullBigDecimalJsonSerializer;
    }
}
