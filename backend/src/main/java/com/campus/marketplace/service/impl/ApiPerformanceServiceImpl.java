package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.response.ApiPerformanceStatistics;
import com.campus.marketplace.common.entity.ApiPerformanceLog;
import com.campus.marketplace.repository.ApiPerformanceLogRepository;
import com.campus.marketplace.service.ApiPerformanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * API性能服务实现
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiPerformanceServiceImpl implements ApiPerformanceService {

    private final ApiPerformanceLogRepository apiPerformanceLogRepository;

    @Override
    public List<ApiPerformanceLog> getSlowQueries(int hours) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        LocalDateTime endTime = LocalDateTime.now();
        return apiPerformanceLogRepository.findSlowQueries(startTime, endTime);
    }

    @Override
    public List<ApiPerformanceStatistics.EndpointStats> getEndpointStatistics(int hours) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        LocalDateTime endTime = LocalDateTime.now();
        
        List<Object[]> results = apiPerformanceLogRepository.getEndpointStatistics(startTime, endTime);
        
        return results.stream()
            .map(row -> ApiPerformanceStatistics.EndpointStats.builder()
                .endpoint((String) row[0])
                .requestCount((Long) row[1])
                .avgDurationMs((Double) row[2])
                .maxDurationMs((Long) row[3])
                .build())
            .collect(Collectors.toList());
    }

    @Override
    public List<ApiPerformanceLog> getErrorRequests(int hours) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        LocalDateTime endTime = LocalDateTime.now();
        return apiPerformanceLogRepository.findErrorRequests(startTime, endTime);
    }

    @Override
    public List<ApiPerformanceStatistics.QpsData> getQpsStatistics(int hours) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        LocalDateTime endTime = LocalDateTime.now();
        
        List<Object[]> results = apiPerformanceLogRepository.getQpsStatistics(startTime, endTime);
        
        return results.stream()
            .map(row -> {
                Timestamp timestamp = (Timestamp) row[0];
                Long count = ((Number) row[1]).longValue();
                return ApiPerformanceStatistics.QpsData.builder()
                    .minute(timestamp.toLocalDateTime())
                    .requestCount(count)
                    .qps(count / 60.0) // 转换为每秒请求数
                    .build();
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 3 * * ?") // 每天凌晨3点执行
    public void cleanupOldLogs() {
        int daysToKeep = 30; // 默认保留30天
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysToKeep);
        apiPerformanceLogRepository.deleteByCreatedAtBefore(cutoff);
        log.info("✅ API性能日志清理完成: 删除{}天前的记录", daysToKeep);
    }
}
