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
package com.alilitech.mybatis.jpa.criteria.expression;

import com.alilitech.mybatis.jpa.criteria.RenderContext;
import com.alilitech.mybatis.jpa.criteria.expression.operator.OperatorExpression;

import java.util.Arrays;
import java.util.List;

/**
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
public class SinglePredicateExpression<T> implements PredicateExpression<T> {

    private VariableExpression<T> variable;

    private OperatorExpression<T> operator;

    private List<ParameterExpression<T>> parameters;

    public SinglePredicateExpression() {
    }

    public SinglePredicateExpression(VariableExpression<T> variable, OperatorExpression<T> operator) {
        this.variable = variable;
        this.operator = operator;
    }

    public SinglePredicateExpression(VariableExpression<T> variable,
                                     OperatorExpression<T> operator,
                                     List<ParameterExpression<T>> parameters) {
        this.variable = variable;
        this.operator = operator;
        this.parameters = parameters;
    }

    public SinglePredicateExpression(VariableExpression<T> variable,
                                     OperatorExpression<T> operator,
                                     ParameterExpression<T> ...parameters) {
        this.variable = variable;
        this.operator = operator;
        this.parameters = Arrays.asList(parameters);
    }

    @Override
    public BooleanOperator getOperator() {
        return null;
    }

    @Override
    public void render(RenderContext renderContext, Expression<T> ...expressions) {

        variable.render(renderContext);
        renderContext.renderBlank();

        if(parameters != null && parameters.size() <= 0) {
            operator.render(renderContext);
        } else {
            Expression[] array = new Expression[parameters.size()];
            operator.render(renderContext, parameters.toArray(array));
        }
    }
}
