package com.campus.marketplace.common.dto.request;

import com.campus.marketplace.common.enums.PaymentMethod;
import com.campus.marketplace.common.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 支付筛选请求参数
 *
 * @author BaSui 😎
 * @date 2025-11-10
 * @description 支付列表查询的筛选参数，继承通用筛选基类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "支付筛选请求参数")
public class PaymentFilterRequest extends BaseFilterRequest {

    /**
     * 订单状态（用于支付筛选）
     */
    @Schema(description = "订单状态（PENDING_PAYMENT/PAID/COMPLETED/REFUNDED）", example = "PAID")
    private OrderStatus status;

    /**
     * 支付方式
     */
    @Schema(description = "支付方式（WECHAT/ALIPAY/POINTS）", example = "WECHAT")
    private PaymentMethod paymentMethod;

    /**
     * 订单号（精确匹配）
     */
    @Schema(description = "订单号", example = "O202510270001")
    private String orderNo;

    /**
     * 最低金额
     */
    @Schema(description = "最低金额", example = "50.00")
    private BigDecimal minAmount;

    /**
     * 最高金额
     */
    @Schema(description = "最高金额", example = "500.00")
    private BigDecimal maxAmount;
}
