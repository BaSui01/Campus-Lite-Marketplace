/**
 * 通知页面 - 不错过任何重要消息！🔔
 * @author BaSui 😎
 * @description 系统通知、消息通知、订单通知
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Skeleton, Tabs } from '@campus/shared/components';
import { notificationService, NotificationType } from '@campus/shared/services';
import { websocketService } from '@campus/shared/utils';
import { getApi } from '@campus/shared/utils/apiClient';
import type { NotificationResponse } from '@campus/shared/api/models';
import type { AxiosError } from 'axios';
import { useNotificationStore } from '../../store';
import './Notifications.css';

// ==================== 类型定义 ====================

type NotificationType = 'all' | 'system' | 'message' | 'order' | 'like' | 'comment';

interface Notification {
  notificationId: string;
  type: NotificationType;
  title: string;
  content: string;
  isRead: boolean;
  createdAt: string;
  relatedId?: string; // 关联的订单ID、消息ID等
}

/**
 * 通知页面组件
 */
const Notifications: React.FC = () => {
  const navigate = useNavigate();
  const toast = useNotificationStore();
  const api = getApi();

  // ==================== 状态管理 ====================

  const [activeTab, setActiveTab] = useState<NotificationType>('all');
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);
  const [unreadCount, setUnreadCount] = useState(0);

  const mapNotificationType = (rawType?: string): NotificationType => {
    const type = rawType?.toLowerCase();
    if (!type) return 'system';
    if (type.includes('order')) return 'order';
    if (type.includes('message')) return 'message';
    if (type.includes('like')) return 'like';
    if (type.includes('comment')) return 'comment';
    return 'system';
  };

  // ==================== 数据加载 ====================

  /**
   * 加载通知列表
   */
  const loadNotifications = async () => {
    setLoading(true);

    try {
      // ✅ 使用 notificationService 获取通知列表
      const response = await notificationService.listNotifications({
        page: 0,
        size: 50,
      });

      const apiNotifications: Notification[] = response.content.map((n) => ({
        notificationId: String(n.id ?? ''),
        type: mapNotificationType(n.type),
        title: n.title || '通知',
        content: n.content || '',
        isRead: n.status === 'READ',
        createdAt: n.createdAt || '',
        relatedId: n.relatedId ? String(n.relatedId) : undefined,
      }));

      // 按类型筛选
      const filteredNotifications =
        activeTab === 'all'
          ? apiNotifications
          : apiNotifications.filter((n) => n.type === activeTab);

      setNotifications(filteredNotifications);
      setUnreadCount(apiNotifications.filter((n) => !n.isRead).length);
    } catch (err: unknown) {
      const error = err as AxiosError<any>;
      console.error('加载通知列表失败：', err);
      toast.error(error.response?.data?.message || error.message || '加载通知失败！😭');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadNotifications();
  }, [activeTab]);

  // ==================== 🔔 实时通知推送（WebSocket）====================

  /**
   * 🔔 监听 WebSocket 实时通知推送
   */
  useEffect(() => {
    console.log('[Notifications] 🔔 开始监听实时通知推送...');

    // 定义通知处理器
    const handleNewNotification = (notification: any) => {
      console.log('[Notifications] 🔔 收到新通知:', notification);

      // 🎯 转换为前端格式
      const newNotification: Notification = {
        notificationId: String(notification.id),
        type: mapNotificationType(notification.type),
        title: notification.title || '通知',
        content: notification.content,
        isRead: false, // 新通知默认未读
        createdAt: new Date().toISOString(),
        relatedId: notification.relatedId ? String(notification.relatedId) : undefined,
      };

      // 🚀 乐观更新 UI（添加到列表顶部）
      setNotifications((prev) => [newNotification, ...prev]);
      setUnreadCount((prev) => prev + 1);

      // 💬 显示 Toast 提示
      toast.info(`📬 新通知：${newNotification.title}`);
    };

    // 📡 订阅通知推送
    websocketService.onNotification(handleNewNotification);

    // 🔌 确保 WebSocket 已连接
    if (!websocketService.isConnected()) {
      console.log('[Notifications] 🔌 WebSocket 未连接，尝试连接...');
      websocketService.connect();
    }

    // 🧹 清理函数（组件卸载时取消订阅）
    return () => {
      console.log('[Notifications] 🧹 取消订阅实时通知推送');
      websocketService.offNotification(handleNewNotification);
    };
  }, []); // 空依赖，只在组件挂载时执行一次

  // ==================== 事件处理 ====================

  /**
   * 标记为已读
   */
  const handleMarkAsRead = async (notificationId: string) => {
    try {
      // 乐观更新 UI
      setNotifications((prev) =>
        prev.map((n) => (n.notificationId === notificationId ? { ...n, isRead: true } : n))
      );
      setUnreadCount((prev) => Math.max(0, prev - 1));

      // ✅ 使用 notificationService 标记已读
      await notificationService.markOneAsRead(Number(notificationId));
    } catch (err: unknown) {
      const error = err as AxiosError<any>;
      console.error('标记已读失败：', err);
      toast.error(error.response?.data?.message || error.message || '操作失败！😭');
    }
  };

  /**
   * 全部标记为已读
   */
  const handleMarkAllAsRead = async () => {
    try {
      // 乐观更新 UI
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
      setUnreadCount(0);

      // ✅ 使用 notificationService 全部标记已读
      await notificationService.markAllAsRead();

      toast.success('已全部标记为已读！✅');
    } catch (err: unknown) {
      const error = err as AxiosError<any>;
      console.error('全部标记已读失败：', err);
      toast.error(error.response?.data?.message || error.message || '操作失败！😭');
    }
  };

  /**
   * 删除通知
   */
  const handleDelete = async (notificationId: string) => {
    if (!window.confirm('确定要删除这条通知吗？')) {
      return;
    }

    try {
      // 乐观更新 UI
      const notification = notifications.find((n) => n.notificationId === notificationId);
      if (notification && !notification.isRead) {
        setUnreadCount((prev) => Math.max(0, prev - 1));
      }
      setNotifications((prev) => prev.filter((n) => n.notificationId !== notificationId));

      // ✅ 使用 notificationService 删除通知
      await notificationService.deleteOne(Number(notificationId));

      toast.success('通知已删除！🗑️');
    } catch (err: unknown) {
      const error = err as AxiosError<any>;
      console.error('删除通知失败：', err);
      toast.error(error.response?.data?.message || error.message || '删除失败！😭');
    }
  };

  /**
   * 点击通知
   */
  const handleClickNotification = (notification: Notification) => {
    // 标记为已读
    if (!notification.isRead) {
      handleMarkAsRead(notification.notificationId);
    }

    // 根据类型跳转
    if (notification.type === 'order' && notification.relatedId) {
      navigate(`/orders/${notification.relatedId}`);
    } else if (notification.type === 'message') {
      navigate('/chat');
    } else if (notification.type === 'like' || notification.type === 'comment') {
      navigate('/community');
    }
  };

  /**
   * 格式化时间
   */
  const formatTime = (time?: string) => {
    if (!time) return '';
    const date = new Date(time);
    const now = new Date();
    const diff = now.getTime() - date.getTime();

    // 1分钟内
    if (diff < 60 * 1000) {
      return '刚刚';
    }

    // 1小时内
    if (diff < 60 * 60 * 1000) {
      const minutes = Math.floor(diff / (60 * 1000));
      return `${minutes}分钟前`;
    }

    // 今天
    if (date.toDateString() === now.toDateString()) {
      return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
    }

    // 昨天
    const yesterday = new Date(now);
    yesterday.setDate(yesterday.getDate() - 1);
    if (date.toDateString() === yesterday.toDateString()) {
      return `昨天 ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`;
    }

    // 其他
    return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' });
  };

  /**
   * 获取通知图标
   */
  const getNotificationIcon = (type: NotificationType) => {
    switch (type) {
      case 'system':
        return '🔔';
      case 'order':
        return '📦';
      case 'message':
        return '💬';
      case 'like':
        return '❤️';
      case 'comment':
        return '💭';
      default:
        return '📢';
    }
  };

  // ==================== 渲染 ====================

  return (
    <div className="notifications-page">
      <div className="notifications-container">
        {/* ==================== 头部 ==================== */}
        <div className="notifications-header">
          <h1 className="notifications-header__title">🔔 通知中心</h1>
          <div className="notifications-header__actions">
            <Button type="default" size="small" onClick={() => navigate('/settings/notifications')}>
              ⚙️ 通知设置
            </Button>
            {unreadCount > 0 && (
              <Button type="primary" size="small" onClick={handleMarkAllAsRead}>
                全部已读 ({unreadCount})
              </Button>
            )}
          </div>
        </div>

        {/* ==================== 标签切换 ==================== */}
        <div className="notifications-tabs">
          <Tabs
            value={activeTab}
            onChange={(value) => setActiveTab(value as NotificationType)}
            tabs={[
              { label: `全部 ${unreadCount > 0 ? `(${unreadCount})` : ''}`, value: 'all' },
              { label: '🔔 系统', value: 'system' },
              { label: '📦 订单', value: 'order' },
              { label: '💬 消息', value: 'message' },
              { label: '❤️ 点赞', value: 'like' },
              { label: '💭 评论', value: 'comment' },
            ]}
          />
        </div>

        {/* ==================== 通知列表 ==================== */}
        <div className="notifications-list">
          {loading ? (
            <Skeleton type="list" count={5} animation="wave" />
          ) : notifications.length === 0 ? (
            <div className="notifications-empty">
              <div className="empty-icon">📭</div>
              <p className="empty-text">暂无通知</p>
              <p className="empty-tip">所有通知都会在这里显示</p>
            </div>
          ) : (
            notifications.map((notification) => (
              <div
                key={notification.notificationId}
                className={`notification-card ${!notification.isRead ? 'unread' : ''}`}
                onClick={() => handleClickNotification(notification)}
              >
                <div className="notification-card__icon">
                  {getNotificationIcon(notification.type)}
                </div>
                <div className="notification-card__content">
                  <div className="notification-card__header">
                    <h3 className="notification-card__title">{notification.title}</h3>
                    <span className="notification-card__time">{formatTime(notification.createdAt)}</span>
                  </div>
                  <p className="notification-card__text">{notification.content}</p>
                </div>
                <button
                  className="notification-card__delete"
                  onClick={(e) => {
                    e.stopPropagation();
                    handleDelete(notification.notificationId);
                  }}
                >
                  🗑️
                </button>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};

export default Notifications;
