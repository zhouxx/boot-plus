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
package com.alilitech.mybatis.jpa.primary.key.snowflake;

import com.alilitech.mybatis.jpa.primary.key.snowflake.generator.SnowflakeGenerator;
import com.alilitech.mybatis.jpa.primary.key.snowflake.generator.SnowflakeGeneratorExtra;
import com.alilitech.mybatis.jpa.primary.key.snowflake.generator.SnowflakeGeneratorOffsetModify;
import com.alilitech.mybatis.jpa.primary.key.snowflake.generator.SnowflakeGeneratorWaiting;

/**
 * @author Zhou Xiaoxiang
 * @since 1.3.1
 */
public enum TimeCallbackStrategy {

    /**
     * 等回拨时间过了
     */
    WAITING(SnowflakeGeneratorWaiting.class),

    /**
     * 备用机
     */
    EXTRA(SnowflakeGeneratorExtra.class),

    /**
     * 偏移量修改
     * 偏移量修改后，ID生成器器的服务重启需要间隔至少回拨时间，或修改偏移量和自动修改后一致
     */
    OFFSET_MODIFY(SnowflakeGeneratorOffsetModify.class);

    private Class<? extends SnowflakeGenerator> clazz;

    TimeCallbackStrategy(Class<? extends SnowflakeGenerator> clazz) {
        this.clazz = clazz;
    }

    public Class<? extends SnowflakeGenerator> getClazz() {
        return clazz;
    }
}
