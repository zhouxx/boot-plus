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
package com.alilitech.integration.dialect.pagination;


/**
 * Pagination sql builder
 * According to original sql, build pageable sql.
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public interface PaginationDialect {

    /**
     * build pagination sql
     *
     * @param originalSql
     * @param offset
     * @param limit
     * @return
     */
    String buildPaginationSql(String originalSql, int offset, int limit);
}