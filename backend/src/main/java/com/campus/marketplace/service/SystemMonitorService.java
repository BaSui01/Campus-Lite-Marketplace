package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.HealthCheckResponse;
import com.campus.marketplace.common.dto.response.SystemMetricsResponse;
import com.campus.marketplace.common.entity.HealthCheckRecord;

import java.util.List;

/**
 * 系统监控服务接口
 * 
 * @author BaSui
 * @date 2025-11-03
 */
public interface SystemMonitorService {

    /**
     * 执行健康检查
     * 
     * @return 健康检查结果
     */
    HealthCheckResponse performHealthCheck();

    /**
     * 获取系统指标
     * 
     * @return 系统指标
     */
    SystemMetricsResponse getSystemMetrics();

    /**
     * 获取健康检查历史
     * 
     * @param hours 最近多少小时
     * @return 健康检查记录列表
     */
    List<HealthCheckRecord> getHealthCheckHistory(int hours);

    /**
     * 清理历史数据（保留30天）
     */
    void cleanupOldRecords(int daysToKeep);
}
