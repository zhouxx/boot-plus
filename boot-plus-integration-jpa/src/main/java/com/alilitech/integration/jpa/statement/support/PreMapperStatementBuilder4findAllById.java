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
package com.alilitech.integration.jpa.statement.support;

import com.alilitech.integration.jpa.definition.GenericType;
import com.alilitech.integration.jpa.statement.MethodType;
import com.alilitech.integration.jpa.statement.PreMapperStatement;
import com.alilitech.integration.jpa.statement.PreMapperStatementBuilder;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class PreMapperStatementBuilder4findAllById extends PreMapperStatementBuilder {

    public PreMapperStatementBuilder4findAllById(Configuration configuration, MapperBuilderAssistant builderAssistant, MethodType methodType) {
        super(configuration, builderAssistant, methodType);
    }

    @Override
    protected void buildPreMapperStatementExtend(PreMapperStatement preMapperStatement, GenericType genericType) {

        preMapperStatement.setSqlCommandType(SqlCommandType.SELECT);

        setNoKeyGenerator(preMapperStatement);

        /*if(methodDefinition.isCompositeResultMap()) {
            String resultMapId = buildResultMap();
            preMapperStatement.setCompositeResultMap(resultMapId);
        } else {
            preMapperStatement.setResultType((Class)genericType.getDomainType());
        }*/

        setFindResultIdOrType(preMapperStatement, genericType);
    }

    @Override
    protected String buildSQL() {

        //sql parts
        List<String> sqlParts = Arrays.asList(
                "SELECT",
                entityMetaData.getColumnNames(),
                "FROM",
                entityMetaData.getTableName(),
                "<where>",
                entityMetaData.getPrimaryColumnMetaData().getColumnName(),
                "IN",
                "<foreach item=\"item\" index=\"index\" open=\"(\" separator=\",\" close=\")\" collection=\"list\">",
                "#{item}",
                "</foreach>",
                "</where>"
        );

        return buildScript(sqlParts);
    }

    protected Class<?> getParameterTypeClass() {
        return entityMetaData.getEntityType();
    }

}
