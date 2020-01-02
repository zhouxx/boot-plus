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
package com.alilitech.integration.jpa.criteria.expression;

import com.alilitech.integration.jpa.criteria.RenderContext;
import com.alilitech.integration.jpa.domain.Direction;

/**
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
public class OrderExpression<T> implements Expression {

    private VariableExpression<T> variable;

    private Direction direction;

    public OrderExpression(VariableExpression<T> variable, Direction direction) {
        this.variable = variable;
        this.direction = direction;
    }

    @Override
    public void render(RenderContext renderContext, Expression ...expressions) {
        variable.render(renderContext);
        renderContext.renderBlank();
        renderContext.renderString(direction.toString());
    }
}
