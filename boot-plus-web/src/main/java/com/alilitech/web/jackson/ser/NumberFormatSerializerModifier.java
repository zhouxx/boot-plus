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
                String sourceFileName = writer.getFullName().getSimpleName();
                String targetFiledName = isEmpty(numberFormat.targetFiled()) ? sourceFileName + "Name" : numberFormat.targetFiled();
                NumberFormatConfig numberFormatConfig = new NumberFormatConfig(numberFormat, targetFiledName, null);

                writer.assignSerializer(new NumberJsonSerializer(numberFormatConfig));
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

    private static boolean isEmpty(Object str) {
        return str == null || "".equals(str);
    }

    protected static class NumberJsonSerializer extends JsonSerializer<Object> {

        private final NumberFormatConfig numberFormatConfig;

        NumberJsonSerializer(NumberFormatConfig numberFormatConfig) {
            this.numberFormatConfig = numberFormatConfig;
        }

        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            Object formatValue = null;
            if(numberFormatConfig.getPattern().equals("")) {
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
                bigDecimalValue = bigDecimalValue.setScale(numberFormatConfig.getScale(), numberFormatConfig.getRound());
                formatValue = bigDecimalValue;
//                gen.writeObject(bigDecimalValue);
            } else {
                DecimalFormat df = new DecimalFormat(numberFormatConfig.getPattern());
                formatValue = df.format(value);
//                gen.writeObject(df.format(value));
            }

            if(!numberFormatConfig.getPre().equals("")) {
                formatValue = numberFormatConfig.getPre() + formatValue;
            }

            if(!numberFormatConfig.getPost().equals("")) {
                formatValue = formatValue + numberFormatConfig.getPost();
            }

            if(numberFormatConfig.isNewTarget()) {
                gen.writeObject(value);
                gen.writeFieldName(numberFormatConfig.getTargetFiledName());
            }

            gen.writeObject(formatValue);

        }
    }

    protected static class NumberFormatConfig {
        private final String pattern;
        private final int scale;
        private final int round;

        private final String pre;
        private final String post;

        private final String targetFiledName;
        private final String defaultValue;
        private final boolean newTarget;

        public NumberFormatConfig(NumberFormat numberFormat, String targetFiledName, String defaultValue) {
            this.pattern = numberFormat.pattern();
            this.scale = numberFormat.scale();
            this.round = numberFormat.round();
            this.targetFiledName = targetFiledName;
            this.defaultValue = defaultValue;
            this.newTarget = numberFormat.newTarget();
            this.pre = numberFormat.pre();
            this.post = numberFormat.post();
        }

        public String getPattern() {
            return pattern;
        }

        public int getScale() {
            return scale;
        }

        public int getRound() {
            return round;
        }

        public String getTargetFiledName() {
            return targetFiledName;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public boolean isNewTarget() {
            return newTarget;
        }

        public String getPre() {
            return pre;
        }

        public String getPost() {
            return post;
        }
    }
}
