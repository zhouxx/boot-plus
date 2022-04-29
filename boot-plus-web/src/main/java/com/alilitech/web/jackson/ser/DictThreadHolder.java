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
package com.alilitech.web.jackson.ser;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Zhou Xiaoxiang
 * @since 1.3.8
 */
public class DictThreadHolder {

    private DictThreadHolder() {
    }

    private static ThreadLocal<Set<String>> notExistDicts = ThreadLocal.withInitial(HashSet::new);

    public static void put(String key) {
        notExistDicts.get().add(key);
    }

    public static boolean exist(String key) {
        return notExistDicts.get().contains(key);
    }

    public static void clear() {
        notExistDicts.get().clear();
    }

    public static void remove() {
        notExistDicts.remove();
    }

}
