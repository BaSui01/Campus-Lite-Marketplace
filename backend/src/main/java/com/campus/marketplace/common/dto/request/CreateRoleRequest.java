package com.campus.marketplace.common.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * 创建角色请求
 *
 * @author BaSui
 * @date 2025-10-29
 */
@Schema(description = "创建角色请求")
public record CreateRoleRequest(
        @NotBlank(message = "角色名称不能为空")
        @Size(max = 50, message = "角色名称长度不能超过 50 个字符")
        @Schema(description = "角色名称，建议以 ROLE_ 开头", example = "ROLE_AUDITOR")
        String name,

        @Size(max = 200, message = "角色描述长度不能超过 200 个字符")
        @Schema(description = "角色描述", example = "负责审核内容的管理员")
        String description,

        @Schema(description = "权限编码集合，如 system:user:view")
        Set<String> permissions
) {
}
