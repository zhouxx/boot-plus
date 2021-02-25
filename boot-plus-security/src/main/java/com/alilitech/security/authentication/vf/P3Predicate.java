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
package com.alilitech.security.authentication.vf;

/**
 * @author Zhou Xiaoxiang
 * @since 1.2.6
 */
public interface P3Predicate<T1, T2, T3> {

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param t1 the input argument
     * @param t2 the input argument
     * @param t3 the input argument
     * @return {@code true} if the input argument matches the predicate,
     * otherwise {@code false}
     */
    boolean test(T1 t1, T2 t2, T3 t3);

}
