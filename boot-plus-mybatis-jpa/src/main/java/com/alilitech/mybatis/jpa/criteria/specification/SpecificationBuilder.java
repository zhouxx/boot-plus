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
package com.alilitech.mybatis.jpa.criteria.specification;

import com.alilitech.mybatis.jpa.criteria.CriteriaBuilder;
import com.alilitech.mybatis.jpa.criteria.CriteriaQuery;
import com.alilitech.mybatis.jpa.criteria.Specification;

/**
 * @author Zhou Xiaoxiang
 * @since 1.1
 */

public interface SpecificationBuilder<T> {

    default OrderBuilder<T> order() {
        return new OrderBuilder<>(this);
    }

    void build(CriteriaBuilder<T> cb, CriteriaQuery<T> query);

    default Specification<T> build() {
        return (cb, query) -> {
            this.build(cb, query);
            return null;
        };
    }
}
