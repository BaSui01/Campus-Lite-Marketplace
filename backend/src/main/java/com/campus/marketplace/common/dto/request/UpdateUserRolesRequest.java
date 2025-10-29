package com.campus.marketplace.common.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

/**
 * 更新用户角色请求
 *
 * @author BaSui
 * @date 2025-10-29
 */
@Schema(description = "更新用户角色请求")
public record UpdateUserRolesRequest(
        @NotNull(message = "角色集合不能为空")
        @Schema(description = "角色名称集合，例如 [\"ROLE_ADMIN\", \"ROLE_STUDENT\"]")
        Set<String> roles
) {
}
