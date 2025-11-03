package com.campus.marketplace.scheduler;

import com.campus.marketplace.service.LogisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 物流定时任务
 * <p>
 * 定时同步物流信息和检查超时物流。
 * 可通过配置 logistics.scheduler.enabled=false 禁用定时任务。
 * </p>
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "logistics.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class LogisticsScheduler {

    private final LogisticsService logisticsService;

    /**
     * 批量同步物流信息
     * <p>
     * 每2小时执行一次，同步待更新的物流信息。
     * </p>
     */
    @Scheduled(cron = "0 0 */2 * * ?") // 每2小时执行一次
    public void batchSyncLogistics() {
        log.info("定时任务：批量同步物流信息");

        try {
            int successCount = logisticsService.batchSyncLogistics();
            log.info("定时任务完成：成功同步 {} 条物流信息", successCount);
        } catch (Exception e) {
            log.error("定时任务失败：批量同步物流信息异常", e);
        }
    }

    /**
     * 检查并标记超时物流
     * <p>
     * 每天凌晨2点执行一次，检查预计送达时间已过但未签收的物流。
     * </p>
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void markOvertimeLogistics() {
        log.info("定时任务：检查并标记超时物流");

        try {
            int overtimeCount = logisticsService.markOvertimeLogistics();
            log.info("定时任务完成：标记 {} 条超时物流", overtimeCount);
        } catch (Exception e) {
            log.error("定时任务失败：检查超时物流异常", e);
        }
    }
}
