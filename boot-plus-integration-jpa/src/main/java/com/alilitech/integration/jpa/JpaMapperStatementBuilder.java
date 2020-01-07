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
package com.alilitech.integration.jpa;


import com.alilitech.integration.jpa.definition.MapperDefinition;
import com.alilitech.integration.jpa.definition.MethodDefinition;
import com.alilitech.integration.jpa.meta.ColumnMetaData;
import com.alilitech.integration.jpa.meta.EntityMetaData;
import com.alilitech.integration.jpa.statement.PreMapperStatement;
import com.alilitech.integration.jpa.statement.PreMapperStatementFactory;
import com.alilitech.integration.jpa.util.ResultMapIdUtils;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.mapping.ResultFlag;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * 构建MapperStatement
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class JpaMapperStatementBuilder {

    private Logger logger = LoggerFactory.getLogger(JpaMapperStatementBuilder.class);

    /** mybatis */
    protected MapperBuilderAssistant builderAssistant;

    protected Configuration configuration;

    protected MapperDefinition mapperDefinition;

    public JpaMapperStatementBuilder(Configuration configuration, MapperDefinition mapperDefinition) {
        this.builderAssistant = new MapperBuilderAssistant(configuration, mapperDefinition.getResource());
        this.configuration = configuration;
        this.mapperDefinition = mapperDefinition;
    }

    public void build() {
        /*String resource = mapperDefinition.getResource1();
        if (!configuration.isResourceLoaded(resource)) {
            configuration.addLoadedResource(resource);
            //bindMapperForNamespace();
        }*/
        builderAssistant.setCurrentNamespace(mapperDefinition.getNameSpace());
        //构造基础BaseResult
        baseResultMapHandle();

        for (MethodDefinition methodDefinition : mapperDefinition.getMethodDefinitions()) {

            if(!configuration.hasStatement(builderAssistant.getCurrentNamespace() + "." + methodDefinition.getMethodName())) {

                logger.trace("add statement==>" + builderAssistant.getCurrentNamespace() + "." + methodDefinition.getMethodName());

                PreMapperStatement preMapperStatement = PreMapperStatementFactory.getInstance().createPreMapperStatement(configuration, builderAssistant, methodDefinition, mapperDefinition.getGenericType());

                builderAssistant.addMappedStatement(
                        preMapperStatement.getId(),
                        preMapperStatement.getSqlSource(),
                        preMapperStatement.getStatementType(),
                        preMapperStatement.getSqlCommandType(),
                        preMapperStatement.getFetchSize(),
                        preMapperStatement.getTimeout(),
                        preMapperStatement.getParameterMap(),
                        preMapperStatement.getParameterType(),
                        preMapperStatement.getResultMap(),
                        preMapperStatement.getResultType(),
                        preMapperStatement.getResultSetType(),
                        preMapperStatement.isFlushCache(),
                        preMapperStatement.isUseCache(),
                        preMapperStatement.isResultOrdered(),
                        preMapperStatement.getKeyGenerator(),
                        preMapperStatement.getKeyProperty(),
                        preMapperStatement.getKeyColumn(),
                        preMapperStatement.getDatabaseId(),
                        preMapperStatement.getLang(),
                        preMapperStatement.getResultSets()
                        );
            }
        }
    }

    private String baseResultMapHandle() {
        EntityMetaData entityMetaData = EntityMetaDataRegistry.getInstance().get(mapperDefinition.getGenericType().getDomainType());

        //BaseResultMap
        String resultMapId = ResultMapIdUtils.buildBaseResultMapId(builderAssistant);

        if(configuration.hasResultMap(resultMapId)) {
            return resultMapId;
        }

        List<ResultMapping> baseMappings = new ArrayList<>();

        for (ColumnMetaData columnMeta : entityMetaData.getColumnMetaDataMap().values()) {
            if(columnMeta.isJoin()) {
                continue;
            }
            List<ResultFlag> flags = new ArrayList<>();
            if (columnMeta.isPrimaryKey()) {
                flags.add(ResultFlag.ID);
            }
            ResultMapping resultMapping = builderAssistant.buildResultMapping(
                    columnMeta.getType(),
                    columnMeta.getProperty(),
                    columnMeta.getColumnName(),
                    columnMeta.getType(),
                    columnMeta.getJdbcType(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    flags
            );

            baseMappings.add(resultMapping);
        }

        //添加BaseResultMap
        ResultMap resultMap = builderAssistant.addResultMap(
                resultMapId,
                entityMetaData.getEntityType(),
                null,
                null,
                baseMappings,
                true
        );

        return resultMap.getId();

    }

}