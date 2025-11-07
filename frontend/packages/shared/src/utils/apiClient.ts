/**
 * 🚀 BaSui 的 API 客户端（基于 OpenAPI 生成代码）
 * @description 统一的 API 客户端配置：Token、拦截器、错误处理
 */

import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios';
import { DefaultApi, Configuration } from '../api';
import { BASE_PATH as DEFAULT_BASE_PATH } from '../api/base';
import { TOKEN_KEY, REFRESH_TOKEN_KEY } from '../constants';

// ==================== 常量配置 ====================

const REQUEST_TIMEOUT = 30000; // 30秒超时

// ==================== BaseURL 解析 ====================

/**
 * 去除末尾多余斜杠，保证 BaseURL 规范
 */
const normalizeBaseUrl = (url: string): string => url.replace(/\/+$/, '');

/**
 * 拼接 API 地址
 */
const joinWithBaseUrl = (baseUrl: string, path: string): string => {
  const normalizedBase = normalizeBaseUrl(baseUrl);
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  return `${normalizedBase}${normalizedPath}`;
};

/**
 * 读取运行环境中的 API 根地址
 */
const resolveEnvBaseUrl = (): string | undefined => {
  let viteBaseUrl: string | undefined;
  try {
    viteBaseUrl = (import.meta as any)?.env?.VITE_API_BASE_URL;
  } catch {
    viteBaseUrl = undefined;
  }

  const globalProcess = (globalThis as any)?.process;
  const nodeBaseUrl = globalProcess?.env?.VITE_API_BASE_URL as string | undefined;

  const globalBaseUrl = (globalThis as any)?.__API_BASE_URL__ as string | undefined;

  return viteBaseUrl || nodeBaseUrl || globalBaseUrl;
};

/** 统一后的 API 根路径（默认回退到生成代码的 BASE_PATH） */
const API_BASE_URL = normalizeBaseUrl(resolveEnvBaseUrl() || DEFAULT_BASE_PATH);

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

/** 单例 DefaultApi 实例缓存 */
let apiInstance: DefaultApi | null = null;

/**
 * 重置 API 实例（用于 Token 或 BaseURL 更新后）
 */
export const resetApiInstance = (): void => {
  apiInstance = null;
};

/**
 * 保存 Token
 */
export const setTokens = (accessToken: string, refreshToken?: string): void => {
  localStorage.setItem(TOKEN_KEY, accessToken);
  if (refreshToken) {
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
  }
  resetApiInstance();
};

/**
 * 清除 Token
 */
export const clearTokens = (): void => {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  resetApiInstance();
};

/**
 * 检查 Token 是否存在
 */
export const hasToken = (): boolean => {
  return !!getAccessToken();
};

// ==================== Axios 实例 ====================

/**
 * 创建 Axios 实例（带拦截器）
 */
const createAxiosInstance = (baseURL: string): AxiosInstance => {
  const instance = axios.create({
    baseURL,
    timeout: REQUEST_TIMEOUT,
    headers: {
      'Content-Type': 'application/json',
    },
  });

  // ==================== 请求拦截器 ====================
  instance.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
      // 💉 注入 JWT Token
      const token = getAccessToken();
      if (token && config.headers) {
        config.headers.Authorization = `Bearer ${token}`;
      }

      console.log(`[API Client] 🚀 ${config.method?.toUpperCase()} ${config.url}`, {
        params: config.params,
        data: config.data,
      });

      return config;
    },
    (error: AxiosError) => {
      console.error('[API Client] ❌ 请求拦截失败:', error);
      return Promise.reject(error);
    }
  );

  // ==================== 响应拦截器 ====================
  instance.interceptors.response.use(
    (response) => {
      console.log(`[API Client] ✅ ${response.config.method?.toUpperCase()} ${response.config.url}`, response.data);
      return response;
    },
    async (error: AxiosError) => {
      const { response } = error;

      console.error(`[API Client] ❌ ${error.config?.method?.toUpperCase()} ${error.config?.url}`, {
        status: response?.status,
        data: response?.data,
      });

      // 🔄 401 Token 过期处理
      if (response?.status === 401) {
        const refreshToken = getRefreshToken();
        if (refreshToken) {
          try {
            // 尝试刷新 Token
            const refreshEndpoint = joinWithBaseUrl(API_BASE_URL, '/api/auth/refresh');
            const { data } = await axios.post(refreshEndpoint, { refreshToken });
            const newAccessToken = data.data?.accessToken;

            if (newAccessToken) {
              setTokens(newAccessToken);
              // 重试原请求
              if (error.config && error.config.headers) {
                error.config.headers.Authorization = `Bearer ${newAccessToken}`;
              }
              return instance.request(error.config!);
            }
          } catch (refreshError) {
            console.error('[API Client] ❌ Token 刷新失败:', refreshError);
            clearTokens();
            // 保存当前路径，登录后跳转回来
            const currentPath = window.location.pathname + window.location.search;
            window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`;
          }
        } else {
          clearTokens();
          // 保存当前路径，登录后跳转回来
          const currentPath = window.location.pathname + window.location.search;
          window.location.href = `/login?redirect=${encodeURIComponent(currentPath)}`;
        }
      }

      // 📢 错误提示
      const errorMessage = (response?.data as any)?.message || error.message || '系统内部错误';
      console.log(`[API Client] 💬 ${errorMessage}`);

      return Promise.reject(error);
    }
  );

  return instance;
};

// ==================== API 客户端实例 ====================

/** 默认 Axios 单例 */
export const axiosInstance = createAxiosInstance(API_BASE_URL);

/**
 * 对外创建新 DefaultApi 的可选项
 */
export interface CreateApiOptions {
  basePath?: string;
  accessToken?: string;
  axiosInstance?: AxiosInstance;
}

/**
 * 返回当前配置的 API 根路径
 */
export const getApiBaseUrl = (): string => API_BASE_URL;

const resolveBasePath = (options?: CreateApiOptions): string => normalizeBaseUrl(options?.basePath || API_BASE_URL);

const resolveAxiosInstance = (options?: CreateApiOptions, basePath?: string): AxiosInstance => {
  if (options?.axiosInstance) {
    return options.axiosInstance;
  }
  if (options?.basePath && normalizeBaseUrl(options.basePath) !== API_BASE_URL) {
    return createAxiosInstance(basePath || normalizeBaseUrl(options.basePath));
  }
  return axiosInstance;
};

/**
 * 创建一个新的 DefaultApi 实例（非单例）
 */
export const createApi = (options?: CreateApiOptions): DefaultApi => {
  const basePath = resolveBasePath(options);
  const axiosClient = resolveAxiosInstance(options, basePath);
  const configuration = new Configuration({
    accessToken: options?.accessToken ?? getAccessToken() ?? undefined,
    basePath,
  });
  return new DefaultApi(configuration, basePath, axiosClient);
};

/**
 * 获取 DefaultApi 单例（懒加载）
 */
export const getApi = (): DefaultApi => {
  if (!apiInstance) {
    apiInstance = createApi();
  }
  return apiInstance;
};

// ==================== 导出 ====================

export const apiClient = axiosInstance; // 导出 axios 实例，供服务层使用
export const http = axiosInstance; // 别名导出，兼容旧代码
export { DefaultApi, Configuration, DEFAULT_BASE_PATH as BASE_PATH };
export default getApi;
