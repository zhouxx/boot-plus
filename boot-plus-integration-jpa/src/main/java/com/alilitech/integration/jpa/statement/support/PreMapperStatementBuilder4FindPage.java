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
 *//*

package com.alilitech.integration.jpa.statement.support;

import com.alilitech.integration.jpa.definition.GenericType;
import com.alilitech.integration.jpa.statement.MethodType;
import com.alilitech.integration.jpa.statement.PreMapperStatement;
import com.alilitech.integration.jpa.statement.PreMapperStatementBuilder;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;


*/
/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 *//*

public class PreMapperStatementBuilder4FindPage extends PreMapperStatementBuilder {

    public PreMapperStatementBuilder4FindPage(Configuration configuration, MapperBuilderAssistant builderAssistant, MethodType methodType) {
        super(configuration, builderAssistant, methodType);
    }

    @Override
    protected void buildPreMapperStatementExtend(PreMapperStatement preMapperStatement, GenericType genericType) {

        preMapperStatement.setResultType((Class)genericType.getDomainType());
        preMapperStatement.setSqlCommandType(SqlCommandType.SELECT);

        setNoKeyGenerator(preMapperStatement);

        setFindResultIdOrType(preMapperStatement, genericType);
    }

    @Override
    protected String buildSQL() {
        StringBuffer script = new StringBuffer()
                .append("<script>")
                .append("SELECT")
                .append(entityMetaData.getColumnNames())
                .append(" FROM ")
                .append(entityMetaData.getTableName())
                .append(buildWhere())
                .append(buildSort())
                .append("</script>");
        */
/*String sql = "<script>" + "SELECT " + entityMetaData.getColumnNames() + " FROM " + entityMetaData.getTableName()
                + buildWhere() + buildSort() + "</script>";
        return sql;*//*

        return script.toString();
    }

    @Override
    protected Class<?> getParameterTypeClass() {
        */
/*List<ParameterDefinition> parameterDescs = methodDefinition.getParameterDefinitions();

        int count = 0;
        Class oneClass = null;

        if(parameterDescs != null && parameterDescs.size() > 0) {
            for(ParameterDefinition parameterDesc : parameterDescs) {
                if(RowBounds.class.isAssignableFrom(parameterDesc.getParameterClass())) {
                    continue;
                }
                oneClass = parameterDesc.getParameterClass();
                count ++;
            }
        }
        if(count == 1) {
            return oneClass;
        } else {
            return void.class;
        }*//*


        return entityMetaData.getEntityType();

    }

}
*/
