/*
 * Copyright 2008-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alilitech.mybatis.jpa.statement.parser;

import com.alilitech.mybatis.jpa.definition.MethodDefinition;
import com.alilitech.mybatis.jpa.domain.Direction;
import com.alilitech.mybatis.jpa.domain.Order;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * order by source
 * @author Oliver Gierke
 * @author Phil Webb
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class OrderBySource implements Render {

    static OrderBySource EMPTY = new OrderBySource("");

    private static final String BLOCK_SPLIT = "(?<=Asc|Desc)(?=\\p{Lu})";
    private static final Pattern DIRECTION_SPLIT = Pattern.compile("(.+?)(Asc|Desc)?$");
    private static final String INVALID_ORDER_SYNTAX = "Invalid order syntax for part %s!";
    private static final Set<String> DIRECTION_KEYWORDS = new HashSet<>(Arrays.asList("Asc", "Desc"));

    private final List<Order> orders;

    /**
     * Creates a new {@link OrderBySource} for the given String clause not doing any checks whether the referenced
     * property actually existsById.
     *
     * @param clause must not be {@literal null}.
     */
    public OrderBySource(String clause) {
        this(clause, null, null);
    }

    public OrderBySource(List<Order> orders) {
        this.orders = orders;
    }

    /**
     * Creates a new {@link OrderBySource} for the given clause, checking the property referenced existsById on the given
     * type.
     *
     * @param clause must not be {@literal null}.
     * @param domainClass can be {@literal null}.
     */
    public OrderBySource(String clause, Optional<Class> domainClass, MethodDefinition methodDefinition) {

        this.orders = new ArrayList<>();

        if (!StringUtils.hasText(clause)) {
            return;
        }

        for (String part : clause.split(BLOCK_SPLIT)) {

            Matcher matcher = DIRECTION_SPLIT.matcher(part);

            if (!matcher.find()) {
                throw new IllegalArgumentException(String.format(INVALID_ORDER_SYNTAX, part));
            }

            String propertyString = matcher.group(1);
            String directionString = matcher.group(2);

            // No property, but only a direction keyword
            if (DIRECTION_KEYWORDS.contains(propertyString) && directionString == null) {
                throw new IllegalArgumentException(String.format(INVALID_ORDER_SYNTAX, part));
            }

            Direction direction = StringUtils.hasText(directionString) ? Direction.fromString(directionString) : Direction.ASC;
            //转换排序字段
            String propertyName = StringUtils.uncapitalize(propertyString);
            //生成PropertyPath
            PropertyPath propertyPath = PropertyPath.from(propertyName, domainClass, methodDefinition);
            String columnName = propertyPath.getColumnName();
            this.orders.add(new Order(direction, columnName));
        }
    }

    @Override
    public void render(RenderContext context) {
        if(!CollectionUtils.isEmpty(orders)) {
            context.renderString("ORDER BY ");
            String delim = "";
            for(Order order : orders) {
                context.renderString(delim);
                context.renderString(StringUtils.isEmpty(context.getVariableAlias()) ? order.getProperty() : context.getVariableAlias() + "." + order.getProperty());
                context.renderBlank();
                context.renderString(order.getDirection().toString());
                delim = ", ";
            }
            //context.renderString(StringUtils.collectionToDelimitedString(orders, ", "));
        }

    }
}
