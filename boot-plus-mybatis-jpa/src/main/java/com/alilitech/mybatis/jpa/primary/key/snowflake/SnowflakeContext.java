package com.alilitech.mybatis.jpa.primary.key.snowflake;


/**
 * @author Wang Chengyang
 * @author Zhou Xiaoxiang
 * @since 1.3.1
 */
public class SnowflakeContext {

    // 下面两个每个5位, 加起来就是10位的工作机器id
    private long groupId;   // 组id
    private long workerId;    // 工作id
    private long extraWorkerId; // 备用工作id

    // 12位的序列号
    private long sequence = 0L;

    public SnowflakeContext(long groupId, long workerId) {
        // check for workerId
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (groupId > maxGroupId || groupId < 0) {
            throw new IllegalArgumentException(String.format("groupId Id can't be greater than %d or less than 0", maxGroupId));
        }
        this.workerId = workerId;
        this.groupId = groupId;
    }

    public SnowflakeContext(long groupId, long workerId, long extraWorkerId) {
        this(workerId, groupId);
        if (extraWorkerId > maxWorkerId || extraWorkerId < 0) {
            throw new IllegalArgumentException(String.format("Extra worker Id can't be greater than %d or less than 0", maxWorkerId));
        }
        if (extraWorkerId == workerId) {
            throw new IllegalArgumentException("Extra worker Id can't be equals to workerId");
        }
        this.extraWorkerId = extraWorkerId;
    }

    // 时间偏移量
    private long offset = 1288834974657L;

    // 长度为5位
    private long groupIdBits = 5L;
    private long workerIdBits = 5L;

    // 最大值
    private long maxGroupId = -1L ^ (-1L << groupIdBits);
    private long maxWorkerId = -1L ^ (-1L << workerIdBits);

    // 序列号id长度
    private long sequenceBits = 12L;
    // 序列号最大值
    private long sequenceMask = -1L ^ (-1L << sequenceBits);

    // 工作id需要左移的位数, 12位
    private long workerIdShift = sequenceBits;
    // 数据id需要左移位数 12+5=17位
    private long groupIdShift = sequenceBits + workerIdBits;
    // 时间戳需要左移位数 12+5+5=22位
    private long timestampLeftShift = sequenceBits + workerIdBits + groupIdBits;

    // 上次时间戳, 初始值为负数
    private long lastTimestamp = -1L;

    // 发生时间回拨时容忍的最大回拨时间 (毫秒)
    private long maxBackTime = 1L*1000;

    public long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(long workerId) {
        this.workerId = workerId;
    }

    public long getExtraWorkerId() {
        return extraWorkerId;
    }

    public void setExtraWorkerId(long extraWorkerId) {
        this.extraWorkerId = extraWorkerId;
    }

    public long getGroupId() {
        return groupId;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getOffset() {
        return offset;
    }

    public long getSequenceMask() {
        return sequenceMask;
    }

    public long getWorkerIdShift() {
        return workerIdShift;
    }

    public long getGroupIdShift() {
        return groupIdShift;
    }

    public long getTimestampLeftShift() {
        return timestampLeftShift;
    }

    public long getLastTimestamp() {
        return lastTimestamp;
    }

    public void setLastTimestamp(long lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

    public long getMaxBackTime() {
        return maxBackTime;
    }

    public void setMaxBackTime(long maxBackTime) {
        this.maxBackTime = maxBackTime;
    }
}
