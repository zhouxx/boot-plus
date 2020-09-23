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

import com.alilitech.mybatis.jpa.definition.MapperDefinition;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class MapperDefinitionRegistry {

    private final Map<Type, MapperDefinition> entityMapperRelation = new ConcurrentHashMap<>();

    public MapperDefinitionRegistry() {
    }

    public Class register(Class<?> mapperClass) {
        synchronized (entityMapperRelation) {
            MapperDefinition mapperDefinition = new MapperDefinition(mapperClass);
            Type entityType = mapperDefinition.getGenericType().getDomainType();
            if (entityType instanceof Class) {
                Class type = (Class) entityType;
                if (!type.isAnonymousClass() && !type.isInterface() && !type.isMemberClass()) {
                    entityMapperRelation.put(entityType, mapperDefinition);
                }

                return type;
            }
        }
        return null;
    }

    public MapperDefinition getMapperDefinition(Type entityType) {
        return entityMapperRelation.get(entityType);
    }

    public Collection<MapperDefinition> values() {
        return entityMapperRelation.values();
    }

}
