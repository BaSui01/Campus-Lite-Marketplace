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
import java.net.InetAddress;
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

    /**
     * 告警阈值配置
     */
    private static final int CRITICAL_ERROR_THRESHOLD = 1;  // 严重错误：出现1次就告警
    private static final int HIGH_ERROR_THRESHOLD = 5;      // 高级错误：出现5次告警
    private static final int MEDIUM_ERROR_THRESHOLD = 10;   // 中级错误：出现10次告警

    @Override
    @Async
    @Transactional
    public void logError(
        Throwable exception,
        String requestPath,
        String httpMethod,
        String clientIp,
        Map<String, Object> requestParams
    ) {
        try {
            ErrorSeverity severity = ErrorSeverity.fromException(exception);

            ErrorLog errorLog = ErrorLog.builder()
                .errorTime(LocalDateTime.now())
                .errorType(exception.getClass().getName())
                .errorMessage(exception.getMessage() != null ? exception.getMessage() : "Unknown error")
                .stackTrace(getStackTrace(exception))
                .severity(severity)
                .requestPath(requestPath)
                .httpMethod(httpMethod)
                .clientIp(clientIp)
                .requestParams(requestParams)
                .serverInfo(getServerInfo())
                .isResolved(false)
                .isAlerted(false)
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
    public void markAsResolved(Long errorId, String resolutionNote) {
        ErrorLog errorLog = errorLogRepository.findById(errorId)
            .orElseThrow(() -> new IllegalArgumentException("错误日志不存在"));

        errorLog.setIsResolved(true);
        errorLog.setResolvedAt(LocalDateTime.now());
        errorLog.setResolutionNote(resolutionNote);

        errorLogRepository.save(errorLog);

        log.info("✅ 错误已标记为已解决: errorId={}", errorId);
    }

    @Override
    @Scheduled(fixedRate = 300000) // 每5分钟检测一次
    public void detectAndAlert() {
        LocalDateTime since = LocalDateTime.now().minusMinutes(5);

        // 检测严重错误
        List<ErrorLog> criticalErrors = errorLogRepository.findUnalertedErrors(
            ErrorSeverity.CRITICAL, since);
        if (!criticalErrors.isEmpty()) {
            sendAlert(ErrorSeverity.CRITICAL, criticalErrors);
            markAsAlerted(criticalErrors);
        }

        // 检测高级错误
        List<ErrorLog> highErrors = errorLogRepository.findUnalertedErrors(
            ErrorSeverity.HIGH, since);
        if (highErrors.size() >= HIGH_ERROR_THRESHOLD) {
            sendAlert(ErrorSeverity.HIGH, highErrors);
            markAsAlerted(highErrors);
        }

        // 检测中级错误
        List<ErrorLog> mediumErrors = errorLogRepository.findUnalertedErrors(
            ErrorSeverity.MEDIUM, since);
        if (mediumErrors.size() >= MEDIUM_ERROR_THRESHOLD) {
            sendAlert(ErrorSeverity.MEDIUM, mediumErrors);
            markAsAlerted(mediumErrors);
        }
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 4 * * ?") // 每天凌晨4点执行
    public void cleanupOldLogs(int daysToKeep) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysToKeep);
        errorLogRepository.deleteByErrorTimeBefore(cutoff);
        log.info("✅ 错误日志清理完成: 删除{}天前的记录", daysToKeep);
    }

    /**
     * 发送告警
     */
    private void sendAlert(ErrorSeverity severity, List<ErrorLog> errors) {
        // 当前使用日志告警，生产环境可集成实际告警系统
        // 可选方案：邮件（JavaMailSender）、短信（阿里云SMS）、钉钉机器人、企业微信
        log.warn("⚠️ 错误告警: 严重程度={}, 错误数量={}", severity, errors.size());
        
        // 打印错误详情
        errors.forEach(error -> 
            log.warn("   - 错误类型={}, 消息={}, 路径={}", 
                error.getErrorType(), error.getErrorMessage(), error.getRequestPath())
        );
    }

    /**
     * 标记为已告警
     */
    @Transactional
    protected void markAsAlerted(List<ErrorLog> errors) {
        errors.forEach(error -> {
            error.setIsAlerted(true);
            error.setAlertedAt(LocalDateTime.now());
        });
        errorLogRepository.saveAll(errors);
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

    /**
     * 获取服务器信息
     */
    private String getServerInfo() {
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            return localhost.getHostName() + " / " + localhost.getHostAddress();
        } catch (Exception e) {
            return "Unknown";
        }
    }
}
