package com.campus.marketplace.common.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 订单相关可配置属性
 *
 * 使用示例：
 * - order.timeout.minutes=30           // 待支付超时分钟数
 * - order.timeout.reminder.lead-minutes=5  // 超时前提醒的提前分钟数
 * - order.timeout.reminder.interval=300000 // 提醒任务轮询间隔（毫秒）
 *
 * @author BaSui
 * @date 2025-11-11
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "order.timeout")
public class OrderProperties {

    /**
     * 待支付超时阈值（分钟）
     */
    private int minutes = 30;

    /**
     * 提前提醒分钟数（如5分钟）
     */
    private int reminderLeadMinutes = 5;

    /**
     * 提醒任务轮询间隔（毫秒）
     */
    private long reminderInterval = 600000; // 默认10分钟
}

