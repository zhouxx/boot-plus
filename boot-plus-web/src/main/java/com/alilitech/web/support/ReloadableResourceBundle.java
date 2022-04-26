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

import sun.util.ResourceBundleEnumeration;

import java.util.Enumeration;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * reloadable resource bundle
 * @author Zhou Xiaoxiang
 * @since 1.3.10
 */
public class ReloadableResourceBundle extends ResourceBundle {

    public ReloadableResourceBundle(Map<String, Object> lookup) {
        this.lookup = lookup;
    }

    public void putAll(Map<String, Object> lookup) {
        this.lookup = lookup;
    }

    @Override
    protected Object handleGetObject(String key) {
        if (key == null) {
            throw new NullPointerException();
        }
        return lookup.get(key);
    }

    @Override
    public Enumeration<String> getKeys() {
        return new ResourceBundleEnumeration(lookup.keySet(), null);
    }

    // ==================privates====================
    private Map<String, Object> lookup;
}
