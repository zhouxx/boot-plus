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

import com.alilitech.web.jackson.ser.SerializerConverter;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * @author Zhou Xiaoxiang
 * @since 1.3.6
 */
public class NumberSerializerConverter implements SerializerConverter<Object> {

    private NumberAnnotationConfig numberAnnotationConfig;

    public NumberSerializerConverter(NumberAnnotationConfig numberAnnotationConfig) {
        this.numberAnnotationConfig = numberAnnotationConfig;
    }

    @Override
    public Object doConvert(Object value, Object currentSourceObject) {
        Object formatValue = null;
        if(numberAnnotationConfig.getPattern().equals("")) {
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

            if(bigDecimalValue == null) {
                throw new UnsupportedOperationException("The type { " + value.getClass() + " } is can not parse with 'com.alilitech.web.jackson.anotation.NumberFormat'");
            }

            bigDecimalValue = bigDecimalValue.setScale(numberAnnotationConfig.getScale(), numberAnnotationConfig.getRound());
            formatValue = bigDecimalValue;
        } else {
            DecimalFormat df = new DecimalFormat(numberAnnotationConfig.getPattern());
            formatValue = df.format(value);
        }

        return formatValue;
    }
}
