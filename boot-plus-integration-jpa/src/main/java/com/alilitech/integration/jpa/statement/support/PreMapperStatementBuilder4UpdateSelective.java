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

import com.alilitech.integration.jpa.anotation.Trigger;
import com.alilitech.integration.jpa.definition.GenericType;
import com.alilitech.integration.jpa.meta.ColumnMetaData;
import com.alilitech.integration.jpa.parameter.TriggerValueType;
import com.alilitech.integration.jpa.statement.MethodType;
import com.alilitech.integration.jpa.statement.PreMapperStatement;
import com.alilitech.integration.jpa.statement.PreMapperStatementBuilder;
import com.alilitech.integration.jpa.statement.StatementAssistant;
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
public class PreMapperStatementBuilder4UpdateSelective extends PreMapperStatementBuilder {

    public PreMapperStatementBuilder4UpdateSelective(Configuration configuration, MapperBuilderAssistant builderAssistant, MethodType methodType) {
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
        // columns
        StringBuilder sets = new StringBuilder();
        sets.append("<trim prefix='' suffix='' suffixOverrides=',' >");
        for (ColumnMetaData columnMetaData : entityMetaData.getColumnMetaDataMap().values()) {
            if(columnMetaData.isPrimaryKey() || columnMetaData.isJoin()) {
               continue;
            }

            //查找触发trigger
            String dataBaseFunction = null;

            if(columnMetaData.getTriggers() != null) {
                for(Trigger trigger : columnMetaData.getTriggers()) {
                    if(trigger.triggerType() == SqlCommandType.UPDATE && trigger.valueType() == TriggerValueType.DatabaseFunction) {
                        dataBaseFunction = trigger.value();
                    }
                }
            }

            if(dataBaseFunction == null) {
                sets.append("<if test='" + columnMetaData.getProperty() + "!= null'>");
                sets.append(columnMetaData.getColumnName()).append(" = ").append(StatementAssistant.resolveSqlParameter(columnMetaData))
                        .append(", ");
                sets.append("</if>");
            } else {
                sets.append(columnMetaData.getColumnName()).append(" = ").append(dataBaseFunction)
                        .append(", ");
            }
        }

        sets.append("</trim>");

        //since 1.1
        List<String> sqlParts = Arrays.asList(
                "UPDATE",
                entityMetaData.getTableName(),
                "SET",
                sets.toString(),
                "WHERE",
                StatementAssistant.buildPrimaryKeyCondition(entityMetaData)
        );

        return buildScript(sqlParts);
    }

    protected Class<?> getParameterTypeClass() {
        return entityMetaData.getEntityType();
    }

}