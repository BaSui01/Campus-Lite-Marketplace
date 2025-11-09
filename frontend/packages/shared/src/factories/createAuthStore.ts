/**
 * 通用认证 Store 工厂
 * @description 支持多端（Portal/Admin）复用的认证状态管理
 */

import { create } from 'zustand';
import type { StateCreator } from 'zustand';
import { persist } from 'zustand/middleware';
import {
  setTokens,
  clearTokens,
  hasToken,
} from '../utils/apiClient';
import { getAccessToken } from '../utils/tokenUtils';

// ==================== 类型定义 ====================

/** 登录结果 */
export interface AuthLoginResult<U> {
  /** 访问令牌 */
  accessToken: string;
  /** 刷新令牌，可为空 */
  refreshToken?: string | null;
  /** 用户信息 */
  user: U;
}

/** 刷新 Token 结果 */
export interface RefreshTokenResult {
  accessToken: string;
  refreshToken?: string | null;
}

/** 权限配置 */
export interface PermissionConfig<U> {
  getPermissions?: (user: U) => string[] | undefined;
  getRoles?: (user: U) => string[] | undefined;
}

/** Store 配置项 */
export interface AuthStoreOptions<U, LoginParams> {
  /** localStorage 持久化的键名 */
  storageKey?: string;
  /** 是否启用持久化（默认启用） */
  persist?: boolean;
  /** 登录方法（必须返回 AccessToken、用户信息等） */
  login: (params: LoginParams) => Promise<AuthLoginResult<U>>;
  /** 登出方法（可选） */
  logout?: () => Promise<void>;
  /** 刷新 Token 方法（可选） */
  refreshToken?: (refreshToken: string | null) => Promise<RefreshTokenResult | void>;
  /** 拉取用户资料的方法（可选，用于刷新页面后恢复状态） */
  fetchProfile?: () => Promise<U>;
  /** 权限/角色提取配置 */
  permissionConfig?: PermissionConfig<U>;
  /** 登录成功回调 */
  onLoginSuccess?: (user: U) => void;
  /** 登出成功回调 */
  onLogoutSuccess?: () => void;
  /** 初始化状态（可选） */
  initialState?: Partial<Pick<AuthStoreState<U, LoginParams>, 'user' | 'token' | 'accessToken' | 'refreshToken' | 'isAuthenticated'>>;
}

/** Store 状态定义 */
export interface AuthStoreState<U, LoginParams> {
  /** 访问令牌（兼容旧字段） */
  token: string | null;
  /** 访问令牌 */
  accessToken: string | null;
  /** 刷新令牌 */
  refreshToken: string | null;
  /** 当前用户 */
  user: U | null;
  /** 是否已登录 */
  isAuthenticated: boolean;
  /** 加载状态（登录、刷新、恢复） */
  isLoading: boolean;
  /** 执行登录 */
  login: (params: LoginParams) => Promise<void>;
  /** 执行登出 */
  logout: () => Promise<void>;
  /** 刷新访问令牌 */
  refreshAccessToken: () => Promise<void>;
  /** 更新用户信息 */
  updateUser: (patch: Partial<U>) => void;
  /** 直接设置用户信息 */
  setUser: (user: U | null) => void;
  /** 从本地存储恢复状态 */
  initFromStorage: () => Promise<void>;
  /** 权限校验：是否拥有某权限 */
  hasPermission: (permission: string) => boolean;
  /** 权限校验：是否拥有任一权限 */
  hasAnyPermission: (permissions: string[]) => boolean;
  /** 角色校验 */
  hasRole: (role: string) => boolean;
}

// ==================== 工具方法 ====================

const normalizeStringArray = (input: unknown): string[] => {
  if (!input) return [];
  if (Array.isArray(input)) {
    return input
      .map((item) => {
        if (!item) return null;
        if (typeof item === 'string') return item;
        if (typeof item === 'object') {
          const candidate = (item as Record<string, unknown>).code
            ?? (item as Record<string, unknown>).name
            ?? (item as Record<string, unknown>).value;
          return typeof candidate === 'string' ? candidate : null;
        }
        return String(item);
      })
      .filter((value): value is string => !!value);
  }
  if (typeof input === 'string') return [input];
  return [];
};

const createPermissionExtractor = <U>(
  config?: PermissionConfig<U>
) => {
  return (user: U | null): string[] => {
    if (!user) return [];

    if (config?.getPermissions) {
      const custom = config.getPermissions(user);
      if (custom && custom.length > 0) {
        return Array.from(new Set(custom));
      }
    }

    const direct = normalizeStringArray((user as never as Record<string, unknown>).permissions);
    if (direct.length > 0) {
      return Array.from(new Set(direct));
    }

    if (config?.getRoles) {
      const rolePermissions = config.getRoles(user)
        ?.flatMap((role) => normalizeStringArray(
          (role as never as Record<string, unknown>)?.permissions ?? (role as unknown)
        )) ?? [];
      if (rolePermissions.length > 0) {
        return Array.from(new Set(rolePermissions));
      }
    }

    const roles = normalizeStringArray((user as never as Record<string, unknown>).roles);
    if (roles.length > 0) {
      return Array.from(new Set(roles));
    }

    return [];
  };
};

const createRoleExtractor = <U>(
  config?: PermissionConfig<U>
) => {
  return (user: U | null): string[] => {
    if (!user) return [];

    if (config?.getRoles) {
      const custom = config.getRoles(user);
      if (custom && custom.length > 0) {
        return Array.from(new Set(custom));
      }
    }

    const direct = normalizeStringArray((user as never as Record<string, unknown>).roles);
    if (direct.length > 0) {
      return Array.from(new Set(direct));
    }

    return [];
  };
};

