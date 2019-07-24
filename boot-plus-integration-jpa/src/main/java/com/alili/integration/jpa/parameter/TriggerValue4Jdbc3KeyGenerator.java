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
package com.alili.integration.jpa.parameter;

import com.alili.integration.jpa.EntityMetaDataRegistry;
import com.alili.integration.jpa.meta.EntityMetaData;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;

import java.sql.Statement;
import java.util.Collection;


/**
 * 两个功能：
 * 一个是为UUID，设置主键，还有主键回显。
 * 一个是设置默认值
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class TriggerValue4Jdbc3KeyGenerator extends Jdbc3KeyGenerator {

    private ParameterAssistant parameterAssistant = new ParameterAssistant();

    @Override
    public void processBefore(Executor executor, MappedStatement mappedStatement, Statement stmt, Object parameterObject) {

        if (!(mappedStatement.getSqlCommandType() == SqlCommandType.INSERT || mappedStatement.getSqlCommandType() == SqlCommandType.UPDATE)) {
            return;
        }

        //批量插入的
        Collection<Object> parameters = parameterAssistant.getParameters(parameterObject);
        if (parameters != null) {
            for (Object parameter : parameters) {
                EntityMetaData entityMetaData = EntityMetaDataRegistry.getInstance().get(parameter.getClass());
                parameterAssistant.populateKeyAndTriggerValue(mappedStatement, parameter, entityMetaData);
            }
        } else {
            EntityMetaData entityMetaData = EntityMetaDataRegistry.getInstance().get(parameterObject.getClass());
            parameterAssistant.populateKeyAndTriggerValue(mappedStatement, parameterObject, entityMetaData);
        }
    }

    @Override
    public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {
        super.processAfter(executor, ms, stmt, parameter);
    }
}
