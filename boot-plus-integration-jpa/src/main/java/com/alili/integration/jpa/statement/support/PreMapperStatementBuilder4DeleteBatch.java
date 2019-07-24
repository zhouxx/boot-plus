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
public class PreMapperStatementBuilder4DeleteBatch extends PreMapperStatementBuilder {

    public PreMapperStatementBuilder4DeleteBatch(Configuration configuration, MapperBuilderAssistant builderAssistant) {
        super(configuration, builderAssistant);
    }

    @Override
    protected void buildPreMapperStatementExtend(PreMapperStatement preMapperStatement, GenericType genericType) {
        preMapperStatement.setResultType(int.class);
        preMapperStatement.setSqlCommandType(SqlCommandType.DELETE);

        setNoKeyGenerator(preMapperStatement);
    }

    @Override
    protected String buildSQL() {
        StringBuffer script = new StringBuffer();
        script.append("<script>");
        script.append("DELETE FROM " + entityMetaData.getTableName());
        script.append(" WHERE " + entityMetaData.getPrimaryColumnMetaData().getColumnName() + " IN ");

        script.append("<foreach collection='list' index='index' item='id' open='(' separator=',' close=')'>");
        script.append("#{id}");
        script.append("</foreach>");

        script.append("</script>");

        return script.toString();
    }

    protected Class<?> getParameterTypeClass() {
        return entityMetaData.getIdType();
    }



}
