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
package com.alilitech.mybatis.jpa.primary.key.snowflake.generator;

import com.alilitech.mybatis.jpa.primary.key.snowflake.SnowflakeContext;

/**
 * 削减偏移量
 * @author Zhou Xiaoxiang
 * @since 1.3.0
 */
public class SnowflakeGeneratorOffsetModify extends AbstractSnowflakeGenerator {
    @Override
    public long generate(SnowflakeContext context) {
        // 下一个ID生成算法
        long currentTimestamp = currentTimestamp();
        // 获取当前时间戳如果小于上次时间戳, 则表示时间出现回滚，减少偏移量，让时间提前
        if (currentTimestamp < context.getLastTimestamp()) {
            context.setOffsetByOffset(context.getLastTimestamp() - currentTimestamp);

            // 将上次时间戳值刷新
            return ((currentTimestamp - context.getOffset()) << context.getTimestampLeftShift()) |
                    (context.getGroupId() << context.getGroupIdShift()) |
                    (context.getExtraWorkerId() << context.getWorkerIdShift()) |
                    context.getSequence();
        }

        return calculate(context, currentTimestamp);
    }
}
