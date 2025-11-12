package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CreateRoleRequest;
import com.campus.marketplace.common.dto.request.UpdateRoleRequest;
import com.campus.marketplace.common.dto.request.UpdateUserRolesRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.RoleDetailResponse;
import com.campus.marketplace.common.dto.response.RoleSummaryResponse;
import com.campus.marketplace.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 *
 * @author BaSui
 * @date 2025-10-29
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "角色管理", description = "系统角色与权限管理接口")
public class RoleAdminController {

    private final RoleService roleService;

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_ROLE_ASSIGN)")
    @Operation(summary = "角色列表", description = "查询所有角色及其概览信息")
    public ApiResponse<List<RoleSummaryResponse>> listRoles() {
        return ApiResponse.success(roleService.listRoles());
    }

    @GetMapping("/roles/{roleId}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_ROLE_ASSIGN)")
    @Operation(summary = "角色详情", description = "根据 ID 查询角色信息与权限集合")
    public ApiResponse<RoleDetailResponse> getRole(
            @Parameter(description = "角色 ID") @PathVariable Long roleId) {
        return ApiResponse.success(roleService.getRole(roleId));
    }

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_ROLE_ASSIGN)")
    @Operation(summary = "创建角色", description = "创建新的角色并绑定权限")
    public ApiResponse<RoleDetailResponse> createRole(
            @Valid @RequestBody CreateRoleRequest request) {
        return ApiResponse.success(roleService.createRole(request));
    }

    @PutMapping("/roles/{roleId}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_ROLE_ASSIGN)")
    @Operation(summary = "更新角色", description = "更新角色描述和权限集合")
    public ApiResponse<RoleDetailResponse> updateRole(
            @Parameter(description = "角色 ID") @PathVariable Long roleId,
            @Valid @RequestBody UpdateRoleRequest request) {
        return ApiResponse.success(roleService.updateRole(roleId, request));
    }

    @DeleteMapping("/roles/{roleId}")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_ROLE_ASSIGN)")
    @Operation(summary = "删除角色", description = "删除角色（若有用户绑定则无法删除）")
    public ApiResponse<Void> deleteRole(
            @Parameter(description = "角色 ID") @PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return ApiResponse.success();
    }

    @PutMapping("/users/{userId}/roles")
    @PreAuthorize("hasAuthority(T(com.campus.marketplace.common.security.PermissionCodes).SYSTEM_ROLE_ASSIGN)")
    @Operation(summary = "更新用户角色", description = "重置指定用户的角色集合")
    public ApiResponse<Void> updateUserRoles(
            @Parameter(description = "用户 ID") @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRolesRequest request) {
        roleService.updateUserRoles(userId, request);
        return ApiResponse.success();
    }
}
