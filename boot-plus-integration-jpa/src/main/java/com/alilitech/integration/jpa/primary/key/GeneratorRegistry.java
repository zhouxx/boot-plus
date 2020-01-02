/**
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
package com.alilitech.integration.jpa.primary.key;

import com.alilitech.integration.jpa.parameter.GenerationType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generator Registry
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class GeneratorRegistry {


    /**
     * registry cache.
     * key is {@link GenerationType } or class extends from {@link KeyGenerator},
     * value is a {@link KeyGenerator} instance
     */
    private Map<Object, KeyGenerator> classMap = new ConcurrentHashMap<>();

    private static final GeneratorRegistry generatorRegistry = new GeneratorRegistry();

    private GeneratorRegistry() {
        register(GenerationType.UUID, new KeyGenerator4UUID());
    }

    public static GeneratorRegistry getInstance() {
        return generatorRegistry;
    }

    public void register(Object key, KeyGenerator value) {
        classMap.put(key, value);
    }

    public KeyGenerator get(Object key) {
        return classMap.get(key);
    }

}
