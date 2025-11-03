package com.campus.marketplace.common.component;

import com.campus.marketplace.common.lock.DistributedLockManager;
import com.campus.marketplace.service.DisputeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 纠纷超时定时任务
 *
 * 定期检查并处理：
 * 1. 协商超时：协商期（3天）到期后自动升级为待仲裁
 * 2. 仲裁超时：仲裁期（3天）到期后自动关闭纠纷
 *
 * 使用分布式锁防止多实例重复执行
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DisputeTimeoutScheduler {

    private final DisputeService disputeService;
    private final DistributedLockManager lockManager;

    // 分布式锁键名
    private static final String LOCK_KEY_NEGOTIATION = "lock:dispute:check-expired-negotiations";
    private static final String LOCK_KEY_ARBITRATION = "lock:dispute:check-expired-arbitrations";

    /**
     * Check expired negotiations
     * Runs every 6 hours (configurable)
     */
    @Scheduled(cron = "${dispute.timeout.check.negotiation.cron:0 0 */6 * * ?}")
    public void checkExpiredNegotiations() {
        // 使用 try-with-resources 自动释放锁（即使抛异常也能释放！）
        try (DistributedLockManager.LockHandle lock = lockManager.tryLock(
                LOCK_KEY_NEGOTIATION, 1, 30, TimeUnit.SECONDS)) {

            // 🔒 锁获取失败？说明其他实例正在执行，跳过本轮！
            if (!lock.acquired()) {
                log.debug("跳过本轮协商超时检查任务，锁被其他实例占用 🚫");
                return;
            }

            log.info("开始执行协商超时检查任务... ⏰");

            // 调用服务层标记过期协商
            int count = disputeService.markExpiredNegotiations();

            log.info("协商超时检查任务完成：标记了 {} 个过期协商 ✅", count);

        } catch (Exception e) {
            // 💥 异常不能影响下次执行！记录日志继续！
            log.error("执行协商超时检查任务失败，下次继续尝试 💊", e);
        }
    }

    /**
     * Check expired arbitrations
     * Runs every 6 hours (configurable)
     */
    @Scheduled(cron = "${dispute.timeout.check.arbitration.cron:0 0 */6 * * ?}")
    public void checkExpiredArbitrations() {
        // 使用 try-with-resources 自动释放锁
        try (DistributedLockManager.LockHandle lock = lockManager.tryLock(
                LOCK_KEY_ARBITRATION, 1, 30, TimeUnit.SECONDS)) {

            // 🔒 锁获取失败？跳过本轮！
            if (!lock.acquired()) {
                log.debug("跳过本轮仲裁超时检查任务，锁被其他实例占用 🚫");
                return;
            }

            log.info("开始执行仲裁超时检查任务... ⏰");

            // 调用服务层标记过期仲裁
            int count = disputeService.markExpiredArbitrations();

            log.info("仲裁超时检查任务完成：标记了 {} 个过期仲裁 ✅", count);

        } catch (Exception e) {
            // 💥 异常不能影响下次执行！
            log.error("执行仲裁超时检查任务失败，下次继续尝试 💊", e);
        }
    }
}
