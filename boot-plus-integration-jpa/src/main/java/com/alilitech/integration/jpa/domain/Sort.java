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
package com.alilitech.integration.jpa.domain;

import com.alilitech.integration.jpa.EntityMetaDataRegistry;
import com.alilitech.integration.jpa.meta.ColumnMetaData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class Sort {

    private List<Order> orders;

    public Sort() {
    }

    public Sort(Order... orders) {
        this(Arrays.asList(orders));
    }

    public Sort(List<Order> orders) {
        this.orders = orders;
    }

    public Sort(String... properties) {
        this(Order.DEFAULT_DIRECTION, properties);
    }

    public Sort(Direction direction, String... properties) {
        this(direction, properties == null ? new ArrayList<>() : Arrays.asList(properties));
    }

    public Sort(Direction direction, List<String> properties) {

        if (properties == null || properties.isEmpty()) {
            throw new IllegalArgumentException("You have to provide at least one property to sort by!");
        }

        this.orders = new ArrayList<>(properties.size());

        for (String property : properties) {
            this.orders.add(new Order(direction, property));
        }
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public boolean hasOrders() {
        return orders != null && orders.size() != 0;
    }

    /**
     * 转换成数据库需要的字段，不能用前台转的字段是order by
     * @param clazz
     */
    public void convert(Class clazz) {

        if(!this.hasOrders()) {
           return;
        }

        Map<String, ColumnMetaData> columnMetaDataMapMap = EntityMetaDataRegistry.getInstance().get(clazz).getColumnMetaDataMap();

        orders.forEach(order -> {
            String columnName = columnMetaDataMapMap.get(order.getProperty()).getColumnName();
            order.setProperty(columnName);
        });
    }

}
