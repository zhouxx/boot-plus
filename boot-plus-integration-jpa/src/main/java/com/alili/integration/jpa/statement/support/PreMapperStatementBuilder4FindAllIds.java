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

import com.alili.integration.jpa.definition.GenericType;
import com.alili.integration.jpa.statement.PreMapperStatement;
import com.alili.integration.jpa.statement.PreMapperStatementBuilder;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;

/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class PreMapperStatementBuilder4FindAllIds extends PreMapperStatementBuilder {

    public PreMapperStatementBuilder4FindAllIds(Configuration configuration, MapperBuilderAssistant builderAssistant) {
        super(configuration, builderAssistant);
    }

    @Override
    protected void buildPreMapperStatementExtend(PreMapperStatement preMapperStatement, GenericType genericType) {

        preMapperStatement.setSqlCommandType(SqlCommandType.SELECT);

        setNoKeyGenerator(preMapperStatement);

        /*if(methodDesc.isResultMap()) {
            String resultMapId = buildResultMap();
            preMapperStatement.setResultMap(resultMapId);
        } else {
            preMapperStatement.setResultType((Class)genericType.getDomainType());
        }*/

        setFindResultIdOrType(preMapperStatement, genericType);
    }

    @Override
    protected String buildSQL() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("<script>");

        stringBuffer.append("SELECT" + entityMetaData.getColumnNames() + " FROM " + entityMetaData.getTableName());
        stringBuffer.append("<where>");
        stringBuffer.append(entityMetaData.getPrimaryColumnMetaData().getColumnName() + " in ");
        stringBuffer.append("<foreach item=\"item\" index=\"index\" open=\"(\" separator=\",\" close=\")\" collection=\"list\">");
        stringBuffer.append("#{item} ");
        stringBuffer.append("</foreach>");

        stringBuffer.append("</where>");
        stringBuffer.append("</script>");

        return stringBuffer.toString();
    }

    protected Class<?> getParameterTypeClass() {
        return entityMetaData.getEntityType();
    }



}
