package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.ErrorLog;
import com.campus.marketplace.common.enums.ErrorSeverity;
import com.campus.marketplace.repository.ErrorLogRepository;
import com.campus.marketplace.service.ErrorLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 错误日志服务实现
 * 
 * 功能：
 * 1. 记录错误日志
 * 2. 错误分级管理
 * 3. 自动告警检测
 * 4. 定时清理历史数据
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorLogServiceImpl implements ErrorLogService {

    private final ErrorLogRepository errorLogRepository;

    @Override
    @Async
    @Transactional
    public void logError(
        Throwable exception,
        String requestUrl,
        String requestMethod,
        String ipAddress,
        Map<String, Object> requestParams
    ) {
        try {
            ErrorSeverity severity = ErrorSeverity.fromException(exception);

            ErrorLog errorLog = ErrorLog.builder()
                .errorType(exception.getClass().getName())
                .errorMessage(exception.getMessage() != null ? exception.getMessage() : "Unknown error")
                .stackTrace(getStackTrace(exception))
                .severity(severity)
                .requestUrl(requestUrl)
                .requestMethod(requestMethod)
                .ipAddress(ipAddress)
                .resolved(false)
                .build();

            errorLogRepository.save(errorLog);

            log.error("❌ 错误已记录: 严重程度={}, 类型={}, 消息={}", 
                severity, exception.getClass().getSimpleName(), exception.getMessage());

        } catch (Exception e) {
            // 记录错误日志失败，不影响主流程
            log.error("❌ 记录错误日志失败", e);
        }
    }

    @Override
    public List<ErrorLog> getUnresolvedErrors() {
        return errorLogRepository.findUnresolvedErrors();
    }

    @Override
    public List<ErrorLog> getErrorsBySeverity(ErrorSeverity severity, int hours) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        LocalDateTime endTime = LocalDateTime.now();
        return errorLogRepository.findBySeverityAndTimeRange(severity, startTime, endTime);
    }

    @Override
    public Map<ErrorSeverity, Long> getErrorStatistics(int hours) {
        LocalDateTime startTime = LocalDateTime.now().minusHours(hours);
        LocalDateTime endTime = LocalDateTime.now();

        List<Object[]> results = errorLogRepository.countBySeverityAndTimeRange(startTime, endTime);

        return results.stream()
            .collect(Collectors.toMap(
                row -> (ErrorSeverity) row[0],
                row -> (Long) row[1]
            ));
    }

    @Override
    @Transactional
    public void markAsResolved(Long errorId) {
        ErrorLog errorLog = errorLogRepository.findById(errorId)
            .orElseThrow(() -> new IllegalArgumentException("错误日志不存在"));

        errorLog.setResolved(true);
        errorLog.setResolvedAt(LocalDateTime.now());

        errorLogRepository.save(errorLog);

        log.info("✅ 错误已标记为已解决: errorId={}", errorId);
    }

    @Override
    // @Scheduled(fixedRate = 300000) // 临时禁用告警功能
    public void detectAndAlert() {
        // 暂时禁用告警功能，等待完善
        log.debug("告警检测已禁用");
        /* LocalDateTime since = LocalDateTime.now().minusMinutes(5);

        // 检测严重错误
        List<ErrorLog> criticalErrors = errorLogRepository.findUnalertedErrors(
            ErrorSeverity.CRITICAL, since);
        if (criticalErrors.size() >= CRITICAL_ERROR_THRESHOLD) {
            sendAlert(ErrorSeverity.CRITICAL, criticalErrors);
        }

         */
    }

    @Override
    @Transactional
    public void cleanupOldLogs(int daysToKeep) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysToKeep);
        errorLogRepository.deleteByCreatedAtBefore(cutoff);
        log.info("✅ 错误日志清理完成: 删除{}天前的记录", daysToKeep);
    }

    @Scheduled(cron = "0 0 4 * * ?") // 每天凌晨4点执行
    public void scheduledCleanup() {
        cleanupOldLogs(30); // 定时任务默认保留30天
    }

    /**
     * 获取堆栈跟踪
     */
    private String getStackTrace(Throwable exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String stackTrace = sw.toString();
        
        // 限制长度（最多10000字符）
        if (stackTrace.length() > 10000) {
            stackTrace = stackTrace.substring(0, 10000) + "...(truncated)";
        }
        
        return stackTrace;
    }
}
