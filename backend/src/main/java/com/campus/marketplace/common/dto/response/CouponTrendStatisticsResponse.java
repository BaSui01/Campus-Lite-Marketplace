package com.campus.marketplace.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 优惠券趋势统计响应 DTO
 *
 * @author BaSui 😎
 * @date 2025-11-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponTrendStatisticsResponse {

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 优惠券代码
     */
    private String code;

    /**
     * 优惠券名称
     */
    private String name;

    /**
     * 统计周期类型（DAILY/WEEKLY/MONTHLY）
     */
    private String periodType;

    /**
     * 趋势数据点列表
     */
    private List<TrendDataPoint> trendData;

    /**
     * 趋势数据点
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendDataPoint {
        /**
         * 日期
         */
        private LocalDate date;

        /**
         * 领取数量
         */
        private Integer receivedCount;

        /**
         * 使用数量
         */
        private Integer usedCount;

        /**
         * 使用率
         */
        private Double useRate;
    }
}
