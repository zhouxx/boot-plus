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
package com.alilitech.mybatis.jpa.meta;

import com.alilitech.mybatis.jpa.anotation.*;
import com.alilitech.mybatis.jpa.anotation.GeneratedValue;
import com.alilitech.mybatis.jpa.primary.key.KeyGenerator;
import com.alilitech.mybatis.jpa.util.ColumnUtils;
import com.alilitech.mybatis.jpa.parameter.GenerationType;
import com.alilitech.mybatis.jpa.JoinType;
import org.apache.ibatis.type.JdbcType;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class ColumnMetaData {

    private EntityMetaData entityMetaData;

    /** 是否为主键 */
    private boolean primaryKey;
    //主键生成策略
    private GenerationType idGenerationType;
    //根据此序列生成
    private String sequenceName;
    //生成主键的类
    private Class<? extends KeyGenerator> idGeneratorClass;

    /** Java fieldName */
    private String property;

    private String columnName;

    /** fieldType */
    private Class<?> type;

    /** mybatis jdbcTypeAlias */
    private String jdbcTypeAlias;

    /** mybatis jdbcType */
    private JdbcType jdbcType;

    /** 持久化字段 */
    private Field field;

    //是否是关联字段
    private boolean join;

    private JoinColumnMetaData joinColumnMetaData;

    private List<Trigger> triggers;

    public ColumnMetaData(Field field, EntityMetaData entityMetaData) {
        this.field = field;
        this.entityMetaData = entityMetaData;
        init();
    }

    public ColumnMetaData(Field field) {
        this.field = field;
        init();
    }

    private void init() {
        this.property = field.getName();
        this.columnName = ColumnUtils.getColumnName(field);
        this.type = field.getType();

        if(type.equals(java.util.Date.class)) {
            jdbcType = JdbcType.TIMESTAMP;
        }

        if(field.isAnnotationPresent(Id.class)) {
            primaryKey = true;
        }
        if(field.isAnnotationPresent(GeneratedValue.class)) {
            GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
            idGenerationType = generatedValue.value();
            sequenceName = generatedValue.sequenceName();
            idGeneratorClass = generatedValue.generatorClass();
        }

        if(field.isAnnotationPresent(TriggerValue.class)) {
            triggers = Arrays.asList(field.getAnnotation(TriggerValue.class).triggers());
        }

        if(field.isAnnotationPresent(OneToOne.class)
                || field.isAnnotationPresent(OneToMany.class)
                || field.isAnnotationPresent(ManyToMany.class)
                || field.isAnnotationPresent(ManyToOne.class)) {
            join = true;
            joinColumnMetaData = new JoinColumnMetaData();
            joinColumnMetaData.setEntityType(entityMetaData.getEntityType());
        }
        if(join) {
            Type type = null;
            //若是集合，则取泛型类型
            if(Collection.class.isAssignableFrom(field.getType())) {
                joinColumnMetaData.setCollection(true);
                Type genericType = field.getGenericType();
                if(genericType instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) genericType;
                    type = parameterizedType.getActualTypeArguments()[0];
                }

            } else {
                type = field.getType();
            }
            joinColumnMetaData.setJoinEntityType(type);

            //若是OneToOne or OneToMany直接获取JoinColumn
            if(field.isAnnotationPresent(javax.persistence.JoinColumn.class)) {
                javax.persistence.JoinColumn joinColumnAnnotation = field.getAnnotation(javax.persistence.JoinColumn.class);
                joinColumnMetaData.setProperty(joinColumnAnnotation.name());
                joinColumnMetaData.setReferencedProperty(joinColumnAnnotation.referencedColumnName());
            }

            //若是ManyToMany,则获取JoinTable
            if(field.isAnnotationPresent(javax.persistence.JoinTable.class)) {
                JoinTable joinTable = field.getAnnotation(javax.persistence.JoinTable.class);
                joinColumnMetaData.setJoinTableName(joinTable.name());

                //判断是否存在joinColumns
                if(joinTable.joinColumns().length > 0) {
                    javax.persistence.JoinColumn joinColumnAnnotation = joinTable.joinColumns()[0];
                    joinColumnMetaData.setProperty(joinColumnAnnotation.name());
                    joinColumnMetaData.setReferencedProperty(joinColumnAnnotation.referencedColumnName());
                }

                //判断是否存在inverseJoinColumns
                if(joinTable.inverseJoinColumns().length > 0) {
                    javax.persistence.JoinColumn joinColumnAnnotationInverse = joinTable.inverseJoinColumns()[0];
                    joinColumnMetaData.setInverseProperty(joinColumnAnnotationInverse.name());
                    joinColumnMetaData.setInverseReferencedProperty(joinColumnAnnotationInverse.referencedColumnName());
                }
            }

            if(field.isAnnotationPresent(OneToOne.class) ) {
                joinColumnMetaData.setJoinType(JoinType.OneToOne);
                if(!StringUtils.isEmpty(field.getAnnotation(OneToOne.class).mappedBy())) {
                    joinColumnMetaData.setMappedProperty(field.getAnnotation(OneToOne.class).mappedBy());
                }
            } else if(field.isAnnotationPresent(OneToMany.class) ) {
                joinColumnMetaData.setJoinType(JoinType.OneToMany);
                if(!StringUtils.isEmpty(field.getAnnotation(OneToMany.class).mappedBy())) {
                    joinColumnMetaData.setMappedProperty(field.getAnnotation(OneToMany.class).mappedBy());
                }
            } else if(field.isAnnotationPresent(ManyToMany.class)) {
                joinColumnMetaData.setJoinType(JoinType.ManyToMany);
                if(!StringUtils.isEmpty(field.getAnnotation(ManyToMany.class).mappedBy())) {
                    joinColumnMetaData.setMappedProperty(field.getAnnotation(ManyToMany.class).mappedBy());
                }
            } else if(field.isAnnotationPresent(ManyToOne.class)) {
                joinColumnMetaData.setJoinType(JoinType.ManyToOne);
            }

            //设置哪些需要关联
            if(field.isAnnotationPresent(MappedStatement.class)) {
                if(field.getAnnotation(MappedStatement.class).exclude().length > 0) {
                    joinColumnMetaData.setExcludes(Arrays.asList(field.getAnnotation(MappedStatement.class).exclude()));
                } else if(field.getAnnotation(MappedStatement.class).include().length > 0) {
                    joinColumnMetaData.setIncludes(Arrays.asList(field.getAnnotation(MappedStatement.class).include()));
                } else {  //还有一种情况就是只是定义关联关系，却实际没有任何关联查询
                    joinColumnMetaData.setJoinNothing(true);
                }
            }

            //设置子查询条件和排序
            if(field.isAnnotationPresent(SubQuery.class)) {
                joinColumnMetaData.setSubQuery(field.getAnnotation(SubQuery.class));
            }
        }
    }

    public EntityMetaData getEntityMetaData() {
        return entityMetaData;
    }

    public void setEntityMetaData(EntityMetaData entityMetaData) {
        this.entityMetaData = entityMetaData;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public GenerationType getIdGenerationType() {
        return idGenerationType;
    }

    public void setIdGenerationType(GenerationType idGenerationType) {
        this.idGenerationType = idGenerationType;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public String getJdbcTypeAlias() {
        if(jdbcType != null) {
            return jdbcType.name();
        }
        return jdbcTypeAlias;
    }

    public void setJdbcTypeAlias(String jdbcTypeAlias) {
        this.jdbcTypeAlias = jdbcTypeAlias;
    }

    public JdbcType getJdbcType() {
        return jdbcType;
    }

    public void setJdbcType(JdbcType jdbcType) {
        this.jdbcType = jdbcType;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public boolean isJoin() {
        return join;
    }

    public void setJoin(boolean join) {
        this.join = join;
    }

    public JoinColumnMetaData getJoinColumnMetaData() {
        return joinColumnMetaData;
    }

    public void setJoinColumnMetaData(JoinColumnMetaData joinColumnMetaData) {
        this.joinColumnMetaData = joinColumnMetaData;
    }

    public List<Trigger> getTriggers() {
        return triggers;
    }

    public void setTriggers(List<Trigger> triggers) {
        this.triggers = triggers;
    }

    public Class<? extends KeyGenerator> getIdGeneratorClass() {
        return idGeneratorClass;
    }

    public void setIdGeneratorClass(Class<? extends KeyGenerator> idGeneratorClass) {
        this.idGeneratorClass = idGeneratorClass;
    }
}
