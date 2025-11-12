package com.campus.marketplace.common.dto.request;

import com.campus.marketplace.common.enums.RefundStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 退款筛选请求参数
 *
 * @author BaSui 😎
 * @date 2025-11-10
 * @description 退款列表查询的筛选参数，继承通用筛选基类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "退款筛选请求参数")
public class RefundFilterRequest extends BaseFilterRequest {

    /**
     * 退款状态
     */
    @Schema(description = "退款状态（APPLIED/APPROVED/REJECTED/REFUNDED）", example = "APPLIED")
    private RefundStatus status;

    /**
     * 订单号（精确匹配）
     */
    @Schema(description = "订单号", example = "O202510270001")
    private String orderNo;
}
