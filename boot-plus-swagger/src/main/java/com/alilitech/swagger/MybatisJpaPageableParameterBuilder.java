/*
 *    Copyright 2017-2022 the original author or authors.
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
package com.alilitech.swagger;

import com.alilitech.mybatis.jpa.domain.Page;
import com.alilitech.mybatis.jpa.domain.Pageable;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.service.ParameterType;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.service.ResolvedMethodParameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@Order(Ordered.LOWEST_PRECEDENCE + 10)
public class MybatisJpaPageableParameterBuilder implements OperationBuilderPlugin {

    private final TypeResolver resolver;
    private final ResolvedType pageableType;

    @Autowired
    public MybatisJpaPageableParameterBuilder(TypeResolver resolver) {
        this.resolver = resolver;
        this.pageableType = resolver.resolve(Pageable.class);
    }

    @Override
    public void apply(OperationContext context) {
        List<ResolvedMethodParameter> methodParameters = context.getParameters();
        List<RequestParameter> parameters = new ArrayList<>();

        for (ResolvedMethodParameter methodParameter : methodParameters) {
            ResolvedType resolvedType = methodParameter.getParameterType();

            if (pageableType.getTypeName().equals(resolvedType.getErasedType().getName())
                || resolver.resolve(Page.class).getTypeName().equals(resolvedType.getErasedType().getName())) {

                parameters.add(new RequestParameterBuilder().in(ParameterType.QUERY).name("page").description("Page number/第几页").build());
                parameters.add(new RequestParameterBuilder().in(ParameterType.QUERY).name("size").description("Page size/每页数量").build());
                parameters.add(new RequestParameterBuilder().in(ParameterType.QUERY).name("sort").description("排序传参格式: property[,property1][,asc or desc]. "
                        + "默认排序是正序. "
                        + "可以传多个").build());

                context.operationBuilder().requestParameters(parameters);
            }
        }
    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }
}
