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

import com.alilitech.mybatis.jpa.definition.JoinStatementDefinition;
import com.alilitech.mybatis.jpa.definition.MapperDefinition;
import com.alilitech.mybatis.jpa.definition.MethodDefinition;
import com.alilitech.mybatis.jpa.definition.ParameterDefinition;
import com.alilitech.mybatis.jpa.meta.JoinColumnMetaData;
import com.alilitech.mybatis.jpa.util.CommonUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class MethodDefinitionAssistant {

    private final JoinColumnMetaData joinColumnMetaData;

    private final MapperDefinitionRegistry mapperDefinitionRegistry;

    public MethodDefinitionAssistant(MapperDefinitionRegistry mapperDefinitionRegistry, JoinColumnMetaData joinColumnMetaData) {
        this.mapperDefinitionRegistry = mapperDefinitionRegistry;
        this.joinColumnMetaData = joinColumnMetaData;
    }

    public void addRelationMethodDefinition() {
        //给被关联方添加查询方法
        MapperDefinition referencedMapperDefinition = mapperDefinitionRegistry.getMapperDefinition(joinColumnMetaData.getJoinEntityType());
        String methodName;
        //直接关联，适用于OneToOne or OneToMany
        if(StringUtils.isEmpty(joinColumnMetaData.getJoinTableName())) {
            methodName = "findWith" + CommonUtils.upperFirst(joinColumnMetaData.getReferencedProperty());
        } else {
            methodName = "findJoinWith" + CommonUtils.upperFirst(joinColumnMetaData.getReferencedProperty());
        }

        MethodDefinition referencedMethodDefinition = new MethodDefinition(referencedMapperDefinition.getNameSpace(), methodName);

        //设置子查询容器
        if(joinColumnMetaData.getSubQuery() != null) {
            SubQueryContainer.getInstance().put(referencedMethodDefinition.getStatementId(), joinColumnMetaData.getSubQuery());
        }

        //多对多要提供中间表相关
        if(!StringUtils.isEmpty(joinColumnMetaData.getJoinTableName())) {
            referencedMethodDefinition.setJoinTableName(joinColumnMetaData.getJoinTableName());
            referencedMethodDefinition.setReferencedColumnName(joinColumnMetaData.getReferencedColumnName());
            referencedMethodDefinition.setInverseReferencedColumnName(joinColumnMetaData.getInverseReferencedColumnName());
            referencedMethodDefinition.setInverseColumnName(joinColumnMetaData.getInverseColumnName());
        }

        referencedMethodDefinition.setBaseResultMap(true);  //关联查询目前只支持一层查询
        //referencedMethodDefinition.setCompositeResultMap(true);  //需要返回resultMap
        referencedMethodDefinition.setOneParameter(true);  //只有一个参数
        ParameterDefinition parameterDefinition = new ParameterDefinition(0, joinColumnMetaData.getPropertyType());
        referencedMethodDefinition.getParameterDefinitions().add(parameterDefinition);
        referencedMapperDefinition.getMethodDefinitions().add(referencedMethodDefinition);

        //给当前Mapper的方法添加关联查询
        MapperDefinition mapperDefinition = mapperDefinitionRegistry.getMapperDefinition(joinColumnMetaData.getEntityType());

        for(MethodDefinition methodDefinition : mapperDefinition.getMethodDefinitions()) {
            if(methodDefinition.isCompositeResultMap()) {

                // 只是定义关联关系，实际并没有任何真正的关联查询
                if(joinColumnMetaData.isJoinNothing()) {
                    continue;
                }

                //被排除的，和不在包含之内的都不需要查询
                if(!CollectionUtils.isEmpty(joinColumnMetaData.getExcludes()) && joinColumnMetaData.getExcludes().contains(methodDefinition.getMethodName())) {
                    continue;
                } else if(!CollectionUtils.isEmpty(joinColumnMetaData.getIncludes()) && !joinColumnMetaData.getIncludes().contains(methodDefinition.getMethodName())) {
                    continue;
                }

                String nestedSelect = referencedMapperDefinition.getNameSpace() + "." + methodName;

                JoinStatementDefinition joinStatementDefinition = new JoinStatementDefinition(
                        (Class<?>) joinColumnMetaData.getJoinEntityType(),
                        joinColumnMetaData.getCurrentProperty(),
                        joinColumnMetaData.getColumnName(),
                        nestedSelect);
                if(!joinColumnMetaData.isCollection()) {
                    joinStatementDefinition.setJavaType((Class<?>) joinColumnMetaData.getJoinEntityType());
                }

                methodDefinition.getJoinStatementDefinitions().add(joinStatementDefinition);
            }
        }
    }
}
