package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * 响应解决方案请求DTO
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespondProposalRequest {

    @NotNull(message = "方案ID不能为空")
    private Long proposalId;

    @NotNull(message = "是否接受不能为空")
    private Boolean accepted;

    @Size(max = 500, message = "响应说明长度不能超过500字符")
    private String responseNote;
}
