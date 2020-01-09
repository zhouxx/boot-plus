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
package com.alilitech.integration.spring;

import com.alilitech.integration.dialect.pagination.PaginationDialect;
import com.alilitech.integration.dialect.primarykey.KeySqlGenerator;
import com.alilitech.integration.jpa.DatabaseType;

/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class DatabaseRegistration {

    private DatabaseType databaseType;

    private KeySqlGenerator keySqlGenerator;

    private PaginationDialect paginationDialect;

    public DatabaseRegistration(DatabaseType databaseType) {
        this.databaseType = databaseType;
    }


    public DatabaseRegistration keySqlGenerator(KeySqlGenerator keySqlGenerator) {
        this.keySqlGenerator = keySqlGenerator;
        return this;
    }

    public DatabaseRegistration paginationDialect(PaginationDialect paginationDialect) {
        this.paginationDialect = paginationDialect;
        return this;
    }


    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public KeySqlGenerator getKeySqlGenerator() {
        return keySqlGenerator;
    }

    public PaginationDialect getPaginationDialect() {
        return paginationDialect;
    }
}
