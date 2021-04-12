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

import com.alilitech.web.jackson.anotation.SerializerFormat;

/**
 * @author Zhou Xiaoxiang
 * @since 1.3.6
 */
public class FormatConfig {

    private boolean originalValueToString;

    private boolean newTarget;

    private String targetFiledName;

    private String pre;

    private String post;

    private boolean defaultNull;

    private String defaultNullValue;

    public FormatConfig(SerializerFormat serializerFormat, String defaultTargetFiledName) {
        this.originalValueToString = serializerFormat.originalValueToString();
        this.newTarget = serializerFormat.newTarget();
        this.targetFiledName = serializerFormat.targetFiled().equals("") ? defaultTargetFiledName : serializerFormat.targetFiled();
        this.pre = serializerFormat.pre();
        this.post = serializerFormat.post();
        this.defaultNull = serializerFormat.defaultNull();
        this.defaultNullValue = serializerFormat.defaultNullValue();
    }

    public FormatConfig(boolean originalValueToString,
                        boolean newTarget, String targetFiledName,
                        String pre, String post,
                        boolean defaultNull, String defaultValue) {
        this.originalValueToString = originalValueToString;
        this.newTarget = newTarget;
        this.targetFiledName = targetFiledName;
        this.pre = pre;
        this.post = post;
        this.defaultNull = defaultNull;
        this.defaultNullValue = defaultValue;
    }

    public boolean isOriginalValueToString() {
        return originalValueToString;
    }

    public boolean isNewTarget() {
        return newTarget;
    }

    public String getTargetFiledName() {
        return targetFiledName;
    }

    public String getPre() {
        return pre;
    }

    public String getPost() {
        return post;
    }

    public boolean isDefaultNull() {
        return defaultNull;
    }

    public String getDefaultNullValue() {
        return defaultNullValue;
    }
}
