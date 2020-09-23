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
package com.alilitech.mybatis.jpa.parameter;

import com.alilitech.mybatis.jpa.EntityMetaDataRegistry;
import com.alilitech.mybatis.jpa.meta.EntityMetaData;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.RowBounds;

import java.sql.Statement;
import java.util.Collection;
import java.util.List;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class TriggerValue4SelectKeyGenerator extends SelectKeyGenerator {

    private boolean executeBefore;
    private MappedStatement keyStatement;

    private ParameterAssistant parameterAssistant = new ParameterAssistant();

    public TriggerValue4SelectKeyGenerator(MappedStatement keyStatement, boolean executeBefore) {
        super(keyStatement, executeBefore);
        this.keyStatement = keyStatement;
        this.executeBefore = executeBefore;
    }

    @Override
    public void processBefore(Executor executor, MappedStatement mappedStatement, Statement stmt, Object parameterObject) {

        if (!(mappedStatement.getSqlCommandType() == SqlCommandType.INSERT || mappedStatement.getSqlCommandType() == SqlCommandType.UPDATE)) {
            return;
        }

        Collection<Object> parameters = parameterAssistant.getParameters(parameterObject);
        //trigger auto set value
        if (parameters != null) {
            for (Object parameter : parameters) {
                EntityMetaData entityMetaData = EntityMetaDataRegistry.getInstance().get(parameter.getClass());
                parameterAssistant.populateKeyAndTriggerValue(mappedStatement, parameter, entityMetaData);
            }
        } else {
            EntityMetaData entityMetaData = EntityMetaDataRegistry.getInstance().get(parameterObject.getClass());
            parameterAssistant.populateKeyAndTriggerValue(mappedStatement, parameterObject, entityMetaData);
        }

        //id select and set
        if(mappedStatement.getSqlCommandType() == SqlCommandType.INSERT) {
            if (executeBefore) {
                if (parameters != null) {
                    for (Object parameter : parameters) {
                        processGeneratedKeys(executor, mappedStatement, parameter);
                    }
                } else {
                    processGeneratedKeys(executor, mappedStatement, parameterObject);
                }

            }
        }
    }

    private void processGeneratedKeys(Executor executor, MappedStatement ms, Object parameter) {
        try {
            if (parameter != null && keyStatement != null && keyStatement.getKeyProperties() != null) {
                String[] keyProperties = keyStatement.getKeyProperties();
                final Configuration configuration = ms.getConfiguration();
                final MetaObject metaParam = configuration.newMetaObject(parameter);
                if (keyProperties != null) {
                    // Do not close keyExecutor.
                    // The transaction will be closed by parent executor.
                    Executor keyExecutor = configuration.newExecutor(executor.getTransaction(), ExecutorType.SIMPLE);
                    List<Object> values = keyExecutor.query(keyStatement, parameter, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
                    if (values.size() == 0) {
                        throw new ExecutorException("SelectKey returned no data.");
                    } else if (values.size() > 1) {
                        throw new ExecutorException("SelectKey returned more than one value.");
                    } else {
                        MetaObject metaResult = configuration.newMetaObject(values.get(0));
                        if (keyProperties.length == 1) {
                            if (metaResult.hasGetter(keyProperties[0])) {
                                setValue(metaParam, keyProperties[0], metaResult.getValue(keyProperties[0]));
                            } else {
                                // no getter for the property - maybe just a single value object
                                // so try that
                                setValue(metaParam, keyProperties[0], values.get(0));
                            }
                        } else {
                            handleMultipleProperties(keyProperties, metaParam, metaResult);
                        }
                    }
                }
            }
        } catch (ExecutorException e) {
            throw e;
        } catch (Exception e) {
            throw new ExecutorException("Error selecting key or setting result to parameter object. Cause: " + e, e);
        }
    }

    private void handleMultipleProperties(String[] keyProperties,
                                          MetaObject metaParam, MetaObject metaResult) {
        String[] keyColumns = keyStatement.getKeyColumns();

        if (keyColumns == null || keyColumns.length == 0) {
            // no key columns specified, just use the property names
            for (String keyProperty : keyProperties) {
                setValue(metaParam, keyProperty, metaResult.getValue(keyProperty));
            }
        } else {
            if (keyColumns.length != keyProperties.length) {
                throw new ExecutorException("If SelectKey has key columns, the number must match the number of key properties.");
            }
            for (int i = 0; i < keyProperties.length; i++) {
                setValue(metaParam, keyProperties[i], metaResult.getValue(keyColumns[i]));
            }
        }
    }

    private void setValue(MetaObject metaParam, String property, Object value) {
        if (metaParam.hasSetter(property)) {
            metaParam.setValue(property, value);
        } else {
            throw new ExecutorException("No setter found for the keyProperty '" + property + "' in " + metaParam.getOriginalObject().getClass().getName() + ".");
        }
    }
}
