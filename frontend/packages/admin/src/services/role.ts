/**
 * ✅ 角色与权限管理服务 - 完全重构版
 * @author BaSui 😎
 * @description 基于 OpenAPI 生成的 DefaultApi，零手写路径！
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type {
  RoleSummaryResponse,
  RoleDetailResponse,
  CreateRoleRequest,
  UpdateRoleRequest,
  UpdateUserRolesRequest,
} from '@campus/shared/api';

export class RoleService {
  /**
   * 获取角色列表
   */
  async listRoles(): Promise<RoleSummaryResponse[]> {
    const api = getApi();
    const response = await api.listRoles();
    return response.data.data as RoleSummaryResponse[];
  }

  /**
   * 获取角色详情
   * @param roleId 角色ID
   */
  async getRole(roleId: number): Promise<RoleDetailResponse> {
    const api = getApi();
    const response = await api.getRole({ roleId });
    return response.data.data as RoleDetailResponse;
  }

  /**
   * 创建角色
   * @param payload 创建角色请求参数
   */
  async createRole(payload: CreateRoleRequest): Promise<RoleDetailResponse> {
    const api = getApi();
    const response = await api.createRole({ createRoleRequest: payload });
    return response.data.data as RoleDetailResponse;
  }

  /**
   * 更新角色
   * @param roleId 角色ID
   * @param payload 更新角色请求参数
   */
  async updateRole(roleId: number, payload: UpdateRoleRequest): Promise<RoleDetailResponse> {
    const api = getApi();
    const response = await api.updateRole({ roleId, updateRoleRequest: payload });
    return response.data.data as RoleDetailResponse;
  }

  /**
   * 删除角色
   * @param roleId 角色ID
   */
  async deleteRole(roleId: number): Promise<void> {
    const api = getApi();
    await api.deleteRole({ roleId });
  }

  /**
   * 更新用户角色
   * @param userId 用户ID
   * @param payload 用户角色列表
   */
  async updateUserRoles(userId: number, payload: UpdateUserRolesRequest): Promise<void> {
    const api = getApi();
    await api.updateUserRoles({ userId, updateUserRolesRequest: payload });
  }
}

// 导出单例
export const roleService = new RoleService();
export default roleService;
