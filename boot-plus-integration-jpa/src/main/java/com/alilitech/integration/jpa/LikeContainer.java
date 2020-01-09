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
package com.alilitech.integration.jpa;

import com.alilitech.integration.jpa.statement.parser.LikeType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * like容器，用于装载所有需要like转换参数的参数
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class LikeContainer {

    private final Map<String, LikeType> likeTypeMap = new ConcurrentHashMap<>();

    private static final LikeContainer likeContainer = new LikeContainer();

    private LikeContainer() {}

    public static LikeContainer getInstance() {
        return likeContainer;
    }

    public void put(String key, LikeType likeType) {
        synchronized (likeTypeMap) {
            likeTypeMap.put(key, likeType);
        }
    }

    public boolean isExist(String key) {
        return likeTypeMap.containsKey(key);
    }

    public LikeType get(String key) {
        return likeTypeMap.get(key);
    }

}
