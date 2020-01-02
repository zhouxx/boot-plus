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
package com.alilitech.integration.jpa.criteria.specification;

import com.alilitech.integration.jpa.criteria.CriteriaBuilder;
import com.alilitech.integration.jpa.criteria.CriteriaQuery;
import com.alilitech.integration.jpa.criteria.expression.OrderExpression;
import com.alilitech.integration.jpa.criteria.expression.PredicateExpression;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
public class PredicateOrderBuilder<T> extends PredicateBuilder<T> {

    private List<OrderExpression<T>> orders = new ArrayList<>();

    public PredicateOrderBuilder(PredicateExpression.BooleanOperator operator) {
        super(operator);
    }

    public PredicateBuilder<T> asc(String property) {
        specifications.add((cb, query) -> {
            orders.add(cb.asc(property));
            return null;
        });
        return this;
    }

    public PredicateBuilder<T> desc(String property) {
        specifications.add((cb, query) -> {
            orders.add(cb.desc(property));
            return null;
        });
        return this;
    }

    protected void buildSpecification(CriteriaBuilder cb, CriteriaQuery query) {
        super.buildSpecification(cb, query);
        if (!CollectionUtils.isEmpty(orders)) {
            query.orderBy(orders.toArray(new OrderExpression[orders.size()]));
        }
    }
}
