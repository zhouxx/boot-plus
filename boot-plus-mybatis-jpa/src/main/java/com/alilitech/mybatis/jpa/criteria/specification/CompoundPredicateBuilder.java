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
package com.alilitech.mybatis.jpa.criteria.specification;

import com.alilitech.mybatis.jpa.criteria.Specification;
import com.alilitech.mybatis.jpa.criteria.expression.CompoundPredicateExpression;
import com.alilitech.mybatis.jpa.criteria.expression.PredicateExpression;
import org.springframework.util.CollectionUtils;

/**
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
public class CompoundPredicateBuilder<T> extends PredicateBuilder<T> {

    public CompoundPredicateBuilder<T> and() {
        this.operator = PredicateExpression.BooleanOperator.AND;
        return this;
    }

    public CompoundPredicateBuilder<T> or() {
        this.operator = PredicateExpression.BooleanOperator.OR;
        return this;
    }

    public CompoundPredicateBuilder() {
        super(new EmptySpecificationBuilder<>());
        this.operator = PredicateExpression.BooleanOperator.AND;
    }

    @Override
    public Specification<T> build() {
        return (cb, query) -> {
            for (Specification<T> specification : specifications) {
                PredicateExpression<T> predicateExpression = specification.toPredicate(cb, query);
                if (predicateExpression != null) {
                    this.predicates.add(predicateExpression);
                }
            }
            if (!CollectionUtils.isEmpty(predicates)) {
                return new CompoundPredicateExpression<>(this.operator, predicates);
            }
            return null;
        };
    }

}
