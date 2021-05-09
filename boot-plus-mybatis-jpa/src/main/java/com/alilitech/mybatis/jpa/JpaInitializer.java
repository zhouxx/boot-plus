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
package com.alilitech.mybatis.jpa;

import com.alilitech.mybatis.jpa.anotation.NoMapperBean;
import com.alilitech.mybatis.jpa.mapper.Mapper;
import com.alilitech.mybatis.jpa.meta.EntityMetaData;
import com.alilitech.mybatis.jpa.meta.JoinColumnMetaData;
import com.alilitech.mybatis.jpa.definition.MapperDefinition;
import com.alilitech.mybatis.jpa.type.BooleanTypeHandler;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 *  Mybatis Jpa initializer
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class JpaInitializer {

    private final Configuration configuration;

    protected final EntityMetaDataRegistry entityMetaDataRegistry = EntityMetaDataRegistry.getInstance();

    protected final MapperDefinitionRegistry mapperDefinitionRegistry = MapperDefinitionRegistry.getInstance();

    public JpaInitializer(Configuration configuration) {

        this.configuration = configuration;

        //register Boolean Handler
        configuration.getTypeHandlerRegistry().register(Boolean.class, new BooleanTypeHandler());

        Collection<Class<?>> mapperClasses = configuration.getMapperRegistry().getMappers();

        for(Class<?> mapperClass : mapperClasses) {
            // If mapper is not extend from Mapper, not register
            if(!Mapper.class.isAssignableFrom(mapperClass)) {
                continue;
            }

            // if mapper has NoMapperBean, not register
            if(mapperClass.isAnnotationPresent(NoMapperBean.class)) {
                continue;
            }

            Class<?> entityType = mapperDefinitionRegistry.register(mapperClass);
            entityMetaDataRegistry.register(entityType);
        }
    }

    public JpaInitializer buildJoinMetaDataAndRelationMethodDefinition() {
        //哪些需要关联的方法
        List<JoinColumnMetaData> joinColumnList = new ArrayList<>();
        //遍历每个实体，找出对应的关联查询
        for(EntityMetaData entityMetaData : entityMetaDataRegistry.values()) {
            joinColumnList.addAll(new JoinColumnMetaDataAssistant(entityMetaData).init().getJoinColumnList());
        }
        //将关联列转换成MethodDefinition，并添加至关联Mapper方法里
        for(JoinColumnMetaData joinColumnMetaData : joinColumnList) {
            new MethodDefinitionAssistant(mapperDefinitionRegistry, joinColumnMetaData).addRelationMethodDefinition();
        }

        return this;
    }

    public void invokeJpaMapperStatementBuilder() {
        for(MapperDefinition mapperDefinition : mapperDefinitionRegistry.values()) {
            JpaMapperStatementBuilder jpaMapperStatementBuilder = new JpaMapperStatementBuilder(configuration, mapperDefinition);
            jpaMapperStatementBuilder.build();
        }
    }

}
