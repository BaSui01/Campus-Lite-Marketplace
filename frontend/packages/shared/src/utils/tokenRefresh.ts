/**
 * Token 自动刷新机制
 * @author BaSui 😎
 * @description 实现 Token 刷新队列、自动重试、防抖处理
 */

import axios, { AxiosInstance, InternalAxiosRequestConfig } from 'axios';

// ==================== 类型定义 ====================

interface RefreshQueueItem {
  resolve: (token: string) => void;
  reject: (error: Error) => void;
}

// ==================== Token 刷新管理器 ====================

class TokenRefreshManager {
  private isRefreshing = false;
  private refreshQueue: RefreshQueueItem[] = [];

  /**
   * 添加到刷新队列
   */
  addToQueue(): Promise<string> {
    return new Promise((resolve, reject) => {
      this.refreshQueue.push({ resolve, reject });
    });
  }

  /**
   * 处理刷新成功
   */
  onRefreshSuccess(newToken: string): void {
    this.refreshQueue.forEach((item) => item.resolve(newToken));
    this.refreshQueue = [];
    this.isRefreshing = false;
  }

  /**
   * 处理刷新失败
   */
  onRefreshFailure(error: Error): void {
    this.refreshQueue.forEach((item) => item.reject(error));
    this.refreshQueue = [];
    this.isRefreshing = false;
  }

  /**
   * 获取刷新状态
   */
  getRefreshingState(): boolean {
    return this.isRefreshing;
  }

  /**
   * 设置刷新状态
   */
  setRefreshing(state: boolean): void {
    this.isRefreshing = state;
  }
}

// 单例
const tokenRefreshManager = new TokenRefreshManager();

// ==================== 配置选项 ====================

export interface TokenRefreshConfig {
  /**
   * 获取访问 Token
   */
  getAccessToken: () => string | null;

  /**
   * 获取刷新 Token
   */
  getRefreshToken: () => string | null;

  /**
   * 保存 Token
   */
  setTokens: (accessToken: string, refreshToken?: string) => void;

  /**
   * 清除 Token
   */
  clearTokens: () => void;

  /**
   * 刷新 Token 的 API 端点
   */
  refreshEndpoint: string;

  /**
   * 刷新失败后的回调（通常是跳转登录页）
   */
  onRefreshFailed?: () => void;

  /**
   * 从刷新响应中提取新 Token
   */
  extractToken?: (response: any) => { accessToken: string; refreshToken?: string };
}

// ==================== Token 刷新拦截器 ====================

/**
 * 安装 Token 自动刷新拦截器
 */
export const installTokenRefreshInterceptor = (
  axiosInstance: AxiosInstance,
  config: TokenRefreshConfig
): number => {
  return axiosInstance.interceptors.response.use(
    (response) => response,
    async (error) => {
      const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

      // 非 401 错误，直接返回
      if (error.response?.status !== 401) {
        return Promise.reject(error);
      }

      // ⚠️ 验证码接口白名单：不触发 Token 刷新
      const requestUrl = originalRequest.url || '';
      if (requestUrl.includes('/captcha/')) {
        console.warn('[Token Refresh] ⚠️ 验证码接口返回 401，跳过 Token 刷新（需要后端配置匿名访问）');
        return Promise.reject(error);
      }

      // 已经重试过，不再重试
      if (originalRequest._retry) {
        console.warn('[Token Refresh] 已重试过，不再重试');
        config.clearTokens();
        config.onRefreshFailed?.();
        return Promise.reject(error);
      }

      // 标记为已重试
      originalRequest._retry = true;

      // 如果正在刷新，加入队列等待
      if (tokenRefreshManager.getRefreshingState()) {
        console.log('[Token Refresh] 正在刷新中，加入队列...');
        try {
          const newToken = await tokenRefreshManager.addToQueue();
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
          return axiosInstance.request(originalRequest);
        } catch (err) {
          return Promise.reject(err);
        }
      }

      // 获取刷新 Token
      const refreshToken = config.getRefreshToken();
      if (!refreshToken) {
        console.warn('[Token Refresh] 无刷新 Token，跳转登录');
        config.clearTokens();
        config.onRefreshFailed?.();
        return Promise.reject(error);
      }

      // 开始刷新
      tokenRefreshManager.setRefreshing(true);
      console.log('[Token Refresh] 开始刷新 Token...');

      try {
        // 调用刷新接口
        const response = await axios.post(config.refreshEndpoint, { refreshToken });

        // 提取新 Token
        const tokens = config.extractToken
          ? config.extractToken(response.data)
          : {
              accessToken: response.data.data?.accessToken,
              refreshToken: response.data.data?.refreshToken,
            };

        if (!tokens.accessToken) {
          throw new Error('刷新 Token 失败：未获取到新 Token');
        }

        console.log('[Token Refresh] Token 刷新成功');

        // 保存新 Token
        config.setTokens(tokens.accessToken, tokens.refreshToken);

        // 通知队列
        tokenRefreshManager.onRefreshSuccess(tokens.accessToken);

        // 重试原请求
        originalRequest.headers.Authorization = `Bearer ${tokens.accessToken}`;
        return axiosInstance.request(originalRequest);
      } catch (refreshError) {
        console.error('[Token Refresh] Token 刷新失败:', refreshError);

        // 通知队列失败
        tokenRefreshManager.onRefreshFailure(
          refreshError instanceof Error ? refreshError : new Error('Token 刷新失败')
        );

        // 清除 Token，跳转登录
        config.clearTokens();
        config.onRefreshFailed?.();

        return Promise.reject(refreshError);
      }
    }
  );
};

// ==================== 导出 ====================

export { tokenRefreshManager };
export default installTokenRefreshInterceptor;
