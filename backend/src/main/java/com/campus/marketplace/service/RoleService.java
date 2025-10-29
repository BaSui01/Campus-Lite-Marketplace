package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateRoleRequest;
import com.campus.marketplace.common.dto.request.UpdateRoleRequest;
import com.campus.marketplace.common.dto.request.UpdateUserRolesRequest;
import com.campus.marketplace.common.dto.response.RoleDetailResponse;
import com.campus.marketplace.common.dto.response.RoleSummaryResponse;

import java.util.List;

/**
 * 角色管理服务
 *
 * @author BaSui
 * @date 2025-10-29
 */
public interface RoleService {

    /**
     * 新建角色并绑定权限。
     */
    RoleDetailResponse createRole(CreateRoleRequest request);

    /**
     * 更新角色描述及权限集合。
     */
    RoleDetailResponse updateRole(Long roleId, UpdateRoleRequest request);

    /**
     * 删除角色（若仍有用户绑定则抛出异常）。
     */
    void deleteRole(Long roleId);

    /**
     * 查询角色详情。
     */
    RoleDetailResponse getRole(Long roleId);

    /**
     * 列出所有角色概览。
     */
    List<RoleSummaryResponse> listRoles();

    /**
     * 替换指定用户的角色集合。
     */
    void updateUserRoles(Long userId, UpdateUserRolesRequest request);
}
