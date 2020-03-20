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
package com.alilitech.mybatis.jpa.statement.parser;

import com.alilitech.mybatis.jpa.anotation.SubQuery;
import com.alilitech.mybatis.jpa.definition.MethodDefinition;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Zhou Xiaoxiang
 * @since 1.1
 */
public class SubQueryPartTree implements Render {

    private List<NoArgPart> noArgParts = new ArrayList<>();

    private OrderBySource orderBySource;

    public SubQueryPartTree(SubQuery subQuery, Class<?> domainClass, MethodDefinition methodDefinition) {
        if(subQuery.predicates().length > 0) {
            for(SubQuery.Predicate predicate : subQuery.predicates()) {
                noArgParts.add(new NoArgPart(predicate.property(), domainClass, methodDefinition, predicate.condition()));
            }
        }

        if(subQuery.orders().length > 0) {
            StringBuilder orderClause = new StringBuilder();
            for(SubQuery.Order order : subQuery.orders()) {
                orderClause.append(order.property().substring(0, 1).toUpperCase() + order.property().substring(1) + order.direction().toMethodNameString());
            }
           this.orderBySource = new OrderBySource(orderClause.toString(), Optional.of(domainClass), methodDefinition);
        }
    }

    @Override
    public void render(RenderContext context) {
        if(!CollectionUtils.isEmpty(noArgParts)) {
            context.renderString("<![CDATA[ ");
            noArgParts.forEach(noArgPart -> {
                context.renderString(" AND ");
                noArgPart.render(context);
            });
            context.renderString(" ]]>");
        }

        if(!StringUtils.isEmpty(orderBySource)) {
            context.renderBlank();
            orderBySource.render(context);
        }

    }
}
