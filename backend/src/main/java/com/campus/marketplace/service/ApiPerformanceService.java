package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.ApiPerformanceStatistics;
import com.campus.marketplace.common.entity.ApiPerformanceLog;

import java.util.List;

/**
 * API性能服务接口
 * 
 * @author BaSui
 * @date 2025-11-03
 */
public interface ApiPerformanceService {

    /**
     * 获取慢查询日志
     * 
     * @param hours 最近多少小时
     * @return 慢查询日志列表
     */
    List<ApiPerformanceLog> getSlowQueries(int hours);

    /**
     * 获取端点性能统计
     * 
     * @param hours 最近多少小时
     * @return 端点性能统计
     */
    List<ApiPerformanceStatistics.EndpointStats> getEndpointStatistics(int hours);

    /**
     * 获取错误请求日志
     * 
     * @param hours 最近多少小时
     * @return 错误请求日志列表
     */
    List<ApiPerformanceLog> getErrorRequests(int hours);

    /**
     * 获取QPS统计
     * 
     * @param hours 最近多少小时
     * @return QPS统计数据
     */
    List<ApiPerformanceStatistics.QpsData> getQpsStatistics(int hours);

    /**
     * 清理历史数据（保留30天）
     */
    void cleanupOldLogs();
}
