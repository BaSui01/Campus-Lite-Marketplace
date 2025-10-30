package com.campus.marketplace.common.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 角色概览响应
 *
 * @author BaSui
 * @date 2025-10-29
 */
@Schema(description = "角色概览")
public record RoleSummaryResponse(
        @Schema(description = "角色 ID", example = "101")
        Long id,

        @Schema(description = "角色名称", example = "ROLE_ADMIN")
        String name,

        @Schema(description = "角色描述")
        String description,

        @Schema(description = "关联权限数量", example = "12")
        int permissionCount,

        @Schema(description = "绑定用户数量", example = "5")
        long userCount,

        @Schema(description = "是否为内置角色", example = "true")
        boolean builtIn
) {
}
