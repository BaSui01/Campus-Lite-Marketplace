/**
 * 角色与权限管理服务（管理端）
 */

import { http } from '../utils/http';
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
