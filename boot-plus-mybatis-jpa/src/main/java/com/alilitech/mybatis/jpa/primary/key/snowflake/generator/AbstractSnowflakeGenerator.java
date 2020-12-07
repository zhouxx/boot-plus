package com.alilitech.mybatis.jpa.primary.key.snowflake.generator;


import com.alilitech.mybatis.jpa.primary.key.snowflake.SnowflakeContext;

/**
 * @author Wang Chengyang
 * @author Zhou Xiaoxiang
 * @since 1.3.1
 */
public abstract class AbstractSnowflakeGenerator implements SnowflakeGenerator {

    // 获取系统时间戳
    protected long currentTimestamp() {
        return System.currentTimeMillis();
    }

    // 获取时间戳, 并与上次时间戳比较
    protected long tilNextMillis(long lastTimestamp) {
        long currentTimestamp = currentTimestamp();
        while (currentTimestamp <= lastTimestamp) {
            currentTimestamp = currentTimestamp();
        }
        return currentTimestamp;
    }

    protected long calculate(SnowflakeContext context, long currentTimestamp) {
        // 获取当前时间戳如果等于上次时间戳（同一毫秒内），则在序列号加一；否则序列号赋值为0，从0开始。
        if (context.getLastTimestamp() == currentTimestamp) {
            context.setSequence((context.getSequence() + 1) & context.getSequenceMask());
            if (context.getSequence() == 0) {
                currentTimestamp = tilNextMillis(context.getLastTimestamp());
            }
        } else {
            context.setSequence(currentTimestamp & 1);   // 新的一毫秒 0或1，不然奇偶不均匀
        }

        // 将上次时间戳值刷新
        context.setLastTimestamp(currentTimestamp);

        /**
         * 返回结果：
         * (currentTimestamp - twepoch) << timestampLeftShift) 表示将时间戳减去初始时间戳，再左移相应位数
         * (datacenterId << datacenterIdShift) 表示将数据id左移相应位数
         * (workerId << workerIdShift) 表示将工作id左移相应位数
         * | 是按位或运算符，例如：x | y，只有当x，y都为0的时候结果才为0，其它情况结果都为1。
         * 因为个部分只有相应位上的值有意义，其它位上都是0，所以将各部分的值进行 | 运算就能得到最终拼接好的id
         */
        return ((currentTimestamp - context.getOffset()) << context.getTimestampLeftShift()) |
                (context.getGroupId() << context.getGroupIdShift()) |
                (context.getWorkerId() << context.getWorkerIdShift()) |
                context.getSequence();
    }
}
