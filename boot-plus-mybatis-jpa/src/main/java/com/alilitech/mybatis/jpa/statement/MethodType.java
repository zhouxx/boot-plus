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


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public enum MethodType {

    FIND_BY_ID("findById", ParameterType.ID),
    FIND_ALL_BY_ID("findAllById", ParameterType.IDS),
    INSERT("insert", ParameterType.ENTITY),
    INSERT_SELECTIVE("insertSelective", ParameterType.ENTITY),
    UPDATE("update", ParameterType.ENTITY),
    UPDATE_SELECTIVE("updateSelective", ParameterType.ENTITY),
    DELETE_BY_ID("deleteById", ParameterType.ID),
    INSERT_BATCH("insertBatch", ParameterType.ENTITIES),
    UPDATE_BATCH("updateBatch", ParameterType.ENTITIES),
    DELETE_BATCH("deleteBatch", ParameterType.IDS),
    EXISTS_BY_ID("existsById", ParameterType.ID),

    FIND_ALL("findAll"),
    FIND_ALL_PAGE("findAllPage"),
    FIND_JOIN("findJoin"),
    FIND_SPECIFICATION("findSpecification"),

    OTHER("other");

    MethodType(String type) {
        this.type = type;
    }

    MethodType(String type, ParameterType parameterType) {
        this.type = type;
        this.parameterType = parameterType;
    }

    private String type;

    private ParameterType parameterType;

    public String getType() {
        return type;
    }

    public ParameterType getParameterType() {
        return parameterType;
    }
}
