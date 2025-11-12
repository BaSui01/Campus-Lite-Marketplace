package com.campus.marketplace.common.dto.request;

import com.campus.marketplace.common.enums.DisputeStatus;
import com.campus.marketplace.common.enums.DisputeType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 纠纷筛选请求参数
 *
 * @author BaSui 😎
 * @date 2025-11-10
 * @description 纠纷列表查询的筛选参数，继承通用筛选基类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "纠纷筛选请求参数")
public class DisputeFilterRequest extends BaseFilterRequest {

    /**
     * 纠纷类型
     */
    @Schema(description = "纠纷类型（REFUND/QUALITY/DELIVERY/OTHER）", example = "REFUND")
    private DisputeType disputeType;

    /**
     * 纠纷状态
     */
    @Schema(description = "纠纷状态（NEGOTIATING/PENDING_ARBITRATION/ARBITRATING/RESOLVED/CANCELLED）", example = "NEGOTIATING")
    private DisputeStatus status;

    /**
     * 仲裁员ID
     */
    @Schema(description = "仲裁员ID", example = "1")
    private Long arbitratorId;

    /**
     * 最小金额
     */
    @Schema(description = "最小金额", example = "0")
    private BigDecimal minAmount;

    /**
     * 最大金额
     */
    @Schema(description = "最大金额", example = "10000")
    private BigDecimal maxAmount;
}
