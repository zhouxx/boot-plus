/**
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
package com.alilitech.web.jackson.ser.support;

import com.alilitech.web.jackson.DefaultNullContextHolder;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
public abstract class NullJsonSerializer extends JsonSerializer<Object> {

    public abstract void writeNullValue(JsonGenerator gen, SerializerProvider serializers) throws IOException;
    public abstract void writeNoNullValue(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException;

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

        //如果进入此方法，但线程变量里指定了不转化，直接序列化
        if(DefaultNullContextHolder.get() != null && !DefaultNullContextHolder.get()) {
            writeNoNullValue(value, gen, serializers);
            return;
        }

        //其它按照正常路数走
        if(value == null) {
            writeNullValue(gen, serializers);
        } else {
            writeNoNullValue(value, gen, serializers);
        }
    }
}
