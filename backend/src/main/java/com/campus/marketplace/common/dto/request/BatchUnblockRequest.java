package com.campus.marketplace.common.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量解除黑名单请求
 *
 * @author BaSui 😎
 * @date 2025-11-07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "批量解除黑名单请求")
public class BatchUnblockRequest {

    @NotEmpty(message = "黑名单ID列表不能为空")
    @Schema(description = "黑名单记录ID列表", example = "[101, 102, 103]", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Long> blacklistIds;
}
