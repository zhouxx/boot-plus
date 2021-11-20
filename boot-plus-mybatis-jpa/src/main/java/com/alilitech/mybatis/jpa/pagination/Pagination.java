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
package com.alilitech.mybatis.jpa.pagination;

import com.alilitech.mybatis.jpa.DatabaseType;
import com.alilitech.mybatis.jpa.domain.Pageable;

import java.beans.Transient;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public abstract class Pagination<T> extends Pageable<T> {

    /**
     * total count
     */
    protected long total;

    /**
     * page size, default value is 10
     */
    protected Integer size = 10;

    /**
     * current page index
     */
    protected Integer page = 1;

    /**
     * need to query the total count, default value is true
     */
    protected boolean selectCount = true;

    /**
     * which database type to use for pagination query
     */
    protected transient DatabaseType databaseType;

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    @Transient
    public boolean isSelectCount() {
        return selectCount;
    }

    @Override
    public void setSelectCount(boolean selectCount) {
        this.selectCount = selectCount;
    }

    @Transient
    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    @Override
    public void setDatabaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
    }
}
