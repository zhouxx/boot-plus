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
package com.alili.integration.jpa.definition;

import com.alili.integration.jpa.anotation.IfTest;
import com.alili.integration.jpa.domain.Sort;
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

    private boolean isOneParameter;

    private boolean methodIfTest = false;

    private IfTest ifTest ;

    private List<ParameterDefinition> parameterDefinitions = new ArrayList<>();

    //是否只是返回基础信息
    private boolean isBase;

    //是直接返回对象，还是需要返回ResultMap
    private boolean isResultMap;

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
        this.isOneParameter = oneParameter(method);
        methodIfTest = method.isAnnotationPresent(IfTest.class);
        if(methodIfTest) {
            ifTest = method.getAnnotation(IfTest.class);
        }

        Annotation[][] annotations = method.getParameterAnnotations();
        Class<?>[] classes = method.getParameterTypes();
        for(int index = 0; index < classes.length; index ++) {

            List<Annotation> annotationList = Arrays.asList(annotations[index]);
            Class parameterClass = classes[index];

            parameterDefinitions.add(new ParameterDefinition(index, parameterClass, Arrays.asList(annotations[index])));

            if(parameterClass.equals(Sort.class)) {
                sortIndex = index;
            }
        }

        if(methodName.startsWith("find")) {
            isResultMap = true;
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
        return isOneParameter;
    }

    public void setOneParameter(boolean oneParameter) {
        isOneParameter = oneParameter;
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

    public boolean isBase() {
        return isBase;
    }

    public void setBase(boolean base) {
        isBase = base;
    }

    public boolean isResultMap() {
        return isResultMap;
    }

    public void setResultMap(boolean resultMap) {
        isResultMap = resultMap;
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

    private boolean oneParameter(Method method) {
        Class<?>[] classes = method.getParameterTypes();
        int count = 0;
        if(classes != null && classes.length > 0) {
            for(Class clazz : classes) {
                if(RowBounds.class.isAssignableFrom(clazz)) {
                    continue;
                }
                count ++;
            }
        }
        if(count == 1) {
            return true;
        } else {
            return false;
        }
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
