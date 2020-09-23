/*
 *    Copyright 2017-2020 the original author or authors.
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

import com.alilitech.web.jackson.anotation.NumberFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.List;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class NumberFormatSerializerModifier extends BeanSerializerModifier {

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        for (BeanPropertyWriter writer : beanProperties) {
            NumberFormat numberFormat = writer.getAnnotation(NumberFormat.class);
            if (numberFormat != null && this.isNumberType(writer)) {
                writer.assignSerializer(new NumberJsonSerializer(numberFormat));
            }
        }

        return beanProperties;
    }

    private boolean isNumberType(BeanPropertyWriter writer) {
        Class<?> clazz = writer.getType().getRawClass();
        return clazz.equals(BigDecimal.class)
                || clazz.equals(byte.class) || clazz.equals(short.class) || clazz.equals(int.class) || clazz.equals(long.class) || clazz.equals(double.class) || clazz.equals(float.class)
                || clazz.equals(Byte.class) || clazz.equals(Integer.class) || clazz.equals(Long.class) || clazz.equals(Double.class) || clazz.equals(Float.class);
    }

    protected static class NumberJsonSerializer extends JsonSerializer<Object> {

        private final NumberFormat annotation;

        NumberJsonSerializer(NumberFormat annotation) {
            this.annotation = annotation;
        }

        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if(annotation.pattern().equals("")) {
                BigDecimal bigDecimalValue = null;
                if(value instanceof BigDecimal) {
                    bigDecimalValue = (BigDecimal) value;
                } else if(value instanceof Double){
                    bigDecimalValue = BigDecimal.valueOf((Double) value);
                } else if(value instanceof Float){
                    bigDecimalValue = BigDecimal.valueOf((Float) value);
                } else if(value instanceof Long){
                    bigDecimalValue = new BigDecimal((Long)value);
                } else if(value instanceof Integer){
                    bigDecimalValue = new BigDecimal((Integer)value);
                } else if(value instanceof Short){
                    bigDecimalValue = new BigDecimal((Short)value);
                } else if(value instanceof Byte){
                    bigDecimalValue = new BigDecimal((Byte)value);
                }
                bigDecimalValue = bigDecimalValue.setScale(annotation.scale(), annotation.round());
                gen.writeObject(bigDecimalValue);
            } else {
                DecimalFormat df = new DecimalFormat(annotation.pattern());
                gen.writeObject(df.format(value));
            }
        }
    }
}
