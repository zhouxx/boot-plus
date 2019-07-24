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
package com.alili.integration.jpa.statement.support;

import com.alili.integration.jpa.parameter.GenerationType;
import com.alili.integration.jpa.meta.ColumnMetaData;
import com.alili.integration.jpa.statement.PreMapperStatement;
import com.alili.integration.jpa.statement.PreMapperStatementBuilder;
import com.alili.integration.jpa.statement.StatementAssistant;
import com.alili.integration.jpa.definition.GenericType;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;

import java.util.Collection;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class PreMapperStatementBuilder4InsertBatch extends PreMapperStatementBuilder {

    public PreMapperStatementBuilder4InsertBatch(Configuration configuration, MapperBuilderAssistant builderAssistant) {
        super(configuration, builderAssistant);
    }

    @Override
    protected void buildPreMapperStatementExtend(PreMapperStatement preMapperStatement, GenericType genericType) {
        preMapperStatement.setResultType(int.class);
        preMapperStatement.setSqlCommandType(SqlCommandType.INSERT);

        setKeyGeneratorAndTriggerValue(preMapperStatement);
    }

    @Override
    protected String buildSQL() {

        /*if(entityMetaData.getPrimaryColumnMetaData() != null
                && entityMetaData.getPrimaryColumnMetaData().getIdGenerationType() == GenerationType.SEQUENCE) {
            StringBuilder columns = new StringBuilder();
            columns.append("<trim suffixOverrides=',' > ");

            StringBuilder values = new StringBuilder();


            for (ColumnMetaData columnMetaData : entityMetaData.getColumnMetaDataMap().values()) {
                if(columnMetaData.isJoin()) {
                    continue;
                }
                if(columnMetaData.isPrimaryKey()) {
                    columns.append(columnMetaData.getColumnName() + ", ");
                    continue;
                }
                columns.append(columnMetaData.getColumnName() + ", ");
                if (values.length() > 0) {
                    values.append(",");
                }
                values.append(SqlAssistant.resolveSqlParameterBySysFunction(columnMetaData, SqlCommandType.INSERT, "rowData"));
            }

            columns.append("</trim> ");

            return "<script>"
                    + " INSERT INTO " + entityMetaData.getTableName() + " (" + columns + " ) "
                    + " SELECT  " + DialectFactory.buildKeyGeneratorScript4Column(entityMetaData.getPrimaryColumnMetaData().getSequenceName())
                    + ", t0.* from ("
                    + "<foreach item='rowData' index='rowIndex' collection='list' separator='union all'>"
                    + " select " + values.toString() + " from dual " + "</foreach>" + ") t0" +
                    "</script>";

        } else {*/
        StringBuilder columns = new StringBuilder();
        columns.append("<trim suffixOverrides=',' > ");

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

        columns.append("</trim> ");

        StringBuffer script = new StringBuffer();
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

        return script.toString();
        /*}*/


    }

    protected Class<?> getParameterTypeClass() {
        return Collection.class;
    }



}
