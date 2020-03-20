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
package com.alilitech.mybatis.jpa;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class DatabaseTypeRegistry {

    private final Map<String, DatabaseType> databaseTypeMap = new ConcurrentHashMap<>();

    private static final DatabaseTypeRegistry databaseTypeRegistry = new DatabaseTypeRegistry();

    private DatabaseTypeRegistry() {
        register(DatabaseType.MYSQL);
        register(DatabaseType.ORACLE);
        register(DatabaseType.DB2);
        register(DatabaseType.DERBY);
        register(DatabaseType.H2);
        register(DatabaseType.HSQL);
        register(DatabaseType.INFORMIX);
        register(DatabaseType.SQL_SERVER);
        register(DatabaseType.MS_SQL_SERVER);
        register(DatabaseType.POSTGRE);
        register(DatabaseType.SYBASE);
    }

    public static DatabaseTypeRegistry getInstance() {
        return databaseTypeRegistry;
    }

    public void register(DatabaseType databaseType) {
         databaseTypeMap.put(databaseType.getDatabaseId(), databaseType);
    }

    public DatabaseType get(String databaseId) {
        return databaseTypeMap.get(databaseId);
    }

}
