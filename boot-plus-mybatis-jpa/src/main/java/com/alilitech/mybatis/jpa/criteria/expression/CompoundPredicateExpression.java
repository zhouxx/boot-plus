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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
public class CompoundPredicateExpression<T> implements PredicateExpression<T> {

    private List<PredicateExpression<T>> predicates = new ArrayList<>();

    private BooleanOperator operator;

    public CompoundPredicateExpression(BooleanOperator operator) {
        this.operator = operator;
    }

    public CompoundPredicateExpression(BooleanOperator operator, PredicateExpression<T>... predicates) {
        this.operator = operator;
        this.predicates.addAll(Arrays.asList(predicates));
    }

    public CompoundPredicateExpression(BooleanOperator operator, List<PredicateExpression<T>> predicates) {
        this.predicates = predicates;
        this.operator = operator;
    }

    private void applyExpressions(List<PredicateExpression<T>> expressions) {
        this.predicates.clear();
        this.predicates.addAll( expressions );
    }


    @Override
    public void render(RenderContext renderContext, Expression<T> ...expressions) {

        if(predicates.isEmpty()) {
            renderContext.renderString("1=1");
            return;
        }

        if(predicates.size() == 1) {
            predicates.get(0).render(renderContext);
            return;
        }

        renderContext.renderString("( ");
        String sep = "";

        for(PredicateExpression predicateExpression : predicates) {
            renderContext.renderString(sep);
            predicateExpression.render(renderContext);
            sep = " " + operator.toString() + " ";
        }

        renderContext.renderString(") ");

    }

    @Override
    public BooleanOperator getOperator() {
        return operator;
    }
}
