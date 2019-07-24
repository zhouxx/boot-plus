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
package com.alili.web.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;

/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class CustomObjectMapper extends ObjectMapper {

    /**
     * 反序列化时忽略未知属性
     * @param ignoreUnknown
     */
    public void setIgnoreUnknown(boolean ignoreUnknown) {
        if(ignoreUnknown) {
            this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }
    }

    /**
     * 序列化时忽略null值
     * @param filterNull
     */
    public void setFilterNull(boolean filterNull) {
        if(filterNull) {
            this.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }
    }

    /**
     * 设置默认时间格式
     * @param dateFormat
     */
    public void setDateFormat(String dateFormat) {
        if(dateFormat != null && !dateFormat.equals("")) {
            this.setDateFormat(new SimpleDateFormat(dateFormat));
        }
    }

    public void setDicFormatSerializerModifier(DictFormatSerializerModifier dictFormatSerializerModifier) {
        this.setSerializerFactory(this.getSerializerFactory().withSerializerModifier(dictFormatSerializerModifier));
    }

    public CustomObjectMapper(boolean defaultNull, String defaultNullValue) {
        this.setSerializerFactory(this.getSerializerFactory().withSerializerModifier(new NumberFormatSerializerModifier()));
        this.setSerializerFactory(this.getSerializerFactory().withSerializerModifier(new NullBeanSerializerModifier(defaultNull, defaultNullValue)));

    }

}
