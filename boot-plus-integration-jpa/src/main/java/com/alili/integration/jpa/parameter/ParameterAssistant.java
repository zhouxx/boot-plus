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

import com.alili.integration.jpa.anotation.Trigger;
import com.alili.integration.jpa.exception.MybatisJpaException;
import com.alili.integration.jpa.meta.ColumnMetaData;
import com.alili.integration.jpa.meta.EntityMetaData;
import com.alili.integration.jpa.primary.key.GeneratorRegistry;
import com.alili.integration.jpa.primary.key.KeyGenerator;
import com.alili.integration.jpa.primary.key.KeyGenerator4Auto;
import org.apache.commons.jexl2.JexlEngine;
import org.apache.commons.jexl2.MapContext;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.MetaObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class ParameterAssistant {

    private Logger logger = LoggerFactory.getLogger(ParameterAssistant.class);

    public Collection<Object> getParameters(Object parameter) {
        Collection<Object> parameters = null;
        if (parameter instanceof Collection) {
            parameters = (Collection) parameter;
        } else if (parameter instanceof Map) {
            Map parameterMap = (Map) parameter;
            if (parameterMap.containsKey("collection")) {
                parameters = (Collection) parameterMap.get("collection");
            } else if (parameterMap.containsKey("list")) {
                parameters = (List) parameterMap.get("list");
            } else if (parameterMap.containsKey("array")) {
                parameters = Arrays.asList((Object[]) parameterMap.get("array"));
            }
        }
        return parameters;
    }

    public Object populateKeyAndTriggerValue(MappedStatement mappedStatement,
                                             Object parameterObject,
                                             EntityMetaData entityMetaData) {
         /* 自定义元对象填充控制器 */
        MetaObject metaObject = mappedStatement.getConfiguration().newMetaObject(parameterObject);

        if (mappedStatement.getSqlCommandType() == SqlCommandType.INSERT) {

            //设置ID
            /**
             * first: according to {@link GenerationType} to get {@link KeyGenerator}
             */
            GenerationType idGenerationType = entityMetaData.getPrimaryColumnMetaData().getIdGenerationType();

            //get id generator class
            Class generatorClass = entityMetaData.getPrimaryColumnMetaData().getIdGeneratorClass();

            KeyGenerator keyGenerator = null;

            if(idGenerationType != GenerationType.AUTO || generatorClass != KeyGenerator4Auto.class) {

                keyGenerator = GeneratorRegistry.getInstance().get(idGenerationType);

                /**
                 * if get {@link KeyGenerator} by {@link GenerationType} is null
                 */
                if(keyGenerator == null) {

                    /**
                     *  get key {@link KeyGenerator} by generator Class
                     */
                    keyGenerator = GeneratorRegistry.getInstance().get(generatorClass);

                    //new key generator
                    if(keyGenerator == null) {
                        try {
                            Object instance = generatorClass.getConstructor().newInstance();
                            if (instance instanceof KeyGenerator) {
                                keyGenerator = (KeyGenerator) instance;

                                // put generator in registry
                                GeneratorRegistry.getInstance().register(generatorClass, keyGenerator);
                            }

                        } catch (Exception e) {
                            throw new MybatisJpaException("occurs errors when try to set id value: " + e.getMessage());
                        }
                    }
                }
            }

            if(keyGenerator != null) {
                Object idValue = keyGenerator.generate();
                metaObject.setValue(entityMetaData.getPrimaryColumnMetaData().getProperty(), idValue);
            }
        }

        //设置默认值
        for(ColumnMetaData columnMetaData : entityMetaData.getColumnMetaDataMap().values()) {
            if(CollectionUtils.isEmpty(columnMetaData.getTriggers())) {
                continue;
            }
            for(Trigger trigger : columnMetaData.getTriggers()) {
                if(trigger.triggerType() == mappedStatement.getSqlCommandType()
                        && trigger.valueType() == TriggerValueType.JavaCode
                        /*&& metaObject.getValue(columnMetaData.getProperty()) == null*/) {

                    Object obj = null;

                    if(!trigger.value().equals("")) {
                        obj = new JexlEngine().createExpression(trigger.value()).evaluate(new MapContext());
                    } else {
                        try {
                            obj = trigger.valueClass().getMethod(trigger.methodName()).invoke(trigger.valueClass().newInstance());
                        } catch (IllegalAccessException e) {
                            logger.error(e.getMessage());
                        } catch (InvocationTargetException e) {
                            logger.error(e.getMessage());
                        } catch (NoSuchMethodException e) {
                            logger.error(e.getMessage());
                        } catch (InstantiationException e) {
                            logger.error(e.getMessage());
                        }
                    }

                    metaObject.setValue(columnMetaData.getProperty(), obj);
                }
            }
        }

        return metaObject.getOriginalObject();
    }
}
