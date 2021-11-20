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
package com.alilitech.mybatis.jpa.domain;

import com.alilitech.mybatis.jpa.pagination.Pagination;

import java.util.List;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
@SuppressWarnings("java:S1948")
public class Page<T> extends Pagination<T> {

    private Iterable<T> content;

    private Sort sort;

    public Page() {
    }

    public Page(Integer page, Integer size) {
        this.page = page;
        this.size = size;
    }

    public Page(Integer page, Integer size, Sort sort) {
        this(page, size);
        this.sort = sort;
    }

    public Page(Order order) {
        this.sort = new Sort(order);
    }

    public Page(Sort sort) {
        this.sort = sort;
    }

    public Page(List<T> content, long total) {
        this.content = content;
        this.total = total;
    }

    public void addOrder(Order order) {
        if(sort == null || !sort.hasOrders()) {
            this.sort = new Sort(order);
        } else {
            sort.getOrders().add(order);
        }
    }

    public Iterable<T> getContent() {
        return content;
    }

    public void setContent(Iterable<T> content) {
        this.content = content;
    }

    public boolean hasContent() {
        return false;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    @Override
    public Integer getPage() {
        return page;
    }

    @Override
    public Integer getSize() {
        return size;
    }

    @Override
    public boolean hasPrevious() {
        return page > 0;
    }

    public static <T> Page<T> get() {
        return new Page<>();
    }

    public Page<T> page(Integer page) {
        this.setPage(page);
        return this;
    }

    public Page<T> size(Integer size) {
        this.setSize(size);
        return this;
    }
}
