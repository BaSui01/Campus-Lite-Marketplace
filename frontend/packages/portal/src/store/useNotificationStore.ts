/**
 * 通知状态管理
 * @author BaSui 😎
 * @description 使用 Zustand 管理全局通知状态（Toast 消息）
 */

import { create } from 'zustand';

/**
 * 通知类型
 */
export type NotificationType = 'success' | 'error' | 'warning' | 'info';

/**
 * 通知项
 */
export interface NotificationItem {
  /**
   * 通知ID
   */
  id: string;

  /**
   * 通知类型
   */
  type: NotificationType;

  /**
   * 通知标题
   */
  title?: string;

  /**
   * 通知内容
   */
  message: string;

  /**
   * 持续时间（毫秒）
   */
  duration?: number;

  /**
   * 创建时间
   */
  createdAt: number;
}

/**
 * 通知状态接口
 */
interface NotificationState {
  /**
   * 通知列表
   */
  notifications: NotificationItem[];

  /**
   * 显示成功通知
   */
  success: (message: string, title?: string, duration?: number) => void;

  /**
   * 显示错误通知
   */
  error: (message: string, title?: string, duration?: number) => void;

  /**
   * 显示警告通知
   */
  warning: (message: string, title?: string, duration?: number) => void;

  /**
   * 显示信息通知
   */
  info: (message: string, title?: string, duration?: number) => void;

  /**
   * 移除通知
   */
  remove: (id: string) => void;

  /**
   * 清空所有通知
   */
  clear: () => void;
}

/**
 * 生成唯一ID
 */
const generateId = (): string => {
  return `notification-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
};

/**
 * 通知状态管理 Store
 */
export const useNotificationStore = create<NotificationState>((set) => ({
  // ==================== 状态 ====================
  notifications: [],

  // ==================== 显示成功通知 ====================
  success: (message: string, title?: string, duration = 3000) => {
    const id = generateId();
    const notification: NotificationItem = {
      id,
      type: 'success',
      title,
      message,
      duration,
      createdAt: Date.now(),
    };

    set((state) => ({
      notifications: [...state.notifications, notification],
    }));

    // 自动移除
    if (duration > 0) {
      setTimeout(() => {
        set((state) => ({
          notifications: state.notifications.filter((n) => n.id !== id),
        }));
      }, duration);
    }
  },

  // ==================== 显示错误通知 ====================
  error: (message: string, title?: string, duration = 5000) => {
    const id = generateId();
    const notification: NotificationItem = {
      id,
      type: 'error',
      title,
      message,
      duration,
      createdAt: Date.now(),
    };

    set((state) => ({
      notifications: [...state.notifications, notification],
    }));

    // 自动移除
    if (duration > 0) {
      setTimeout(() => {
        set((state) => ({
          notifications: state.notifications.filter((n) => n.id !== id),
        }));
      }, duration);
    }
  },

  // ==================== 显示警告通知 ====================
  warning: (message: string, title?: string, duration = 4000) => {
    const id = generateId();
    const notification: NotificationItem = {
      id,
      type: 'warning',
      title,
      message,
      duration,
      createdAt: Date.now(),
    };

    set((state) => ({
      notifications: [...state.notifications, notification],
    }));

    // 自动移除
    if (duration > 0) {
      setTimeout(() => {
        set((state) => ({
          notifications: state.notifications.filter((n) => n.id !== id),
        }));
      }, duration);
    }
  },

  // ==================== 显示信息通知 ====================
  info: (message: string, title?: string, duration = 3000) => {
    const id = generateId();
    const notification: NotificationItem = {
      id,
      type: 'info',
      title,
      message,
      duration,
      createdAt: Date.now(),
    };

    set((state) => ({
      notifications: [...state.notifications, notification],
    }));

    // 自动移除
    if (duration > 0) {
      setTimeout(() => {
        set((state) => ({
          notifications: state.notifications.filter((n) => n.id !== id),
        }));
      }, duration);
    }
  },

  // ==================== 移除通知 ====================
  remove: (id: string) => {
    set((state) => ({
      notifications: state.notifications.filter((n) => n.id !== id),
    }));
  },

  // ==================== 清空所有通知 ====================
  clear: () => {
    set({ notifications: [] });
  },
}));

export default useNotificationStore;
