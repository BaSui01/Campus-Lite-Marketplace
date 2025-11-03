package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.response.HealthCheckResponse;
import com.campus.marketplace.common.dto.response.SystemMetricsResponse;
import com.campus.marketplace.common.entity.HealthCheckRecord;
import com.campus.marketplace.common.enums.HealthStatus;
import com.campus.marketplace.repository.HealthCheckRecordRepository;
import com.campus.marketplace.service.SystemMonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统监控服务实现
 * 
 * 功能：
 * 1. 健康检查（数据库、Redis、JVM）
 * 2. 系统指标采集（CPU、内存、磁盘）
 * 3. 健康检查历史记录
 * 4. 定时清理历史数据
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemMonitorServiceImpl implements SystemMonitorService {

    private final DataSource dataSource;
    private final RedisTemplate<String, Object> redisTemplate;
    private final HealthCheckRecordRepository healthCheckRecordRepository;

    /**
     * 内存使用率警告阈值（85%）
     */
    private static final double MEMORY_WARNING_THRESHOLD = 0.85;

    @Override
    public HealthCheckResponse performHealthCheck() {
        long startTime = System.currentTimeMillis();
        Map<String, HealthCheckResponse.ComponentHealth> components = new HashMap<>();

        // 检查数据库
        components.put("database", checkDatabase());

        // 检查Redis
        components.put("redis", checkRedis());

        // 检查JVM
        components.put("jvm", checkJvm());

        // 计算整体状态
        HealthStatus overallStatus = calculateOverallStatus(components);

        long responseTime = System.currentTimeMillis() - startTime;

        HealthCheckResponse response = HealthCheckResponse.builder()
            .checkTime(LocalDateTime.now())
            .overallStatus(overallStatus)
            .components(components)
            .responseTimeMs(responseTime)
            .build();

        // 保存健康检查记录
        saveHealthCheckRecord(response);

        log.info("✅ 健康检查完成: 状态={}, 响应时间={}ms", overallStatus, responseTime);

        return response;
    }

    /**
     * 检查数据库连接
     */
    private HealthCheckResponse.ComponentHealth checkDatabase() {
        Map<String, Object> details = new HashMap<>();
        
        try (Connection conn = dataSource.getConnection()) {
            boolean isValid = conn.isValid(3);
            details.put("connected", isValid);
            details.put("vendor", conn.getMetaData().getDatabaseProductName());
            details.put("version", conn.getMetaData().getDatabaseProductVersion());

            return HealthCheckResponse.ComponentHealth.builder()
                .name("database")
                .status(isValid ? HealthStatus.HEALTHY : HealthStatus.UNHEALTHY)
                .details(details)
                .build();
        } catch (Exception e) {
            log.error("❌ 数据库健康检查失败", e);
            details.put("connected", false);
            return HealthCheckResponse.ComponentHealth.builder()
                .name("database")
                .status(HealthStatus.UNHEALTHY)
                .details(details)
                .error(e.getMessage())
                .build();
        }
    }

    /**
     * 检查Redis连接
     */
    private HealthCheckResponse.ComponentHealth checkRedis() {
        Map<String, Object> details = new HashMap<>();
        
        try {
            // 测试Redis连接
            redisTemplate.getConnectionFactory().getConnection().ping();
            details.put("connected", true);

            return HealthCheckResponse.ComponentHealth.builder()
                .name("redis")
                .status(HealthStatus.HEALTHY)
                .details(details)
                .build();
        } catch (Exception e) {
            log.error("❌ Redis健康检查失败", e);
            details.put("connected", false);
            return HealthCheckResponse.ComponentHealth.builder()
                .name("redis")
                .status(HealthStatus.UNHEALTHY)
                .details(details)
                .error(e.getMessage())
                .build();
        }
    }

    /**
     * 检查JVM状态
     */
    private HealthCheckResponse.ComponentHealth checkJvm() {
        Map<String, Object> details = new HashMap<>();
        
        try {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double memoryUsagePercent = (double) usedMemory / totalMemory;

            details.put("totalMemoryMB", totalMemory / (1024 * 1024));
            details.put("usedMemoryMB", usedMemory / (1024 * 1024));
            details.put("freeMemoryMB", freeMemory / (1024 * 1024));
            details.put("memoryUsagePercent", Math.round(memoryUsagePercent * 100 * 100.0) / 100.0);
            details.put("activeThreads", Thread.activeCount());

            // 判断状态
            HealthStatus status = memoryUsagePercent > MEMORY_WARNING_THRESHOLD 
                ? HealthStatus.DEGRADED 
                : HealthStatus.HEALTHY;

            return HealthCheckResponse.ComponentHealth.builder()
                .name("jvm")
                .status(status)
                .details(details)
                .build();
        } catch (Exception e) {
            log.error("❌ JVM健康检查失败", e);
            return HealthCheckResponse.ComponentHealth.builder()
                .name("jvm")
                .status(HealthStatus.UNHEALTHY)
                .details(details)
                .error(e.getMessage())
                .build();
        }
    }

    /**
     * 计算整体健康状态
     */
    private HealthStatus calculateOverallStatus(Map<String, HealthCheckResponse.ComponentHealth> components) {
        boolean hasUnhealthy = components.values().stream()
            .anyMatch(c -> c.getStatus() == HealthStatus.UNHEALTHY);
        
        if (hasUnhealthy) {
            // 如果数据库不健康，整体不健康
            if (components.get("database").getStatus() == HealthStatus.UNHEALTHY) {
                return HealthStatus.UNHEALTHY;
            }
            // 其他组件不健康，降级
            return HealthStatus.DEGRADED;
        }

        boolean hasDegraded = components.values().stream()
            .anyMatch(c -> c.getStatus() == HealthStatus.DEGRADED);
        
        return hasDegraded ? HealthStatus.DEGRADED : HealthStatus.HEALTHY;
    }

    /**
     * 保存健康检查记录
     */
    @Transactional
    protected void saveHealthCheckRecord(HealthCheckResponse response) {
        try {
            Map<String, HealthCheckRecord.ComponentStatus> componentDetails = new HashMap<>();
            
            response.getComponents().forEach((name, health) -> {
                componentDetails.put(name, new HealthCheckRecord.ComponentStatus(
                    health.getName(),
                    health.getStatus(),
                    health.getDetails(),
                    health.getError()
                ));
            });

            HealthCheckRecord record = HealthCheckRecord.builder()
                .checkTime(response.getCheckTime())
                .overallStatus(response.getOverallStatus())
                .componentDetails(componentDetails)
                .responseTimeMs(response.getResponseTimeMs())
                .build();

            healthCheckRecordRepository.save(record);
        } catch (Exception e) {
            log.error("❌ 保存健康检查记录失败", e);
        }
    }

    @Override
    public SystemMetricsResponse getSystemMetrics() {
        Runtime runtime = Runtime.getRuntime();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        // 内存指标
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double memoryUsagePercent = (double) usedMemory / totalMemory * 100;

        // CPU指标
        double cpuUsage = osBean.getSystemLoadAverage();

        // 磁盘指标
        File root = new File("/");
        long totalDiskSpace = root.getTotalSpace();
        long freeDiskSpace = root.getFreeSpace();
        long usedDiskSpace = totalDiskSpace - freeDiskSpace;
        double diskUsagePercent = (double) usedDiskSpace / totalDiskSpace * 100;

        // 系统运行时间
        long uptime = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;

        return SystemMetricsResponse.builder()
            .cpuUsagePercent(cpuUsage >= 0 ? Math.round(cpuUsage * 100.0) / 100.0 : 0.0)
            .memoryUsagePercent(Math.round(memoryUsagePercent * 100.0) / 100.0)
            .totalMemoryMB(totalMemory / (1024 * 1024))
            .usedMemoryMB(usedMemory / (1024 * 1024))
            .freeMemoryMB(freeMemory / (1024 * 1024))
            .diskUsagePercent(Math.round(diskUsagePercent * 100.0) / 100.0)
            .totalDiskSpaceGB(totalDiskSpace / (1024 * 1024 * 1024))
            .freeDiskSpaceGB(freeDiskSpace / (1024 * 1024 * 1024))
            .activeThreadCount(Thread.activeCount())
            .uptimeSeconds(uptime)
            .build();
    }

    @Override
    public List<HealthCheckRecord> getHealthCheckHistory(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return healthCheckRecordRepository.findRecentRecords(since);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanupOldRecords(int daysToKeep) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysToKeep);
        healthCheckRecordRepository.deleteByCheckTimeBefore(cutoff);
        log.info("✅ 健康检查历史数据清理完成: 删除{}天前的记录", daysToKeep);
    }

    /**
     * 定时健康检查（每5分钟执行一次）
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void scheduledHealthCheck() {
        performHealthCheck();
    }
}
