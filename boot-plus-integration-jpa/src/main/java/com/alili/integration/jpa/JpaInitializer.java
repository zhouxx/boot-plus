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
package com.alili.integration.jpa;

import com.alili.integration.jpa.meta.EntityMetaData;
import com.alili.integration.jpa.meta.JoinColumnMetaData;
import com.alili.integration.jpa.definition.MapperDefinition;
import com.alili.integration.jpa.type.BooleanTypeHandler;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandler;

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

    private Configuration configuration;

    protected final EntityMetaDataRegistry entityMetaDataRegistry = EntityMetaDataRegistry.getInstance();

    protected final MapperDescriptionRegistry mapperDescriptionRegistry = new MapperDescriptionRegistry();

    public JpaInitializer(Configuration configuration) {

        this.configuration = configuration;

        //注册Boolean Handler
        configuration.getTypeHandlerRegistry().register((Class)Boolean.class, (TypeHandler)(new BooleanTypeHandler()));

        Collection<Class<?>> mapperClasses = configuration.getMapperRegistry().getMappers();

        for(Class<?> mapperClass : mapperClasses) {
            Class entityType = mapperDescriptionRegistry.register(mapperClass);
            entityMetaDataRegistry.register(entityType);
        }
    }

    public JpaInitializer buildJoinMetaDataAndRelationMethodDesc() {
        //哪些需要关联的方法
        List<JoinColumnMetaData> joinColumnList = new ArrayList<>();
        //遍历每个实体，找出对应的关联查询
        for(EntityMetaData entityMetaData : entityMetaDataRegistry.values()) {
            joinColumnList.addAll(new JoinColumnMetaDataAssistant(entityMetaData).init().getJoinColumnList());
        }
        //将关联列转换成MethodDesc，并添加至关联Mapper方法里
        for(JoinColumnMetaData joinColumnMetaData : joinColumnList) {
            new MethodDescriptionAssistant(mapperDescriptionRegistry, joinColumnMetaData).addRelationMethodDesc();
        }

        return this;
    }

    public void invokeJpaMapperStatementBuilder() {
        for(MapperDefinition mapperDefinition : mapperDescriptionRegistry.values()) {
            JpaMapperStatementBuilder jpaMapperStatementBuilder = new JpaMapperStatementBuilder(configuration, mapperDefinition);
            jpaMapperStatementBuilder.build();
        }
    }

}
