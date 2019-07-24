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
public class PreMapperStatementBuilder4UpdateBatch extends PreMapperStatementBuilder {

    public PreMapperStatementBuilder4UpdateBatch(Configuration configuration, MapperBuilderAssistant builderAssistant) {
        super(configuration, builderAssistant);
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

        StringBuffer script = new StringBuffer();

        script.append("<script>");
        script.append("<if test=\"_databaseId != 'Oracle'\">");
        script.append("<foreach item='rowData' index='rowIndex' collection='list' separator=';'>"
                + "UPDATE " + entityMetaData.getTableName() + " <set> " + sets.toString() + "</set>"
                + buildWhere("rowData") + "</foreach>");
        script.append("</if>");

        script.append("<if test=\"_databaseId == 'Oracle'\">");
        script.append("<foreach item='rowData' index='rowIndex' collection='list' separator=';' open='begin' close='; end;'>"
                + "UPDATE " + entityMetaData.getTableName() + " <set> " + sets.toString() + "</set>"
                + buildWhere("rowData") + "</foreach>");
        script.append("</if>");

        script.append("</script>");

        return script.toString();
    }

    protected Class<?> getParameterTypeClass() {
        return Collection.class;
    }

}
