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

    public static MessageResourceCollection EMPTY = new MessageResourceCollection();

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
