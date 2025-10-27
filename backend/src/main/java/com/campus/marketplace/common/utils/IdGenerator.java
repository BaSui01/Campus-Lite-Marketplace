package com.campus.marketplace.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 分布式 ID 生成器（雪花算法）
 * 
 * 生成唯一的订单号、消息 ID 等
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Slf4j
@Component
public class IdGenerator {

    // 起始时间戳（2025-01-01 00:00:00）
    private static final long START_TIMESTAMP = 1735660800000L;

    // 机器 ID 所占位数
    private static final long WORKER_ID_BITS = 5L;
    // 数据中心 ID 所占位数
    private static final long DATACENTER_ID_BITS = 5L;
    // 序列号所占位数
    private static final long SEQUENCE_BITS = 12L;

    // 机器 ID 最大值
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    // 数据中心 ID 最大值
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    // 序列号最大值
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    // 机器 ID 左移位数
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    // 数据中心 ID 左移位数
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    // 时间戳左移位数
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;

    private final long workerId;
    private final long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    /**
     * 构造函数（默认机器 ID 和数据中心 ID 为 1）
     */
    public IdGenerator() {
        this(1L, 1L);
    }

    /**
     * 构造函数
     * 
     * @param workerId 机器 ID
     * @param datacenterId 数据中心 ID
     */
    public IdGenerator(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(
                    String.format("Worker ID 不能大于 %d 或小于 0", MAX_WORKER_ID));
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(
                    String.format("Datacenter ID 不能大于 %d 或小于 0", MAX_DATACENTER_ID));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
        log.info("ID 生成器初始化完成: workerId={}, datacenterId={}", workerId, datacenterId);
    }

    /**
     * 生成下一个 ID（线程安全）
     */
    public synchronized long nextId() {
        long timestamp = getCurrentTimestamp();

        // 时钟回拨检测
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("时钟回拨，拒绝生成 ID。上次时间戳: %d, 当前时间戳: %d",
                            lastTimestamp, timestamp));
        }

        // 同一毫秒内，序列号递增
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 序列号溢出，等待下一毫秒
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒，序列号重置
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // 组装 ID
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence;
    }

    /**
     * 生成订单号
     * 格式：ORD + 时间戳 + 6位随机数
     */
    public String generateOrderNo() {
        long id = nextId();
        return "ORD" + id;
    }

    /**
     * 获取当前时间戳
     */
    private long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 等待下一毫秒
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = getCurrentTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentTimestamp();
        }
        return timestamp;
    }
}
