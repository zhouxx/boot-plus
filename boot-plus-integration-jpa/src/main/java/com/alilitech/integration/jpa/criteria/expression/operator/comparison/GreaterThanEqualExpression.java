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
package com.alilitech.integration.jpa.criteria.expression.operator.comparison;

import com.alilitech.integration.jpa.criteria.RenderContext;
import com.alilitech.integration.jpa.criteria.expression.Expression;

/**
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
public class GreaterThanEqualExpression<T> extends ComparisonExpression<T> {

    public GreaterThanEqualExpression() {
        this.argsQuantity = 1;
    }

    @Override
    public void render(RenderContext renderContext, Expression<T>...expressions) {
        renderContext.renderString(">=");
        renderContext.renderBlank();
        expressions[0].render(renderContext);
    }
}
