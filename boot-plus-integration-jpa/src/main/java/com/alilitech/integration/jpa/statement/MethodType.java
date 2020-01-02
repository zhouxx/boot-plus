/**
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
package com.alilitech.integration.jpa.statement;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public enum MethodType {

    /*find("find"),*/
    findById("findById", ParameterType.ID),
    findAllById("findAllById", ParameterType.IDS),
    /*findPage("findPage"),*/
    insert("insert", ParameterType.ENTITY),
    insertSelective("insertSelective", ParameterType.ENTITIES),
    update("update", ParameterType.ENTITY),
    updateSelective("updateSelective", ParameterType.ENTITY),
    deleteById("deleteById", ParameterType.ID),
    insertBatch("insertBatch", ParameterType.ENTITIES),
    updateBatch("updateBatch", ParameterType.ENTITIES),
    deleteBatch("deleteBatch", ParameterType.IDS),
    existsById("existsById", ParameterType.ID),

    findAll("findAll"),
    findAllPage("findAllPage"),
    findJoin("findJoin"),
    findSpecification("findSpecification"),
    findPageSpecification("findPageSpecification"),

    other("other");

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
