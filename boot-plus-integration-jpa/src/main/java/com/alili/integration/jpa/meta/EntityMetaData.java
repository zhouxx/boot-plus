/**
 *    Copyright 2017-2019 the original author or authors.
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
package com.alili.integration.jpa.meta;


import com.alili.integration.jpa.definition.GenericType;
import com.alili.integration.jpa.util.EntityUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class EntityMetaData {

    /** 实体类型 */
    private Class<?> entityType;

    private Class<?> idType;

    /** 表名 */
    private String tableName;

    private String entityName;

    /** 主键column元数据 */
    private ColumnMetaData primaryColumnMetaData;

    /** 列名集 */
    private List<String> columnNameList;

    /** column元数据集 {key-fieldName} */
    private Map<String, ColumnMetaData> columnMetaDataMap;

    private String columnNames;

    private boolean hasTriggerValue;

    public EntityMetaData(Class clazz) {
        this.entityType = clazz;

        // 初始化集合
        columnNameList = new ArrayList<String>();
        columnMetaDataMap = new LinkedHashMap<>();

        init();
    }

    public EntityMetaData(GenericType genericType) {
        this.entityType = (Class)genericType.getDomainType();
        this.idType = (Class)genericType.getIdType();

        // 初始化集合
        columnNameList = new ArrayList<String>();
        columnMetaDataMap = new LinkedHashMap<>();

        init();
    }

    private void init() {
        this.tableName = EntityUtils.getTableName(entityType);
        this.entityName = EntityUtils.getEntityName(entityType);
        //primaryColumnMetaData = new ColumnMetaData(EntityUtils.getPrimaryField(entityType));

        // 持久化字段集
        List<Field> fields = EntityUtils.getPersistentFields(entityType);

        StringBuilder columnNamesTemp = new StringBuilder();

        for (Field field : fields) {
            ColumnMetaData columnMetaData = new ColumnMetaData(field, this);
            if(columnMetaData.isPrimaryKey()) {
                primaryColumnMetaData = columnMetaData;
            }
            if(columnMetaData.getTriggers() != null) {
                this.hasTriggerValue = true;
            }
            columnMetaDataMap.put(field.getName(), columnMetaData);
            if(!columnMetaData.isJoin()) {
                columnNamesTemp.append(", ").append(columnMetaData.getColumnName());
            }
        }
        columnNames = columnNamesTemp.substring(1);
    }

    public Class<?> getEntityType() {
        return entityType;
    }

    public void setEntityType(Class<?> entityType) {
        this.entityType = entityType;
    }

    public Class<?> getIdType() {
        return idType;
    }

    public void setIdType(Class<?> idType) {
        this.idType = idType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public ColumnMetaData getPrimaryColumnMetaData() {
        return primaryColumnMetaData;
    }

    public void setPrimaryColumnMetaData(ColumnMetaData primaryColumnMetaData) {
        this.primaryColumnMetaData = primaryColumnMetaData;
    }

    public List<String> getColumnNameList() {
        return columnNameList;
    }

    public void setColumnNameList(List<String> columnNameList) {
        this.columnNameList = columnNameList;
    }

    public Map<String, ColumnMetaData> getColumnMetaDataMap() {
        return columnMetaDataMap;
    }

    public void setColumnMetaDataMap(Map<String, ColumnMetaData> columnMetaDataMap) {
        this.columnMetaDataMap = columnMetaDataMap;
    }

    public String getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String columnNames) {
        this.columnNames = columnNames;
    }

    public String getColumnNames(String alias) {
        StringBuilder columnNamesTemp = new StringBuilder();
        for(ColumnMetaData columnMetaData : columnMetaDataMap.values()) {
            if(!columnMetaData.isJoin()) {
                columnNamesTemp.append(", ").append(alias + ".").append(columnMetaData.getColumnName());
            }
        }

        return columnNamesTemp.substring(1);
    }

    public boolean isHasTriggerValue() {
        return hasTriggerValue;
    }

    public void setHasTriggerValue(boolean hasTriggerValue) {
        this.hasTriggerValue = hasTriggerValue;
    }

}
