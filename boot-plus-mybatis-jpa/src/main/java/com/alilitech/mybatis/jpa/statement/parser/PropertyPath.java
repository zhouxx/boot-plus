/*
 *    Copyright 2008-2019 the original author or authors.
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


import com.alilitech.mybatis.jpa.EntityMetaDataRegistry;
import com.alilitech.mybatis.jpa.definition.MethodDefinition;
import com.alilitech.mybatis.jpa.exception.PropertyNotFoundException;
import com.alilitech.mybatis.jpa.util.CommonUtils;

import java.util.Optional;

/**
 *
 * @author Oliver Gierke
 * @author Martin Baumgartner
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class PropertyPath {

    private String name;

    private String columnName;

    public PropertyPath(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public static PropertyPath from(String name, Optional<Class<?>> clazzOptional, MethodDefinition methodDefinition) {
        PropertyPath propertyPath = new PropertyPath(name);
        String columnName = "";
        if(!clazzOptional.isPresent()) {
            columnName = CommonUtils.camelToUnderline(name);
        } else {
            if(!EntityMetaDataRegistry.getInstance().get(clazzOptional.get()).getColumnMetaDataMap().containsKey(name)) {
                throw new PropertyNotFoundException(clazzOptional.get(), name, methodDefinition.getStatementId(), "Can not found property!");
            }
            columnName = EntityMetaDataRegistry.getInstance().get(clazzOptional.get()).getColumnMetaDataMap().get(name).getColumnName();
        }

        propertyPath.setColumnName(columnName);
        return propertyPath;
    }
}
