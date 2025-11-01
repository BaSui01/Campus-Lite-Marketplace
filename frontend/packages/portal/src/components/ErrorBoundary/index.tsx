/**
 * 错误边界组件 - 捕获子组件树中的 JavaScript 错误！🛡️
 * @author BaSui 😎
 * @description 防止整个应用因为某个组件报错而崩溃白屏
 */

import React, { Component, ErrorInfo, ReactNode } from 'react';
import './ErrorBoundary.css';

/**
 * ErrorBoundary Props
 */
interface Props {
  /**
   * 子组件
   */
  children: ReactNode;

  /**
   * 错误回退 UI（可选）
   */
  fallback?: (error: Error, errorInfo: ErrorInfo, reset: () => void) => ReactNode;

  /**
   * 错误回调函数（可选）
   */
  onError?: (error: Error, errorInfo: ErrorInfo) => void;
}

/**
 * ErrorBoundary State
 */
interface State {
  /**
   * 是否有错误
   */
  hasError: boolean;

  /**
   * 错误对象
   */
  error: Error | null;

  /**
   * 错误信息
   */
  errorInfo: ErrorInfo | null;
}

/**
 * 错误边界组件
 *
 * 使用示例：
 * ```tsx
 * <ErrorBoundary>
 *   <YourComponent />
 * </ErrorBoundary>
 * ```
 */
class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
    };
  }

  /**
   * 捕获错误并更新 state
   */
  static getDerivedStateFromError(error: Error): Partial<State> {
    // 更新 state 以显示错误 UI
    return {
      hasError: true,
      error,
    };
  }

  /**
   * 错误日志记录
   */
  componentDidCatch(error: Error, errorInfo: ErrorInfo): void {
    // 记录错误到控制台
    console.error('❌ ErrorBoundary 捕获到错误:', error);
    console.error('📍 错误组件栈:', errorInfo.componentStack);

    // 更新 errorInfo
    this.setState({
      errorInfo,
    });

    // 调用外部错误回调（如果有）
    if (this.props.onError) {
      this.props.onError(error, errorInfo);
    }

    // TODO: 将错误发送到错误监控服务（如 Sentry）
    // 示例：Sentry.captureException(error);
  }

  /**
   * 重置错误状态
   */
  handleReset = (): void => {
    this.setState({
      hasError: false,
      error: null,
      errorInfo: null,
    });
  };

  /**
   * 刷新页面
   */
  handleRefresh = (): void => {
    window.location.reload();
  };

  /**
   * 返回首页
   */
  handleGoHome = (): void => {
    window.location.href = '/';
  };

  /**
   * 渲染错误 UI
   */
  renderErrorUI(): ReactNode {
    const { error, errorInfo } = this.state;

    // 如果有自定义 fallback，使用它
    if (this.props.fallback && error && errorInfo) {
      return this.props.fallback(error, errorInfo, this.handleReset);
    }

    // 否则使用默认错误 UI
    return (
      <div className="error-boundary">
        <div className="error-boundary__container">
          {/* 错误图标 */}
          <div className="error-boundary__icon">💥</div>

          {/* 错误标题 */}
          <h1 className="error-boundary__title">哎呀！出错了...</h1>

          {/* 错误描述 */}
          <p className="error-boundary__description">
            应用遇到了一个意外错误，但别担心，这不是你的问题！😰
          </p>

          {/* 错误详情（开发环境显示） */}
          {process.env.NODE_ENV === 'development' && error && (
            <details className="error-boundary__details">
              <summary className="error-boundary__summary">查看错误详情</summary>
              <div className="error-boundary__error-info">
                <div className="error-boundary__error-message">
                  <strong>错误消息：</strong>
                  <pre>{error.toString()}</pre>
                </div>
                {errorInfo && (
                  <div className="error-boundary__error-stack">
                    <strong>组件栈：</strong>
                    <pre>{errorInfo.componentStack}</pre>
                  </div>
                )}
              </div>
            </details>
          )}

          {/* 操作按钮 */}
          <div className="error-boundary__actions">
            <button
              className="error-boundary__btn error-boundary__btn--primary"
              onClick={this.handleReset}
            >
              🔄 重试
            </button>
            <button
              className="error-boundary__btn error-boundary__btn--secondary"
              onClick={this.handleRefresh}
            >
              🔃 刷新页面
            </button>
            <button
              className="error-boundary__btn error-boundary__btn--outline"
              onClick={this.handleGoHome}
            >
              🏠 返回首页
            </button>
          </div>

          {/* 提示信息 */}
          <p className="error-boundary__tip">
            如果问题持续存在，请联系技术支持 📧
          </p>
        </div>
      </div>
    );
  }

  /**
   * 渲染组件
   */
  render(): ReactNode {
    if (this.state.hasError) {
      return this.renderErrorUI();
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
