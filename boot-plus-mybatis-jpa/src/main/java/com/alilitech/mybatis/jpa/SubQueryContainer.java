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
package com.alilitech.mybatis.jpa;

import com.alilitech.mybatis.jpa.anotation.SubQuery;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SubQuery container
 * the key is "namespace.methodName", and the value is the subQuery of the definitions
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class SubQueryContainer {

    private final Map<String, SubQuery> subQueryMap = new ConcurrentHashMap<>();

    private static final SubQueryContainer subQueryContainer = new SubQueryContainer();

    private SubQueryContainer() {}

    public static SubQueryContainer getInstance() {
        return subQueryContainer;
    }

    public void put(String key, SubQuery subQuery) {
        synchronized (subQueryMap) {
            subQueryMap.put(key, subQuery);
        }
    }

    public boolean isExist(String key) {
        return subQueryMap.containsKey(key);
    }

    public SubQuery get(String key) {
        return subQueryMap.get(key);
    }

}
