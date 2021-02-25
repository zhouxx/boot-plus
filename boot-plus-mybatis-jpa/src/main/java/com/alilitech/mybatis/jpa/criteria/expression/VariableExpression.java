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

import com.alilitech.mybatis.jpa.EntityMetaDataRegistry;
import com.alilitech.mybatis.jpa.criteria.RenderContext;

/**
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
public class VariableExpression<T> implements AtomicExpression<T> {

    private String variableName;

    public VariableExpression() {
    }

    public VariableExpression(Class<T> domainClass, String variableName) {
        this.setVariableName(domainClass, variableName);
    }

    public void setVariableName(Class<T> domainClass, String variableName) {
        this.variableName = EntityMetaDataRegistry.getInstance().get(domainClass).getColumnMetaDataMap().get(variableName).getColumnName();
    }

    @Override
    public void render(RenderContext renderContext, Expression<T> ...expressions) {
        renderContext.renderString(variableName);
    }
}
