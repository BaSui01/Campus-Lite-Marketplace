/**
 * 设置全局拦截器和错误处理
 * @author BaSui 😎
 * @description 管理端专用：集成 Token 刷新、错误处理、Tab 同步
 */

import { message as antdMessage } from 'antd';
import { axiosInstance, installErrorHandler, initTabSync } from '@campus/shared';
import { useAuthStore, forceLogoutWithoutBroadcast } from '@/stores/auth';

let isHandlingUnauthorized = false;

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
      // ⚠️ 防止无限重定向：如果已经在登录页，不再处理
      if (window.location.pathname === '/admin/login') {
        console.warn('[Error Handler] ⚠️ 已在登录页，跳过 401 处理');
        return;
      }

      if (isHandlingUnauthorized) {
        return;
      }
      isHandlingUnauthorized = true;

      console.warn('[Error Handler] 401 未授权，触发强制登出');
      antdMessage.warning('登录状态已失效，请重新登录', 3);

      const { logout, isAuthenticated } = useAuthStore.getState();
      const redirectToLogin = () => {
        setTimeout(() => {
          window.location.href = '/admin/login';
        }, 800);
      };
      const finalize = () => {
        isHandlingUnauthorized = false;
        redirectToLogin();
      };

      if (isAuthenticated) {
        logout()
          .catch((error) => {
            console.warn('[Error Handler] 强制登出失败', error);
          })
          .finally(finalize);
      } else {
        forceLogoutWithoutBroadcast();
        finalize();
      }
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
    onLogin: () => {
      // Tab 同步登录：刷新页面以重新加载状态
      antdMessage.success('其他标签页已登录，正在同步...', 1);
      setTimeout(() => window.location.reload(), 1000);
    },
    onLogout: () => {
      forceLogoutWithoutBroadcast();
      antdMessage.warning('其他标签页已登出，即将跳转...', 1);
      setTimeout(() => window.location.href = '/admin/login', 1500);
    },
    onTokenRefresh: () => {
      // Token 刷新由 apiClient 自动处理
    },
    onPermissionUpdate: () => {
      // 权限更新：刷新页面
      setTimeout(() => window.location.reload(), 500);
    },
    debug: false, // 关闭调试日志
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
