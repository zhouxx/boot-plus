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
import com.alilitech.integration.jpa.statement.parser.PartTree;
import com.alilitech.integration.jpa.statement.parser.RenderContext;
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
public class PreMapperStatementBuilder4FindJoin extends PreMapperStatementBuilder {

    public PreMapperStatementBuilder4FindJoin(Configuration configuration, MapperBuilderAssistant builderAssistant, MethodType methodType) {
        super(configuration, builderAssistant, methodType);
    }

    @Override
    protected void buildPreMapperStatementExtend(PreMapperStatement preMapperStatement, GenericType genericType) {

        preMapperStatement.setSqlCommandType(SqlCommandType.SELECT);

        setNoKeyGenerator(preMapperStatement);

        setFindResultIdOrType(preMapperStatement, genericType);
    }

    @Override
    protected String buildSQL() {

        String joinTableNameAlias = "t0";
        String joinTableName = methodDefinition.getJoinTableName();

        String tableNameAlias = "t1";
        String tableName = entityMetaData.getTableName();

        PartTree partTree = new PartTree(null, methodDefinition);

        RenderContext context = new RenderContext(joinTableNameAlias, null);
        partTree.render(context);

        //sql parts
        List<String> sqlParts = Arrays.asList(
                "SELECT",
                entityMetaData.getColumnNames(tableNameAlias),
                "FROM",
                joinTableName,
                joinTableNameAlias,
                "INNER JOIN",
                tableName,
                tableNameAlias,
                "ON",
                joinTableNameAlias+ "." + methodDefinition.getInverseReferencedColumnName(),
                "=",
                tableNameAlias + "." + methodDefinition.getInverseColumnName(),
                context.getScript()
        );

        return buildScript(sqlParts);
    }

    @Override
    protected Class<?> getParameterTypeClass() {
        if (methodDefinition.getParameterDefinitions().size() > 0) {
            // Mybatis mapper 方法最多支持一个参数,先设置成Object.class,mybatis会在sql中解析
            return (Object.class);
        }
        return void.class;
    }

}
