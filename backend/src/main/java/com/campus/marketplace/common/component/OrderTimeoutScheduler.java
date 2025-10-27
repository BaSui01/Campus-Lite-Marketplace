package com.campus.marketplace.common.component;

import com.campus.marketplace.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class OrderTimeoutScheduler {

    private final OrderService orderService;
    private final RedissonClient redissonClient;

    private static final String LOCK_KEY = "lock:order:cancel-timeout";

    @Scheduled(fixedDelayString = "${order.timeout.cancel.interval:300000}") // 默认5分钟
    public void cancelTimeoutOrdersJob() {
        RLock lock = redissonClient.getLock(LOCK_KEY);
        try {
            if (lock.tryLock(1, 30, TimeUnit.SECONDS)) {
                int count = orderService.cancelTimeoutOrders();
                log.info("定时取消超时订单完成: count={}", count);
            } else {
                log.debug("跳过本轮超时订单取消任务，锁被占用");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("执行超时订单取消任务失败", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
