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
package com.alilitech.datasource.routing.encrypt.resolver;

/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.2.6
 */
public interface EncryptPropertyResolver {

    /**
     * determine whether to resolve
     * @param key property key
     * @param value property value
     * @return result
     */
    boolean supportResolve(String key, String value);

    /**
     * resolve
     * @param value property value
     * @return the result of resolve
     */
    String resolve(String value);

}
