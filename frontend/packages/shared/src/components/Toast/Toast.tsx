/**
 * Toast 组件 - 消息提示专家！🔔
 * @author BaSui 😎
 * @description 轻量级消息提示组件，支持成功、错误、警告、信息四种类型
 */

import React from 'react';
import { createRoot } from 'react-dom/client';
import './Toast.css';

/**
 * Toast 类型枚举
 * - success: 成功提示（绿色）
 * - error: 错误提示（红色）
 * - warning: 警告提示（橙色）
 * - info: 信息提示（蓝色）
 */
export type ToastType = 'success' | 'error' | 'warning' | 'info';

/**
 * Toast 位置枚举
 */
export type ToastPosition = 'top' | 'top-left' | 'top-right' | 'bottom' | 'bottom-left' | 'bottom-right';

/**
 * Toast 配置接口
 */
export interface ToastOptions {
  /**
   * Toast 类型
   * @default 'info'
   */
  type?: ToastType;

  /**
   * 显示位置
   * @default 'top'
   */
  position?: ToastPosition;

  /**
   * 显示时长（毫秒），0 表示不自动关闭
   * @default 3000
   */
  duration?: number;

  /**
   * 是否可手动关闭
   * @default true
   */
  closable?: boolean;

  /**
   * 关闭回调
   */
  onClose?: () => void;
}

/**
 * Toast Item 接口
 */
interface ToastItem extends ToastOptions {
  id: string;
  message: string;
  visible: boolean;
}

/**
 * Toast 图标映射
 */
const TOAST_ICONS: Record<ToastType, string> = {
  success: '✅',
  error: '❌',
  warning: '⚠️',
  info: 'ℹ️',
};

/**
 * Toast Container 组件
 */
const ToastContainer: React.FC<{ toasts: ToastItem[]; onRemove: (id: string) => void }> = ({
  toasts,
  onRemove,
}) => {
  // 按位置分组 Toast
  const groupedToasts = toasts.reduce((acc, toast) => {
    const position = toast.position || 'top';
    if (!acc[position]) {
      acc[position] = [];
    }
    acc[position].push(toast);
    return acc;
  }, {} as Record<ToastPosition, ToastItem[]>);

  return (
    <>
      {Object.entries(groupedToasts).map(([position, items]) => (
        <div key={position} className={`campus-toast-container campus-toast-container--${position}`}>
          {items.map((toast) => (
            <div
              key={toast.id}
              className={`campus-toast campus-toast--${toast.type} ${
                toast.visible ? 'campus-toast--visible' : 'campus-toast--hidden'
              }`}
            >
              {/* Toast 图标 */}
              <span className="campus-toast__icon">{TOAST_ICONS[toast.type || 'info']}</span>

              {/* Toast 消息 */}
              <span className="campus-toast__message">{toast.message}</span>

              {/* 关闭按钮 */}
              {toast.closable && (
                <span className="campus-toast__close" onClick={() => onRemove(toast.id)}>
                  ✕
                </span>
              )}
            </div>
          ))}
        </div>
      ))}
    </>
  );
};

/**
 * Toast Manager 单例
 */
class ToastManager {
  private toasts: ToastItem[] = [];
  private container: HTMLDivElement | null = null;
  private root: any = null;

  /**
   * 初始化容器
   */
  private ensureContainer() {
    if (!this.container) {
      this.container = document.createElement('div');
      this.container.id = 'campus-toast-root';
      document.body.appendChild(this.container);
      this.root = createRoot(this.container);
    }
  }

  /**
   * 渲染 Toast
   */
  private render() {
    this.ensureContainer();
    this.root.render(<ToastContainer toasts={this.toasts} onRemove={this.remove.bind(this)} />);
  }

  /**
   * 显示 Toast
   */
  show(message: string, options: ToastOptions = {}) {
    const id = `toast-${Date.now()}-${Math.random()}`;
    const toast: ToastItem = {
      id,
      message,
      visible: false,
      type: options.type || 'info',
      position: options.position || 'top',
      duration: options.duration !== undefined ? options.duration : 3000,
      closable: options.closable !== undefined ? options.closable : true,
      onClose: options.onClose,
    };

    this.toasts.push(toast);
    this.render();

    // 延迟显示（触发动画）
    setTimeout(() => {
      toast.visible = true;
      this.render();
    }, 10);

    // 自动关闭
    if (toast.duration && toast.duration > 0) {
      setTimeout(() => {
        this.remove(id);
      }, toast.duration);
    }

    return id;
  }

  /**
   * 移除 Toast
   */
  remove(id: string) {
    const toast = this.toasts.find((t) => t.id === id);
    if (toast) {
      toast.visible = false;
      this.render();

      // 动画结束后移除
      setTimeout(() => {
        this.toasts = this.toasts.filter((t) => t.id !== id);
        this.render();
        toast.onClose?.();
      }, 300);
    }
  }

  /**
   * 清除所有 Toast
   */
  clear() {
    this.toasts = [];
    this.render();
  }

  /**
   * 成功提示
   */
  success(message: string, options?: Omit<ToastOptions, 'type'>) {
    return this.show(message, { ...options, type: 'success' });
  }

  /**
   * 错误提示
   */
  error(message: string, options?: Omit<ToastOptions, 'type'>) {
    return this.show(message, { ...options, type: 'error' });
  }

  /**
   * 警告提示
   */
  warning(message: string, options?: Omit<ToastOptions, 'type'>) {
    return this.show(message, { ...options, type: 'warning' });
  }

  /**
   * 信息提示
   */
  info(message: string, options?: Omit<ToastOptions, 'type'>) {
    return this.show(message, { ...options, type: 'info' });
  }
}

/**
 * Toast 单例实例
 */
export const toast = new ToastManager();

/**
 * Toast 组件（用于导出类型）
 *
 * @example
 * ```tsx
 * import { toast } from '@campus/shared';
 *
 * // 成功提示
 * toast.success('操作成功！');
 *
 * // 错误提示
 * toast.error('操作失败，请重试！');
 *
 * // 警告提示
 * toast.warning('请注意数据备份！');
 *
 * // 信息提示
 * toast.info('这是一条普通消息');
 *
 * // 自定义配置
 * toast.success('保存成功！', {
 *   position: 'top-right',
 *   duration: 5000,
 *   closable: false,
 * });
 *
 * // 清除所有 Toast
 * toast.clear();
 * ```
 */
export default toast;
