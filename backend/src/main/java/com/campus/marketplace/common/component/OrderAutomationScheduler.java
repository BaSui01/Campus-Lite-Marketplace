package com.campus.marketplace.common.component;

import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.enums.NotificationType;
import com.campus.marketplace.common.enums.OrderStatus;
import com.campus.marketplace.common.lock.DistributedLockManager;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 订单自动化调度器
 * 
 * 负责自动确认收货、异常订单检测等定时任务
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class OrderAutomationScheduler {

    private final OrderRepository orderRepository;
    private final NotificationService notificationService;
    private final DistributedLockManager lockManager;

    private static final String AUTO_CONFIRM_LOCK_KEY = "lock:order:auto-confirm";
    private static final String ABNORMAL_DETECT_LOCK_KEY = "lock:order:abnormal-detect";

    /**
     * 自动确认收货定时任务
     * 
     * 每天凌晨2点执行，将超过7天未确认的已送达订单自动标记为已完成
     */
    @Scheduled(cron = "${order.automation.auto-confirm.cron:0 0 2 * * ?}")
    public void autoConfirmReceiptJob() {
        try (DistributedLockManager.LockHandle lock = lockManager.tryLock(AUTO_CONFIRM_LOCK_KEY, 1, 30, TimeUnit.SECONDS)) {
            if (!lock.acquired()) {
                log.debug("跳过本轮自动确认收货任务，锁被占用");
                return;
            }

            log.info("开始执行自动确认收货任务");

            // 查询7天前更新的已送达订单
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            List<Order> deliveredOrders = orderRepository.findByStatusAndUpdatedAtBefore(
                OrderStatus.DELIVERED,
                sevenDaysAgo
            );

            if (deliveredOrders.isEmpty()) {
                log.info("无需要自动确认的订单");
                return;
            }

            // 批量更新订单状态
            for (Order order : deliveredOrders) {
                order.setStatus(OrderStatus.COMPLETED);
            }
            orderRepository.saveAll(deliveredOrders);

            // 发送通知
            for (Order order : deliveredOrders) {
                try {
                    // 通知买家
                    notificationService.sendNotification(
                        order.getBuyerId(),
                        NotificationType.ORDER_COMPLETED,
                        "订单已自动确认收货",
                        "您的订单 " + order.getOrderNo() + " 已超过7天未确认收货，系统已自动确认完成",
                        order.getId(),
                        "ORDER",
                        "/orders/" + order.getOrderNo()
                    );

                    // 通知卖家
                    notificationService.sendNotification(
                        order.getSellerId(),
                        NotificationType.ORDER_COMPLETED,
                        "订单已自动确认收货",
                        "订单 " + order.getOrderNo() + " 已超过7天未确认，系统已自动完成",
                        order.getId(),
                        "ORDER",
                        "/orders/" + order.getOrderNo()
                    );
                } catch (Exception e) {
                    log.error("发送自动确认收货通知失败: orderNo={}", order.getOrderNo(), e);
                }
            }

            log.info("自动确认收货任务完成，共处理{}个订单", deliveredOrders.size());

        } catch (Exception e) {
            log.error("执行自动确认收货任务失败", e);
        }
    }

    /**
     * 异常订单检测定时任务
     * 
     * 每天凌晨3点执行，检测以下异常订单并发送通知：
     * 1. 已支付但超过3天未发货
     * 2. 已发货但超过7天未送达
     */
    @Scheduled(cron = "${order.automation.abnormal-detect.cron:0 0 3 * * ?}")
    public void detectAbnormalOrdersJob() {
        try (DistributedLockManager.LockHandle lock = lockManager.tryLock(ABNORMAL_DETECT_LOCK_KEY, 1, 30, TimeUnit.SECONDS)) {
            if (!lock.acquired()) {
                log.debug("跳过本轮异常订单检测任务，锁被占用");
                return;
            }

            log.info("开始执行异常订单检测任务");

            // 1. 检测已支付但超过3天未发货的订单
            detectPaidButNotShipped();

            // 2. 检测已发货但超过7天未送达的订单
            detectShippedButNotDelivered();

            log.info("异常订单检测任务完成");

        } catch (Exception e) {
            log.error("执行异常订单检测任务失败", e);
        }
    }

    /**
     * 检测已支付但未发货的订单
     */
    private void detectPaidButNotShipped() {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        List<Order> paidOrders = orderRepository.findByStatusAndUpdatedAtBefore(
            OrderStatus.PAID,
            threeDaysAgo
        );

        if (paidOrders.isEmpty()) {
            log.info("无已支付未发货的异常订单");
            return;
        }

        log.warn("检测到{}个已支付但超过3天未发货的订单", paidOrders.size());

        for (Order order : paidOrders) {
            try {
                // 通知卖家发货
                notificationService.sendNotification(
                    order.getSellerId(),
                    NotificationType.ORDER_PAID,
                    "订单发货超时提醒",
                    "订单 " + order.getOrderNo() + " 已支付超过3天，请尽快发货！",
                    order.getId(),
                    "ORDER",
                    "/orders/" + order.getOrderNo()
                );
            } catch (Exception e) {
                log.error("发送发货超时通知失败: orderNo={}", order.getOrderNo(), e);
            }
        }
    }

    /**
     * 检测已发货但未送达的订单
     */
    private void detectShippedButNotDelivered() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Order> shippedOrders = orderRepository.findByStatusAndUpdatedAtBefore(
            OrderStatus.SHIPPED,
            sevenDaysAgo
        );

        if (shippedOrders.isEmpty()) {
            log.info("无已发货未送达的异常订单");
            return;
        }

        log.warn("检测到{}个已发货但超过7天未送达的订单", shippedOrders.size());

        for (Order order : shippedOrders) {
            try {
                // 通知买家
                notificationService.sendNotification(
                    order.getBuyerId(),
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    "订单物流异常提醒",
                    "订单 " + order.getOrderNo() + " 已发货超过7天未送达，请联系卖家或客服",
                    order.getId(),
                    "ORDER",
                    "/orders/" + order.getOrderNo()
                );

                // 通知卖家
                notificationService.sendNotification(
                    order.getSellerId(),
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    "订单物流异常提醒",
                    "订单 " + order.getOrderNo() + " 已发货超过7天未送达，请关注物流状态",
                    order.getId(),
                    "ORDER",
                    "/orders/" + order.getOrderNo()
                );
            } catch (Exception e) {
                log.error("发送物流异常通知失败: orderNo={}", order.getOrderNo(), e);
            }
        }
    }
}
