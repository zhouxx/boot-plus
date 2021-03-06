/*
 *    Copyright 2017-2021 the original author or authors.
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
package com.alilitech.mybatis.jpa.statement;

import com.alilitech.mybatis.jpa.definition.GenericType;
import com.alilitech.mybatis.jpa.definition.MethodDefinition;
import com.alilitech.mybatis.jpa.exception.StatementNotSupportException;
import com.alilitech.mybatis.jpa.statement.support.*;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.apache.ibatis.session.Configuration;

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

    private final Log log = LogFactory.getLog(PreMapperStatementFactory.class);

    private static PreMapperStatementFactory preMapperStatementFactory;

    private final Map<MethodType, Class<? extends PreMapperStatementBuilder>> cacheMap = new ConcurrentHashMap<>();

    private PreMapperStatementFactory() {}

    public static PreMapperStatementFactory getInstance() {
        if(preMapperStatementFactory == null) {

            synchronized (PreMapperStatementFactory.class) {

                if(preMapperStatementFactory == null) {
                    preMapperStatementFactory = new PreMapperStatementFactory();

                    //注册相关类型的builder
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.other, PreMapperStatementBuilder4Find.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.findSpecification, PreMapperStatementBuilder4FindSpecification.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.findById, PreMapperStatementBuilder4FindById.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.findAll, PreMapperStatementBuilder4FindAll.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.findAllById, PreMapperStatementBuilder4findAllById.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.findJoin, PreMapperStatementBuilder4FindJoin.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.insert, PreMapperStatementBuilder4Insert.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.insertSelective, PreMapperStatementBuilder4InsertSelective.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.update, PreMapperStatementBuilder4Update.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.updateSelective, PreMapperStatementBuilder4UpdateSelective.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.deleteById, PreMapperStatementBuilder4DeleteById.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.insertBatch, PreMapperStatementBuilder4InsertBatch.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.updateBatch, PreMapperStatementBuilder4UpdateBatch.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.deleteBatch, PreMapperStatementBuilder4DeleteBatch.class);
                    preMapperStatementFactory.registerPreMapperStatementBuilder(MethodType.existsById, PreMapperStatementBuilder4ExistsById.class);
                }
            }
        }

        return preMapperStatementFactory;
    }

    public void registerPreMapperStatementBuilder(MethodType methodType, Class<? extends PreMapperStatementBuilder> clazz) {
        this.cacheMap.put(methodType, clazz);
    }

    public PreMapperStatement createPreMapperStatement(Configuration configuration, MapperBuilderAssistant builderAssistant, MethodDefinition methodDefinition, GenericType genericType) {
        MethodType methodType = StatementAssistant.resolveMethodType(methodDefinition);

        if(!cacheMap.containsKey(methodType)) {
            throw new StatementNotSupportException(methodDefinition.getNameSpace(), methodDefinition.getMethodName());
        }

        PreMapperStatementBuilder preMapperStatementBuilder = null;
        try {
            Constructor<?> constructor = cacheMap.get(methodType).getConstructor(Configuration.class, MapperBuilderAssistant.class, MethodType.class);
            preMapperStatementBuilder = (PreMapperStatementBuilder) constructor.newInstance(configuration, builderAssistant, methodType);
            return preMapperStatementBuilder.buildPreMapperStatement(methodDefinition, genericType);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("instance 'PreMapperStatementBuilder' error: ", e);
        }

        return null;

    }

}
