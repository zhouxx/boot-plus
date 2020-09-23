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
package com.alilitech.mybatis.jpa.definition;

import org.springframework.core.GenericTypeResolver;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 用于生成关联查询的mapping，一个语句可能会有多个mapping
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class MapperDefinition {

    private Class mapper;

    private String resource;

    private String resource1;

    private String nameSpace;

    private GenericType genericType;

    private List<MethodDefinition> methodDefinitions = new ArrayList<>();

    public MapperDefinition(Class<?> mapper) {
        this.resource = mapper.getName().replace(".", "/") + ".java (best guess)";
        this.resource1 = mapper.toString();
        this.nameSpace = mapper.getName();
        this.genericType = genericType(mapper);

        for (Method method : mapper.getMethods()) {
            MethodDefinition methodDefinition = new MethodDefinition(nameSpace, method);
            methodDefinitions.add(methodDefinition);
        }
    }

    public Class getMapper() {
        return mapper;
    }

    public void setMapper(Class mapper) {
        this.mapper = mapper;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getResource1() {
        return resource1;
    }

    public void setResource1(String resource1) {
        this.resource1 = resource1;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public GenericType getGenericType() {
        return genericType;
    }

    public void setGenericType(GenericType genericType) {
        this.genericType = genericType;
    }

    public List<MethodDefinition> getMethodDefinitions() {
        return methodDefinitions;
    }

    public void setMethodDefinitions(List<MethodDefinition> methodDefinitions) {
        this.methodDefinitions = methodDefinitions;
    }

    private GenericType genericType(Class<?> mapper) {
        Map<TypeVariable, Type> map = GenericTypeResolver.getTypeVariableMap(mapper);

        GenericType genericType = new GenericType(mapper);

        map.forEach((typeVariable, type) -> {
            if(typeVariable.getName().equals("T")) {
                genericType.setDomainType(type);
            }
            if(typeVariable.getName().equals("ID")) {
                genericType.setIdType(type);
            }
        });

        return genericType;
    }
}
