package com.campus.marketplace.task;

import com.campus.marketplace.common.entity.DataBackup;
import com.campus.marketplace.service.DataBackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 备份数据清理定时任务
 * 
 * 功能说明：
 * 1. 每天凌晨2点自动清理过期备份数据
 * 2. 每天上午10点检查即将过期的备份并发送提醒
 * 3. 记录清理日志，便于审计和监控
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BackupCleanupTask {

    private final DataBackupService dataBackupService;

    /**
     * 清理过期备份数据
     * 
     * 执行时间：每天凌晨2点
     * Cron表达式：0 0 2 * * ?
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredBackups() {
        log.info("=== 开始执行备份数据清理任务 ===");
        
        try {
            long startTime = System.currentTimeMillis();
            
            // 执行清理
            int deletedCount = dataBackupService.cleanupExpiredBackups();
            
            long executionTime = System.currentTimeMillis() - startTime;
            
            if (deletedCount > 0) {
                log.info("备份清理任务完成: 删除了 {} 条过期备份，耗时 {} ms", 
                        deletedCount, executionTime);
            } else {
                log.info("备份清理任务完成: 无过期备份需要清理，耗时 {} ms", executionTime);
            }
            
        } catch (Exception e) {
            log.error("备份清理任务执行失败", e);
        }
    }

    /**
     * 检查即将过期的备份
     * 
     * 执行时间：每天上午10点
     * Cron表达式：0 0 10 * * ?
     * 
     * 功能：检查7天内即将过期的备份，记录日志提醒管理员
     */
    @Scheduled(cron = "0 0 10 * * ?")
    public void checkExpiringBackups() {
        log.info("=== 开始检查即将过期的备份 ===");
        
        try {
            List<DataBackup> expiringBackups = dataBackupService.findExpiringBackups();
            
            if (expiringBackups.isEmpty()) {
                log.info("没有即将过期的备份");
                return;
            }
            
            log.warn("发现 {} 个备份即将在7天内过期:", expiringBackups.size());
            
            // 统计各实体类型的即将过期备份数量
            expiringBackups.stream()
                    .collect(
                            java.util.stream.Collectors.groupingBy(
                                    DataBackup::getEntityType,
                                    java.util.stream.Collectors.counting()
                            )
                    )
                    .forEach((entityType, count) -> 
                            log.warn("  - {}: {} 个备份即将过期", entityType, count));
            
        } catch (Exception e) {
            log.error("检查即将过期备份失败", e);
        }
    }

    /**
     * 备份数据统计报告
     * 
     * 执行时间：每周一上午9点
     * Cron表达式：0 0 9 ? * MON
     * 
     * 功能：生成备份数据的统计报告
     */
    @Scheduled(cron = "0 0 9 ? * MON")
    public void generateBackupStatistics() {
        log.info("=== 开始生成备份数据统计报告 ===");
        
        try {
            // 统计各实体类型的备份情况
            String[] entityTypes = {"Goods", "Order", "User", "BatchOperation"};
            
            log.info("备份数据统计报告（截至当前）:");
            
            for (String entityType : entityTypes) {
                long count = dataBackupService.countBackupsByEntityType(entityType);
                Long totalSize = dataBackupService.getTotalBackupSize(entityType);
                
                if (count > 0) {
                    double avgSize = totalSize.doubleValue() / count;
                    log.info("  - {}: {} 个备份, 总大小 {} bytes (平均 {} bytes/个)",
                            entityType, count, totalSize, String.format("%.2f", avgSize));
                }
            }
            
        } catch (Exception e) {
            log.error("生成备份统计报告失败", e);
        }
    }
}
