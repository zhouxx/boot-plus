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
package com.alilitech.web.support;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.lang.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * message source collection
 *
 * @author Zhou Xiaoxiang
 * @since 1.3.10
 */
public class MessageResourceCollection {

    public static final MessageResourceCollection EMPTY = new MessageResourceCollection();

    private final Map<String, MessageSource> messageSourceMap = new ConcurrentHashMap<>();

    public void merge(MessageResourceCollection messageResourceCollection) {
        this.messageSourceMap.putAll(messageResourceCollection.messageSourceMap);
    }

    public void putMessageResource(String key, MessageSource messageSource) {
        messageSourceMap.put(key, messageSource);
    }

    public boolean containsKey(String key) {
        return messageSourceMap.containsKey(key);
    }

    public final String getMessage(String key, String code, @Nullable Object[] args, @Nullable String defaultMessage, Locale locale) {
        if(!messageSourceMap.containsKey(key)) {
            return null;
        }
        return messageSourceMap.get(key).getMessage(code, args, defaultMessage, locale);
    }

    public final String getMessage(String key, String code, @Nullable Object[] args, Locale locale) throws NoSuchMessageException {
        if(!messageSourceMap.containsKey(key)) {
            return null;
        }
        return messageSourceMap.get(key).getMessage(code, args, locale);
    }

    public void clear() {
        messageSourceMap.clear();
    }

    public Set<String> getKeys() {
        return messageSourceMap.keySet();
    }

    public static class MessageResourceCollectionBuilder {

        private final MessageResourceCollection messageResourceCollection = new MessageResourceCollection();

        private MessageResourceCollectionBuilder() {}

        public static MessageResourceCollectionBuilder newBuilder() {
            return new MessageResourceCollectionBuilder();
        }

        public MessageResourceCollectionBuilder putMessageResource(String key, MessageSource messageSource) {
            messageResourceCollection.putMessageResource(key, messageSource);
            return this;
        }

        public MessageResourceCollection build() {
            return messageResourceCollection;
        }



    }

}
