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
package com.alilitech.mybatis.jpa.criteria.specification;

import com.alilitech.mybatis.jpa.criteria.CriteriaBuilder;
import com.alilitech.mybatis.jpa.criteria.CriteriaQuery;
import com.alilitech.mybatis.jpa.criteria.expression.OrderExpression;
import com.alilitech.mybatis.jpa.domain.Direction;
import com.alilitech.mybatis.jpa.domain.Order;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
public class OrderBuilder<T> extends AbstractSpecificationBuilder<T> {

    private List<OrderExpression<T>> orderExpressions = new ArrayList<>();

    public OrderBuilder(SpecificationBuilder<T> specificationBuilder) {
        super(specificationBuilder);
    }

    public OrderBuilder<T> asc(String property) {
        specifications.add((cb, query) -> {
            orderExpressions.add(cb.asc(property));
            return null;
        });
        return this;
    }

    public OrderBuilder<T> desc(String property) {
        specifications.add((cb, query) -> {
            orderExpressions.add(cb.desc(property));
            return null;
        });
        return this;
    }

    public OrderBuilder<T> orders(Order ...orders) {
        if(orders != null && orders.length > 0) {
            for(Order order : orders) {
                specifications.add((cb, query) -> {
                    if(order.getDirection().equals(Direction.ASC)) {
                        orderExpressions.add(cb.asc(order.getProperty()));
                    } else {
                        orderExpressions.add(cb.desc(order.getProperty()));
                    }
                    return null;
                });
            }
        }

        return this;
    }

    @Override
    public void build(CriteriaBuilder cb, CriteriaQuery query) {
        specificationBuilder.build(cb, query);
        for (int i = 0; i < specifications.size(); i++) {
            specifications.get(i).toPredicate(cb, query);
        }
        if (!CollectionUtils.isEmpty(orderExpressions)) {
            query.orderBy(orderExpressions.toArray(new OrderExpression[orderExpressions.size()]));
        }
    }

}
