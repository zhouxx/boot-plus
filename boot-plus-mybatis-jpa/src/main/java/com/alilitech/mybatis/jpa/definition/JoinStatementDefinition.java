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
package com.alilitech.mybatis.jpa.definition;


/**
 * 用于生成关联查询的mapping，一个语句可能会有多个mapping
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class JoinStatementDefinition {

    private Class<?> resultType;

    private String property;

    private String column;

    private String nestedSelect;

    private Class<?> javaType;

    public JoinStatementDefinition(Class<?> resultType, String property, String column, String nestedSelect) {
        this.resultType = resultType;
        this.property = property;
        this.column = column;
        this.nestedSelect = nestedSelect;
    }

    public Class<?> getResultType() {
        return resultType;
    }

    public void setResultType(Class<?> resultType) {
        this.resultType = resultType;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public String getNestedSelect() {
        return nestedSelect;
    }

    public void setNestedSelect(String nestedSelect) {
        this.nestedSelect = nestedSelect;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public void setJavaType(Class<?> javaType) {
        this.javaType = javaType;
    }

}
