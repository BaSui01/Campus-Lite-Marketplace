/**
 * Token 刷新机制单元测试
 * @author BaSui 😎
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios';
import { installTokenRefreshInterceptor, type TokenRefreshConfig } from '../tokenRefresh';

describe('Token Refresh Interceptor', () => {
  let axiosInstance: AxiosInstance;
  let config: TokenRefreshConfig;
  let mockGetAccessToken: ReturnType<typeof vi.fn>;
  let mockGetRefreshToken: ReturnType<typeof vi.fn>;
  let mockSetTokens: ReturnType<typeof vi.fn>;
  let mockClearTokens: ReturnType<typeof vi.fn>;
  let mockOnRefreshFailed: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    // 创建 mock 函数
    mockGetAccessToken = vi.fn(() => 'old-access-token');
    mockGetRefreshToken = vi.fn(() => 'refresh-token');
    mockSetTokens = vi.fn();
    mockClearTokens = vi.fn();
    mockOnRefreshFailed = vi.fn();

    // 创建 axios 实例
    axiosInstance = axios.create({
      baseURL: 'http://localhost:8200',
    });

    // 配置
    config = {
      getAccessToken: mockGetAccessToken,
      getRefreshToken: mockGetRefreshToken,
      setTokens: mockSetTokens,
      clearTokens: mockClearTokens,
      refreshEndpoint: 'http://localhost:8200/api/auth/refresh',
      onRefreshFailed: mockOnRefreshFailed,
    };

    // 安装拦截器
    installTokenRefreshInterceptor(axiosInstance, config);
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('应该成功安装拦截器', () => {
    expect(axiosInstance.interceptors.response).toBeDefined();
  });

  it('应该在 401 错误时触发 Token 刷新', async () => {
    // Mock 刷新 Token API
    vi.spyOn(axios, 'post').mockResolvedValueOnce({
      data: {
        data: {
          accessToken: 'new-access-token',
          refreshToken: 'new-refresh-token',
        },
      },
    });

    // Mock 原始请求
    const mockRequest = vi.fn().mockResolvedValueOnce({ data: { success: true } });
    axiosInstance.request = mockRequest;

    // 创建 401 错误
    const error = {
      response: { status: 401 },
      config: {
        url: '/api/test',
        method: 'get',
        headers: {},
      } as InternalAxiosRequestConfig,
    } as AxiosError;

    // 手动触发拦截器
    const interceptor = axiosInstance.interceptors.response['handlers'][0];
    
    try {
      await interceptor.rejected(error);
    } catch (err) {
      // 预期会重试请求
    }

    // 验证是否调用了刷新 API
    expect(axios.post).toHaveBeenCalledWith(
      config.refreshEndpoint,
      { refreshToken: 'refresh-token' }
    );

    // 验证是否保存了新 Token
    expect(mockSetTokens).toHaveBeenCalledWith('new-access-token', 'new-refresh-token');
  });

  it('应该在没有刷新 Token 时直接跳转登录', async () => {
    // 设置没有刷新 Token
    mockGetRefreshToken.mockReturnValueOnce(null);

    // 创建 401 错误
    const error = {
      response: { status: 401 },
      config: {
        url: '/api/test',
        method: 'get',
        headers: {},
      } as InternalAxiosRequestConfig,
    } as AxiosError;

    // 手动触发拦截器
    const interceptor = axiosInstance.interceptors.response['handlers'][0];
    
    try {
      await interceptor.rejected(error);
    } catch (err) {
      // 预期会抛出错误
    }

    // 验证是否清除了 Token
    expect(mockClearTokens).toHaveBeenCalled();

    // 验证是否调用了失败回调
    expect(mockOnRefreshFailed).toHaveBeenCalled();
  });

  it('应该在刷新失败时清除 Token 并跳转登录', async () => {
    // Mock 刷新 Token API 失败
    vi.spyOn(axios, 'post').mockRejectedValueOnce(new Error('Refresh failed'));

    // 创建 401 错误
    const error = {
      response: { status: 401 },
      config: {
        url: '/api/test',
        method: 'get',
        headers: {},
      } as InternalAxiosRequestConfig,
    } as AxiosError;

    // 手动触发拦截器
    const interceptor = axiosInstance.interceptors.response['handlers'][0];
    
    try {
      await interceptor.rejected(error);
    } catch (err) {
      // 预期会抛出错误
    }

    // 验证是否清除了 Token
    expect(mockClearTokens).toHaveBeenCalled();

    // 验证是否调用了失败回调
    expect(mockOnRefreshFailed).toHaveBeenCalled();
  });

  it('应该在非 401 错误时直接返回错误', async () => {
    // 创建 403 错误
    const error = {
      response: { status: 403 },
      config: {
        url: '/api/test',
        method: 'get',
        headers: {},
      } as InternalAxiosRequestConfig,
    } as AxiosError;

    // 手动触发拦截器
    const interceptor = axiosInstance.interceptors.response['handlers'][0];
    
    try {
      await interceptor.rejected(error);
    } catch (err) {
      expect(err).toBe(error);
    }

    // 验证没有调用刷新 API
    expect(axios.post).not.toHaveBeenCalled();
  });

  it('应该在已重试过的请求中不再重试', async () => {
    // 创建已标记为重试的 401 错误
    const error = {
      response: { status: 401 },
      config: {
        url: '/api/test',
        method: 'get',
        headers: {},
        _retry: true, // 已重试标记
      } as InternalAxiosRequestConfig & { _retry?: boolean },
    } as AxiosError;

    // 手动触发拦截器
    const interceptor = axiosInstance.interceptors.response['handlers'][0];
    
    try {
      await interceptor.rejected(error);
    } catch (err) {
      // 预期会抛出错误
    }

    // 验证没有调用刷新 API
    expect(axios.post).not.toHaveBeenCalled();

    // 验证是否清除了 Token
    expect(mockClearTokens).toHaveBeenCalled();
  });

  it('应该支持自定义 Token 提取函数', async () => {
    // 创建自定义提取函数
    const customExtractToken = vi.fn((response: any) => ({
      accessToken: response.token,
      refreshToken: response.refresh,
    }));

    // 重新安装拦截器
    const customConfig: TokenRefreshConfig = {
      ...config,
      extractToken: customExtractToken,
    };

    const customInstance = axios.create({ baseURL: 'http://localhost:8200' });
    installTokenRefreshInterceptor(customInstance, customConfig);

    // Mock 刷新 Token API（自定义格式）
    vi.spyOn(axios, 'post').mockResolvedValueOnce({
      data: {
        token: 'custom-access-token',
        refresh: 'custom-refresh-token',
      },
    });

    // 创建 401 错误
    const error = {
      response: { status: 401 },
      config: {
        url: '/api/test',
        method: 'get',
        headers: {},
      } as InternalAxiosRequestConfig,
    } as AxiosError;

    // 手动触发拦截器
    const interceptor = customInstance.interceptors.response['handlers'][0];
    
    try {
      await interceptor.rejected(error);
    } catch (err) {
      // 预期会重试
    }

    // 验证是否调用了自定义提取函数
    expect(customExtractToken).toHaveBeenCalled();
  });
});
