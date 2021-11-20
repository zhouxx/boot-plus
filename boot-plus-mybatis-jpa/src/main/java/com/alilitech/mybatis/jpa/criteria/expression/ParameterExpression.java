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
package com.alilitech.mybatis.jpa.criteria.expression;

import com.alilitech.mybatis.jpa.criteria.RenderContext;

/**
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
public class ParameterExpression<T> implements AtomicExpression<T> {

    private Object paramValue;

    public ParameterExpression(Object paramValue) {
        this.paramValue = paramValue;
    }

    public void formatValue(String pattern) {
        this.paramValue = pattern.replace("\\{0\\}", paramValue.toString());
    }

    @Override
    public void render(RenderContext renderContext, Expression<T> ...expressions) {
        String paramName = renderContext.getParamPrefix() + renderContext.getParamIndexAndIncrement();
        renderContext.getParamValues().put(paramName, paramValue);
        renderContext.renderString("#{");
        renderContext.renderString(renderContext.getParamPrefixPrefix() + paramName);
        renderContext.renderString("}");
    }

}
