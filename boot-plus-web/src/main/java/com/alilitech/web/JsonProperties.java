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
package com.alilitech.web;

import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@ConfigurationProperties(prefix = "mvc.json", ignoreUnknownFields = false)
public class JsonProperties {

    /**
     * 属性为空的时候会自动转化成默认格式（未设置默认值的时候开启默认值设计）
     */
    private boolean defaultNull = false;

    /**
     * 属性为null的时自动转化的值（全局），设置后表示全部应用此设置
     */
    private String defaultNullValue;

    public boolean isDefaultNull() {
        return defaultNull;
    }

    public void setDefaultNull(boolean defaultNull) {
        this.defaultNull = defaultNull;
    }

    public String getDefaultNullValue() {
        return defaultNullValue;
    }

    public void setDefaultNullValue(String defaultNullValue) {
        this.defaultNullValue = defaultNullValue;
    }

}
