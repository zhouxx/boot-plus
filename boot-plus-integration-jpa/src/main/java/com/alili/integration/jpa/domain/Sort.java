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
package com.alili.integration.jpa.domain;

import com.alili.integration.jpa.EntityMetaDataRegistry;
import com.alili.integration.jpa.meta.ColumnMetaData;

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

    public Sort(Order.Direction direction, String... properties) {
        this(direction, properties == null ? new ArrayList<String>() : Arrays.asList(properties));
    }

    public Sort(Order.Direction direction, List<String> properties) {

        if (properties == null || properties.isEmpty()) {
            throw new IllegalArgumentException("You have to provide at least one property to sort by!");
        }

        this.orders = new ArrayList<Order>(properties.size());

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
        if(orders == null || orders.size() == 0) {
            return false;
        }
        return true;
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

        orders.stream().forEach(order -> {
            String columnName = columnMetaDataMapMap.get(order.getProperty()).getColumnName();
            order.setProperty(columnName);
        });
    }

}
