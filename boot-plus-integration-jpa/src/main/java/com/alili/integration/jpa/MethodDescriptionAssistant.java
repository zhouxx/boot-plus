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
package com.alili.integration.jpa;

import com.alili.integration.jpa.definition.ParameterDefinition;
import com.alili.integration.jpa.meta.JoinColumnMetaData;
import com.alili.integration.jpa.definition.JoinStatementDefinition;
import com.alili.integration.jpa.definition.MapperDefinition;
import com.alili.integration.jpa.definition.MethodDefinition;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class MethodDescriptionAssistant {

    private JoinColumnMetaData joinColumnMetaData;

    private MapperDescriptionRegistry mapperDescriptionRegistry;

    public MethodDescriptionAssistant(MapperDescriptionRegistry mapperDescriptionRegistry, JoinColumnMetaData joinColumnMetaData) {
        this.mapperDescriptionRegistry = mapperDescriptionRegistry;
        this.joinColumnMetaData = joinColumnMetaData;
    }

    public void addRelationMethodDesc() {
        //给被关联方添加查询方法
        MapperDefinition referencedMapperDesc = mapperDescriptionRegistry.getMapperDescription(joinColumnMetaData.getJoinEntityType());
        String methodName = "";
        //直接关联，适用于OneToOne or OneToMany
        if(StringUtils.isEmpty(joinColumnMetaData.getJoinTableName())) {
            methodName = "findWith" + joinColumnMetaData.getReferencedProperty().substring(0, 1).toUpperCase() + joinColumnMetaData.getReferencedProperty().substring(1);
        } else {
            methodName = "findJoinWith" + joinColumnMetaData.getReferencedProperty().substring(0, 1).toUpperCase() + joinColumnMetaData.getReferencedProperty().substring(1);
        }

        MethodDefinition referencedMethodDesc = new MethodDefinition(methodName);

        //多对多要提供中间表相关
        if(!StringUtils.isEmpty(joinColumnMetaData.getJoinTableName())) {
            referencedMethodDesc.setJoinTableName(joinColumnMetaData.getJoinTableName());
            referencedMethodDesc.setReferencedColumnName(joinColumnMetaData.getReferencedColumnName());
            referencedMethodDesc.setInverseReferencedColumnName(joinColumnMetaData.getInverseReferencedColumnName());
            referencedMethodDesc.setInverseColumnName(joinColumnMetaData.getInverseColumnName());
        }

        referencedMethodDesc.setBase(true);  //关联查询目前只支持一层查询
        referencedMethodDesc.setResultMap(true);  //需要返回resultMap
        referencedMethodDesc.setOneParameter(true);  //只有一个参数
        ParameterDefinition parameterDesc = new ParameterDefinition(0, joinColumnMetaData.getPropertyType());
        referencedMethodDesc.getParameterDefinitions().add(parameterDesc);
        referencedMapperDesc.getMethodDefinitions().add(referencedMethodDesc);

        //给当前Mapper的方法添加关联查询
        MapperDefinition mapperDesc = mapperDescriptionRegistry.getMapperDescription(joinColumnMetaData.getEntityType());

        for(MethodDefinition methodDesc : mapperDesc.getMethodDefinitions()) {
            if(methodDesc.getMethodName().startsWith("find")
                    && !methodDesc.getMethodName().startsWith("findWith")
                    && !methodDesc.getMethodName().startsWith("findJoinWith")) {

                //被排除的，和不在包含之内的都不需要查询
                if(!CollectionUtils.isEmpty(joinColumnMetaData.getExcludes()) && joinColumnMetaData.getExcludes().contains(methodDesc.getMethodName())) {
                    continue;
                } else if(!CollectionUtils.isEmpty(joinColumnMetaData.getIncludes()) && !joinColumnMetaData.getIncludes().contains(methodDesc.getMethodName())) {
                    continue;
                }

                String nestedSelect = referencedMapperDesc.getNameSpace() + "." + methodName;

                JoinStatementDefinition joinStatementDesc = new JoinStatementDefinition(
                        (Class) joinColumnMetaData.getJoinEntityType(),
                        joinColumnMetaData.getCurrentProperty(),
                        joinColumnMetaData.getColumnName(),
                        nestedSelect);
                if(!joinColumnMetaData.isCollection()) {
                    joinStatementDesc.setJavaType((Class) joinColumnMetaData.getJoinEntityType());
                }

                methodDesc.getJoinStatementDefinitions().add(joinStatementDesc);
            }
        }
    }
}
