package com.campus.marketplace.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统指标响应DTO
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemMetricsResponse {

    /**
     * CPU使用率（百分比）
     */
    private Double cpuUsagePercent;

    /**
     * 内存使用率（百分比）
     */
    private Double memoryUsagePercent;

    /**
     * 总内存（MB）
     */
    private Long totalMemoryMB;

    /**
     * 已用内存（MB）
     */
    private Long usedMemoryMB;

    /**
     * 空闲内存（MB）
     */
    private Long freeMemoryMB;

    /**
     * 磁盘使用率（百分比）
     */
    private Double diskUsagePercent;

    /**
     * 总磁盘空间（GB）
     */
    private Long totalDiskSpaceGB;

    /**
     * 可用磁盘空间（GB）
     */
    private Long freeDiskSpaceGB;

    /**
     * 活动线程数
     */
    private Integer activeThreadCount;

    /**
     * 系统运行时间（秒）
     */
    private Long uptimeSeconds;
}
