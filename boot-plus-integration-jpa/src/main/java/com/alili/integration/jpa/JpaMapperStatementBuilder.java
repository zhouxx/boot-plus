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


import com.alili.integration.jpa.definition.MapperDefinition;
import com.alili.integration.jpa.definition.MethodDefinition;
import com.alili.integration.jpa.meta.ColumnMetaData;
import com.alili.integration.jpa.meta.EntityMetaData;
import com.alili.integration.jpa.statement.PreMapperStatement;
import com.alili.integration.jpa.statement.PreMapperStatementFactory;
import com.alili.integration.jpa.util.ResultMapIdUtils;
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

    protected MapperDefinition mapperDesc;

    public JpaMapperStatementBuilder(Configuration configuration, MapperDefinition mapperDesc) {
        this.builderAssistant = new MapperBuilderAssistant(configuration, mapperDesc.getResource());
        this.configuration = configuration;

        this.mapperDesc = mapperDesc;
    }

    public void build() {
        /*String resource = mapperDesc.getResource1();
        if (!configuration.isResourceLoaded(resource)) {
            configuration.addLoadedResource(resource);
            //bindMapperForNamespace();
        }*/
        builderAssistant.setCurrentNamespace(mapperDesc.getNameSpace());
        //构造基础BaseResult
        baseResultMapHandle();

        for (MethodDefinition methodDesc : mapperDesc.getMethodDefinitions()) {

            if(!configuration.hasStatement(builderAssistant.getCurrentNamespace() + "." + methodDesc.getMethodName())) {

                logger.trace("add statement==>" + builderAssistant.getCurrentNamespace() + "." + methodDesc.getMethodName());

                PreMapperStatement preMapperStatement = PreMapperStatementFactory.getInstance().createPreMapperStatement(configuration, builderAssistant, methodDesc, mapperDesc.getGenericType());

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
        EntityMetaData entityMetaData = EntityMetaDataRegistry.getInstance().get(mapperDesc.getGenericType().getDomainType());

        //BaseResultMap
        String resultMapId = ResultMapIdUtils.buildBaseResultMapId(builderAssistant);

        if(configuration.hasResultMap(resultMapId)) {
            return resultMapId;
        }

        List<ResultMapping> baseMappings = new ArrayList<>();
        List<ResultMapping> joinMappings = new ArrayList<>();

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
