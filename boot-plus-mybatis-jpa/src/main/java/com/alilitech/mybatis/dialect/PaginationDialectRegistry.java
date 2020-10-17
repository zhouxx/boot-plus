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

import com.alilitech.mybatis.dialect.pagination.PaginationDialect;
import com.alilitech.mybatis.dialect.pagination.support.*;
import com.alilitech.mybatis.jpa.DatabaseType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Pagination Dialect Registry
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class PaginationDialectRegistry {

    private final Map<DatabaseType, PaginationDialect> paginationDialectMap = new ConcurrentHashMap<>();

    private static final PaginationDialectRegistry paginationDialectRegistry = new PaginationDialectRegistry();

    private PaginationDialectRegistry() {
        this.register(DatabaseType.MYSQL, new MySqlPaginationDialect());
        this.register(DatabaseType.ORACLE, new OraclePaginationDialect());
        this.register(DatabaseType.SQL_SERVER, new SQLServerPaginationDialect());
        this.register(DatabaseType.MS_SQL_SERVER, new SQLServerPaginationDialect());
        this.register(DatabaseType.DB2, new DB2PaginationDialect());
        this.register(DatabaseType.H2, new H2PaginationDialect());
        this.register(DatabaseType.HSQL, new HSQLPaginationDialect());
        this.register(DatabaseType.POSTGRE, new PostgrePaginationDialect());
        this.register(DatabaseType.SQLITE, new SQLitePaginationDialect());
    }

    public static final PaginationDialectRegistry getInstance() {
        return paginationDialectRegistry;
    }

    public void register(DatabaseType databaseType, PaginationDialect paginationDialect) {
        synchronized (paginationDialectMap) {
            paginationDialectMap.put(databaseType, paginationDialect);
        }
    }

    public PaginationDialect get(DatabaseType databaseType) {
        return paginationDialectMap.get(databaseType);
    }

}
