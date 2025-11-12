/**
 * useAuth Hook - 认证状态管理大师！🔐
 * @author BaSui 😎
 * @description 管理用户登录状态、Token 刷新、权限检查
 */

import { useState, useEffect, useCallback } from 'react';
import { tokenStorage } from '../utils/storage';
import { createApi } from '../utils/apiClient';
import type { User as ApiUser } from '../api/models/user';

// 创建 API 客户端实例
const api = createApi();

/**
 * 扩展 API 的 User 类型，添加权限信息
 * @description 基于 OpenAPI 生成的 User 类型，扩展权限相关字段
 */
export interface User extends Omit<ApiUser, 'roles'> {
  /**
   * 角色列表（字符串数组，用于权限检查）
   */
  roles?: string[];

  /**
   * 权限列表（字符串数组，用于权限检查）
   */
  permissions?: string[];
}

/**
 * 登录参数接口
 */
export interface LoginParams {
  username: string;
  password: string;
  rememberMe?: boolean;
}

/**
 * useAuth Hook 返回值接口
 */
export interface UseAuthResult {
  /**
   * 当前用户信息
   */
  user: User | null;

  /**
   * 是否已登录
   */
  isAuthenticated: boolean;

  /**
   * 是否正在加载
   */
  loading: boolean;

  /**
   * 登录方法
   */
  login: (params: LoginParams) => Promise<void>;

  /**
   * 登出方法
   */
  logout: () => Promise<void>;

  /**
   * 刷新 Token 方法
   */
  refreshToken: () => Promise<void>;

  /**
   * 检查权限方法
   */
  hasPermission: (permission: string) => boolean;

  /**
   * 检查角色方法
   */
  hasRole: (role: string) => boolean;
}

/**
 * useAuth Hook
 *
 * @description 认证状态管理 Hook，提供登录、登出、Token 刷新、权限检查等功能
 *
 * @example
 * ```tsx
 * function LoginPage() {
 *   const { login, isAuthenticated, loading } = useAuth();
 *
 *   const handleLogin = async () => {
 *     try {
 *       await login({ username: 'admin', password: '123456' });
 *       // 登录成功，跳转到首页
 *       navigate('/');
 *     } catch (error) {
 *       // 登录失败，显示错误提示
 *       toast.error('登录失败！');
 *     }
 *   };
 *
 *   if (isAuthenticated) {
 *     return <Navigate to="/" />;
 *   }
 *
 *   return (
 *     <div>
 *       <Button onClick={handleLogin} loading={loading}>
 *         登录
 *       </Button>
 *     </div>
 *   );
 * }
 * ```
 *
 * @example
 * ```tsx
 * function ProtectedPage() {
 *   const { user, hasPermission, logout } = useAuth();
 *
 *   if (!hasPermission('admin.users.view')) {
 *     return <div>无权访问</div>;
 *   }
 *
 *   return (
 *     <div>
 *       <h1>欢迎，{user?.nickname}！</h1>
 *       <Button onClick={logout}>退出登录</Button>
 *     </div>
 *   );
 * }
 * ```
 */
export const useAuth = (): UseAuthResult => {
  // 用户信息状态
  const [user, setUser] = useState<User | null>(null);

  // 加载状态
  const [loading, setLoading] = useState(false);

  // 是否已登录
  const isAuthenticated = !!user && !!tokenStorage.getAccessToken();

  /**
   * 获取当前用户信息
   */
  const fetchUser = useCallback(async () => {
    try {
      const token = tokenStorage.getAccessToken();
      if (!token) {
        setUser(null);
        return;
      }

      setLoading(true);
      // 调用后端 API 获取用户信息
      const response = await api.getCurrentUserProfile();
      setUser(response.data.data as any);
    } catch (error) {
      console.error('获取用户信息失败:', error);
      setUser(null);
      tokenStorage.clearTokens();
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * 登录方法
   */
  const login = useCallback(async (params: LoginParams) => {
    try {
      setLoading(true);

      // 调用后端登录 API
      const response = await api.login({
        loginRequest: {
          username: params.username,
          password: params.password,
        },
      });

      // 保存 Token
      const data = response.data.data as any;
      tokenStorage.setTokens(data.accessToken, data.refreshToken);

      // 获取用户信息
      await fetchUser();
    } catch (error) {
      console.error('登录失败:', error);
      throw error;
    } finally {
      setLoading(false);
    }
  }, [fetchUser]);

  /**
   * 登出方法
   */
  const logout = useCallback(async () => {
    try {
      setLoading(true);

      // 调用后端登出 API（Token 通过 Header 自动传递）
      await api.logout({} as any);
    } catch (error) {
      console.error('登出失败:', error);
    } finally {
      // 清除 Token 和用户信息
      tokenStorage.clearTokens();
      setUser(null);
      setLoading(false);
    }
  }, []);

  /**
   * 刷新 Token 方法
   */
  const refreshToken = useCallback(async () => {
    try {
      const refreshTokenValue = tokenStorage.getRefreshToken();
      if (!refreshTokenValue) {
        throw new Error('Refresh token 不存在');
      }

      // 调用后端刷新 Token API（Token 通过 Header 自动传递）
      const response = await api.refresh({} as any);

      // 保存新的 Token
      const data = response.data.data as any;
      tokenStorage.setTokens(data.accessToken, data.refreshToken);
    } catch (error) {
      console.error('刷新 Token 失败:', error);
      // 刷新失败，清除 Token 和用户信息
      tokenStorage.clearTokens();
      setUser(null);
      throw error;
    }
  }, []);

  /**
   * 检查权限方法
   */
  const hasPermission = useCallback((permission: string): boolean => {
    if (!user || !user.permissions) {
      return false;
    }
    return user.permissions.includes(permission);
  }, [user]);

  /**
   * 检查角色方法
   */
  const hasRole = useCallback((role: string): boolean => {
    if (!user || !user.roles) {
      return false;
    }
    return user.roles.includes(role);
  }, [user]);

  /**
   * 组件挂载时，自动获取用户信息
   */
  useEffect(() => {
    const token = tokenStorage.getAccessToken();
    if (token && !user) {
      fetchUser();
    }
  }, [fetchUser, user]);

  return {
    user,
    isAuthenticated,
    loading,
    login,
    logout,
    refreshToken,
    hasPermission,
    hasRole,
  };
};

export default useAuth;
