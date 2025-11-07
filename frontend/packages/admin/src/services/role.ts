/**
 * ⚠️ 警告：此文件仍使用手写 API 路径（http.get/post/put/delete）
 * 🔧 需要重构：将所有 http. 调用替换为 getApi() + DefaultApi 方法
 * 📋 参考：frontend/packages/shared/src/services/order.ts（已完成重构）
 * 👉 重构步骤：
 *    1. 找到对应的 OpenAPI 生成的方法名（在 api/api/default-api.ts）
 *    2. 替换为：const api = getApi(); api.methodName(...)
 *    3. 更新返回值类型
 */
/**
 * 角色与权限管理服务（管理端）
 */

import { getApi } from '../utils/apiClient';
import type { ApiResponse } from '../types';

export interface RoleSummary {
  id: number;
  name: string;
  description?: string;
  permissionCount: number;
  userCount: number;
  builtIn: boolean;
}

export interface RoleDetail {
  id: number;
  name: string;
  description?: string;
  permissions: string[];
  userCount: number;
  createdAt: string;
}

export interface CreateRolePayload {
  name: string;
  description?: string;
  permissions: string[];
}

export interface UpdateRolePayload {
  description?: string;
  permissions: string[];
}

export interface UpdateUserRolesPayload {
  roles: string[];
}

export class RoleService {
  async listRoles(): Promise<RoleSummary[]> {
    const res = await http.get<ApiResponse<RoleSummary[]>>('/api/admin/roles');
    return res.data;
  }

  async getRole(roleId: number): Promise<RoleDetail> {
    const res = await http.get<ApiResponse<RoleDetail>>(`/api/admin/roles/${roleId}`);
    return res.data;
  }

  async createRole(payload: CreateRolePayload): Promise<RoleDetail> {
    const res = await http.post<ApiResponse<RoleDetail>>('/api/admin/roles', payload);
    return res.data;
  }

  async updateRole(roleId: number, payload: UpdateRolePayload): Promise<RoleDetail> {
    const res = await http.put<ApiResponse<RoleDetail>>(`/api/admin/roles/${roleId}`, payload);
    return res.data;
  }

  async deleteRole(roleId: number): Promise<void> {
    await http.delete<ApiResponse<void>>(`/api/admin/roles/${roleId}`);
  }

  async updateUserRoles(userId: number, payload: UpdateUserRolesPayload): Promise<void> {
    await http.put<ApiResponse<void>>(`/api/admin/users/${userId}/roles`, payload);
  }
}

export const roleService = new RoleService();
export default roleService;
