/**
 * 认证状态管理
 * @author BaSui 😎
 * @description 使用 Zustand 管理用户认证状态
 */

import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { User, LoginRequest, RegisterRequest } from '@campus/shared';
import { authService, setItem, getItem, removeItem, TOKEN_KEY, REFRESH_TOKEN_KEY } from '@campus/shared';

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
   */
  login: (data: LoginRequest) => Promise<void>;

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
            const { user, accessToken, refreshToken } = response.data;

            // 保存到 LocalStorage
            setItem(TOKEN_KEY, accessToken);
            setItem(REFRESH_TOKEN_KEY, refreshToken);

            // 更新状态
            set({
              user,
              accessToken,
              refreshToken,
              isAuthenticated: true,
              isLoading: false,
            });

            console.log('✅ 登录成功:', user.username);
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

            // 保存到 LocalStorage
            setItem(TOKEN_KEY, accessToken);
            setItem(REFRESH_TOKEN_KEY, refreshToken);

            // 更新状态
            set({
              user,
              accessToken,
              refreshToken,
              isAuthenticated: true,
              isLoading: false,
            });

            console.log('✅ 注册成功:', user.username);
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
          // 清除本地存储
          removeItem(TOKEN_KEY);
          removeItem(REFRESH_TOKEN_KEY);

          // 清除状态
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

            // 保存到 LocalStorage
            setItem(TOKEN_KEY, newAccessToken);
            setItem(REFRESH_TOKEN_KEY, newRefreshToken);

            // 更新状态
            set({
              accessToken: newAccessToken,
              refreshToken: newRefreshToken,
            });

            console.log('✅ 令牌刷新成功');
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
        const accessToken = getItem(TOKEN_KEY);
        const refreshToken = getItem(REFRESH_TOKEN_KEY);

        if (accessToken && refreshToken) {
          // 从 LocalStorage 恢复认证状态
          set({
            accessToken,
            refreshToken,
            isAuthenticated: true,
          });

          // 获取当前用户信息
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
