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

import com.alilitech.mybatis.jpa.primary.key.snowflake.TimeCallbackStrategy;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Zhou Xiaoxiang
 * @since 1.3.0
 */
@ConfigurationProperties(prefix = "mybatis")
public class MybatisJpaProperties {

    private MapperScan mapperScan = new MapperScan();

    private Page page = new Page();

    private Snowflake snowflake = new Snowflake();

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

    public Snowflake getSnowflake() {
        return snowflake;
    }

    public void setSnowflake(Snowflake snowflake) {
        this.snowflake = snowflake;
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

    /**
     * snowflake id properties
     *
     * @since 1.3.1
     */
    public static class Snowflake {

        /**
         * like roomId, or stationId
         */
        private long groupId = 1L;

        /**
         * worker id
         */
        private long workerId = 1L;

        /**
         * 时间偏移量，默认2010-01-01的时间戳
         */
        private long offset = 1262275200000L;

        /**
         * time call back strategy
         */
        private TimeCallbackStrategy timeCallbackStrategy = TimeCallbackStrategy.WAITING;

        /**
         * 当TimeCallbackStrategy是WAITING时，这个时候有允许最大回拨时间
         * 默认是1秒，超过则报错，会造成无法插入
         */
        private long maxBackTime = 1L;

        /**
         * 当TimeCallbackStrategy是EXTRA时，备用workerId
         * 只有时钟回拨的时候才会启用
         */
        private String extraWorkerId;

        public long getGroupId() {
            return groupId;
        }

        public void setGroupId(long groupId) {
            this.groupId = groupId;
        }

        public long getWorkerId() {
            return workerId;
        }

        public void setWorkerId(long workerId) {
            this.workerId = workerId;
        }

        public TimeCallbackStrategy getTimeCallbackStrategy() {
            return timeCallbackStrategy;
        }

        public void setTimeCallbackStrategy(TimeCallbackStrategy timeCallbackStrategy) {
            this.timeCallbackStrategy = timeCallbackStrategy;
        }

        public long getMaxBackTime() {
            return maxBackTime;
        }

        public void setMaxBackTime(long maxBackTime) {
            this.maxBackTime = maxBackTime;
        }

        public String getExtraWorkerId() {
            return extraWorkerId;
        }

        public void setExtraWorkerId(String extraWorkerId) {
            this.extraWorkerId = extraWorkerId;
        }

        public long getOffset() {
            return offset;
        }

        public void setOffset(long offset) {
            this.offset = offset;
        }
    }
}
