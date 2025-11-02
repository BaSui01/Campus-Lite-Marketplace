/**
 * 管理端认证 Store（基于共享 createAuthStore）
 */

import {
  createAuthStore,
  authService,
  type LoginRequest,
  type LoginResponse,
  type ApiResponse,
} from '@campus/shared';

type RawUser = LoginResponse['user'] & {
  permissions?: unknown;
  roles?: unknown;
};

export type AdminUser = RawUser & {
  permissions: string[];
  roles: string[];
};

const toStringArray = (input: unknown): string[] => {
  if (!input) {
    return [];
  }
  if (Array.isArray(input)) {
    return input
      .map((item) => {
        if (!item) return null;
        if (typeof item === 'string') return item;
        if (typeof item === 'object') {
          const candidate =
            (item as Record<string, unknown>).code ??
            (item as Record<string, unknown>).name ??
            (item as Record<string, unknown>).value;
          return typeof candidate === 'string' ? candidate : null;
        }
        return String(item);
      })
      .filter((value): value is string => !!value);
  }
  if (typeof input === 'string') {
    return [input];
  }
  return [];
};

const extractPermissionsFromRoles = (roles: unknown): string[] => {
  if (!Array.isArray(roles)) return [];

  const permissions = roles.flatMap((role) => {
    if (!role || typeof role !== 'object') return [];
    const rawPermissions = (role as Record<string, unknown>).permissions;
    return toStringArray(rawPermissions);
  });

  return permissions;
};

const normalizeUser = (user: RawUser): AdminUser => {
  const roleCodes = toStringArray(user.roles);
  const permissionCodes = Array.from(
    new Set([
      ...toStringArray(user.permissions),
      ...extractPermissionsFromRoles(user.roles),
    ])
  );

  return {
    ...user,
    roles: roleCodes,
    permissions: permissionCodes,
  };
};

export const useAuthStore = createAuthStore<AdminUser, LoginRequest>({
  storageKey: 'admin-auth-storage',

  login: async (params) => {
    const response: ApiResponse<LoginResponse> = await authService.login(params);

    if (response.code !== 200 || !response.data) {
      throw new Error(response.message || '登录失败');
    }

    const { accessToken, refreshToken, user } = response.data;

    if (!accessToken) {
      throw new Error('登录失败：未获取到访问令牌');
    }

    return {
      accessToken,
      refreshToken,
      user: normalizeUser(user as RawUser),
    };
  },

  logout: async () => {
    try {
      await authService.logout();
    } catch (error) {
      console.warn('登出接口调用失败，忽略错误继续清理本地状态', error);
    }
  },

  permissionConfig: {
    getPermissions: (user) => user?.permissions ?? [],
    getRoles: (user) => user?.roles ?? [],
  },
});
