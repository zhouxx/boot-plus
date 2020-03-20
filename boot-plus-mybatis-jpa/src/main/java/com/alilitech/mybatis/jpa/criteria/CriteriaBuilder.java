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

import com.alilitech.mybatis.jpa.criteria.expression.*;
import com.alilitech.mybatis.jpa.criteria.expression.operator.OperatorExpression;
import com.alilitech.mybatis.jpa.criteria.expression.operator.comparison.*;
import com.alilitech.mybatis.jpa.criteria.expression.operator.like.*;
import com.alilitech.mybatis.jpa.domain.Direction;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
public class CriteriaBuilder<T> {

    private Class<T> domainClass;

    public CriteriaBuilder(Class<T> domainClass) {
        this.domainClass = domainClass;
    }

    public PredicateExpression<T> and(PredicateExpression<T> ...predicates) {
        return new CompoundPredicateExpression<>(PredicateExpression.BooleanOperator.AND, predicates);
    }

    public PredicateExpression<T> or(PredicateExpression<T> ...predicates) {
        return new CompoundPredicateExpression<>(PredicateExpression.BooleanOperator.OR, predicates);
    }

    public PredicateExpression<T> equal(String property, Object value) {
        return this.buildPredicate(property, new EqualExpression<>(), value);
    }

    public PredicateExpression<T> notEqual(String property, Object value) {
        return this.buildPredicate(property, new NotEqualExpression<>(), value);
    }

    public PredicateExpression<T> greaterThan(String property, Object value) {
        return this.buildPredicate(property, new GreaterThanExpression<>(), value);
    }

    public PredicateExpression<T> greaterThanEqual(String property, Object value) {
        return this.buildPredicate(property, new GreaterThanEqualExpression<>(), value);
    }

    public PredicateExpression<T> lessThan(String property, Object value) {
        return this.buildPredicate(property, new LessThanExpression<>(), value);
    }

    public PredicateExpression<T> lessThanEqual(String property, Object value) {
        return this.buildPredicate(property, new LessThanEqualExpression<>(), value);
    }

    public PredicateExpression<T> isNull(String property) {
        return this.buildPredicate(property, new IsNullExpression<>());
    }

    public PredicateExpression<T> isNotNull(String property) {
        return this.buildPredicate(property, new IsNotNullExpression<>());
    }

    public PredicateExpression<T> between(String property, Object value1, Object value2) {
        return this.buildPredicate(property, new BetweenExpression<>(), value1, value2);
    }

    public PredicateExpression<T> notBetween(String property, Object value1, Object value2) {
        return this.buildPredicate(property, new NotBetweenExpression<>(), value1, value2);
    }

    public PredicateExpression<T> in(String property, Object ...values) {
        return this.buildPredicate(property, new InExpression<>(), values);
    }

    public PredicateExpression<T> in(String property, List<?> values) {
        return this.buildPredicate(property, new InExpression<>(), values.toArray());
    }

    public PredicateExpression<T> freeLike(String property, Object value) {
        return this.buildPredicate(property, new FreeLikeExpression<>(), value);
    }

    public PredicateExpression<T> notFreeLike(String property, Object value) {
        return this.buildPredicate(property, new NotFreeLikeExpression<>(), value);
    }

    public PredicateExpression<T> like(String property, Object value) {
        return this.buildPredicate(property, new LikeExpression<>(), value);
    }

    public PredicateExpression<T> notLike(String property, Object value) {
        return this.buildPredicate(property, new NotLikeExpression<>(), value);
    }

    public PredicateExpression<T> startsWith(String property, Object value) {
        return this.buildPredicate(property, new StartsWithExpression<>(), value);
    }

    public PredicateExpression<T> endsWith(String property, Object value) {
        return this.buildPredicate(property, new EndsWithExpression<>(), value);
    }

    private PredicateExpression<T> buildPredicate(String property, OperatorExpression<T> operator, Object ...values) {
        VariableExpression variable = new VariableExpression<T>(domainClass, property);
        if(values != null && values.length > 0) {
            List<ParameterExpression<T>> parameters = Arrays.stream(values).map(value -> new ParameterExpression<T>(value)).collect(Collectors.toList());
            return new SinglePredicateExpression<>(variable, operator, parameters);
        } else {
            return new SinglePredicateExpression<>(variable, operator);
        }
    }

    private PredicateExpression<T> buildPredicate(VariableExpression<T> variable, OperatorExpression<T> operator, ParameterExpression<T> ...parameters) {
        return new SinglePredicateExpression<>(variable, operator, parameters);
    }

    public OrderExpression<T> desc(String property) {
        VariableExpression variable = new VariableExpression<T>(domainClass, property);
        return new OrderExpression<>(variable, Direction.DESC);
    }

    public OrderExpression<T> asc(String property) {
        VariableExpression variable = new VariableExpression<T>(domainClass, property);
        return new OrderExpression<>(variable, Direction.ASC);
    }
}
