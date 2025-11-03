package com.campus.marketplace.common.dto.request;

import com.campus.marketplace.common.enums.ArbitrationResult;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 仲裁纠纷请求DTO
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArbitrateDisputeRequest {

    @NotNull(message = "纠纷ID不能为空")
    private Long disputeId;

    @NotNull(message = "仲裁结果不能为空")
    private ArbitrationResult result;

    @DecimalMin(value = "0.00", message = "退款金额不能为负数")
    private BigDecimal refundAmount;

    @NotBlank(message = "仲裁理由不能为空")
    @Size(min = 20, max = 2000, message = "仲裁理由长度必须在20-2000字符之间")
    private String reason;

    @Size(max = 1000, message = "买家证据分析长度不能超过1000字符")
    private String buyerEvidenceAnalysis;

    @Size(max = 1000, message = "卖家证据分析长度不能超过1000字符")
    private String sellerEvidenceAnalysis;
}
