package com.campus.marketplace.common.component;

import com.campus.marketplace.common.entity.Order;
import com.campus.marketplace.common.entity.RefundRequest;
import com.campus.marketplace.common.enums.PaymentMethod;
import com.campus.marketplace.common.enums.RefundStatus;
import com.campus.marketplace.repository.OrderRepository;
import com.campus.marketplace.repository.RefundRequestRepository;
import com.campus.marketplace.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 退款失败重试调度器
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundRetryScheduler {

    private final RefundRequestRepository refundRepository;
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final RedissonClient redissonClient;

    @Value("${refund.retry.max:5}")
    private int maxRetry;

    @Value("${refund.retry.backoff.minutes:10}")
    private int backoffMinutes;

    private static final String LOCK_KEY = "lock:refund:retry";

    @Scheduled(fixedDelayString = "${refund.retry.scan.interval.ms:600000}")
    public void retryFailedRefunds() {
        RLock lock = redissonClient.getLock(LOCK_KEY);
        try {
            if (!lock.tryLock(2, 30, TimeUnit.SECONDS)) return;

            LocalDateTime before = LocalDateTime.now().minusMinutes(backoffMinutes);
            List<RefundRequest> candidates = refundRepository.findRetryCandidates(maxRetry, before);
            for (RefundRequest r : candidates) {
                try {
                    Order order = orderRepository.findByOrderNo(r.getOrderNo()).orElse(null);
                    if (order == null) continue;
                    boolean ok = paymentService.refund(order, r.getAmount(), PaymentMethod.valueOf(order.getPaymentMethod()));
                    if (ok) {
                        r.setStatus(RefundStatus.REFUNDED);
                        r.setLastError(null);
                    } else {
                        r.setStatus(RefundStatus.FAILED);
                        r.setRetryCount(r.getRetryCount() == null ? 1 : r.getRetryCount() + 1);
                        r.setLastError("channel refund failed");
                    }
                    refundRepository.save(r);
                } catch (Exception ex) {
                    log.error("退款重试失败: refundNo={}", r.getRefundNo(), ex);
                    r.setStatus(RefundStatus.FAILED);
                    r.setRetryCount(r.getRetryCount() == null ? 1 : r.getRetryCount() + 1);
                    r.setLastError(ex.getMessage());
                    refundRepository.save(r);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("执行退款重试任务失败", e);
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }
}
