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

import com.alili.integration.jpa.statement.PreMapperStatement;
import com.alili.integration.jpa.statement.PreMapperStatementBuilder;
import com.alili.integration.jpa.definition.GenericType;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class PreMapperStatementBuilder4Exists extends PreMapperStatementBuilder {

    public PreMapperStatementBuilder4Exists(Configuration configuration, MapperBuilderAssistant builderAssistant) {
        super(configuration, builderAssistant);
    }

    @Override
    protected void buildPreMapperStatementExtend(PreMapperStatement preMapperStatement, GenericType genericType) {

        preMapperStatement.setResultType(Boolean.class);
        preMapperStatement.setSqlCommandType(SqlCommandType.SELECT);

        setNoKeyGenerator(preMapperStatement);

    }

    @Override
    protected String buildSQL() {
        StringBuffer buffer = new StringBuffer()
                .append("<script>")
                .append("SELECT ")
                .append(entityMetaData.getColumnNames())
                .append(" FROM ")
                .append(entityMetaData.getTableName())
                .append(buildWhere())
                .append("</script>");
        return buffer.toString();
        /*return "<script>" + "SELECT count(1) " + " FROM " + entityMetaData.getTableName()
                + buildWhere() + "</script>";*/
    }

    @Override
    protected Class<?> getParameterTypeClass() {
        return entityMetaData.getIdType();
    }
}
