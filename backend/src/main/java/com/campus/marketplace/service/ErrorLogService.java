package com.campus.marketplace.service;

import com.campus.marketplace.common.entity.ErrorLog;
import com.campus.marketplace.common.enums.ErrorSeverity;

import java.util.List;
import java.util.Map;

/**
 * 错误日志服务接口
 * 
 * @author BaSui
 * @date 2025-11-03
 */
public interface ErrorLogService {

    /**
     * 记录错误日志
     * 
     * @param exception 异常
     * @param requestUrl 请求URL
     * @param requestMethod 请求方法
     * @param ipAddress IP地址
     * @param requestParams 请求参数
     */
    void logError(
        Throwable exception,
        String requestUrl,
        String requestMethod,
        String ipAddress,
        Map<String, Object> requestParams
    );

    /**
     * 获取未解决的错误
     * 
     * @return 未解决的错误列表
     */
    List<ErrorLog> getUnresolvedErrors();

    /**
     * 获取指定严重程度的错误
     * 
     * @param severity 严重程度
     * @param hours 最近多少小时
     * @return 错误列表
     */
    List<ErrorLog> getErrorsBySeverity(ErrorSeverity severity, int hours);

    /**
     * 获取错误统计
     * 
     * @param hours 最近多少小时
     * @return 错误统计数据
     */
    Map<ErrorSeverity, Long> getErrorStatistics(int hours);

    /**
     * 标记错误为已解决
     * 
     * @param errorId 错误ID
     */
    void markAsResolved(Long errorId);

    /**
     * 检测并发送告警
     */
    void detectAndAlert();

    /**
     * 清理历史数据（保留30天）
     */
    void cleanupOldLogs(int daysToKeep);
}
