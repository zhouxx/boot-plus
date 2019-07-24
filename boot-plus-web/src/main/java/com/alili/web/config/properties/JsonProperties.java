/**
 *    Copyright 2017-2019 the original author or authors.
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
package com.alili.web.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.TimeZone;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@ConfigurationProperties(prefix = "mvc.json", ignoreUnknownFields = false)
public class JsonProperties {

    /**
     * 是否过滤Null字段
     */
    private boolean filterNull = false;

    /**
     * 属性为空的时候会自动转化成默认格式（未设置默认值的时候开启默认值设计）
     */
    private boolean defaultNull = false;

    /**
     * 属性为null的时自动转化的值（全局），设置后表示全部应用此设置
     */
    private String defaultNullValue;

    /**
     * 忽略未知属性
     */
    private boolean ignoreUnknown = false;

    /**
     * 默认日期格式
     */
    private String dateFormat = "yyyy-MM-dd HH:mm:ss";

    private String timezone = TimeZone.getDefault().getID();

    public boolean isFilterNull() {
        return filterNull;
    }

    public void setFilterNull(boolean filterNull) {
        this.filterNull = filterNull;
    }

    public boolean isDefaultNull() {
        return defaultNull;
    }

    public void setDefaultNull(boolean defaultNull) {
        this.defaultNull = defaultNull;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getDefaultNullValue() {
        return defaultNullValue;
    }

    public void setDefaultNullValue(String defaultNullValue) {
        this.defaultNullValue = defaultNullValue;
    }

    public boolean isIgnoreUnknown() {
        return ignoreUnknown;
    }

    public void setIgnoreUnknown(boolean ignoreUnknown) {
        this.ignoreUnknown = ignoreUnknown;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
