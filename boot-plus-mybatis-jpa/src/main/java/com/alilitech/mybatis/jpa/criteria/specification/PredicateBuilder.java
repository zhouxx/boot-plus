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

import com.alilitech.mybatis.jpa.criteria.CriteriaBuilder;
import com.alilitech.mybatis.jpa.criteria.CriteriaQuery;
import com.alilitech.mybatis.jpa.criteria.Specification;
import com.alilitech.mybatis.jpa.criteria.expression.PredicateExpression;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
public class PredicateBuilder<T> extends AbstractSpecificationBuilder<T> {

    protected List<Specification<T>> specifications = new ArrayList<>();

    protected PredicateExpression.BooleanOperator operator;

    protected List<PredicateExpression<T>> predicates = new ArrayList<>();

    public PredicateBuilder(SpecificationBuilder<T> specificationBuilder) {
        super(specificationBuilder);
    }

    public PredicateBuilder(SpecificationBuilder specificationBuilder, PredicateExpression.BooleanOperator operator) {
        super(specificationBuilder);
        this.operator = operator;
    }

    public PredicateBuilder<T> equal(String property, Object value) {
        specifications.add((cb, query) -> cb.equal(property, value));
        return this;
    }

    public PredicateBuilder<T> equal(boolean condition, String property, Object value) {
        if(condition) {
            specifications.add((cb, query) -> cb.equal(property, value));
        }
        return this;
    }

    public PredicateBuilder<T> notEqual(String property, Object value) {
        specifications.add((cb, query) -> cb.notEqual(property, value));
        return this;
    }

    public PredicateBuilder<T> notEqual(boolean condition, String property, Object value) {
        if(condition) {
            specifications.add((cb, query) -> cb.notEqual(property, value));
        }
        return this;
    }

    public PredicateBuilder<T> greaterThan(String property, Object value) {
        specifications.add((cb, query) -> cb.greaterThan(property, value));
        return this;
    }

    public PredicateBuilder<T> greaterThan(boolean condition, String property, Object value) {
        if(condition) {
            specifications.add((cb, query) -> cb.greaterThan(property, value));
        }
        return this;
    }

    public PredicateBuilder<T> greaterThanEqual(String property, Object value) {
        specifications.add((cb, query) -> cb.greaterThanEqual(property, value));
        return this;
    }

    public PredicateBuilder<T> greaterThanEqual(boolean condition, String property, Object value) {
        if(condition) {
            specifications.add((cb, query) -> cb.greaterThanEqual(property, value));
        }
        return this;
    }

    public PredicateBuilder<T> lessThan(String property, Object value) {
        specifications.add((cb, query) -> cb.lessThan(property, value));
        return this;
    }

    public PredicateBuilder<T> lessThan(boolean condition, String property, Object value) {
        if(condition) {
            specifications.add((cb, query) -> cb.lessThan(property, value));
        }
        return this;
    }

    public PredicateBuilder<T> lessThanEqual(String property, Object value) {
        specifications.add((cb, query) -> cb.lessThanEqual(property, value));
        return this;
    }

    public PredicateBuilder<T> lessThanEqual(boolean condition, String property, Object value) {
        if(condition) {
            specifications.add((cb, query) -> cb.lessThanEqual(property, value));
        }
        return this;
    }

    public PredicateBuilder<T> isNull(String property) {
        specifications.add((cb, query) -> cb.isNull(property));
        return this;
    }

    public PredicateBuilder<T> isNull(boolean condition, String property) {
        if(condition) {
            specifications.add((cb, query) -> cb.isNull(property));
        }
        return this;
    }

    public PredicateBuilder<T> isNotNull(String property) {
        specifications.add((cb, query) -> cb.isNotNull(property));
        return this;
    }

    public PredicateBuilder<T> isNotNull(boolean condition, String property) {
        if(condition) {
            specifications.add((cb, query) -> cb.isNotNull(property));
        }
        return this;
    }

