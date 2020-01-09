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
package com.alilitech.integration.jpa.meta;


import com.alilitech.integration.jpa.JoinType;
import com.alilitech.integration.jpa.domain.Order;

import javax.persistence.JoinColumn;
import java.lang.reflect.Type;
import java.util.List;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class JoinColumnMetaData {

    private JoinType joinType;

    private boolean isCollection;

    private Type entityType;

    private Type joinEntityType;

    //最终需要设置到哪个对象里去
    private String currentProperty;

    //中间表名称
    private String joinTableName;
    //关联字段类型
    private Class<?> propertyType;
    //当前对象的属性
    private String property = "";
    //引用对方对象的属性
    private String referencedProperty = "";
    //关联列名称
    private String columnName;
    //被关联的列名称(中间表与当前表关联的字段名称)
    private String referencedColumnName;

    private Class<?> inversePropertyType;

    private String inverseProperty = "";

    private String inverseReferencedProperty = "";

    private String inverseColumnName;
    //(中间表与被关联表关联的字段名称)
    private String inverseReferencedColumnName;

    private String mappedProperty;

    private JoinColumn joinColumn;

    private List<String> excludes;

    private List<String> includes;

    private List<Order> orders;

    public JoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(JoinType joinType) {
        this.joinType = joinType;
    }

    public boolean isCollection() {
        return isCollection;
    }

    public void setCollection(boolean collection) {
        isCollection = collection;
    }

    public Type getEntityType() {
        return entityType;
    }

    public void setEntityType(Type entityType) {
        this.entityType = entityType;
    }

    public Type getJoinEntityType() {
        return joinEntityType;
    }

    public void setJoinEntityType(Type joinEntityType) {
        this.joinEntityType = joinEntityType;
    }

    public String getCurrentProperty() {
        return currentProperty;
    }

    public void setCurrentProperty(String currentProperty) {
        this.currentProperty = currentProperty;
    }

    public String getJoinTableName() {
        return joinTableName;
    }

    public void setJoinTableName(String joinTableName) {
        this.joinTableName = joinTableName;
    }

    public Class<?> getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(Class<?> propertyType) {
        this.propertyType = propertyType;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getReferencedProperty() {
        return referencedProperty;
    }

    public void setReferencedProperty(String referencedProperty) {
        this.referencedProperty = referencedProperty;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getReferencedColumnName() {
        return referencedColumnName;
    }

    public void setReferencedColumnName(String referencedColumnName) {
        this.referencedColumnName = referencedColumnName;
    }

    public Class<?> getInversePropertyType() {
        return inversePropertyType;
    }

    public void setInversePropertyType(Class<?> inversePropertyType) {
        this.inversePropertyType = inversePropertyType;
    }

    public String getInverseProperty() {
        return inverseProperty;
    }

    public void setInverseProperty(String inverseProperty) {
        this.inverseProperty = inverseProperty;
    }

    public String getInverseReferencedProperty() {
        return inverseReferencedProperty;
    }

    public void setInverseReferencedProperty(String inverseReferencedProperty) {
        this.inverseReferencedProperty = inverseReferencedProperty;
    }

    public String getInverseColumnName() {
        return inverseColumnName;
    }

    public void setInverseColumnName(String inverseColumnName) {
        this.inverseColumnName = inverseColumnName;
    }

    public String getInverseReferencedColumnName() {
        return inverseReferencedColumnName;
    }

    public void setInverseReferencedColumnName(String inverseReferencedColumnName) {
        this.inverseReferencedColumnName = inverseReferencedColumnName;
    }

    public String getMappedProperty() {
        return mappedProperty;
    }

    public void setMappedProperty(String mappedProperty) {
        this.mappedProperty = mappedProperty;
    }

    public JoinColumn getJoinColumn() {
        return joinColumn;
    }

    public void setJoinColumn(JoinColumn joinColumn) {
        this.joinColumn = joinColumn;
    }

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    public List<String> getIncludes() {
        return includes;
    }

    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }
}
