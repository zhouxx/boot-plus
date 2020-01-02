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
import com.alilitech.integration.jpa.domain.Sort;
import com.alilitech.integration.jpa.criteria.CriteriaQuery;
import com.alilitech.integration.jpa.statement.parser.PartTree;
import org.apache.ibatis.session.RowBounds;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
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

    //是否是条件查询
    private boolean criteriaQuery;

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
        this.oneParameter = calculateOneParameter(method);
        this.criteriaQuery = calculateCriteriaQuery(method);
        methodIfTest = method.isAnnotationPresent(IfTest.class);
        if(methodIfTest) {
            ifTest = method.getAnnotation(IfTest.class);
        }

        Annotation[][] annotations = method.getParameterAnnotations();
        Class<?>[] classes = method.getParameterTypes();
        for(int index = 0; index < classes.length; index ++) {

            Class parameterClass = classes[index];

            parameterDefinitions.add(new ParameterDefinition(index, parameterClass, Arrays.asList(annotations[index])));

            if(parameterClass.equals(Sort.class)) {
                sortIndex = index;
            }
        }

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

    public boolean isCriteriaQuery() {
        return criteriaQuery;
    }

    public void setCriteriaQuery(boolean criteriaQuery) {
        this.criteriaQuery = criteriaQuery;
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

    private boolean calculateOneParameter(Method method) {
        Class<?>[] classes = method.getParameterTypes();
        int count = 0;
        if(classes.length > 0) {
            for(Class clazz : classes) {
                if(RowBounds.class.isAssignableFrom(clazz)) {
                    continue;
                }
                count ++;
            }
        }
        return count == 1;
    }

    private boolean calculateCriteriaQuery(Method method) {
        Class<?>[] classes = method.getParameterTypes();
        if(classes.length > 0) {
            for(Class clazz : classes) {
                if(CriteriaQuery.class.isAssignableFrom(clazz)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodDefinition)) return false;

        MethodDefinition that = (MethodDefinition) o;

        return methodName != null ? methodName.equals(that.methodName) : that.methodName == null;
    }

    @Override
    public int hashCode() {
        return methodName != null ? methodName.hashCode() : 0;
    }
}
