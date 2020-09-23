/*
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

import com.alilitech.mybatis.jpa.definition.GenericType;
import com.alilitech.mybatis.jpa.meta.ColumnMetaData;
import com.alilitech.mybatis.jpa.statement.MethodType;
import com.alilitech.mybatis.jpa.statement.PreMapperStatement;
import com.alilitech.mybatis.jpa.statement.PreMapperStatementBuilder;
import com.alilitech.mybatis.jpa.statement.StatementAssistant;
import com.alilitech.mybatis.jpa.statement.parser.RenderContext;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.jdbc.SQL;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class PreMapperStatementBuilder4Update extends PreMapperStatementBuilder {

    public PreMapperStatementBuilder4Update(Configuration configuration, MapperBuilderAssistant builderAssistant, MethodType methodType) {
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
        RenderContext renderContext = new RenderContext();
        buildSimplePart(entityMetaData.getPrimaryColumnMetaData().getProperty()).render(renderContext);

        return new SQL() {
            {
                UPDATE(entityMetaData.getTableName());
                for (ColumnMetaData columnMetaData : entityMetaData.getColumnMetaDataMap().values()) {
                    if(columnMetaData.isPrimaryKey() || columnMetaData.isJoin()) {
                       continue;
                    }
                    SET(columnMetaData.getColumnName() + " = " + StatementAssistant.resolveSqlParameterBySysFunction(columnMetaData, SqlCommandType.UPDATE));
                }
                WHERE(renderContext.getScript());
            }
        }.toString();
    }

    protected Class<?> getParameterTypeClass() {
        return entityMetaData.getEntityType();
    }

}
