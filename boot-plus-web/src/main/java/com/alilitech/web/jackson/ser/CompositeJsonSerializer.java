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

import com.alilitech.web.jackson.ser.converter.JsonFormatter;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Zhou Xiaoxiang
 * @since 1.3.6
 */
public class CompositeJsonSerializer extends JsonSerializer<Object> {

    private List<SerializerConverter> serializerConverters = new ArrayList<>();

    private JsonFormatter jsonFormatter;

    public CompositeJsonSerializer(JsonFormatter jsonFormatter) {
        this.jsonFormatter = jsonFormatter;
    }

    public CompositeJsonSerializer addConverter(SerializerConverter serializerConverter) {
        serializerConverters.add(serializerConverter);
        return this;
    }

    public CompositeJsonSerializer addAllConvert(Collection<SerializerConverter> serializerConverters) {
        this.serializerConverters.addAll(serializerConverters);
        return this;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        Object tmpValue = value;

        Object currentValue = gen.getCurrentValue();

        if(!serializerConverters.isEmpty()) {
            for(SerializerConverter serializerConverter : serializerConverters) {
                tmpValue = serializerConverter.doConvert(tmpValue, currentValue);
            }
        }

        if(jsonFormatter == null) {
            gen.writeObject(tmpValue);
        } else {
            jsonFormatter.serialize(value, tmpValue, gen);
        }
    }
}
