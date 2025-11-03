package com.campus.marketplace.common.dto.request;

import com.campus.marketplace.common.enums.DisputeType;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * 创建纠纷请求DTO
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateDisputeRequest {

    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotNull(message = "纠纷类型不能为空")
    private DisputeType disputeType;

    @NotBlank(message = "纠纷描述不能为空")
    @Size(min = 20, max = 1000, message = "纠纷描述长度必须在20-1000字符之间")
    private String description;
}
