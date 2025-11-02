/**
 * useNotification Hook - 系统通知订阅大师！🔔
 * @author BaSui 😎
 * @description 订阅 WebSocket 系统通知，提供通知列表和管理功能
 */

import { useState, useEffect, useCallback } from 'react';
import { websocketService } from '../utils/websocket';
import type { Notification } from '../types';

/**
 * useNotification 配置选项
 */
export interface UseNotificationOptions {
  /**
   * 收到新通知回调
   */
  onNewNotification?: (notification: Notification) => void;

  /**
   * 是否自动订阅
   * @default true
   */
  autoSubscribe?: boolean;

  /**
   * 最大通知数量（超过后自动删除最旧的通知）
   * @default 50
   */
  maxCount?: number;

  /**
   * 是否自动播放通知音效
   * @default false
   */
  playSound?: boolean;
}

/**
 * useNotification 返回值
 */
export interface UseNotificationResult {
  /**
   * 通知列表
   */
  notifications: Notification[];

  /**
   * 最新通知
   */
  lastNotification: Notification | null;

  /**
   * 未读通知数量
   */
  unreadCount: number;

  /**
   * 标记通知为已读
   */
  markAsRead: (notificationId: number) => void;

  /**
   * 标记所有通知为已读
   */
  markAllAsRead: () => void;

  /**
   * 删除通知
   */
  removeNotification: (notificationId: number) => void;

  /**
   * 清空所有通知
   */
  clearNotifications: () => void;

  /**
   * 手动订阅
   */
  subscribe: () => void;

  /**
   * 手动取消订阅
   */
  unsubscribe: () => void;
}

/**
 * useNotification Hook
 *
 * @description
 * 订阅 WebSocket 系统通知，提供通知列表管理、已读标记、删除等功能。
 * 支持通知数量限制、音效播放等高级特性。
 *
 * @param options 配置选项
 * @returns 系统通知订阅结果
 *
 * @example
 * ```tsx
 * // 基础用法
 * function NotificationBell() {
 *   const { notifications, unreadCount, markAsRead } = useNotification({
 *     onNewNotification: (notification) => {
 *       console.log('收到新通知:', notification);
 *     },
 *   });
 *
 *   return (
 *     <div>
 *       <Badge count={unreadCount}>
 *         <BellIcon />
 *       </Badge>
 *       <List>
 *         {notifications.map((notif) => (
 *           <ListItem key={notif.id} onClick={() => markAsRead(notif.id)}>
 *             {notif.content}
 *           </ListItem>
 *         ))}
 *       </List>
 *     </div>
 *   );
 * }
 * ```
 *
 * @example
 * ```tsx
 * // 带音效和通知数量限制
 * function NotificationCenter() {
 *   const { notifications, clearNotifications } = useNotification({
 *     playSound: true,
 *     maxCount: 20,
 *     onNewNotification: (notification) => {
 *       // 显示浏览器通知
 *       if (Notification.permission === 'granted') {
 *         new Notification(notification.title, {
 *           body: notification.content,
 *         });
 *       }
 *     },
 *   });
 *
 *   return (
 *     <div>
 *       <Button onClick={clearNotifications}>清空通知</Button>
 *       {notifications.map((notif) => (
 *         <div key={notif.id}>{notif.content}</div>
 *       ))}
 *     </div>
 *   );
 * }
 * ```
 */
export const useNotification = (
  options: UseNotificationOptions = {}
): UseNotificationResult => {
  const {
    onNewNotification,
    autoSubscribe = true,
    maxCount = 50,
    playSound = false,
  } = options;

  // 通知列表
  const [notifications, setNotifications] = useState<Notification[]>([]);

  // 最新通知
  const [lastNotification, setLastNotification] = useState<Notification | null>(null);

  // 未读通知数量
  const unreadCount = notifications.filter((n) => !n.isRead).length;

  /**
   * 播放通知音效
   */
  const playNotificationSound = useCallback(() => {
    if (playSound && typeof Audio !== 'undefined') {
      try {
        // 使用浏览器内置音效
        const audio = new Audio('/sounds/notification.mp3');
        audio.play().catch((err) => {
          console.warn('播放通知音效失败:', err);
        });
      } catch (error) {
        console.warn('创建音效失败:', error);
      }
    }
  }, [playSound]);

  /**
   * 处理收到的通知
   */
  const handleNotification = useCallback(
    (notification: Notification) => {
      // 更新通知列表
      setNotifications((prev) => {
        const newList = [notification, ...prev];
        // 如果超过最大数量，删除最旧的通知
        if (newList.length > maxCount) {
          return newList.slice(0, maxCount);
        }
        return newList;
      });

      setLastNotification(notification);

      // 播放音效
      playNotificationSound();

      // 触发回调
      onNewNotification?.(notification);
    },
    [maxCount, playNotificationSound, onNewNotification]
  );

  /**
   * 标记通知为已读
   */
  const markAsRead = useCallback((notificationId: number) => {
    setNotifications((prev) =>
      prev.map((n) => (n.id === notificationId ? { ...n, isRead: true } : n))
    );
  }, []);

  /**
   * 标记所有通知为已读
   */
  const markAllAsRead = useCallback(() => {
    setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
  }, []);

  /**
   * 删除通知
   */
  const removeNotification = useCallback((notificationId: number) => {
    setNotifications((prev) => prev.filter((n) => n.id !== notificationId));
  }, []);

  /**
   * 清空所有通知
   */
  const clearNotifications = useCallback(() => {
    setNotifications([]);
    setLastNotification(null);
  }, []);

  /**
   * 订阅系统通知
   */
  const subscribe = useCallback(() => {
    websocketService.onNotification(handleNotification);
  }, [handleNotification]);

  /**
   * 取消订阅系统通知
   */
  const unsubscribe = useCallback(() => {
    websocketService.offNotification(handleNotification);
  }, [handleNotification]);

  /**
   * 自动订阅/取消订阅
   */
  useEffect(() => {
    if (autoSubscribe) {
      subscribe();

      // 清理函数
      return () => {
        unsubscribe();
      };
    }
  }, [autoSubscribe, subscribe, unsubscribe]);

  return {
    notifications,
    lastNotification,
    unreadCount,
    markAsRead,
    markAllAsRead,
    removeNotification,
    clearNotifications,
    subscribe,
    unsubscribe,
  };
};

export default useNotification;
