package com.alilitech.mybatis.jpa.primary.key.snowflake.generator;


import com.alilitech.mybatis.jpa.primary.key.snowflake.SnowflakeContext;

/**
 * 启用备用workerId
 * @author Wang Chengyang
 * @author Zhou Xiaoxiang
 * @since 1.3.1
 */
public class SnowflakeGeneratorExtra extends AbstractSnowflakeGenerator {

    @Override
    public synchronized long generate(SnowflakeContext context) {
        // 下一个ID生成算法
        long currentTimestamp = currentTimestamp();
        // 获取当前时间戳如果小于上次时间戳, 则表示时间出现回滚，启用备用工作id
        if (currentTimestamp < context.getLastTimestamp()) {

            // 将上次时间戳值刷新
            return ((currentTimestamp - context.getOffset()) << context.getTimestampLeftShift()) |
                    (context.getGroupId() << context.getGroupIdShift()) |
                    (context.getExtraWorkerId() << context.getWorkerIdShift()) |
                    context.getSequence();
        }
        return calculate(context, currentTimestamp);
    }
}
