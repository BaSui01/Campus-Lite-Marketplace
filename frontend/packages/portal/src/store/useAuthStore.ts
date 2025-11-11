/**
 * 认证状态管理
 * @author BaSui 😎
 * @description 使用 Zustand 管理用户认证状态
 */

import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { User, LoginRequest, RegisterRequest } from '@campus/shared';
import { Services } from '@campus/shared';
import { setItem, getItem, removeItem, TOKEN_KEY, REFRESH_TOKEN_KEY } from '@campus/shared';

// 🔧 BaSui 修复：从 Services 命名空间解构 authService
const { authService } = Services;

/**
 * 认证状态接口
 */
interface AuthState {
  /**
   * 当前用户信息
   */
  user: User | null;

  /**
   * 访问令牌
   */
  accessToken: string | null;

  /**
   * 刷新令牌
   */
  refreshToken: string | null;

  /**
   * 是否已登录
   */
  isAuthenticated: boolean;

  /**
   * 是否正在加载
   */
  isLoading: boolean;

  /**
   * 登录
   * @returns 如果需要 2FA，返回 { requires2FA: true, tempToken: string }，否则返回 void
   */
  login: (data: LoginRequest) => Promise<void | { requires2FA: true; tempToken?: string }>;

  /**
   * 注册
   */
  register: (data: RegisterRequest) => Promise<void>;

  /**
   * 登出
   */
  logout: () => Promise<void>;

  /**
   * 刷新令牌
   */
  refreshAccessToken: () => Promise<void>;

  /**
   * 更新用户信息
   */
  updateUser: (user: Partial<User>) => void;

  /**
   * 初始化认证状态（从 LocalStorage 恢复）
   */
  init: () => void;
}

/**
 * 认证状态管理 Store
 */
