/*
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
package com.alilitech.mybatis.jpa.primary.key;

import com.alilitech.mybatis.jpa.exception.MybatisJpaException;
import com.alilitech.mybatis.jpa.parameter.GenerationType;

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
    private final Map<Object, KeyGenerator> cacheMap = new ConcurrentHashMap<>();

    private static final GeneratorRegistry generatorRegistry = new GeneratorRegistry();

    private GeneratorRegistry() {
        register(GenerationType.UUID, new KeyGenerator4UUID());
    }

    public static GeneratorRegistry getInstance() {
        return generatorRegistry;
    }

    public void register(Object key, KeyGenerator value) {
        cacheMap.put(key, value);
    }

    public KeyGenerator get(Object key) {
        return cacheMap.get(key);
    }

    public KeyGenerator getOrRegister(Class generatorClass) {
        if(!cacheMap.containsKey(generatorClass)) {

            if(KeyGenerator.class.isAssignableFrom(generatorClass)) {
                throw new MybatisJpaException("The generate class of primary key must implement from KeyGenerator.");
            }

            try {
                Object instance = generatorClass.getConstructor().newInstance();
                if (instance instanceof KeyGenerator) {
                    KeyGenerator keyGenerator = (KeyGenerator) instance;
                    // put generator in registry
                    register(generatorClass, keyGenerator);
                }

            } catch (Exception e) {
                throw new MybatisJpaException("occurs errors when try to init primary key generator: " + e.getMessage());
            }
        }

        return cacheMap.get(generatorClass);
    }
}
