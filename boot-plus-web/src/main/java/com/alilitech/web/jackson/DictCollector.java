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

import com.alilitech.web.support.MessageResourceCollection;
import com.alilitech.web.support.ResourceBundleCollection;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * 字典收集器
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public interface DictCollector {

    /**
     * 实现这个接口的将全部被加入字典，加入字典的Collector必须被spring管理
     * @return  输出字典map
     */
    default ResourceBundleCollection findDictAndValues() {
        return ResourceBundleCollection.EMPTY;
    }

    /**
     * 带有国际化的字典, 默认兼容原有的不带国际化的字典
     */
    default MessageResourceCollection findLocaleDictAndValues() {
        Locale localeDefault = Locale.getDefault();
        ResourceBundleCollection resourceBundleCollection = this.findDictAndValues();

        if(resourceBundleCollection == null) {
            return MessageResourceCollection.EMPTY;
        }

        MessageResourceCollection messageResourceCollection = resourceBundleCollection.covertToMessageResourceCollection(localeDefault);

        return messageResourceCollection;
    }

}
