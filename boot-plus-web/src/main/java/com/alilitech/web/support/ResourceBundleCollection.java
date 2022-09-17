/*
 *    Copyright 2017-2022 the original author or authors.
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

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * resource bundle collection
 *
 * @author Zhou Xiaoxiang
 * @since 1.3.10
 */
public class ResourceBundleCollection {

    protected final Map<String, ResourceBundle> resourceBundleMap =
            new ConcurrentHashMap<>();

    public static final ResourceBundleCollection EMPTY = new ResourceBundleCollection();

    private ResourceBundleCollection() {
    }

    public MessageResourceCollection covertToMessageResourceCollection(Locale locale) {
        MessageResourceCollection.MessageResourceCollectionBuilder builder = MessageResourceCollection.newBuilder();
        resourceBundleMap.forEach((s, resourceBundle) -> {
            ReloadableMessageResource messageResource = new ReloadableMessageResource();
            messageResource.putResourceBundle(locale, resourceBundle);
            builder.putMessageResource(s, messageResource);
        });
        return builder.build();
    }

    public void merge(ResourceBundleCollection resourceBundleCollection) {
        this.resourceBundleMap.putAll(resourceBundleCollection.resourceBundleMap);
    }

    public void putResourceBundle(String key, ResourceBundle resourceBundle) {
        resourceBundleMap.put(key, resourceBundle);
    }

    public void putResourceBundle(String key, Map<String, Object> resourceMap) {
        resourceBundleMap.put(key, new ReloadableResourceBundle(resourceMap));
    }

    public boolean containsKey(String key) {
        return resourceBundleMap.containsKey(key);
    }

    public Set<String> getKeys() {
        return resourceBundleMap.keySet();
    }

    public void clear() {
        resourceBundleMap.clear();
    }

    public void removeByKeys(Set<String> keys) {
        if(keys == null || keys.isEmpty()) {
            return;
        }
        for(String key : keys) {
            resourceBundleMap.remove(key);
        }
    }

    public ResourceBundle getResourceBundle(String key) {
        return resourceBundleMap.get(key);
    }

    public static ResourceBundleCollectionBuilder newBuilder() {
        return new ResourceBundleCollectionBuilder();
    }

    public static class ResourceBundleCollectionBuilder {

        protected final ResourceBundleCollection resourceBundleCollection = new ResourceBundleCollection();

        private ResourceBundleCollectionBuilder() {}

        public ResourceBundleCollectionBuilder putResourceBundles(Map<String, ResourceBundle> resourceBundleMap) {
            resourceBundleCollection.resourceBundleMap.putAll(resourceBundleMap);
            return this;
        }

        public ResourceBundleCollectionBuilder putResourceBundlesMap(Map<String, Map<String, Object>> resourceMap) {
            if(resourceMap == null) {
                return this;
            }
            resourceMap.forEach(resourceBundleCollection::putResourceBundle);
            return this;
        }

        public ResourceBundleCollectionBuilder putResourceBundle(String key, ResourceBundle resourceBundle) {
            resourceBundleCollection.putResourceBundle(key, resourceBundle);
            return this;
        }

        public ResourceBundleCollectionBuilder putResourceBundleMap(String key, Map<String, Object> resourceMap) {
            resourceBundleCollection.putResourceBundle(key, resourceMap);
            return this;
        }

        public ResourceBundleCollection build() {
            return resourceBundleCollection;
        }
    }
}
