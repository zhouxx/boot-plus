/*
 *    Copyright 2017-2021 the original author or authors.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

import javax.sql.DataSource;


/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.0
 */
public class DefaultDynamicDataSource extends DynamicRoutingDataSource {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    protected Object determineCurrentLookupKey() {
        String dataSourceKey = DataSourceContextHolder.getDataSource() == null ? DEFAULT_DATASOURCE_KEY : DataSourceContextHolder.getDataSource();
        log.trace("Current DataSource is [{}]", dataSourceKey);
        return dataSourceKey;
    }

    public void saveDataSource(String lookupKey, String driverClassName, String url,
                                 String username, String password) {

        DataSourceProperties dataSourceProperties = new DataSourceProperties();
        dataSourceProperties.setDriverClassName(driverClassName);
        dataSourceProperties.setUsername(username);
        dataSourceProperties.setPassword(password);
        dataSourceProperties.setUrl(url);

        DataSource dataSource = dataSourceProperties.initializeDataSourceBuilder().build();

        resolvedDataSources.put(lookupKey, dataSource);
    }

    public void saveDataSource(String lookupKey, String url, String username, String password) {
        this.saveDataSource(lookupKey, null, url, username, password);
    }

    public void removeDataSource(String lookupKey) {
        resolvedDataSources.remove(lookupKey);
    }
}
