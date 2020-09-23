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

import com.alilitech.mybatis.dialect.primarykey.KeySqlGenerator;
import com.alilitech.mybatis.dialect.primarykey.support.DB2KeySqlGenerator;
import com.alilitech.mybatis.dialect.primarykey.support.H2KeySqlGenerator;
import com.alilitech.mybatis.dialect.primarykey.support.OracleKeySqlGenerator;
import com.alilitech.mybatis.dialect.primarykey.support.PostgreKeySqlGenerator;
import com.alilitech.mybatis.jpa.DatabaseType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class KeySqlDialectRegistry {

    private final Map<DatabaseType, KeySqlGenerator> paginationDialectMap = new ConcurrentHashMap<>();

    private static final KeySqlDialectRegistry keySqlDialectRegistry = new KeySqlDialectRegistry();

    private KeySqlDialectRegistry() {
        this.register(DatabaseType.ORACLE, new OracleKeySqlGenerator());
        this.register(DatabaseType.DB2, new DB2KeySqlGenerator());
        this.register(DatabaseType.H2, new H2KeySqlGenerator());
        this.register(DatabaseType.POSTGRE, new PostgreKeySqlGenerator());
    }

    public static KeySqlDialectRegistry getInstance() {
        return keySqlDialectRegistry;
    }

    public void register(DatabaseType databaseType, KeySqlGenerator keySqlGenerator) {
        synchronized (paginationDialectMap) {
            paginationDialectMap.put(databaseType, keySqlGenerator);
        }
    }

    public KeySqlGenerator get(DatabaseType databaseType) {
        return paginationDialectMap.get(databaseType);
    }

}
