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
package com.alilitech.mybatis.jpa;

import com.alilitech.mybatis.jpa.meta.EntityMetaData;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class EntityMetaDataRegistry {

    private final Log log = LogFactory.getLog(EntityMetaDataRegistry.class);

    private final Map<Class<?>, EntityMetaData> entityMetaDataMap = new ConcurrentHashMap<>();

    private static final EntityMetaDataRegistry entityMetaDataRegistry = new EntityMetaDataRegistry();

    private EntityMetaDataRegistry() {
    }

    public static EntityMetaDataRegistry getInstance() {
        return entityMetaDataRegistry;
    }

    public void register(Class<?> entityClass) {
        synchronized (entityMetaDataMap) {
            EntityMetaData entityMetaData = new EntityMetaData(entityClass);
            if(entityMetaData.getPrimaryColumnMetaData() == null) {
                log.warn("The domain class called '" + entityClass.getName() + "' do not have a primary key.");
            }
            entityMetaDataMap.put(entityClass, entityMetaData);
        }
    }

    public EntityMetaData get(Type clazz) {
        return entityMetaDataMap.get(clazz);
    }

    public Collection<EntityMetaData> values() {
        return entityMetaDataMap.values();
    }

}
