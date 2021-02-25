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
package com.alilitech.mybatis.jpa.criteria;

import com.alilitech.mybatis.jpa.criteria.expression.CompoundPredicateExpression;
import com.alilitech.mybatis.jpa.criteria.expression.OrderExpression;
import com.alilitech.mybatis.jpa.criteria.expression.PredicateExpression;

import java.util.Map;

/**
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
public class CriteriaQuery<T> {

    private PredicateExpression.BooleanOperator booleanOperator = PredicateExpression.BooleanOperator.AND;

    private final RenderContext renderContext = new RenderContext();

    private String whereScript;

    private Map<String, Object> paramValues;

    private String orderByScript;

    public CriteriaQuery(Class<T> returnType) {
    }

    public CriteriaQuery(Class<T> returnType, PredicateExpression.BooleanOperator booleanOperator) {
        this(returnType);
        this.booleanOperator = booleanOperator;
    }

    public CriteriaQuery where(PredicateExpression ... predicateExpressions) {
        new CompoundPredicateExpression<T>(booleanOperator, predicateExpressions).render(renderContext);
        whereScript = renderContext.getScript();
        paramValues = renderContext.getParamValues();
        renderContext.clearScript();
        return this;
    }

    public CriteriaQuery orderBy(OrderExpression ...orderExpressions) {
        if(orderExpressions != null && orderExpressions.length > 0) {
            renderContext.renderString("ORDER BY ");
            String split = "";
            for (OrderExpression orderExpression : orderExpressions) {
                renderContext.renderString(split);
                orderExpression.render(renderContext);
                split = ", ";
            }
            orderByScript = renderContext.getScript();
        }
        return this;
    }

    public String getWhereScript() {
        return whereScript;
    }

    public Map<String, Object> getParamValues() {
        return paramValues;
    }

    public String getOrderByScript() {
        return orderByScript;
    }
}
