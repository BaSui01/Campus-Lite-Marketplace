package com.campus.marketplace.common.dto;

import com.campus.marketplace.common.enums.LogisticsCompany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 物流统计DTO
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogisticsStatisticsDTO {

    /**
     * 平均送达时间（小时）- 按快递公司统计
     */
    private Map<LogisticsCompany, Double> averageDeliveryTime;

    /**
     * 延误率（百分比）- 按快递公司统计
     */
    private Map<LogisticsCompany, Double> overtimeRate;

    /**
     * 用户评分 - 按快递公司统计
     */
    private Map<LogisticsCompany, Double> userRating;

    /**
     * 总订单数
     */
    private Integer totalOrders;

    /**
     * 超时订单数
     */
    private Integer overtimeOrders;
}
