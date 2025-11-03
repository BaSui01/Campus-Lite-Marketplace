package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 提出解决方案请求DTO
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProposeDisputeRequest {

    @NotNull(message = "纠纷ID不能为空")
    private Long disputeId;

    @NotBlank(message = "方案内容不能为空")
    @Size(min = 10, max = 1000, message = "方案内容长度必须在10-1000字符之间")
    private String content;

    @NotNull(message = "提议退款金额不能为空")
    @DecimalMin(value = "0.00", message = "退款金额不能为负数")
    private BigDecimal proposedRefundAmount;
}
