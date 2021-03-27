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
package com.alilitech.mybatis.jpa.statement.support;

import com.alilitech.mybatis.jpa.definition.GenericType;
import com.alilitech.mybatis.jpa.meta.ColumnMetaData;
import com.alilitech.mybatis.jpa.statement.MethodType;
import com.alilitech.mybatis.jpa.statement.PreMapperStatement;
import com.alilitech.mybatis.jpa.statement.PreMapperStatementBuilder;
import com.alilitech.mybatis.jpa.statement.StatementAssistant;
import com.alilitech.mybatis.jpa.statement.parser.RenderContext;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class PreMapperStatementBuilder4UpdateBatch extends PreMapperStatementBuilder {

    public PreMapperStatementBuilder4UpdateBatch(Configuration configuration, MapperBuilderAssistant builderAssistant, MethodType methodType) {
        super(configuration, builderAssistant, methodType);
    }

    @Override
    protected void buildPreMapperStatementExtend(PreMapperStatement preMapperStatement, GenericType genericType) {
        preMapperStatement.setResultType(int.class);
        preMapperStatement.setSqlCommandType(SqlCommandType.UPDATE);

        setKeyGeneratorAndTriggerValue(preMapperStatement);
    }

    @Override
    protected String buildSQL() {


        StringBuilder sets = new StringBuilder();
        for (ColumnMetaData columnMetaData : entityMetaData.getColumnMetaDataMap().values()) {
            if(columnMetaData.isPrimaryKey() || columnMetaData.isJoin()) {
                continue;
            }
            sets.append("<if test='" + "rowData." + columnMetaData.getProperty() + "!= null'> ");
            sets.append(columnMetaData.getColumnName()).append(" = ").append(StatementAssistant.resolveSqlParameter(columnMetaData, "rowData"))
                    .append(", ");
            sets.append("</if> ");
        }

        //since 1.1
        RenderContext renderContext = new RenderContext(null, "rowData");
        buildSimplePart(entityMetaData.getPrimaryColumnMetaData().getProperty()).render(renderContext);

        List<String> sqlParts = Arrays.asList(
                "<if test=\"_databaseId != 'Oracle'\">",
                "<foreach item='rowData' index='rowIndex' collection='collection' separator=';'>",
                "UPDATE",
                entityMetaData.getTableName(),
                "<set>",
                sets.toString(),
                "</set>",
                "WHERE",
                renderContext.getScript(),
                "</foreach>",
                "</if>",
                "<if test=\"_databaseId == 'Oracle'\">",
                "<foreach item='rowData' index='rowIndex' collection='collection' separator=';' open='begin' close='; end;'>",
                "UPDATE",
                entityMetaData.getTableName(),
                "<set>",
                sets.toString(),
                "</set>",
                "WHERE",
                renderContext.getScript(),
                "</foreach>",
                "</if>"
        );

        return buildScript(sqlParts);
        /*script.append("<if test=\"_databaseId != 'Oracle'\">");
        script.append("<foreach item='rowData' index='rowIndex' collection='list' separator=';'>"
                + "UPDATE " + entityMetaData.getTableName() + " <set> " + sets.toString() + "</set>"
                + buildWhere("rowData") + "</foreach>");
        script.append("</if>");

        script.append("<if test=\"_databaseId == 'Oracle'\">");
        script.append("<foreach item='rowData' index='rowIndex' collection='list' separator=';' open='begin' close='; end;'>"
                + "UPDATE " + entityMetaData.getTableName() + " <set> " + sets.toString() + "</set>"
                + buildWhere("rowData") + "</foreach>");
        script.append("</if>");

        script.append("</script>");*/
    }

    protected Class<?> getParameterTypeClass() {
        return Collection.class;
    }

}
