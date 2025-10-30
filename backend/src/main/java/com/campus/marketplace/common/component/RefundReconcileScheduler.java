package com.campus.marketplace.common.component;

import com.campus.marketplace.common.entity.RefundRequest;
import com.campus.marketplace.common.lock.DistributedLockManager;
import com.campus.marketplace.repository.RefundRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 对账任务：按日生成差异报告（简化版：标记长时间处理中记录）
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundReconcileScheduler {

    private final RefundRequestRepository refundRepository;
    private final DistributedLockManager lockManager;

    @Value("${refund.reconcile.stuck.hours:24}")
    private int stuckHours;

    private static final String LOCK_KEY = "lock:refund:reconcile";

    // 每日 01:10 执行
    @Scheduled(cron = "${refund.reconcile.cron:0 10 1 * * *}")
    public void generateRefundReconcileReport() {
        try (DistributedLockManager.LockHandle lock = lockManager.tryLock(LOCK_KEY, 2, 60, TimeUnit.SECONDS)) {
            if (!lock.acquired()) {
                return;
            }
            LocalDate today = LocalDate.now();
            LocalDateTime stuckBefore = LocalDateTime.now().minusHours(stuckHours);
            List<RefundRequest> stuck = refundRepository.findStuckProcessing(stuckBefore);

            log.info("[REFUND-RECONCILE] date={}, stuckProcessingCount={}", today, stuck.size());
            for (RefundRequest r : stuck) {
                log.warn("[REFUND-RECONCILE] STUCK refundNo={}, orderNo={}, updatedAt={}, retryCount={}, lastError={}",
                        r.getRefundNo(), r.getOrderNo(), r.getUpdatedAt(), r.getRetryCount(), r.getLastError());
            }
        } catch (Exception e) {
            log.error("退款对账任务失败", e);
        }
    }
}
