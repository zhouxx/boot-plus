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

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.method.HandlerMethod;

/**
 * Expands mybatis-jpa {@code Pageable}/{@code Page} arguments into query parameters.
 * The types are matched by name so this module does not compile against mybatis-jpa.
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@Order(Ordered.LOWEST_PRECEDENCE + 10)
public class MybatisJpaPageableParameterBuilder implements OperationCustomizer {

    private static final String PAGEABLE = "com.alilitech.mybatis.jpa.domain.Pageable";
    private static final String PAGE = "com.alilitech.mybatis.jpa.domain.Page";

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        boolean matched = false;
        for (MethodParameter methodParameter : handlerMethod.getMethodParameters()) {
            String typeName = methodParameter.getParameterType().getName();
            if (PAGEABLE.equals(typeName) || PAGE.equals(typeName)) {
                matched = true;
                break;
            }
        }
        if (!matched) {
            return operation;
        }
        operation.addParametersItem(query("page", "Page number/第几页"));
        operation.addParametersItem(query("size", "Page size/每页数量"));
        operation.addParametersItem(query("sort", "排序传参格式: property[,property1][,asc or desc]. 默认排序是正序. 可以传多个"));
        return operation;
    }

    private Parameter query(String name, String description) {
        return new Parameter().in("query").name(name).description(description).schema(new StringSchema());
    }
}
