package com.alilitech.mybatis.jpa.primary.key.snowflake.generator;


import com.alilitech.mybatis.jpa.primary.key.snowflake.SnowflakeContext;

import java.util.concurrent.TimeUnit;

/**
 * 强制等待回拨时间
 * @author Wang Chengyang
 * @author Zhou Xiaoxiang
 * @since 1.3.1
 */
public class SnowflakeGeneratorWaiting extends AbstractSnowflakeGenerator {

    // 发生时间回拨时容忍的最大回拨时间 (秒)
    private static final long BACK_TIME_MAX = 1L;

    @Override
    public synchronized long generate(SnowflakeContext context) {
        // 下一个ID生成算法
        long currentTimestamp = currentTimestamp();
        // 获取当前时间戳如果小于上次时间戳，则表示时间戳获取出现异常
        if (currentTimestamp < context.getLastTimestamp()) {
            System.err.printf("clock is moving backwards.  Rejecting requests until %d.", context.getLastTimestamp());
            // 占用下一秒
            if ((currentTimestamp - context.getLastTimestamp()) / 1000 < BACK_TIME_MAX) { // 发生时间回拨在1秒内, 最大容忍回拨时间
                try {
                    TimeUnit.MILLISECONDS.sleep(context.getLastTimestamp() - currentTimestamp);
                } catch (InterruptedException e) {
                    // do nothing
                }
            } else {
                throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", context.getLastTimestamp() - currentTimestamp));
            }
            currentTimestamp = currentTimestamp();
        }
        return calculate(context, currentTimestamp);
    }

}
