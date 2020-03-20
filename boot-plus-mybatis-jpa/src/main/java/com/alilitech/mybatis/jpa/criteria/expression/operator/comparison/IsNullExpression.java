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
package com.alilitech.mybatis.jpa.criteria.expression.operator.comparison;

import com.alilitech.mybatis.jpa.criteria.RenderContext;
import com.alilitech.mybatis.jpa.criteria.expression.Expression;

/**
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
public class IsNullExpression<T> extends ComparisonExpression<T> {

    public IsNullExpression() {
        this.argsQuantity = 0;
    }

    @Override
    public void render(RenderContext renderContext, Expression<T>...expressions) {
        //valid args quantity
        validArgsQuantity(expressions);

        renderContext.renderString("is null");
    }

}
