package com.campus.marketplace.common.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 🚩 BaSui 的功能开关更新请求 - 更新开关配置！😎
 *
 * @author BaSui
 * @date 2025-11-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "功能开关更新请求")
public class FeatureFlagUpdateRequest {

    @Schema(description = "功能描述", example = "新版UI界面功能（更新版）")
    @Size(max = 255, message = "功能描述长度不能超过255")
    private String description;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "规则配置（JSON格式）", example = "{\"allowEnvs\":[\"dev\",\"test\"],\"allowUserIds\":[1,2,3],\"allowCampusIds\":[10,20]}")
    private String rulesJson;
}
