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
package com.alilitech.web.jackson;

import com.alilitech.web.JsonProperties;
import com.alilitech.web.jackson.deser.NumberFormatDeserializerModifier;
import com.alilitech.web.jackson.ser.*;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
public class BootPlusModule extends SimpleModule {

    private final JsonProperties jsonProperties;

    private final CompositeSerializerModifier compositeSerializerModifier;

    public BootPlusModule(JsonProperties jsonProperties,
                          CompositeSerializerModifier compositeSerializerModifier) {
        super(BootPlusModule.class.getName(), new Version(1, 0, 0, null, null, null));
        this.jsonProperties = jsonProperties;
        this.compositeSerializerModifier = compositeSerializerModifier;

    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);

        //SerializerModifier
//        context.addBeanSerializerModifier(new NumberFormatSerializerModifier());
        context.addBeanSerializerModifier(compositeSerializerModifier);
        context.addBeanSerializerModifier(new NullBeanSerializerModifier(jsonProperties.isDefaultNull(), jsonProperties.getDefaultNullValue()));

        //DeserializerModifier
        context.addBeanDeserializerModifier(new NumberFormatDeserializerModifier());

    }

}
