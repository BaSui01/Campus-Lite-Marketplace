/**
 * 全局状态管理导出
 * @author BaSui 😎
 * @description 导出所有 Zustand Store
 */

// 认证状态
export { useAuthStore } from './useAuthStore';
export type { default as AuthState } from './useAuthStore';

// 通知状态
export { useNotificationStore } from './useNotificationStore';
export type { NotificationItem, NotificationType } from './useNotificationStore';
