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


import com.alilitech.mybatis.jpa.anotation.Trigger;
import com.alilitech.mybatis.jpa.definition.GenericType;
import com.alilitech.mybatis.jpa.meta.ColumnMetaData;
import com.alilitech.mybatis.jpa.parameter.GenerationType;
import com.alilitech.mybatis.jpa.parameter.TriggerValueType;
import com.alilitech.mybatis.jpa.statement.MethodType;
import com.alilitech.mybatis.jpa.statement.PreMapperStatement;
import com.alilitech.mybatis.jpa.statement.PreMapperStatementBuilder;
import com.alilitech.mybatis.jpa.statement.StatementAssistant;
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
public class PreMapperStatementBuilder4InsertSelective extends PreMapperStatementBuilder {

    public PreMapperStatementBuilder4InsertSelective(Configuration configuration, MapperBuilderAssistant builderAssistant, MethodType methodType) {
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
        // columns
        StringBuilder columns = new StringBuilder();
        columns.append("<trim prefix='(' suffix=')' suffixOverrides=',' >");
        // values
        StringBuilder values = new StringBuilder();
        values.append("<trim prefix='(' suffix=')' suffixOverrides=',' >");
        for (ColumnMetaData columnMetaData : entityMetaData.getColumnMetaDataMap().values()) {
            //自增主键不在插入列
            if(columnMetaData.isPrimaryKey() && columnMetaData.getIdGenerationType() == GenerationType.IDENTITY) {
                continue;
            }
            if(columnMetaData.isJoin()) {
                continue;
            }

            //查找触发trigger
            String dataBaseFunction = null;

            if(columnMetaData.getTriggers() != null) {
                for(Trigger trigger : columnMetaData.getTriggers()) {
                    if(trigger.triggerType() == SqlCommandType.INSERT && trigger.valueType() == TriggerValueType.DatabaseFunction) {
                        dataBaseFunction = trigger.value();
                    }
                }
            }

            if(dataBaseFunction == null) {
                // columns
                columns.append("<if test='" + columnMetaData.getProperty() + " != null'>");
                columns.append(columnMetaData.getColumnName() + ", ");
                columns.append("</if> ");
                // values
                values.append("<if test='" + columnMetaData.getProperty() + " != null'>");
                values.append(StatementAssistant.resolveSqlParameter(columnMetaData) + ", ");
                values.append("</if> ");
            } else {
                // columns
                columns.append(columnMetaData.getColumnName() + ", ");
                // values
                values.append(dataBaseFunction + ", ");
            }

        }

        columns.append("</trim>");
        values.append("</trim>");

        //since 1.1
        List<String> sqlParts = Arrays.asList(
                "INSERT",
                "INTO",
                entityMetaData.getTableName(),
                columns.toString(),
                "VALUES",
                values.toString()
        );

        return buildScript(sqlParts);
    }

    protected Class<?> getParameterTypeClass() {
        return entityMetaData.getEntityType();
    }

}
