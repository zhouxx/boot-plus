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
package com.alilitech.mybatis;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 * @author Zhou Xiaoxiang
 * @since 1.3.0
 */
@ConfigurationProperties(prefix = "mybatis")
public class MybatisJpaProperties {

    private MapperScan mapperScan = new MapperScan();

    private Page page = new Page();

    public MapperScan getMapperScan() {
        return mapperScan;
    }

    public void setMapperScan(MapperScan mapperScan) {
        this.mapperScan = mapperScan;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public static class MapperScan {

        private String[] basePackages;

        public String[] getBasePackages() {
            return basePackages;
        }

        public void setBasePackages(String[] basePackages) {
            this.basePackages = basePackages;
        }
    }

    public static class Page {

        private boolean autoDialect = false;

        public boolean isAutoDialect() {
            return autoDialect;
        }

        public void setAutoDialect(boolean autoDialect) {
            this.autoDialect = autoDialect;
        }
    }

}
