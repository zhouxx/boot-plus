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
public class PreMapperStatementBuilder4Find extends PreMapperStatementBuilder {

    public PreMapperStatementBuilder4Find(Configuration configuration, MapperBuilderAssistant builderAssistant) {
        super(configuration, builderAssistant);
    }

    @Override
    protected void buildPreMapperStatementExtend(PreMapperStatement preMapperStatement, GenericType genericType) {

        preMapperStatement.setSqlCommandType(SqlCommandType.SELECT);

        setNoKeyGenerator(preMapperStatement);

        setFindResultIdOrType(preMapperStatement, genericType);
    }

    @Override
    protected String buildSQL() {
        StringBuffer buffer = new StringBuffer()
                .append("<script>")
                .append("SELECT")
                .append(entityMetaData.getColumnNames())
                .append(" FROM ")
                .append(entityMetaData.getTableName())
                .append(buildWhere())
                .append(buildSort())
                .append("</script>");
        return buffer.toString();
        /*String sql = "<script>" + "SELECT " + entityMetaData.getColumnNames() + " FROM " + entityMetaData.getTableName()
                + buildWhere() + buildSort() + "</script>";
//      System.out.println(sql);
        return sql;*/
    }

    @Override
    protected Class<?> getParameterTypeClass() {
        /*if (methodDesc.getParameterDefinitions().size() > 0) {
            // Mybatis mapper 方法最多支持一个参数,先设置成Object.class,mybatis会在sql中解析
            return (Object.class);
        }
        return void.class;*/
        return entityMetaData.getEntityType();
    }



}
