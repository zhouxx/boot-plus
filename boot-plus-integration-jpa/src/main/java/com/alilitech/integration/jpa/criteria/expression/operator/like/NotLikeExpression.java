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
package com.alilitech.integration.jpa.criteria.expression.operator.like;

import com.alilitech.integration.jpa.criteria.RenderContext;
import com.alilitech.integration.jpa.criteria.expression.Expression;
import com.alilitech.integration.jpa.criteria.expression.ParameterExpression;
import com.alilitech.integration.jpa.criteria.expression.operator.OperatorExpression;

/**
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
public class NotLikeExpression<T> extends OperatorExpression<T> {

    public NotLikeExpression() {
        this.argsQuantity = 1;
    }

    @Override
    public void render(RenderContext renderContext, Expression<T>... expressions) {
        renderContext.renderString("not like");
        renderContext.renderBlank();

        ParameterExpression<T> parameterExpression = (ParameterExpression<T>) expressions[0];
        parameterExpression.formatValue("%{0}%");
        parameterExpression.render(renderContext);
    }
}