export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      // ==================== 状态 ====================
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,
      isLoading: false,

      // ==================== 登录 ====================
      login: async (data: LoginRequest) => {
        set({ isLoading: true });

        try {
          const response = await authService.login(data);

          if (response.code === 200 && response.data) {
            // 🔧 BaSui 修复：后端返回的字段名称是 accessToken, refreshToken, userInfo
            // ⚠️ 确保与 OpenAPI 生成的 LoginResponse 类型一致
            const { accessToken, refreshToken, userInfo, requires2FA, tempToken } = response.data;

            // 🔐 检查是否需要 2FA 验证（新增 - BaSui 2025-11-11）
            if (requires2FA) {
              set({ isLoading: false });
              // 返回 2FA 信息而不是抛出错误
              return { requires2FA: true, tempToken };
            }

            // ✅ 验证必需字段
            if (!accessToken) {
              throw new Error('登录失败：未获取到访问令牌');
            }

            if (!refreshToken) {
              throw new Error('登录失败：未获取到刷新令牌');
            }

            if (!userInfo) {
              throw new Error('登录失败：未获取到用户信息');
            }

            // 🔧 BaSui 修复：移除双重存储，只使用 Zustand persist
            // ❌ 旧代码：同时保存到 localStorage 和 Zustand persist，导致数据不一致
            // setItem(TOKEN_KEY, accessToken || '');
            // setItem(REFRESH_TOKEN_KEY, refreshToken || '');

            // ✅ 新代码：只更新 Zustand 状态，由 persist 中间件自动保存到 localStorage
            set({
              user: userInfo as any, // 将 UserInfo 转换为 User
              accessToken: accessToken, // ✅ 保存 accessToken
              refreshToken: refreshToken, // ✅ 保存 refreshToken
              isAuthenticated: true,
              isLoading: false,
            });

            console.log('✅ 登录成功:', userInfo?.username);
            console.log('✅ Access Token 已保存:', accessToken ? '是' : '否');
            console.log('✅ Refresh Token 已保存:', refreshToken ? '是' : '否');
            console.log('✅ Token 已保存到 Zustand persist（15分钟有效）');
          } else {
            throw new Error(response.message || '登录失败');
          }
        } catch (error: any) {
          set({ isLoading: false });
          console.error('❌ 登录失败:', error);
          throw error;
        }
      },

      // ==================== 注册 ====================
      register: async (data: RegisterRequest) => {
        set({ isLoading: true });

        try {
          const response = await authService.register(data);

          if (response.code === 200 && response.data) {
            const { user, accessToken, refreshToken } = response.data;

            // 🔧 BaSui 修复：移除双重存储，只使用 Zustand persist
            // ❌ 旧代码：同时保存到 localStorage 和 Zustand persist
            // setItem(TOKEN_KEY, accessToken);
            // setItem(REFRESH_TOKEN_KEY, refreshToken);

            // ✅ 新代码：只更新 Zustand 状态，由 persist 中间件自动保存
            set({
              user,
              accessToken,
              refreshToken,
              isAuthenticated: true,
              isLoading: false,
            });

            console.log('✅ 注册成功:', user.username);
            console.log('✅ Token 已保存到 Zustand persist');
          } else {
            throw new Error(response.message || '注册失败');
          }
        } catch (error: any) {
          set({ isLoading: false });
          console.error('❌ 注册失败:', error);
          throw error;
        }
      },

      // ==================== 登出 ====================
      logout: async () => {
        try {
          // 调用登出接口
          await authService.logout();
        } catch (error) {
          console.error('登出接口调用失败:', error);
        } finally {
          // 🔧 BaSui 修复：移除手动清除 localStorage，由 Zustand persist 自动管理
          // ❌ 旧代码：手动清除 localStorage
          // removeItem(TOKEN_KEY);
          // removeItem(REFRESH_TOKEN_KEY);

          // ✅ 新代码：只清除 Zustand 状态，persist 中间件会自动同步到 localStorage
          set({
            user: null,
            accessToken: null,
            refreshToken: null,
            isAuthenticated: false,
          });

          console.log('✅ 已登出');
        }
      },

      // ==================== 刷新令牌 ====================
      refreshAccessToken: async () => {
        const { refreshToken } = get();

        if (!refreshToken) {
          throw new Error('刷新令牌不存在');
        }

        try {
          const response = await authService.refreshToken({ refreshToken });

          if (response.code === 200 && response.data) {
            const { accessToken: newAccessToken, refreshToken: newRefreshToken } = response.data;

            // 🔧 BaSui 修复：移除双重存储，只使用 Zustand persist
            // ❌ 旧代码：手动保存到 localStorage
            // setItem(TOKEN_KEY, newAccessToken);
            // setItem(REFRESH_TOKEN_KEY, newRefreshToken);

            // ✅ 新代码：只更新 Zustand 状态，persist 中间件会自动保存
            set({
              accessToken: newAccessToken,
              refreshToken: newRefreshToken,
            });

            console.log('✅ 令牌刷新成功（已保存到 Zustand persist）');
          } else {
            throw new Error(response.message || '令牌刷新失败');
          }
        } catch (error: any) {
          console.error('❌ 令牌刷新失败:', error);
          // 刷新失败，清除认证状态
          get().logout();
          throw error;
        }
      },

      // ==================== 更新用户信息 ====================
      updateUser: (userData: Partial<User>) => {
        set((state) => ({
          user: state.user ? { ...state.user, ...userData } : null,
        }));
      },

      // ==================== 初始化 ====================
      init: () => {
        // 🔧 BaSui 修复：从 Zustand persist 恢复状态，不需要手动读取 localStorage
        // ❌ 旧代码：手动从 localStorage 读取 Token
        // const accessToken = getItem(TOKEN_KEY);
        // const refreshToken = getItem(REFRESH_TOKEN_KEY);

        // ✅ 新代码：Zustand persist 中间件会自动恢复状态
        // 只需要检查当前状态是否已认证
        const { accessToken, refreshToken, isAuthenticated } = get();

        if (accessToken && refreshToken && isAuthenticated) {
          // 获取当前用户信息（如果还没有）
          if (!get().user) {
            authService
              .getCurrentUser()
              .then((response) => {
                if (response.code === 200 && response.data) {
                  set({ user: response.data });
                  console.log('✅ 用户信息已恢复:', response.data.username);
                }
              })
              .catch((error) => {
                console.error('❌ 获取用户信息失败:', error);
                // 获取失败，清除认证状态
                get().logout();
              });
          }
        }
      },
    }),
    {
      name: 'auth-storage', // LocalStorage key
      partialize: (state) => ({
        // 只持久化这些字段
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        user: state.user,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);

export default useAuthStore;
