/**
 * 🚀 BaSui 的 Axios 封装层
 * @description 统一管理 HTTP 请求：拦截器、JWT、错误处理、重试机制
 */

import axios, { AxiosInstance, AxiosRequestConfig, AxiosError, AxiosResponse } from 'axios';
import { Configuration } from './api';

// ==================== 类型定义 ====================

/** API 响应基础结构 */
export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
  timestamp?: string;
}

/** 请求配置扩展 */
export interface RequestConfig extends AxiosRequestConfig {
  /** 是否跳过 JWT Token */
  skipAuth?: boolean;
  /** 是否显示错误提示 */
  showError?: boolean;
  /** 是否启用重试 */
  enableRetry?: boolean;
  /** 重试次数 */
  retryCount?: number;
}

// ==================== 常量配置 ====================

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8200/api';
const REQUEST_TIMEOUT = 30000; // 30秒超时
const TOKEN_KEY = 'auth_token';
const REFRESH_TOKEN_KEY = 'refresh_token';

// ==================== Axios 实例 ====================

/**
 * 创建 Axios 实例（带默认配置）
 */
const axiosInstance: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: REQUEST_TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ==================== Token 管理 ====================

/**
 * 获取访问 Token
 */
export const getAccessToken = (): string | null => {
  return localStorage.getItem(TOKEN_KEY);
};

/**
 * 获取刷新 Token
 */
export const getRefreshToken = (): string | null => {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
};

/**
 * 保存 Token
 */
export const setTokens = (accessToken: string, refreshToken?: string): void => {
  localStorage.setItem(TOKEN_KEY, accessToken);
  if (refreshToken) {
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  }
};

/**
 * 清除 Token
 */
export const clearTokens = (): void => {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
};

/**
 * 检查 Token 是否存在
 */
export const hasToken = (): boolean => {
  return !!getAccessToken();
};

// ==================== 请求拦截器 ====================

axiosInstance.interceptors.request.use(
  config => {
    const token = getAccessToken();
    const skipAuth = (config as RequestConfig).skipAuth;

    // 🔐 自动注入 JWT Token（除非显式跳过）
    if (token && !skipAuth) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // 📝 打印请求日志（开发环境）
    if (import.meta.env.DEV) {
      console.log(`[Axios] 🚀 ${config.method?.toUpperCase()} ${config.url}`, config.data);
    }

    return config;
  },
  error => {
    console.error('[Axios] ❌ 请求配置失败:', error);
    return Promise.reject(error);
  }
);

// ==================== 响应拦截器 ====================

axiosInstance.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    // 📝 打印响应日志（开发环境）
    if (import.meta.env.DEV) {
      console.log(
        `[Axios] ✅ ${response.config.method?.toUpperCase()} ${response.config.url}`,
        response.data
      );
    }

    // 🎯 统一响应处理
    const { code, message, data } = response.data;

    // 成功响应（code = 0 或 200）
    if (code === 0 || code === 200) {
      return response;
    }

    // 业务错误
    console.error(`[Axios] ⚠️ 业务错误 [${code}]: ${message}`);
    return Promise.reject({ code, message, data });
  },
  async (error: AxiosError<ApiResponse>) => {
    const { response, config } = error;
    const requestConfig = config as RequestConfig;

    // 📝 打印错误日志
    console.error(`[Axios] ❌ ${config?.method?.toUpperCase()} ${config?.url}`, error);

    // 🔄 Token 过期处理（401）
    if (response?.status === 401) {
      const refreshToken = getRefreshToken();

      if (refreshToken && !requestConfig._retry) {
        requestConfig._retry = true;

        try {
          // 🔄 刷新 Token
          const { data } = await axios.post(`${API_BASE_URL}/auth/refresh`, {
            refreshToken,
          });

          const newAccessToken = data.data.accessToken;
          setTokens(newAccessToken, data.data.refreshToken);

          // 🔁 重新发起原请求
          if (config) {
            config.headers.Authorization = `Bearer ${newAccessToken}`;
            return axiosInstance.request(config);
          }
        } catch (refreshError) {
          // 刷新失败，清除 Token 并跳转登录
          console.error('[Axios] 🚨 Token刷新失败，清除认证信息');
          clearTokens();
          window.location.href = '/login';
          return Promise.reject(refreshError);
        }
      } else {
        // 无 Refresh Token 或已重试，跳转登录
        clearTokens();
        window.location.href = '/login';
      }
    }

    // 🔄 网络错误重试（可选）
    if (requestConfig.enableRetry && !response) {
      const retryCount = requestConfig.retryCount || 3;
      requestConfig._retryCount = (requestConfig._retryCount || 0) + 1;

      if (requestConfig._retryCount < retryCount) {
        console.warn(`[Axios] 🔄 重试请求 (${requestConfig._retryCount}/${retryCount})`);
        return axiosInstance.request(config!);
      }
    }

    // 🚨 错误提示（可选）
    const showError = requestConfig.showError !== false; // 默认显示
    if (showError) {
      const errorMessage = response?.data?.message || error.message || '请求失败';
      // TODO: 调用全局提示组件显示错误
      console.error(`[Axios] 💬 ${errorMessage}`);
    }

    return Promise.reject(error);
  }
);

// ==================== OpenAPI Configuration ====================

/**
 * 创建 OpenAPI Configuration（用于生成的 API）
 */
export const createApiConfig = (): Configuration => {
  return new Configuration({
    basePath: API_BASE_URL,
    accessToken: getAccessToken() || undefined,
  });
};

// ==================== 导出 ====================

export default axiosInstance;
export { axiosInstance };

/**
 * 🎯 使用示例：
 *
 * ```typescript
 * import { axiosInstance, createApiConfig } from '@campus/shared';
 * import { DefaultApi } from '@campus/shared/api';
 *
 * // 方式1：直接使用 Axios
 * const response = await axiosInstance.get('/users/profile');
 *
 * // 方式2：使用生成的 API（推荐）
 * const api = new DefaultApi(createApiConfig(), API_BASE_URL, axiosInstance);
 * const profile = await api.getCurrentUserProfile();
 * ```
 */