const ensureToken = (value: string | null | undefined): string | null => {
  if (value && typeof value === 'string' && value.trim().length > 0) {
    return value;
  }
  return null;
};

// ==================== 工厂实现 ====================

export const createAuthStore = <U, LoginParams = Record<string, unknown>>(
  options: AuthStoreOptions<U, LoginParams>
) => {
  const {
    storageKey = 'auth-storage',
    persist: enablePersist = true,
    login,
    logout,
    refreshToken,
    fetchProfile,
    permissionConfig,
    onLoginSuccess,
    onLogoutSuccess,
    initialState,
  } = options;

  const extractPermissions = createPermissionExtractor(permissionConfig);
  const extractRoles = createRoleExtractor(permissionConfig);

  const baseCreator: StateCreator<AuthStoreState<U, LoginParams>> = (set, get) => ({
    token: ensureToken(initialState?.token ?? initialState?.accessToken ?? null),
    accessToken: ensureToken(initialState?.accessToken ?? initialState?.token ?? null),
    refreshToken: ensureToken(initialState?.refreshToken ?? null),
    user: initialState?.user ?? null,
    isAuthenticated: initialState?.isAuthenticated ?? false,
    isLoading: false,

    login: async (params: LoginParams) => {
      set({ isLoading: true });
      try {
        const result = await login(params);
        const accessToken = ensureToken(result.accessToken);
        const refreshTokenValue = ensureToken(result.refreshToken ?? null);

        if (!accessToken) {
          throw new Error('登录失败：未返回访问令牌');
        }

        setTokens(accessToken, refreshTokenValue ?? undefined);

        set({
          token: accessToken,
          accessToken,
          refreshToken: refreshTokenValue,
          user: result.user,
          isAuthenticated: true,
          isLoading: false,
        });

        onLoginSuccess?.(result.user);
      } catch (error) {
        set({ isLoading: false });
        clearTokens();
        set({
          token: null,
          accessToken: null,
          refreshToken: null,
          user: null,
          isAuthenticated: false,
        });
        throw error;
      }
    },

    logout: async () => {
      try {
        if (logout) {
          await logout();
        }
      } finally {
        clearTokens();
        set({
          token: null,
          accessToken: null,
          refreshToken: null,
          user: null,
          isAuthenticated: false,
        });
        onLogoutSuccess?.();
      }
    },

    refreshAccessToken: async () => {
      if (!refreshToken) {
        throw new Error('未提供刷新 Token 方法');
      }

      const currentRefreshToken = get().refreshToken;
      if (!currentRefreshToken) {
        throw new Error('刷新令牌不存在');
      }

      set({ isLoading: true });
      try {
        const result = await refreshToken(currentRefreshToken);
        if (!result || !result.accessToken) {
          throw new Error('刷新 Token 失败');
        }

        const accessToken = ensureToken(result.accessToken);
        const refreshTokenValue = ensureToken(result.refreshToken ?? currentRefreshToken);

        if (!accessToken) {
          throw new Error('刷新 Token 失败：无有效访问令牌');
        }

        setTokens(accessToken, refreshTokenValue ?? undefined);

        set({
          token: accessToken,
          accessToken,
          refreshToken: refreshTokenValue,
          isAuthenticated: true,
          isLoading: false,
        });
      } catch (error) {
        set({ isLoading: false });
        clearTokens();
        set({
          token: null,
          accessToken: null,
          refreshToken: null,
          user: null,
          isAuthenticated: false,
        });
        throw error;
      }
    },

    updateUser: (patch: Partial<U>) => {
      const currentUser = get().user;
      if (!currentUser) return;

      set({
        user: { ...currentUser, ...patch },
      });
    },

    setUser: (user: U | null) => {
      const token = get().token ?? getAccessToken();
      set({
        user,
        isAuthenticated: !!token && !!user,
        token: token,
        accessToken: token,
      });
    },

    initFromStorage: async () => {
      const state = get();
      const storedToken = state.token ?? state.accessToken ?? getAccessToken();

      if (storedToken) {
        set({
          token: storedToken,
          accessToken: storedToken,
          isAuthenticated: true,
        });

        if (!state.user && fetchProfile) {
          set({ isLoading: true });
          try {
            const profile = await fetchProfile();
            set({
              user: profile,
              isAuthenticated: true,
            });
          } catch (error) {
            clearTokens();
            set({
              token: null,
              accessToken: null,
              refreshToken: null,
              user: null,
              isAuthenticated: false,
            });
            throw error;
          } finally {
            set({ isLoading: false });
          }
        }
      } else if (!hasToken()) {
        set({
          token: null,
          accessToken: null,
          refreshToken: null,
          user: null,
          isAuthenticated: false,
        });
      }
    },

    hasPermission: (permission: string) => {
      if (!permission) return false;
      const permissions = extractPermissions(get().user);
      return permissions.includes(permission);
    },

    hasAnyPermission: (permissions: string[]) => {
      if (!permissions || permissions.length === 0) return false;
      const userPermissions = extractPermissions(get().user);
      if (userPermissions.length === 0) return false;
      return permissions.some((item) => userPermissions.includes(item));
    },

    hasRole: (role: string) => {
      if (!role) return false;
      const roles = extractRoles(get().user);
      return roles.includes(role);
    },
  });

  if (!enablePersist) {
    return create<AuthStoreState<U, LoginParams>>(baseCreator);
  }

  return create<AuthStoreState<U, LoginParams>>()(
    persist(baseCreator, {
      name: storageKey,
      partialize: (state) => ({
        token: state.token,
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        user: state.user,
        isAuthenticated: state.isAuthenticated,
      }),
    })
  );
};
