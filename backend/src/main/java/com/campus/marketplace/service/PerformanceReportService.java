package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.PerformanceReportResponse;

/**
 * 性能报表服务接口
 * 
 * @author BaSui
 * @date 2025-11-03
 */
public interface PerformanceReportService {

    /**
     * 生成性能报表
     * 
     * @param hours 最近多少小时
     * @return 性能报表
     */
    PerformanceReportResponse generateReport(int hours);

    /**
     * 计算系统健康度评分
     * 
     * @param hours 最近多少小时
     * @return 健康度评分（0-100）
     */
    Double calculateHealthScore(int hours);
}
