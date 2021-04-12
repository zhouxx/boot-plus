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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * @author Zhou Xiaoxiang
 * @since 1.3.6
 */
public class JsonFormatter {

    private FormatConfig formatConfig;

    public JsonFormatter(FormatConfig formatConfig) {
        this.formatConfig = formatConfig;
    }

    public void serialize(Object originalValue, Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

        if(value == null && formatConfig.isDefaultNull()) {
            value = formatConfig.getDefaultNullValue();
        }

        if(formatConfig.isOriginalValueToString()) {
            originalValue = originalValue == null ? null : originalValue.toString();
        }

        if(!formatConfig.getPre().equals("")) {
            value = formatConfig.getPre() + value;
        }

        if(!formatConfig.getPost().equals("")) {
            value = value + formatConfig.getPost();
        }

        // 如果是新的目标属性，则先写原始的，再写格式化的
        if(formatConfig.isNewTarget()) {
            gen.writeObject(originalValue);
            gen.writeFieldName(formatConfig.getTargetFiledName());
        }

        gen.writeObject(value);
    }
}