    public PredicateBuilder<T> between(String property, Object value1, Object value2) {
        specifications.add((cb, query) -> cb.between(property, value1, value2));
        return this;
    }

    public PredicateBuilder<T> between(boolean condition, String property, Object value1, Object value2) {
        if(condition) {
            specifications.add((cb, query) -> cb.between(property, value1, value2));
        }
        return this;
    }

    public PredicateBuilder<T> notBetween(String property, Object value1, Object value2) {
        specifications.add((cb, query) -> cb.notBetween(property, value1, value2));
        return this;
    }

    public PredicateBuilder<T> notBetween(boolean condition, String property, Object value1, Object value2) {
        if(condition) {
            specifications.add((cb, query) -> cb.notBetween(property, value1, value2));
        }
        return this;
    }

    public PredicateBuilder<T> in(String property, Object ...values) {
        specifications.add((cb, query) -> cb.in(property, values));
        return this;
    }

    public PredicateBuilder<T> in(boolean condition, String property, Object ...values) {
        if(condition) {
            specifications.add((cb, query) -> cb.in(property, values));
        }
        return this;
    }

    public PredicateBuilder<T> in(String property, List<?> values) {
        specifications.add((cb, query) -> cb.in(property, values));
        return this;
    }

    public PredicateBuilder<T> in(boolean condition, String property, List<?> values) {
        if(condition) {
            specifications.add((cb, query) -> cb.in(property, values));
        }
        return this;
    }

    public PredicateBuilder<T> freeLike(String property, Object value) {
        specifications.add((cb, query) -> cb.freeLike(property, value));
        return this;
    }

    public PredicateBuilder<T> freeLike(boolean condition, String property, Object value) {
        if(condition) {
            specifications.add((cb, query) -> cb.freeLike(property, value));
        }
        return this;
    }

    public PredicateBuilder<T> like(String property, Object value) {
        specifications.add((cb, query) -> cb.like(property, value));
        return this;
    }

    public PredicateBuilder<T> like(boolean condition, String property, Object value) {
        if(condition) {
            specifications.add((cb, query) -> cb.like(property, value));
        }
        return this;
    }

    public PredicateBuilder<T> startsWith(String property, Object value) {
        specifications.add((cb, query) -> cb.startsWith(property, value));
        return this;
    }

    public PredicateBuilder<T> startsWith(boolean condition, String property, Object value) {
        if(condition) {
            specifications.add((cb, query) -> cb.startsWith(property, value));
        }
        return this;
    }

    public PredicateBuilder<T> endsWith(String property, Object value) {
        specifications.add((cb, query) -> cb.endsWith(property, value));
        return this;
    }

    public PredicateBuilder<T> endsWith(boolean condition, String property, Object value) {
        if(condition) {
            specifications.add((cb, query) -> cb.endsWith(property, value));
        }
        return this;
    }

    public PredicateBuilder<T> nested(Consumer<CompoundPredicateBuilder<T>> consumer) {
        CompoundPredicateBuilder<T> predicateBuilder = new CompoundPredicateBuilder<>();
        consumer.accept(predicateBuilder);
        specifications.add(predicateBuilder.build());
        return this;
    }

    @Override
    public void build(CriteriaBuilder cb, CriteriaQuery query) {
        for (int i = 0; i < specifications.size(); i++) {
            PredicateExpression predicateExpression = specifications.get(i).toPredicate(cb, query);
            if(predicateExpression != null) {
                this.predicates.add(predicateExpression);
            }
        }
        if (!CollectionUtils.isEmpty(predicates)) {
            if(PredicateExpression.BooleanOperator.OR.equals(operator)) {
                query.where(cb.or(predicates.toArray(new PredicateExpression[predicates.size()])));
            } else {
                query.where(cb.and(predicates.toArray(new PredicateExpression[predicates.size()])));
            }
        }
    }
}
