package com.campus.marketplace.common.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 🚩 BaSui 的功能开关创建请求 - 创建新开关！😎
 *
 * @author BaSui
 * @date 2025-11-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "功能开关创建请求")
public class FeatureFlagCreateRequest {

    @NotBlank(message = "功能开关Key不能为空")
    @Size(max = 128, message = "功能开关Key长度不能超过128")
    @Schema(description = "功能开关Key（唯一标识）", example = "NEW_UI_FEATURE", required = true)
    private String key;

    @Schema(description = "功能描述", example = "新版UI界面功能")
    @Size(max = 255, message = "功能描述长度不能超过255")
    private String description;

    @Schema(description = "是否启用", example = "false")
    @Builder.Default
    private Boolean enabled = false;

    @Schema(description = "规则配置（JSON格式）", example = "{\"allowEnvs\":[\"dev\"],\"allowUserIds\":[1,2,3]}")
    private String rulesJson;
}
