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
package com.alilitech.swagger;

import com.alilitech.constants.Profiles;
import com.alilitech.mybatis.jpa.domain.Page;
import com.alilitech.mybatis.jpa.domain.Pageable;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.schema.TypeNameExtractor;
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
@Profile("!" + Profiles.SPRING_PROFILE_PRODUCTION)
@Order(Ordered.LOWEST_PRECEDENCE + 10)
public class MybatisJpaPageableParameterBuilder implements OperationBuilderPlugin {

    private final TypeNameExtractor nameExtractor;
    private final TypeResolver resolver;
    private final ResolvedType pageableType;

    @Autowired
    public MybatisJpaPageableParameterBuilder(TypeNameExtractor nameExtractor, TypeResolver resolver) {
        this.nameExtractor = nameExtractor;
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
//                ParameterContext parameterContext = new ParameterContext(methodParameter,
//                        new ParameterBuilder(),
//                        context.getDocumentationContext(),
//                        context.getGenericsNamingStrategy(),
//                        context);
//                Function<ResolvedType, ? extends ModelReference> factory = createModelRefFactory(parameterContext);

//                ModelReference intModel = factory.apply(resolver.resolve(Integer.TYPE));
//                ModelReference stringModel = factory.apply(resolver.resolve(List.class, String.class));

                parameters.add(new RequestParameterBuilder().in(ParameterType.QUERY).name("page").description("Page number/第几页").build());
                parameters.add(new RequestParameterBuilder().in(ParameterType.QUERY).name("size").description("Page size/每页数量").build());
                parameters.add(new RequestParameterBuilder().in(ParameterType.QUERY).name("sort").description("排序传参格式: property[,property1][,asc or desc]. "
                        + "默认排序是正序. "
                        + "可以传多个").build());


//                parameters.add(new ParameterBuilder()
//                        .parameterType("query").name("page").modelRef(intModel)
//                        .description("Page number/第几页")
//                        .build());
//                parameters.add(new ParameterBuilder()
//                        .parameterType("query").name("size").modelRef(intModel)
//                        .description("Page size/每页数量")
//                        .build());
//                parameters.add(new ParameterBuilder()
//                        .parameterType("query")
//                        .name("sort")
//                        .modelRef(stringModel)
//                        .allowMultiple(true)
//                        .description("Sorting criteria in the format: property(,asc or desc). "
//                                + "Default sort order is ascending. "
//                                + "Multiple sort criteria are supported.")
//                        .build());
                context.operationBuilder().requestParameters(parameters);
            }
        }
    }

//    private Function<ResolvedType, ? extends ModelReference> createModelRefFactory(ParameterContext context) {
//        ModelContext modelContext = inputParam(
//                context.getGroupName(),
//                context.resolvedMethodParameter().getParameterType(),
//                context.getDocumentationType(),
//                context.getAlternateTypeProvider(),
//                context.getGenericNamingStrategy(),
//                context.getIgnorableParameterTypes());
//        return ResolvedTypes.modelRefFactory(modelContext, nameExtractor);
//    }

    @Override
    public boolean supports(DocumentationType delimiter) {
        return true;
    }
}
