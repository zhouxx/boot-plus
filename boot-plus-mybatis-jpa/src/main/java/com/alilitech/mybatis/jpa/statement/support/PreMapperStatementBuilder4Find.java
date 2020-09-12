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
package com.alilitech.mybatis.jpa.statement.support;

import com.alilitech.mybatis.jpa.SubQueryContainer;
import com.alilitech.mybatis.jpa.anotation.SubQuery;
import com.alilitech.mybatis.jpa.definition.GenericType;
import com.alilitech.mybatis.jpa.statement.MethodType;
import com.alilitech.mybatis.jpa.statement.PreMapperStatement;
import com.alilitech.mybatis.jpa.statement.PreMapperStatementBuilder;
import com.alilitech.mybatis.jpa.statement.parser.PartTree;
import com.alilitech.mybatis.jpa.statement.parser.RenderContext;
import com.alilitech.mybatis.jpa.statement.parser.SubQueryPartTree;
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
public class PreMapperStatementBuilder4Find extends PreMapperStatementBuilder {

    /**
     * 是否是 {@link SqlCommandType#DELETE}
     */
    protected boolean delete = false;

    public PreMapperStatementBuilder4Find(Configuration configuration, MapperBuilderAssistant builderAssistant, MethodType methodType) {
        super(configuration, builderAssistant, methodType);
    }

    @Override
    protected void buildPreMapperStatementExtend(PreMapperStatement preMapperStatement, GenericType genericType) {

        if(delete) {
            preMapperStatement.setSqlCommandType(SqlCommandType.DELETE);
        } else {
            preMapperStatement.setSqlCommandType(SqlCommandType.SELECT);
        }

        setNoKeyGenerator(preMapperStatement);
        setFindResultIdOrType(preMapperStatement, genericType);
    }

    @Override
    protected String buildSQL() {

        PartTree partTree = buildPartTree();

        String operation = "SELECT";
        String selectPart = entityMetaData.getColumnNames();

        if(partTree.isCountProjection()) {
            selectPart = "COUNT(1)";
            resultType = methodDefinition.getReturnType();
        } else if(partTree.isDelete()) {
            operation = "DELETE";
            selectPart = "";
            resultType = methodDefinition.getReturnType();
            delete = true;
        } else if(partTree.isExistsProjection()) {
            selectPart = "COUNT(1)";
            resultType = methodDefinition.getReturnType();
        }
        //剩下的就是常规查询了
        else {
            // default select
        }

        RenderContext context = new RenderContext();
        partTree.render(context);

        /**
         * if {@link SubQueryContainer} containers statementId, parse the predicates and orders
         */
        if(SubQueryContainer.getInstance().isExist(methodDefinition.getStatementId())) {
            SubQuery subQuery = SubQueryContainer.getInstance().get(methodDefinition.getStatementId());
            SubQueryPartTree subQueryPartTree = new SubQueryPartTree(subQuery, entityMetaData.getEntityType(), methodDefinition);
            subQueryPartTree.render(context);
        }

        List<String> sqlParts = Arrays.asList(
                operation,
                selectPart,
                "FROM",
                entityMetaData.getTableName(),
                context.getScript(),
                buildSort()
        );

        return buildScript(sqlParts);
    }

    @Override
    protected Class<?> getParameterTypeClass() {
        return entityMetaData.getEntityType();
    }

}
