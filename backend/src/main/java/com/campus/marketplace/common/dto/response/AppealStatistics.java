package com.campus.marketplace.common.dto.response;

import lombok.*;

/**
 * 申诉统计信息DTO
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppealStatistics {

    /**
     * 总申诉数量
     */
    private long totalCount;

    /**
     * 待处理申诉数量
     */
    private long pendingCount;

    /**
     * 审核中的数量
     */
    private long reviewingCount;

    /**
     * 已完成数量
     */
    private long completedCount;

    /**
     * 申诉成功数量
     */
    private long approvedCount;

    /**
     * 申诉驳回数量
     */
    private long rejectedCount;

    /**
     * 用户取消数量
     */
    private long cancelledCount;

    /**
     * 过期数量
     */
    private long expiredCount;

    /**
     * 今日新增申诉数量
     */
    private long todayCount;

    /**
     * 本周新增申诉数量
     */
    private long weeklyCount;

    /**
     * 本月新增申诉数量
     */
    private long monthlyCount;

    /**
     * 不同类型的申诉统计
     */
    private AppealTypeStatistics typeStatistics;

    /**
     * 是否有待处理的申诉
     */
    public boolean hasPendingAppeals() {
        return pendingCount > 0;
    }

    /**
     * 获取待处理比例
     */
    public double getPendingRate() {
        return totalCount > 0 ? (double) pendingCount / totalCount : 0.0;
    }

    /**
     * 获取完成率
     */
    public double getCompletionRate() {
        return totalCount > 0 ? (double) completedCount / totalCount : 0.0;
    }
}
