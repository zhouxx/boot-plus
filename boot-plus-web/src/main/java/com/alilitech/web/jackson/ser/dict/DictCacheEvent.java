/*
 *    Copyright 2017-present the original author or authors.
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
package com.alilitech.web.jackson.ser.dict;

import com.alilitech.web.jackson.DictCollector;
import org.springframework.context.ApplicationEvent;

/**
 * @author Zhou Xiaoxiang
 * @since 2.0.0
 */
public class DictCacheEvent extends ApplicationEvent {

    protected DictCollector dictCollector;

    /**
     * Create a new ContextStartedEvent.
     *
     * @param source the {@code ApplicationContext} that the event is raised for
     *               (must not be {@code null})
     */
    public DictCacheEvent(Object source) {
        super(source);
    }

    public DictCacheEvent(Object source, DictCollector dictCollector) {
        this(source);
        this.dictCollector = dictCollector;
    }

    public DictCollector getDictCollector() {
        return dictCollector;
    }
}
