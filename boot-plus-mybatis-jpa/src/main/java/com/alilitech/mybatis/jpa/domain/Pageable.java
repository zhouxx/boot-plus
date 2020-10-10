/*
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
package com.alilitech.mybatis.jpa.domain;

import com.alilitech.mybatis.jpa.DatabaseType;
import org.apache.ibatis.session.RowBounds;

import java.io.Serializable;

/**
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public abstract class Pageable<T> extends RowBounds implements Serializable {

    /**
     * Returns the page to be returned.
     * @return the page to be returned.
     */
    public abstract Integer getPage();

    /**
     * Returns the number of items to be returned.
     * @return the number of items of that page
     */
    public abstract Integer getSize();


    public abstract long getTotal() ;


    /**
     * Returns the sorting parameters.
     * @return sort
     */
    public abstract Sort getSort();

    /**
     * Set return's content
     * @param content content
     */
    public abstract void setContent(Iterable<T> content);

    /**
     * Returns whether there's a previous {@link Pageable} we can access from the current one. Will return
     * {@literal false} in case the current {@link Pageable} already refers to the first page.
     * @return has previous
     */
    public abstract boolean hasPrevious();

    /**
     * set select count
     * @return
     */
    public abstract void setSelectCount(boolean selectCount);

    /**
     * set the current page used {@link DatabaseType}
     */
    public abstract void setDatabaseType(DatabaseType databaseType);

}
