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
package com.alilitech.mybatis.jpa.criteria.specification;

import static com.alilitech.mybatis.jpa.criteria.expression.PredicateExpression.BooleanOperator.*;

/**
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
public class Specifications {

    static EmptySpecificationBuilder<?> emptySpecificationBuilder = new EmptySpecificationBuilder<>();

    private Specifications() {
    }

    public static <T> PredicateBuilder<T> and() {
        return new PredicateBuilder<>(emptySpecificationBuilder, AND);
    }
    public static <T> PredicateBuilder<T> or() {
        return new PredicateBuilder<>(emptySpecificationBuilder, OR);
    }

    public static <T> OrderBuilder<T> order() {
        return new OrderBuilder(emptySpecificationBuilder);
    }

}
