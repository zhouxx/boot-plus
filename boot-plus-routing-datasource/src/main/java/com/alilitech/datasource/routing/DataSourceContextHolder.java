/*
 *    Copyright 2017-2022 the original author or authors.
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
package com.alilitech.datasource.routing;


/**
 * Datasource holder, datasource name just in thread
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class DataSourceContextHolder {

    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

    private DataSourceContextHolder() {
    }

    /**
     * set data source name
     */
    public static void setDataSource(String dataSourceName) {
        contextHolder.set(dataSourceName);
    }

    /**
     * get data source name
     */
    public static String getDataSource() {
        return contextHolder.get();
    }

    /**
     * clear data source name
     */
    public static void clearDataSource() {
        contextHolder.remove();
    }

}
