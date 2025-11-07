package com.campus.marketplace.common.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 🏫 BaSui 的校园创建请求 - 新增校园用！😎
 *
 * @author BaSui
 * @date 2025-11-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建校园请求")
public class CampusCreateRequest {

    @Schema(description = "校园编码（唯一）", example = "CAMPUS_001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "校园编码不能为空")
    @Size(max = 50, message = "校园编码长度不能超过50")
    private String code;

    @Schema(description = "校园名称", example = "北京大学", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "校园名称不能为空")
    @Size(max = 100, message = "校园名称长度不能超过100")
    private String name;
}
