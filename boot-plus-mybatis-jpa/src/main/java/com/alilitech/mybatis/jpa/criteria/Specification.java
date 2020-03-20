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
package com.alilitech.mybatis.jpa.criteria;

import com.alilitech.mybatis.jpa.criteria.expression.CompoundPredicateExpression;
import com.alilitech.mybatis.jpa.criteria.expression.PredicateExpression;

import java.io.Serializable;

/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
public interface Specification<T> extends Serializable {

    default Specification<T> and(Specification<T> other) {
        return (cb, query) -> new CompoundPredicateExpression<T>(PredicateExpression.BooleanOperator.AND, this.toPredicate(cb, query), other.toPredicate(cb, query));
    }

    default Specification<T> or(Specification<T> other) {
        return (cb, query) -> new CompoundPredicateExpression<T>(PredicateExpression.BooleanOperator.OR, this.toPredicate(cb, query), other.toPredicate(cb, query));
    }

    PredicateExpression toPredicate(CriteriaBuilder cb, CriteriaQuery query);

}
