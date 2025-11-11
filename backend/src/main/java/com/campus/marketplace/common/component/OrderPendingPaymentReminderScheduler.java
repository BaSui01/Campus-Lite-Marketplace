package com.campus.marketplace.common.component;

import com.campus.marketplace.common.config.properties.OrderProperties;
import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.enums.NotificationType;
import com.campus.marketplace.common.enums.OrderStatus;
import com.campus.marketplace.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 待支付订单提醒调度器
 *
 * 在订单超时前（如5分钟）给买家/卖家发送一次提醒通知。
 * 使用 Redis 防重复键，避免重复提醒。
 *
 * @author BaSui
 * @date 2025-11-11
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class OrderPendingPaymentReminderScheduler {

    private final OrderRepository orderRepository;
    private final NotificationDispatcher notificationDispatcher;
    private final RedisTemplate<String, Object> redisTemplate;
    private final OrderProperties orderProperties;

    private static final String REMIND_KEY_PREFIX = "order:pending:reminded:";

    @Scheduled(fixedDelayString = "${order.timeout.reminder.interval:600000}") // 默认10分钟轮询
    public void remindPendingPayment() {
        try {
            int timeoutMinutes = Math.max(1, orderProperties.getMinutes());
            int leadMinutes = Math.max(1, Math.min(timeoutMinutes - 1, orderProperties.getReminderLeadMinutes()));

            // 时间窗口： [now - timeoutMinutes, now - (timeoutMinutes - leadMinutes))
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime createdAfter = now.minusMinutes(timeoutMinutes);
            LocalDateTime createdBefore = now.minusMinutes(timeoutMinutes - leadMinutes);

            List<Order> candidates = orderRepository.findPendingPaymentBetweenCreatedAt(
                    OrderStatus.PENDING_PAYMENT, createdBefore, createdAfter
            );
            if (candidates.isEmpty()) {
                log.debug("本轮无需要提醒的待支付订单");
                return;
            }

            for (Order order : candidates) {
                String key = REMIND_KEY_PREFIX + order.getOrderNo();
                Boolean firstSet = redisTemplate.opsForValue().setIfAbsent(key, 1);
                if (Boolean.FALSE.equals(firstSet)) {
                    continue; // 已提醒过
                }
                // 设置 TTL 至订单超时点，避免遗留脏键
                long ttlMillis = Math.max(60_000L, TimeUnit.MINUTES.toMillis(timeoutMinutes - leadMinutes));
                redisTemplate.expire(key, ttlMillis, TimeUnit.MILLISECONDS);

                Map<String, Object> params = new HashMap<>();
                params.put("orderNo", order.getOrderNo());
                params.put("minutesLeft", leadMinutes);

                // 买家提醒
                notificationDispatcher.enqueueTemplate(
                        order.getBuyerId(),
                        "ORDER_PENDING_REMINDER_BUYER",
                        params,
                        NotificationType.ORDER_PENDING.name(),
                        order.getId(),
                        "ORDER",
                        "/orders/" + order.getOrderNo()
                );
                // 卖家提醒
                notificationDispatcher.enqueueTemplate(
                        order.getSellerId(),
                        "ORDER_PENDING_REMINDER_SELLER",
                        params,
                        NotificationType.ORDER_PENDING.name(),
                        order.getId(),
                        "ORDER",
                        "/orders/" + order.getOrderNo()
                );
            }

            log.info("待支付提醒完成，本轮处理 {} 笔订单", candidates.size());
        } catch (Exception e) {
            log.error("执行待支付提醒任务失败", e);
        }
    }
}

