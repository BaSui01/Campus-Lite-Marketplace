package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.BanUserRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 🎯 BaSui 的管理员用户管理控制器 - 专注用户管理，统计功能已迁移！😎
 *
 * 功能范围：
 * - 👮 用户管理：封禁/解封用户、自动解封过期用户
 *
 * ⚠️ 注意：
 * - 统计相关接口已迁移到 {@link AdminStatisticsController}
 * - 所有接口仅管理员可访问
 *
 * @author BaSui
 * @date 2025-10-29
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "管理员用户管理", description = "管理员后台用户管理相关接口")
public class AdminController {

    private final UserService userService;

    // ========== 用户管理模块 ==========

    @PostMapping("/users/ban")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_USER_BAN)")
    @Operation(summary = "封禁用户", description = "管理员封禁违规用户")
    public ApiResponse<Void> banUser(@Valid @RequestBody BanUserRequest request) {
        userService.banUser(request);
        return ApiResponse.success(null);
    }

    @PostMapping("/users/{userId}/unban")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_USER_BAN)")
    @Operation(summary = "解封用户", description = "管理员解封用户")
    public ApiResponse<Void> unbanUser(
            @Parameter(description = "用户 ID", example = "10002") @PathVariable Long userId
    ) {
        userService.unbanUser(userId);
        return ApiResponse.success(null);
    }

    @PostMapping("/users/auto-unban")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_USER_BAN)")
    @Operation(summary = "自动解封过期用户", description = "定时任务：自动解封封禁已过期的用户")
    public ApiResponse<Integer> autoUnbanExpiredUsers() {
        int count = userService.autoUnbanExpiredUsers();
        return ApiResponse.success(count);
    }
}
