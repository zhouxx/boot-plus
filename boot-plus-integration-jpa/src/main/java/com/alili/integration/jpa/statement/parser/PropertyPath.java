/**
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
package com.alili.integration.jpa.statement.parser;


import com.alili.integration.jpa.EntityMetaDataRegistry;
import com.alili.integration.jpa.util.ColumnUtils;
import com.alili.integration.jpa.util.CommonUtils;

/**
 *
 * @author Oliver Gierke
 * @author Martin Baumgartner
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class PropertyPath {

    private Class<?> ownerType;

    private Class<?> type;

    private String name;

    private String ColumnName;

    public PropertyPath(Class<?> ownerType, String name) {
        this.ownerType = ownerType;
        this.name = name;
    }

    public Class<?> getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(Class<?> ownerType) {
        this.ownerType = ownerType;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColumnName() {
        return ColumnName;
    }

    public void setColumnName(String columnName) {
        ColumnName = columnName;
    }

    public static PropertyPath from(String name, Class<?> clazz) {
        PropertyPath propertyPath = new PropertyPath(clazz, name);
        String columnName = "";
        if(clazz == null) {
            columnName = CommonUtils.camelToUnderline(name);
        } else {
            columnName = EntityMetaDataRegistry.getInstance().get(clazz).getColumnMetaDataMap().get(name).getColumnName();
        }

        propertyPath.setColumnName(columnName);
        return propertyPath;
    }
}
