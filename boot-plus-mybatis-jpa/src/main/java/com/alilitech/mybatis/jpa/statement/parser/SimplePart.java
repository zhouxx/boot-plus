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
package com.alilitech.mybatis.jpa.statement.parser;

import com.alilitech.mybatis.jpa.definition.MethodDefinition;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * Simple predicate sql script, without any conditions
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
public class SimplePart implements Render {

    private PropertyPath propertyPath;

    public SimplePart(String property, Class<?> domainClass, MethodDefinition definition) {
        Assert.hasText(property, "SimplePart property must not be null or empty!");
        Assert.notNull(domainClass, "Type must not be null!");

        this.propertyPath = PropertyPath.from(property, Optional.of(domainClass), definition);
    }

    @Override
    public void render(RenderContext context) {
        context.renderString(StringUtils.isEmpty(context.getVariableAlias()) ? propertyPath.getColumnName() : context.getVariableAlias() + "." + propertyPath.getColumnName());
        context.renderBlank();
        context.renderString("=");
        context.renderBlank();
        context.renderString("#{");
        context.renderString(StringUtils.isEmpty(context.getArgAlias()) ? propertyPath.getName() : context.getArgAlias() + "." + propertyPath.getName());
        context.renderString("}");
    }
}
