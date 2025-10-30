package com.campus.marketplace.common.component;

import com.campus.marketplace.common.lock.DistributedLockManager;
import com.campus.marketplace.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Order Timeout Scheduler
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class OrderTimeoutScheduler {

    private final OrderService orderService;
    private final DistributedLockManager lockManager;

    private static final String LOCK_KEY = "lock:order:cancel-timeout";

    @Scheduled(fixedDelayString = "${order.timeout.cancel.interval:300000}") // 默认5分钟
    public void cancelTimeoutOrdersJob() {
        try (DistributedLockManager.LockHandle lock = lockManager.tryLock(LOCK_KEY, 1, 30, TimeUnit.SECONDS)) {
            if (!lock.acquired()) {
                log.debug("跳过本轮超时订单取消任务，锁被占用");
                return;
            }
            int count = orderService.cancelTimeoutOrders();
            log.info("定时取消超时订单完成: count={}", count);
        } catch (Exception e) {
            log.error("执行超时订单取消任务失败", e);
        }
    }
}
