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
package com.alili.integration.jpa.statement;

import com.alili.integration.jpa.definition.GenericType;
import com.alili.integration.jpa.definition.MethodDefinition;
import com.alili.integration.jpa.exception.MybatisJpaException;
import com.alili.integration.jpa.statement.support.*;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class PreMapperStatementFactory {

    private Logger logger = LoggerFactory.getLogger(PreMapperStatementFactory.class);

    private static PreMapperStatementFactory preMapperStatementFactory;

    private final Map<MethodType, Class<? extends PreMapperStatementBuilder>> cacheMap = new ConcurrentHashMap<>();

    private PreMapperStatementFactory() {}

    public static PreMapperStatementFactory getInstance() {
        if(preMapperStatementFactory == null) {

            synchronized (PreMapperStatementFactory.class) {

                if(preMapperStatementFactory == null) {
                    preMapperStatementFactory = new PreMapperStatementFactory();

                    //注册相关类型的builder
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.find, PreMapperStatementBuilder4Find.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.findOne, PreMapperStatementBuilder4FindOne.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.findAll, PreMapperStatementBuilder4FindAll.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.findAllPage, PreMapperStatementBuilder4FindAllPage.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.findAllIds, PreMapperStatementBuilder4FindAllIds.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.findPage, PreMapperStatementBuilder4FindPage.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.findJoin, PreMapperStatementBuilder4FindJoin.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.insert, PreMapperStatementBuilder4Insert.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.insertSelective, PreMapperStatementBuilder4InsertSelective.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.update, PreMapperStatementBuilder4Update.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.updateSelective, PreMapperStatementBuilder4UpdateSelective.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.delete, PreMapperStatementBuilder4Delete.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.insertBatch, PreMapperStatementBuilder4InsertBatch.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.updateBatch, PreMapperStatementBuilder4UpdateBatch.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.deleteBatch, PreMapperStatementBuilder4DeleteBatch.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.exists, PreMapperStatementBuilder4Exists.class);
                }

            }

        }

        return preMapperStatementFactory;
    }

    public void registerPreMapperStatementBuilder(MethodType methodType, Class<? extends PreMapperStatementBuilder> clazz) {
        this.cacheMap.put(methodType, clazz);
    }

    public PreMapperStatement createPreMapperStatement(Configuration configuration, MapperBuilderAssistant builderAssistant, MethodDefinition methodDesc, GenericType genericType) {
        MethodType methodType = StatementAssistant.resolveMethodType(methodDesc.getMethodName());

        if(!cacheMap.containsKey(methodType)) {
            throw new MybatisJpaException("Can not find " + methodType.getType() + " for PreMapperStatementBuilder.class!");
        }

        Constructor constructor = null;
        try {
            constructor = cacheMap.get(methodType).getConstructor(Configuration.class, MapperBuilderAssistant.class);
        } catch (NoSuchMethodException e) {
            logger.error(e.getMessage());
        }
        PreMapperStatementBuilder preMapperStatementBuilder = null;
        try {
            preMapperStatementBuilder = (PreMapperStatementBuilder) constructor.newInstance(configuration, builderAssistant);
        } catch (InstantiationException e) {
            logger.error(e.getMessage());
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage());
        } catch (InvocationTargetException e) {
            logger.error(e.getMessage());
        }

        return preMapperStatementBuilder.buildPreMapperStatement(methodDesc, genericType);
    }

}
