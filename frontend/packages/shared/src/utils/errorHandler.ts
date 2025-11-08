/**
 * 全局错误处理
 * @author BaSui 😎
 * @description 统一处理 401/403/500 等错误，提供友好提示
 */

import { AxiosError, AxiosInstance } from 'axios';

// ==================== 类型定义 ====================

export interface ErrorHandlerConfig {
  /**
   * 显示错误消息的函数
   */
  showError: (message: string, duration?: number) => void;

  /**
   * 401 未授权处理
   */
  onUnauthorized?: () => void;

  /**
   * 403 无权限处理
   */
  onForbidden?: () => void;

  /**
   * 500 服务器错误处理
   */
  onServerError?: () => void;

  /**
   * 网络错误处理
   */
  onNetworkError?: () => void;

  /**
   * 自定义错误消息映射
   */
  customMessages?: Record<number, string>;

  /**
   * 是否启用错误日志上报
   */
  enableErrorReport?: boolean;

  /**
   * 错误日志上报函数
   */
  reportError?: (error: ErrorInfo) => void;
}

export interface ErrorInfo {
  code: number;
  message: string;
  url?: string;
  method?: string;
  timestamp: number;
  stack?: string;
}

// ==================== 默认错误消息 ====================

const DEFAULT_ERROR_MESSAGES: Record<number, string> = {
  400: '请求参数错误',
  401: '登录已过期，请重新登录',
  403: '您没有权限执行此操作',
  404: '请求的资源不存在',
  408: '请求超时，请稍后重试',
  500: '服务器内部错误',
  502: '网关错误',
  503: '服务暂时不可用',
  504: '网关超时',
};

// ==================== 错误处理器 ====================

class GlobalErrorHandler {
  private config: Required<ErrorHandlerConfig>;

  constructor(config: ErrorHandlerConfig) {
    this.config = {
      showError: config.showError,
      onUnauthorized: config.onUnauthorized ?? (() => {}),
      onForbidden: config.onForbidden ?? (() => {}),
      onServerError: config.onServerError ?? (() => {}),
      onNetworkError: config.onNetworkError ?? (() => {}),
      customMessages: config.customMessages ?? {},
      enableErrorReport: config.enableErrorReport ?? false,
      reportError: config.reportError ?? (() => {}),
    };
  }

  /**
   * 获取错误消息
   */
  private getErrorMessage(statusCode: number, errorData?: any): string {
    // 优先使用自定义消息
    if (this.config.customMessages[statusCode]) {
      return this.config.customMessages[statusCode];
    }

    // 尝试从响应数据中提取消息
    if (errorData?.message) {
      return errorData.message;
    }

    // 使用默认消息
    if (DEFAULT_ERROR_MESSAGES[statusCode]) {
      return DEFAULT_ERROR_MESSAGES[statusCode];
    }

    // 通用错误消息
    if (statusCode >= 500) {
      return '服务器错误，请稍后重试';
    }
    if (statusCode >= 400) {
      return '请求错误，请检查后重试';
    }

    return '未知错误，请稍后重试';
  }

  /**
   * 处理错误
   */
  handleError(error: AxiosError): void {
    const { response, code, message } = error;

    // 网络错误
    if (!response) {
      if (code === 'ECONNABORTED') {
        this.config.showError('请求超时，请检查网络连接', 3);
      } else if (code === 'ERR_NETWORK') {
        this.config.showError('网络连接失败，请检查网络', 3);
        this.config.onNetworkError();
      } else {
        this.config.showError(message || '网络错误', 3);
        this.config.onNetworkError();
      }
      this.reportErrorIfEnabled({
        code: 0,
        message: message || '网络错误',
        timestamp: Date.now(),
      });
      return;
    }

    const statusCode = response.status;
    const errorMessage = this.getErrorMessage(statusCode, response.data);

    // 显示错误消息
    this.config.showError(errorMessage, 3);

    // 特定状态码处理
    switch (statusCode) {
      case 401:
        this.config.onUnauthorized();
        break;
      case 403:
        this.config.onForbidden();
        break;
      case 500:
      case 502:
      case 503:
      case 504:
        this.config.onServerError();
        break;
    }

    // 错误上报
    this.reportErrorIfEnabled({
      code: statusCode,
      message: errorMessage,
      url: error.config?.url,
      method: error.config?.method?.toUpperCase(),
      timestamp: Date.now(),
      stack: error.stack,
    });
  }

  /**
   * 上报错误（如果启用）
   */
  private reportErrorIfEnabled(errorInfo: ErrorInfo): void {
    if (this.config.enableErrorReport) {
      try {
        this.config.reportError(errorInfo);
      } catch (err) {
        console.error('[Error Handler] 错误上报失败:', err);
      }
    }
  }
}

// ==================== 安装错误处理拦截器 ====================

/**
 * 安装全局错误处理拦截器
 */
export const installErrorHandler = (
  axiosInstance: AxiosInstance,
  config: ErrorHandlerConfig
): number => {
  const handler = new GlobalErrorHandler(config);

  return axiosInstance.interceptors.response.use(
    (response) => response,
    (error: AxiosError) => {
      // 处理错误
      handler.handleError(error);

      // 继续抛出错误（让业务层也能捕获）
      return Promise.reject(error);
    }
  );
};

// ==================== 导出 ====================

export { GlobalErrorHandler };
export default installErrorHandler;
