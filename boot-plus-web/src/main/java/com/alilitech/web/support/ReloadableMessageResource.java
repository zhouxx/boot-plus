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

import org.springframework.context.support.AbstractMessageSource;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

/**
 * reloadable message resource
 * @author Zhou Xiaoxiang
 * @since 1.3.10
 */
public class ReloadableMessageResource extends AbstractMessageSource {

    private final Map<Locale, ResourceBundle> localeResourceBundleMap =
            new ConcurrentHashMap<>();

    public void putResourceBundle(Locale locale, ResourceBundle resourceBundle) {
        localeResourceBundleMap.put(locale, resourceBundle);
    }

    public void putResourceBundle(Locale locale, Map<String, Object> resourceMap) {
        localeResourceBundleMap.put(locale, new ReloadableResourceBundle(resourceMap));
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        ResourceBundle bundle = getResourceBundle(locale);
        if (bundle != null) {
            MessageFormat messageFormat = createMessageFormat(bundle.getString(code), locale);
            return messageFormat;
        }
        return null;
    }

    private ResourceBundle getResourceBundle(Locale locale) {
        return localeResourceBundleMap.get(locale);
    }
}
