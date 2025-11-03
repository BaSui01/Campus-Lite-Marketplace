package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.response.ApiPerformanceStatistics;
import com.campus.marketplace.common.dto.response.PerformanceReportResponse;
import com.campus.marketplace.common.entity.ApiPerformanceLog;
import com.campus.marketplace.common.enums.ErrorSeverity;
import com.campus.marketplace.common.enums.HealthStatus;
import com.campus.marketplace.repository.ApiPerformanceLogRepository;
import com.campus.marketplace.repository.ErrorLogRepository;
import com.campus.marketplace.repository.HealthCheckRecordRepository;
import com.campus.marketplace.service.PerformanceReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 性能报表服务实现
 * 
 * 功能：
 * 1. 生成综合性能报表
 * 2. 计算系统健康度评分
 * 3. 生成优化建议
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceReportServiceImpl implements PerformanceReportService {

    private final HealthCheckRecordRepository healthCheckRecordRepository;
    private final ApiPerformanceLogRepository apiPerformanceLogRepository;
    private final ErrorLogRepository errorLogRepository;

    @Override
    public PerformanceReportResponse generateReport(int hours) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        LocalDateTime endTime = LocalDateTime.now();

        // 健康概览
        PerformanceReportResponse.HealthOverview healthOverview = buildHealthOverview(startTime, endTime);

        // API性能概览
        PerformanceReportResponse.ApiPerformanceOverview apiPerformanceOverview = 
            buildApiPerformanceOverview(startTime, endTime);

        // 错误统计
        PerformanceReportResponse.ErrorStatistics errorStatistics = 
            buildErrorStatistics(startTime, endTime);

        // 计算健康度评分
        Double healthScore = calculateHealthScore(hours);

        // 生成优化建议
        List<PerformanceReportResponse.OptimizationSuggestion> suggestions = 
            generateOptimizationSuggestions(healthOverview, apiPerformanceOverview, errorStatistics);

        return PerformanceReportResponse.builder()
            .startTime(startTime)
            .endTime(endTime)
            .healthScore(healthScore)
            .healthOverview(healthOverview)
            .apiPerformanceOverview(apiPerformanceOverview)
            .errorStatistics(errorStatistics)
            .suggestions(suggestions)
            .build();
    }

    @Override
    public Double calculateHealthScore(int hours) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        LocalDateTime endTime = LocalDateTime.now();

        // 健康检查评分（40分）
        double healthCheckScore = calculateHealthCheckScore(startTime, endTime);

        // API性能评分（30分）
        double apiPerformanceScore = calculateApiPerformanceScore(startTime, endTime);

        // 错误率评分（30分）
        double errorScore = calculateErrorScore(startTime, endTime);

        double totalScore = healthCheckScore + apiPerformanceScore + errorScore;

        log.info("✅ 系统健康度评分: 总分={}, 健康检查={}, API性能={}, 错误率={}", 
            totalScore, healthCheckScore, apiPerformanceScore, errorScore);

        return Math.round(totalScore * 100.0) / 100.0;
    }

    /**
     * 构建健康概览
     */
    private PerformanceReportResponse.HealthOverview buildHealthOverview(
        LocalDateTime startTime, LocalDateTime endTime
    ) {
        long totalChecks = healthCheckRecordRepository.count();
        long healthyCount = healthCheckRecordRepository.countByStatusAndTimeRange(
            HealthStatus.HEALTHY, startTime, endTime);
        long degradedCount = healthCheckRecordRepository.countByStatusAndTimeRange(
            HealthStatus.DEGRADED, startTime, endTime);
        long unhealthyCount = healthCheckRecordRepository.countByStatusAndTimeRange(
            HealthStatus.UNHEALTHY, startTime, endTime);

        double healthRate = totalChecks > 0 ? (double) healthyCount / totalChecks * 100 : 100.0;
        
        Double avgResponseTime = healthCheckRecordRepository.getAverageResponseTime(startTime, endTime);

        return PerformanceReportResponse.HealthOverview.builder()
            .totalChecks(totalChecks)
            .healthyCount(healthyCount)
            .degradedCount(degradedCount)
            .unhealthyCount(unhealthyCount)
            .healthRate(Math.round(healthRate * 100.0) / 100.0)
            .avgResponseTime(avgResponseTime != null ? Math.round(avgResponseTime * 100.0) / 100.0 : 0.0)
            .build();
    }

    /**
     * 构建API性能概览
     */
    private PerformanceReportResponse.ApiPerformanceOverview buildApiPerformanceOverview(
        LocalDateTime startTime, LocalDateTime endTime
    ) {
        List<ApiPerformanceLog> allLogs = apiPerformanceLogRepository.findAll();
        
        long totalRequests = allLogs.size();
        long successRequests = allLogs.stream().filter(ApiPerformanceLog::getIsSuccess).count();
        long failedRequests = totalRequests - successRequests;
        long slowQueryCount = allLogs.stream().filter(ApiPerformanceLog::getIsSlow).count();

        double successRate = totalRequests > 0 ? (double) successRequests / totalRequests * 100 : 100.0;
        double slowQueryRate = totalRequests > 0 ? (double) slowQueryCount / totalRequests * 100 : 0.0;

        // 计算平均响应时间
        double avgResponseTime = allLogs.stream()
            .mapToLong(ApiPerformanceLog::getDurationMs)
            .average()
            .orElse(0.0);

        // 计算P95和P99
        List<Long> sortedDurations = allLogs.stream()
            .map(ApiPerformanceLog::getDurationMs)
            .sorted()
            .collect(Collectors.toList());

        double p95ResponseTime = calculatePercentile(sortedDurations, 95);
        double p99ResponseTime = calculatePercentile(sortedDurations, 99);

        // Top 10慢接口
        List<Object[]> endpointStats = apiPerformanceLogRepository.getEndpointStatistics(startTime, endTime);
        List<ApiPerformanceStatistics.EndpointStats> top10SlowApis = endpointStats.stream()
            .limit(10)
            .map(row -> ApiPerformanceStatistics.EndpointStats.builder()
                .endpoint((String) row[0])
                .requestCount((Long) row[1])
                .avgDurationMs((Double) row[2])
                .maxDurationMs((Long) row[3])
                .build())
            .collect(Collectors.toList());

        return PerformanceReportResponse.ApiPerformanceOverview.builder()
            .totalRequests(totalRequests)
            .successRequests(successRequests)
            .failedRequests(failedRequests)
            .successRate(Math.round(successRate * 100.0) / 100.0)
            .slowQueryCount(slowQueryCount)
            .slowQueryRate(Math.round(slowQueryRate * 100.0) / 100.0)
            .avgResponseTime(Math.round(avgResponseTime * 100.0) / 100.0)
            .p95ResponseTime(Math.round(p95ResponseTime * 100.0) / 100.0)
            .p99ResponseTime(Math.round(p99ResponseTime * 100.0) / 100.0)
            .top10SlowApis(top10SlowApis)
            .build();
    }

    /**
     * 构建错误统计
     */
    private PerformanceReportResponse.ErrorStatistics buildErrorStatistics(
        LocalDateTime startTime, LocalDateTime endTime
    ) {
        List<Object[]> errorCounts = errorLogRepository.countBySeverityAndTimeRange(startTime, endTime);
        
        Map<ErrorSeverity, Long> errorMap = errorCounts.stream()
            .collect(Collectors.toMap(
                row -> (ErrorSeverity) row[0],
                row -> (Long) row[1]
            ));

        long totalErrors = errorMap.values().stream().mapToLong(Long::longValue).sum();
        long criticalErrors = errorMap.getOrDefault(ErrorSeverity.CRITICAL, 0L);
        long highErrors = errorMap.getOrDefault(ErrorSeverity.HIGH, 0L);
        long mediumErrors = errorMap.getOrDefault(ErrorSeverity.MEDIUM, 0L);
        long lowErrors = errorMap.getOrDefault(ErrorSeverity.LOW, 0L);

        long unresolvedErrors = errorLogRepository.findUnresolvedErrors().size();

        // Top 10错误类型
        List<Object[]> errorTypes = errorLogRepository.countByErrorType(startTime, endTime);
        Map<String, Long> top10ErrorTypes = errorTypes.stream()
            .limit(10)
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> (Long) row[1],
                (a, b) -> a,
                LinkedHashMap::new
            ));

        return PerformanceReportResponse.ErrorStatistics.builder()
            .totalErrors(totalErrors)
            .criticalErrors(criticalErrors)
            .highErrors(highErrors)
            .mediumErrors(mediumErrors)
            .lowErrors(lowErrors)
            .unresolvedErrors(unresolvedErrors)
            .top10ErrorTypes(top10ErrorTypes)
            .build();
    }

    /**
     * 生成优化建议
     */
    private List<PerformanceReportResponse.OptimizationSuggestion> generateOptimizationSuggestions(
        PerformanceReportResponse.HealthOverview healthOverview,
        PerformanceReportResponse.ApiPerformanceOverview apiPerformanceOverview,
        PerformanceReportResponse.ErrorStatistics errorStatistics
    ) {
        List<PerformanceReportResponse.OptimizationSuggestion> suggestions = new ArrayList<>();

        // 1. 健康检查建议
        if (healthOverview.getHealthRate() < 95.0) {
            suggestions.add(PerformanceReportResponse.OptimizationSuggestion.builder()
                .type(PerformanceReportResponse.SuggestionType.STABILITY)
                .priority("高")
                .title("系统健康率偏低")
                .description(String.format("当前健康率为%.2f%%，低于95%%的健康阈值", healthOverview.getHealthRate()))
                .impact("影响系统稳定性和用户体验")
                .expectedBenefit("提升健康率至95%以上，减少服务不可用时间")
                .build());
        }

        // 2. API性能建议
        if (apiPerformanceOverview.getSlowQueryRate() > 10.0) {
            suggestions.add(PerformanceReportResponse.OptimizationSuggestion.builder()
                .type(PerformanceReportResponse.SuggestionType.PERFORMANCE)
                .priority("高")
                .title("慢查询率过高")
                .description(String.format("当前慢查询率为%.2f%%，超过10%%的警戒线", apiPerformanceOverview.getSlowQueryRate()))
                .impact("严重影响接口响应速度和用户体验")
                .expectedBenefit("优化慢查询，提升接口平均响应速度30%")
                .build());
        }

        if (apiPerformanceOverview.getP95ResponseTime() > 1000.0) {
            suggestions.add(PerformanceReportResponse.OptimizationSuggestion.builder()
                .type(PerformanceReportResponse.SuggestionType.PERFORMANCE)
                .priority("中")
                .title("P95响应时间过长")
                .description(String.format("当前P95响应时间为%.0fms，超过1000ms的警戒线", apiPerformanceOverview.getP95ResponseTime()))
                .impact("影响用户体验，特别是高峰时段")
                .expectedBenefit("优化响应时间，提升用户满意度")
                .build());
        }

        if (apiPerformanceOverview.getSuccessRate() < 99.0) {
            suggestions.add(PerformanceReportResponse.OptimizationSuggestion.builder()
                .type(PerformanceReportResponse.SuggestionType.STABILITY)
                .priority("高")
                .title("API成功率偏低")
                .description(String.format("当前成功率为%.2f%%，低于99%%的目标", apiPerformanceOverview.getSuccessRate()))
                .impact("频繁的错误影响用户操作流程")
                .expectedBenefit("提升成功率至99%以上，减少用户投诉")
                .build());
        }

        // 3. 错误建议
        if (errorStatistics.getCriticalErrors() > 0) {
            suggestions.add(PerformanceReportResponse.OptimizationSuggestion.builder()
                .type(PerformanceReportResponse.SuggestionType.STABILITY)
                .priority("严重")
                .title("存在严重错误")
                .description(String.format("检测到%d个严重错误，需要立即处理", errorStatistics.getCriticalErrors()))
                .impact("可能导致系统崩溃或核心功能不可用")
                .expectedBenefit("消除严重错误，确保系统稳定运行")
                .build());
        }

        if (errorStatistics.getUnresolvedErrors() > 10) {
            suggestions.add(PerformanceReportResponse.OptimizationSuggestion.builder()
                .type(PerformanceReportResponse.SuggestionType.STABILITY)
                .priority("中")
                .title("未解决错误累积过多")
                .description(String.format("当前有%d个未解决的错误", errorStatistics.getUnresolvedErrors()))
                .impact("错误累积可能导致问题恶化")
                .expectedBenefit("及时处理错误，避免问题扩散")
                .build());
        }

        // 4. 资源使用建议（基于健康检查）
        if (healthOverview.getAvgResponseTime() > 500.0) {
            suggestions.add(PerformanceReportResponse.OptimizationSuggestion.builder()
                .type(PerformanceReportResponse.SuggestionType.RESOURCE)
                .priority("中")
                .title("健康检查响应时间过长")
                .description(String.format("平均健康检查响应时间为%.0fms，超过500ms", healthOverview.getAvgResponseTime()))
                .impact("可能存在资源瓶颈或网络问题")
                .expectedBenefit("优化资源配置，提升系统响应能力")
                .build());
        }

        // 按优先级排序
        suggestions.sort((a, b) -> {
            Map<String, Integer> priorityMap = Map.of("严重", 1, "高", 2, "中", 3, "低", 4);
            return priorityMap.getOrDefault(a.getPriority(), 4) - priorityMap.getOrDefault(b.getPriority(), 4);
        });

        return suggestions;
    }

    /**
     * 计算健康检查评分
     */
    private double calculateHealthCheckScore(LocalDateTime startTime, LocalDateTime endTime) {
        long totalChecks = healthCheckRecordRepository.count();
        if (totalChecks == 0) {
            return 40.0; // 默认满分
        }

        long healthyCount = healthCheckRecordRepository.countByStatusAndTimeRange(
            HealthStatus.HEALTHY, startTime, endTime);
        
        double healthRate = (double) healthyCount / totalChecks;
        return healthRate * 40.0;
    }

    /**
     * 计算API性能评分
     */
    private double calculateApiPerformanceScore(LocalDateTime startTime, LocalDateTime endTime) {
        List<ApiPerformanceLog> allLogs = apiPerformanceLogRepository.findAll();
        
        if (allLogs.isEmpty()) {
            return 30.0; // 默认满分
        }

        long successCount = allLogs.stream().filter(ApiPerformanceLog::getIsSuccess).count();
        long slowCount = allLogs.stream().filter(ApiPerformanceLog::getIsSlow).count();

        double successRate = (double) successCount / allLogs.size();
        double slowRate = (double) slowCount / allLogs.size();

        // 成功率权重70%，慢查询率权重30%
        double score = (successRate * 0.7 + (1 - slowRate) * 0.3) * 30.0;
        
        return Math.max(0, score);
    }

    /**
     * 计算错误率评分
     */
    private double calculateErrorScore(LocalDateTime startTime, LocalDateTime endTime) {
        List<Object[]> errorCounts = errorLogRepository.countBySeverityAndTimeRange(startTime, endTime);
        
        if (errorCounts.isEmpty()) {
            return 30.0; // 默认满分
        }

        long criticalErrors = errorCounts.stream()
            .filter(row -> row[0] == ErrorSeverity.CRITICAL)
            .mapToLong(row -> (Long) row[1])
            .sum();

        long highErrors = errorCounts.stream()
            .filter(row -> row[0] == ErrorSeverity.HIGH)
            .mapToLong(row -> (Long) row[1])
            .sum();

        long totalErrors = errorCounts.stream()
            .mapToLong(row -> (Long) row[1])
            .sum();

        // 严重错误扣10分/个，高级错误扣5分/个，其他错误扣1分/个
        double deduction = criticalErrors * 10 + highErrors * 5 + (totalErrors - criticalErrors - highErrors);
        
        return Math.max(0, 30.0 - deduction);
    }

    /**
     * 计算百分位数
     */
    private double calculatePercentile(List<Long> sortedValues, int percentile) {
        if (sortedValues.isEmpty()) {
            return 0.0;
        }

        int index = (int) Math.ceil((percentile / 100.0) * sortedValues.size()) - 1;
        index = Math.max(0, Math.min(index, sortedValues.size() - 1));
        
        return sortedValues.get(index).doubleValue();
    }
}
