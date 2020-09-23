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
package com.alilitech.mybatis.dialect;


import com.alilitech.mybatis.jpa.DatabaseTypeRegistry;
import com.alilitech.mybatis.jpa.DatabaseType;
import com.alilitech.mybatis.jpa.pagination.PageHelper;
import com.alilitech.mybatis.jpa.pagination.Pagination;


/**
 * Sql Dialect Factory
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class SqlDialectFactory {

    private final DatabaseType databaseType;

    public SqlDialectFactory(String databaseId) {
        this.databaseType = DatabaseTypeRegistry.getInstance().get(databaseId);
    }

    /**
     * generate pageable SQL
     * @param page pageable Object
     * @param buildSql original SQL
     * @return pageable sql
     */
    public String buildPaginationSql(Pagination<?> page, String buildSql)  {
        return PaginationDialectRegistry.getInstance().get(databaseType).buildPaginationSql(buildSql, PageHelper.offsetCurrent(page), page.getSize());
    }

    /**
     * generate the sql to select the sequence
     * @param sequenceName sequence name
     * @return key generator script
     */
    public String buildKeyGeneratorScript(String sequenceName) {
        return KeySqlDialectRegistry.getInstance().get(databaseType).generateKeySql(sequenceName);
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }
}
