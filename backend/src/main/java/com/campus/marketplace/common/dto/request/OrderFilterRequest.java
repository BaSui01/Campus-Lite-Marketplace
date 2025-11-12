package com.campus.marketplace.common.dto.request;

import com.campus.marketplace.common.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 订单筛选请求参数
 *
 * @author BaSui 😎
 * @date 2025-11-10
 * @description 订单列表查询的筛选参数，继承通用筛选基类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "订单筛选请求参数")
public class OrderFilterRequest extends BaseFilterRequest {

    /**
     * 订单状态
     */
    @Schema(description = "订单状态（PENDING_PAYMENT/PAID/SHIPPED/COMPLETED/CANCELLED/REFUNDING/REFUNDED）", example = "PAID")
    private OrderStatus status;

    /**
     * 买家 ID
     */
    @Schema(description = "买家 ID", example = "10001")
    private Long buyerId;

    /**
     * 卖家 ID
     */
    @Schema(description = "卖家 ID", example = "10002")
    private Long sellerId;

    /**
     * 最低金额
     */
    @Schema(description = "最低金额", example = "100.00")
    private BigDecimal minAmount;

    /**
     * 最高金额
     */
    @Schema(description = "最高金额", example = "1000.00")
    private BigDecimal maxAmount;
}
