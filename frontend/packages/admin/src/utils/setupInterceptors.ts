/**
 * 设置全局拦截器和错误处理
 * @author BaSui 😎
 * @description 管理端专用：集成 Token 刷新、错误处理、Tab 同步
 */

import { message as antdMessage } from 'antd';
import { axiosInstance, installErrorHandler, initTabSync } from '@campus/shared';
import { useAuthStore } from '@/stores/auth';

/**
 * 初始化全局拦截器
 */
export const setupInterceptors = (): void => {
  console.log('[Setup] 🔧 初始化全局拦截器...');

  // ==================== 安装全局错误处理 ====================
  installErrorHandler(axiosInstance, {
    showError: (message: string, duration = 3) => {
      antdMessage.error(message, duration);
    },
    onUnauthorized: () => {
      console.warn('[Error Handler] 401 未授权');
      // Token 刷新会自动处理，这里只记录日志
    },
    onForbidden: () => {
      console.warn('[Error Handler] 403 无权限');
      antdMessage.warning('您没有权限执行此操作', 3);
    },
    onServerError: () => {
      console.error('[Error Handler] 服务器错误');
    },
    onNetworkError: () => {
      console.error('[Error Handler] 网络错误');
    },
    enableErrorReport: false, // TODO: 接入错误上报服务（Sentry）
    customMessages: {
      // 可以自定义特定状态码的错误消息
    },
  });

  console.log('[Setup] ✅ 全局错误处理已安装');

  // ==================== 初始化 Tab 同步 ====================
  initTabSync({
    channelName: 'admin-auth-sync',
    onLogin: (user, token) => {
      console.log('[Tab Sync] 收到登录事件', { user, token });
      useAuthStore.getState().setUser(user);
      useAuthStore.getState().setToken(token);
      antdMessage.success('其他标签页已登录，当前页面已同步', 2);
    },
    onLogout: () => {
      console.log('[Tab Sync] 收到登出事件');
      useAuthStore.getState().logout();
      antdMessage.warning('其他标签页已登出，当前页面即将跳转', 2);
      setTimeout(() => {
        window.location.href = '/admin/login';
      }, 2000);
    },
    onTokenRefresh: (token) => {
      console.log('[Tab Sync] 收到 Token 刷新事件');
      useAuthStore.getState().setToken(token);
    },
    onPermissionUpdate: (permissions) => {
      console.log('[Tab Sync] 收到权限更新事件', permissions);
      // TODO: 更新权限状态
    },
    debug: import.meta.env.DEV, // 开发环境启用调试日志
  });

  console.log('[Setup] ✅ Tab 同步已初始化');
};

/**
 * 清理拦截器
 */
export const cleanupInterceptors = (): void => {
  console.log('[Setup] 🧹 清理拦截器...');
  // TODO: 如果需要，可以在这里清理拦截器
};

export default setupInterceptors;
