/**
 * 全局错误处理单元测试
 * @author BaSui 😎
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import axios, { AxiosInstance, AxiosError } from 'axios';
import { installErrorHandler, type ErrorHandlerConfig, type ErrorInfo } from '../errorHandler';

describe('Error Handler', () => {
  let axiosInstance: AxiosInstance;
  let mockShowError: ReturnType<typeof vi.fn>;
  let mockOnUnauthorized: ReturnType<typeof vi.fn>;
  let mockOnForbidden: ReturnType<typeof vi.fn>;
  let mockOnServerError: ReturnType<typeof vi.fn>;
  let mockOnNetworkError: ReturnType<typeof vi.fn>;
  let mockReportError: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    // 创建 mock 函数
    mockShowError = vi.fn();
    mockOnUnauthorized = vi.fn();
    mockOnForbidden = vi.fn();
    mockOnServerError = vi.fn();
    mockOnNetworkError = vi.fn();
    mockReportError = vi.fn();

    // 创建 axios 实例
    axiosInstance = axios.create({
      baseURL: 'http://localhost:8200',
    });

    // 配置
    const config: ErrorHandlerConfig = {
      showError: mockShowError,
      onUnauthorized: mockOnUnauthorized,
      onForbidden: mockOnForbidden,
      onServerError: mockOnServerError,
      onNetworkError: mockOnNetworkError,
      enableErrorReport: true,
      reportError: mockReportError,
    };

    // 安装拦截器
    installErrorHandler(axiosInstance, config);
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('应该成功安装错误处理拦截器', () => {
    expect(axiosInstance.interceptors.response).toBeDefined();
  });

  it('应该处理 401 未授权错误', async () => {
    const error = {
      response: {
        status: 401,
        data: { message: '登录已过期' },
      },
      config: { url: '/api/test', method: 'get' },
    } as AxiosError;

    const interceptor = axiosInstance.interceptors.response['handlers'][0];
    
    try {
      await interceptor.rejected(error);
    } catch (err) {
      // 预期会抛出错误
    }

    expect(mockShowError).toHaveBeenCalledWith('登录已过期', 3);
    expect(mockOnUnauthorized).toHaveBeenCalled();
    expect(mockReportError).toHaveBeenCalledWith(
      expect.objectContaining({
        code: 401,
        message: '登录已过期',
      })
    );
  });

  it('应该处理 403 无权限错误', async () => {
    const error = {
      response: {
        status: 403,
        data: {},
      },
      config: { url: '/api/test', method: 'get' },
    } as AxiosError;

    const interceptor = axiosInstance.interceptors.response['handlers'][0];
    
    try {
      await interceptor.rejected(error);
    } catch (err) {
      // 预期会抛出错误
    }

    expect(mockShowError).toHaveBeenCalledWith('您没有权限执行此操作', 3);
    expect(mockOnForbidden).toHaveBeenCalled();
  });

  it('应该处理 500 服务器错误', async () => {
    const error = {
      response: {
        status: 500,
        data: {},
      },
      config: { url: '/api/test', method: 'get' },
    } as AxiosError;

    const interceptor = axiosInstance.interceptors.response['handlers'][0];
    
    try {
      await interceptor.rejected(error);
    } catch (err) {
      // 预期会抛出错误
    }

    expect(mockShowError).toHaveBeenCalledWith('服务器内部错误', 3);
    expect(mockOnServerError).toHaveBeenCalled();
  });

  it('应该处理网络错误', async () => {
    const error = {
      code: 'ERR_NETWORK',
      message: '网络连接失败',
      config: { url: '/api/test', method: 'get' },
    } as AxiosError;

    const interceptor = axiosInstance.interceptors.response['handlers'][0];
    
    try {
      await interceptor.rejected(error);
    } catch (err) {
      // 预期会抛出错误
    }

    expect(mockShowError).toHaveBeenCalledWith('网络连接失败，请检查网络', 3);
    expect(mockOnNetworkError).toHaveBeenCalled();
  });

  it('应该处理请求超时', async () => {
    const error = {
      code: 'ECONNABORTED',
      message: '请求超时',
      config: { url: '/api/test', method: 'get' },
    } as AxiosError;

    const interceptor = axiosInstance.interceptors.response['handlers'][0];
    
    try {
      await interceptor.rejected(error);
    } catch (err) {
      // 预期会抛出错误
    }

    expect(mockShowError).toHaveBeenCalledWith('请求超时，请检查网络连接', 3);
  });

  it('应该使用自定义错误消息', async () => {
    // 重新创建实例，使用自定义消息
    const customInstance = axios.create({ baseURL: 'http://localhost:8200' });
    
    installErrorHandler(customInstance, {
      showError: mockShowError,
      customMessages: {
        404: '资源走丢了',
        500: '服务器打瞌睡了',
      },
    });

    const error = {
      response: {
        status: 404,
        data: {},
      },
      config: { url: '/api/test', method: 'get' },
    } as AxiosError;

    const interceptor = customInstance.interceptors.response['handlers'][0];
    
    try {
      await interceptor.rejected(error);
    } catch (err) {
      // 预期会抛出错误
    }

    expect(mockShowError).toHaveBeenCalledWith('资源走丢了', 3);
  });

  it('应该从响应数据中提取错误消息', async () => {
    const error = {
      response: {
        status: 400,
        data: { message: '参数格式不正确' },
      },
      config: { url: '/api/test', method: 'get' },
    } as AxiosError;

    const interceptor = axiosInstance.interceptors.response['handlers'][0];
    
    try {
      await interceptor.rejected(error);
    } catch (err) {
      // 预期会抛出错误
    }

    expect(mockShowError).toHaveBeenCalledWith('参数格式不正确', 3);
  });

  it('应该在禁用错误上报时不调用上报函数', async () => {
    // 重新创建实例，禁用错误上报
    const noReportInstance = axios.create({ baseURL: 'http://localhost:8200' });
    
    installErrorHandler(noReportInstance, {
      showError: mockShowError,
      enableErrorReport: false,
      reportError: mockReportError,
    });

    const error = {
      response: {
        status: 500,
        data: {},
      },
      config: { url: '/api/test', method: 'get' },
    } as AxiosError;

    const interceptor = noReportInstance.interceptors.response['handlers'][0];
    
    try {
      await interceptor.rejected(error);
    } catch (err) {
      // 预期会抛出错误
    }

    expect(mockReportError).not.toHaveBeenCalled();
  });

  it('应该正确上报错误信息', async () => {
    const error = {
      response: {
        status: 500,
        data: { message: '数据库连接失败' },
      },
      config: { url: '/api/users', method: 'post' },
      stack: 'Error stack trace...',
    } as AxiosError;

    const interceptor = axiosInstance.interceptors.response['handlers'][0];
    
    try {
      await interceptor.rejected(error);
    } catch (err) {
      // 预期会抛出错误
    }

    expect(mockReportError).toHaveBeenCalledWith(
      expect.objectContaining({
        code: 500,
        message: '数据库连接失败',
        url: '/api/users',
        method: 'POST',
        timestamp: expect.any(Number),
        stack: 'Error stack trace...',
      })
    );
  });
});
