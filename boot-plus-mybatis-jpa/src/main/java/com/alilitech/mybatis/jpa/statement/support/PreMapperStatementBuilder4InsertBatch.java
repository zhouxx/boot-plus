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
import com.alilitech.mybatis.jpa.parameter.GenerationType;
import com.alilitech.mybatis.jpa.statement.MethodType;
import com.alilitech.mybatis.jpa.statement.PreMapperStatement;
import com.alilitech.mybatis.jpa.statement.PreMapperStatementBuilder;
import com.alilitech.mybatis.jpa.statement.StatementAssistant;
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
public class PreMapperStatementBuilder4InsertBatch extends PreMapperStatementBuilder {

    public PreMapperStatementBuilder4InsertBatch(Configuration configuration, MapperBuilderAssistant builderAssistant, MethodType methodType) {
        super(configuration, builderAssistant, methodType);
    }

    @Override
    protected void buildPreMapperStatementExtend(PreMapperStatement preMapperStatement, GenericType genericType) {
        preMapperStatement.setResultType(int.class);
        preMapperStatement.setSqlCommandType(SqlCommandType.INSERT);
        setKeyGeneratorAndTriggerValue(preMapperStatement);
    }

    @Override
    protected String buildSQL() {

        StringBuilder columns = new StringBuilder();
        columns.append("<trim suffixOverrides=',' >");

        StringBuilder values = new StringBuilder();

        for (ColumnMetaData columnMetaData : entityMetaData.getColumnMetaDataMap().values()) {
            //自增主键不在插入列
            if(columnMetaData.isPrimaryKey() && columnMetaData.getIdGenerationType() == GenerationType.IDENTITY) {
                continue;
            }

            if(columnMetaData.isJoin()) {
                continue;
            }
            columns.append(columnMetaData.getColumnName() + ", ");
            if (values.length() > 0) {
                values.append(",");
            }
            values.append(StatementAssistant.resolveSqlParameterBySysFunction(columnMetaData, SqlCommandType.INSERT, "rowData"));
        }

        columns.append("</trim>");

        //since 1.1
        List<String> sqlParts = Arrays.asList(
                "<if test=\"_databaseId != 'Oracle'\">",
                "INSERT INTO",
                entityMetaData.getTableName(),
                "(",
                columns.toString(),
                ")",
                "VALUES",
                "<foreach item='rowData' index='rowIndex' collection='collection' separator=','>",
                "(",
                values.toString(),
                ")",
                "</foreach>",
                "</if>",
                "<if test=\"_databaseId == 'Oracle'\">",
                "<foreach item='rowData' index='rowIndex' collection='collection' separator=';' open='begin' close='; end;'>",
                "INSERT ALL",
                "INTO",
                entityMetaData.getTableName(),
                "(",
                columns.toString(),
                ")",
                "VALUES",
                "(",
                values.toString(),
                ")",
                "</foreach>",
                "select 1 from dual",
                "</if>"
        );

        /** StringBuilder script = new StringBuilder();
        script.append("<script>");
        script.append("<if test=\"_databaseId != 'Oracle'\">");
        script.append(" INSERT INTO " + entityMetaData.getTableName() + " (" + columns
                + " ) " + " VALUES " + "<foreach item='rowData' index='rowIndex' collection='list' separator=','>"
                + "( " + values.toString() + " )" + "</foreach>");
        script.append("</if>");
        script.append("<if test=\"_databaseId == 'Oracle'\">");
        script.append(" INSERT ALL " + "<foreach item='rowData' index='rowIndex' collection='list' separator=' '>"
                        + "INTO " + entityMetaData.getTableName() + " (" + columns
                        + " ) " + " VALUES " + "( " + values.toString() + " )" + "</foreach>");
        script.append("select 1 from dual");
        script.append("</if>");
        script.append("</script>");

        return script.toString();*/

        return buildScript(sqlParts);

    }

    protected Class<?> getParameterTypeClass() {
        return Collection.class;
    }

}
