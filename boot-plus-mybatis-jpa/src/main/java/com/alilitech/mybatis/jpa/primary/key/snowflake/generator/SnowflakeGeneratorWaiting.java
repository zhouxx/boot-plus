package com.alilitech.mybatis.jpa.primary.key.snowflake.generator;


import com.alilitech.mybatis.jpa.primary.key.snowflake.SnowFlakeKeyGenerateException;
import com.alilitech.mybatis.jpa.primary.key.snowflake.SnowflakeContext;

import java.util.concurrent.TimeUnit;

/**
 * 强制等待回拨时间
 *
 * @author Wang Chengyang
 * @author Zhou Xiaoxiang
 * @since 1.3.1
 */
public class SnowflakeGeneratorWaiting extends AbstractSnowflakeGenerator {

    @Override
    public synchronized long generate(SnowflakeContext context) {
        // 下一个ID生成算法
        long currentTimestamp = currentTimestamp();
        long lastTimestamp = context.getLastTimestamp();
        // 获取当前时间戳如果小于上次时间戳，则表示时间戳获取出现异常
        if(currentTimestamp < lastTimestamp) {
            if ((lastTimestamp - currentTimestamp) < context.getMaxBackTime()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(lastTimestamp - currentTimestamp);
                } catch (InterruptedException e) {
                    throw new SnowFlakeKeyGenerateException("Waiting time to over occur exception!");
                }
            } else {
                throw new SnowFlakeKeyGenerateException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp - currentTimestamp));
            }
            log.warn("Clock is moving backwards. Back time is " + (lastTimestamp - currentTimestamp) + " ms.");
        }

        return calculate(context, currentTimestamp);
    }

}
