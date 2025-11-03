package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 价格批量调整请求
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceBatchRequest {

    /**
     * 目标商品ID列表
     */
    @NotEmpty(message = "商品ID列表不能为空")
    private List<Long> targetIds;

    /**
     * 调价类型：PERCENTAGE（百分比）、FIXED（固定金额）
     */
    @NotNull(message = "调价类型不能为空")
    private PriceAdjustType adjustType;

    /**
     * 调价值（百分比或固定金额）
     */
    @NotNull(message = "调价值不能为空")
    private BigDecimal adjustValue;

    /**
     * 调价原因
     */
    private String changeReason;

    /**
     * 调价类型枚举
     */
    public enum PriceAdjustType {
        PERCENTAGE,  // 百分比调整
        FIXED        // 固定金额调整
    }
}
