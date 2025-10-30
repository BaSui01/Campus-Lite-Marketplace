package com.campus.marketplace.common.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * 更新角色请求
 *
 * @author BaSui
 * @date 2025-10-29
 */
@Schema(description = "更新角色请求")
public record UpdateRoleRequest(
        @Size(max = 200, message = "角色描述长度不能超过 200 个字符")
        @Schema(description = "角色描述", example = "更新后的角色说明")
        String description,

        @Schema(description = "新的权限编码集合，若为空则清空权限")
        Set<String> permissions
) {
}
