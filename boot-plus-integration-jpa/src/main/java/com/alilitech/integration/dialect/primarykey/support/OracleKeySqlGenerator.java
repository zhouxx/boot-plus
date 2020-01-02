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
package com.alilitech.integration.dialect.primarykey.support;


import com.alilitech.integration.dialect.primarykey.KeySqlGenerator;


/**
 * Oracle Sequence
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class OracleKeySqlGenerator implements KeySqlGenerator {

    @Override
    public String generateKeySql(String incrementerName) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(incrementerName);
        sql.append(".NEXTVAL FROM DUAL");
        return sql.toString();
    }

}
