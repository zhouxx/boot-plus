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
package com.alili.integration.jpa;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class DatabaseType {

    private final String databaseId;

    private final String desc;

    private DatabaseType(String databaseId) {
        this(databaseId, null);
    }

    private DatabaseType(String databaseId, String desc) {
        this.databaseId = databaseId;
        this.desc = desc;
    }

    public final static DatabaseType valueOf(String databaseId) {
        return new DatabaseType(databaseId);
    }

    public final static DatabaseType valueOf(String databaseId, String desc) {
        return new DatabaseType(databaseId, desc);
    }

    public static final DatabaseType MYSQL;
    public static final DatabaseType ORACLE;
    public static final DatabaseType DB2;
    public static final DatabaseType DERBY;
    public static final DatabaseType H2;
    public static final DatabaseType HSQL;
    public static final DatabaseType INFORMIX;
    public static final DatabaseType SQL_SERVER;
    public static final DatabaseType MS_SQL_SERVER;
    public static final DatabaseType POSTGRE;
    public static final DatabaseType SYBASE;

    static {
        MYSQL = valueOf("MySQL", "MySql database");
        ORACLE = valueOf("Oracle", "Oracle database");
        DB2 = valueOf("DB2", "DB2 database");
        DERBY = valueOf("Derby", "Derby database");
        H2 = valueOf("H2", "H2 database");
        HSQL = valueOf("HSQL", "HSql database");
        INFORMIX = valueOf("Informix", "Informix database");
        SQL_SERVER = valueOf("MS-SQL", "SqlServer database");
        MS_SQL_SERVER = valueOf("Microsoft SQL Server", "SqlServer database");
        POSTGRE = valueOf("PostgreSQL", "MySql database");
        SYBASE = valueOf("Sybase", "Sybase database");
    }

    public String getDatabaseId() {
        return databaseId;
    }

    public String getDesc() {
        return desc;
    }
}
