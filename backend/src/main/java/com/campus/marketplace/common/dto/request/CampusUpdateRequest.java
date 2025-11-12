package com.campus.marketplace.common.dto.request;

import com.campus.marketplace.common.enums.CampusStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 🏫 BaSui 的校园更新请求 - 修改校园信息！😎
 *
 * @author BaSui
 * @date 2025-11-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新校园请求")
public class CampusUpdateRequest {

    @Schema(description = "校园名称", example = "北京大学", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "校园名称不能为空")
    @Size(max = 100, message = "校园名称长度不能超过100")
    private String name;

    @Schema(description = "校园状态", example = "ACTIVE", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "校园状态不能为空")
    private CampusStatus status;
}
