package com.campus.marketplace.common.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 角色详情响应
 *
 * @author BaSui
 * @date 2025-10-29
 */
@Schema(description = "角色详情")
public record RoleDetailResponse(
        @Schema(description = "角色 ID", example = "101")
        Long id,

        @Schema(description = "角色名称", example = "ROLE_ADMIN")
        String name,

        @Schema(description = "角色描述")
        String description,

        @Schema(description = "绑定的权限编码集合")
        Set<String> permissions,

        @Schema(description = "绑定该角色的用户数量", example = "12")
        long userCount,

        @Schema(description = "角色创建时间")
        LocalDateTime createdAt
) {
}
