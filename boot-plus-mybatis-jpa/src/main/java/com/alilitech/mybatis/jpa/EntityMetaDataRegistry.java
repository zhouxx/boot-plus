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
package com.alilitech.mybatis.jpa;

import com.alilitech.mybatis.jpa.meta.EntityMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Logger logger = LoggerFactory.getLogger(EntityMetaDataRegistry.class);

    private final Map<Class, EntityMetaData> entityMetaDataMap = new ConcurrentHashMap<>();

    private static final EntityMetaDataRegistry entityMetaDataFactory = new EntityMetaDataRegistry();

    private EntityMetaDataRegistry() {
    }

    public static EntityMetaDataRegistry getInstance() {
        return entityMetaDataFactory;
    }

    public void register(Class entityClass) {
        synchronized (entityMetaDataMap) {
            EntityMetaData entityMetaData = new EntityMetaData(entityClass);
            if(entityMetaData.getPrimaryColumnMetaData() == null) {
                logger.warn("The domain class called '{}' do not have a primary key.", entityClass.getName());
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
