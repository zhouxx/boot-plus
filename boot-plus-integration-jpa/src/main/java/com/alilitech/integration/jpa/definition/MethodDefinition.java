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
package com.alilitech.integration.jpa.definition;

import com.alilitech.integration.jpa.anotation.IfTest;
import com.alilitech.integration.jpa.exception.ParameterNumberNotMatchException;
import com.alilitech.integration.jpa.statement.parser.PartTree;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class MethodDefinition {

    private String nameSpace;

    private String methodName;

    //是否是一个参数
    private boolean oneParameter;

    //是否有if test
    private boolean methodIfTest = false;

    private IfTest ifTest ;

    private List<ParameterDefinition> parameterDefinitions = new ArrayList<>();

    //返回类型
    private Class<?> returnType;

    //是否只是返回基础信息
    private boolean baseResultMap;

    //返回复合resultMap(是否是复合查询）
    private boolean compositeResultMap;

    //传入的Sort对象index
    private int sortIndex = -1;

    //是否是specification查询
    private boolean specification;

    //此方法需要关联查询的部分，可以有多个关联
    private List<JoinStatementDefinition> joinStatementDefinitions = new ArrayList<>();

    //===========以下字段多对多关联需要====================
    //中间表名称
    private String joinTableName;

    //被关联的列名称
    private String referencedColumnName;

    private String inverseColumnName;

    private String inverseReferencedColumnName;

    public MethodDefinition(String nameSpace, Method method) {
        this(method);
        this.nameSpace = nameSpace;
    }

    public MethodDefinition(String nameSpace, String methodName) {
        this(methodName);
        this.nameSpace = nameSpace;
    }

    public MethodDefinition(String methodName) {
        this.methodName = methodName;
    }

    public MethodDefinition(Method method) {
        this.methodName = method.getName();
        methodIfTest = method.isAnnotationPresent(IfTest.class);
        if(methodIfTest) {
            ifTest = method.getAnnotation(IfTest.class);
        }

        Parameter[] parameters = method.getParameters();
        for(int index = 0; index < parameters.length; index ++) {
            parameterDefinitions.add(new ParameterDefinition(index, parameters[index]));
        }
        int size = calculate(parameterDefinitions);

        if(specification && size != 1) {
            throw new ParameterNumberNotMatchException(this.getNameSpace(), this.methodName, 1, size);
        }

        this.oneParameter = size == 1;
        returnType = method.getReturnType();

        if(PartTree.QUERY_PREFIX_TEMPLATE.matcher(methodName).find()
                && !methodName.startsWith("findWith")
                && !methodName.startsWith("findJoinWith")) {
            compositeResultMap = true;
        }
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public void setNameSpace(String nameSpace) {
        this.nameSpace = nameSpace;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public boolean isOneParameter() {
        return oneParameter;
    }

    public void setOneParameter(boolean oneParameter) {
        this.oneParameter = oneParameter;
    }

    public boolean isMethodIfTest() {
        return methodIfTest;
    }

    public void setMethodIfTest(boolean methodIfTest) {
        this.methodIfTest = methodIfTest;
    }

    public IfTest getIfTest() {
        return ifTest;
    }

    public void setIfTest(IfTest ifTest) {
        this.ifTest = ifTest;
    }

    public List<ParameterDefinition> getParameterDefinitions() {
        return parameterDefinitions;
    }

    public void setParameterDefinitions(List<ParameterDefinition> parameterDefinitions) {
        this.parameterDefinitions = parameterDefinitions;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public boolean isBaseResultMap() {
        return baseResultMap;
    }

    public void setBaseResultMap(boolean baseResultMap) {
        this.baseResultMap = baseResultMap;
    }

    public boolean isCompositeResultMap() {
        return compositeResultMap;
    }

    public void setCompositeResultMap(boolean compositeResultMap) {
        this.compositeResultMap = compositeResultMap;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public boolean isSpecification() {
        return specification;
    }

    public void setSpecification(boolean specification) {
        this.specification = specification;
    }

    public List<JoinStatementDefinition> getJoinStatementDefinitions() {
        return joinStatementDefinitions;
    }

    public void setJoinStatementDefinitions(List<JoinStatementDefinition> joinStatementDefinitions) {
        this.joinStatementDefinitions = joinStatementDefinitions;
    }

    public String getJoinTableName() {
        return joinTableName;
    }

    public void setJoinTableName(String joinTableName) {
        this.joinTableName = joinTableName;
    }

    public String getReferencedColumnName() {
        return referencedColumnName;
    }

    public void setReferencedColumnName(String referencedColumnName) {
        this.referencedColumnName = referencedColumnName;
    }

    public String getInverseColumnName() {
        return inverseColumnName;
    }

    public void setInverseColumnName(String inverseColumnName) {
        this.inverseColumnName = inverseColumnName;
    }

    public String getInverseReferencedColumnName() {
        return inverseReferencedColumnName;
    }

    public void setInverseReferencedColumnName(String inverseReferencedColumnName) {
        this.inverseReferencedColumnName = inverseReferencedColumnName;
    }

    private int calculate(List<ParameterDefinition> parameterDefinitions) {
        int count = 0;
        for (ParameterDefinition parameterDefinition : parameterDefinitions) {
            if (parameterDefinition.isPage()) {
                continue;
            }

            if(parameterDefinition.isSort()) {
                sortIndex = parameterDefinition.getIndex();
            }

            if(parameterDefinition.isSpecification()) {
                specification = true;
            }

            count++;
        }
        return count;
    }

}
