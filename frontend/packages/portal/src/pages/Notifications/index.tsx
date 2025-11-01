/**
 * 通知页面 - 不错过任何重要消息！🔔
 * @author BaSui 😎
 * @description 系统通知、消息通知、订单通知
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Skeleton, Tabs } from '@campus/shared/components';
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

  // ==================== 状态管理 ====================

  const [activeTab, setActiveTab] = useState<NotificationType>('all');
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [loading, setLoading] = useState(true);
  const [unreadCount, setUnreadCount] = useState(0);

  // ==================== 数据加载 ====================

  /**
   * 加载通知列表
   */
  const loadNotifications = async () => {
    setLoading(true);

    try {
      // 🚀 调用真实后端 API 获取通知列表
      // TODO: 集成真实 API
      // const response = await notificationService.listNotifications({ type: activeTab });
      // setNotifications(response.data);
      // setUnreadCount(response.data.filter((n: Notification) => !n.isRead).length);

      // 临时模拟数据
      const mockNotifications: Notification[] = [
        {
          notificationId: '1',
          type: 'system',
          title: '系统通知',
          content: '欢迎使用校园轻享集市！🎉',
          isRead: false,
          createdAt: new Date(Date.now() - 1000 * 60 * 5).toISOString(),
        },
        {
          notificationId: '2',
          type: 'order',
          title: '订单通知',
          content: '您的订单已发货，请注意查收！📦',
          isRead: false,
          createdAt: new Date(Date.now() - 1000 * 60 * 30).toISOString(),
          relatedId: 'ORDER-001',
        },
        {
          notificationId: '3',
          type: 'message',
          title: '新消息',
          content: '张三给你发送了一条消息',
          isRead: true,
          createdAt: new Date(Date.now() - 1000 * 60 * 60).toISOString(),
        },
        {
          notificationId: '4',
          type: 'like',
          title: '点赞通知',
          content: '李四点赞了你的帖子',
          isRead: true,
          createdAt: new Date(Date.now() - 1000 * 60 * 60 * 2).toISOString(),
        },
        {
          notificationId: '5',
          type: 'comment',
          title: '评论通知',
          content: '王五评论了你的帖子：不错！👍',
          isRead: true,
          createdAt: new Date(Date.now() - 1000 * 60 * 60 * 5).toISOString(),
        },
      ];

      // 按类型筛选
      const filteredNotifications =
        activeTab === 'all'
          ? mockNotifications
          : mockNotifications.filter((n) => n.type === activeTab);

      setNotifications(filteredNotifications);
      setUnreadCount(mockNotifications.filter((n) => !n.isRead).length);
    } catch (err: any) {
      console.error('加载通知列表失败：', err);
      toast.error(err.response?.data?.message || '加载通知失败！😭');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadNotifications();
  }, [activeTab]);

  // ==================== 事件处理 ====================

  /**
   * 标记为已读
   */
  const handleMarkAsRead = async (notificationId: string) => {
    try {
      // 🚀 调用真实后端 API 标记已读
      // TODO: 集成真实 API
      // await notificationService.markAsRead(notificationId);

      // 乐观更新 UI
      setNotifications((prev) =>
        prev.map((n) => (n.notificationId === notificationId ? { ...n, isRead: true } : n))
      );

      setUnreadCount((prev) => Math.max(0, prev - 1));
    } catch (err: any) {
      console.error('标记已读失败：', err);
      toast.error(err.response?.data?.message || '操作失败！😭');
    }
  };

  /**
   * 全部标记为已读
   */
  const handleMarkAllAsRead = async () => {
    try {
      // 🚀 调用真实后端 API 全部标记已读
      // TODO: 集成真实 API
      // await notificationService.markAllAsRead();

      // 乐观更新 UI
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
      setUnreadCount(0);
      toast.success('已全部标记为已读！✅');
    } catch (err: any) {
      console.error('全部标记已读失败：', err);
      toast.error(err.response?.data?.message || '操作失败！😭');
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
      // 🚀 调用真实后端 API 删除通知
      // TODO: 集成真实 API
      // await notificationService.deleteNotification(notificationId);

      // 乐观更新 UI
      const notification = notifications.find((n) => n.notificationId === notificationId);
      if (notification && !notification.isRead) {
        setUnreadCount((prev) => Math.max(0, prev - 1));
      }
      setNotifications((prev) => prev.filter((n) => n.notificationId !== notificationId));
      toast.success('通知已删除！🗑️');
    } catch (err: any) {
      console.error('删除通知失败：', err);
      toast.error(err.response?.data?.message || '删除失败！😭');
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
          {unreadCount > 0 && (
            <Button type="primary" size="small" onClick={handleMarkAllAsRead}>
              全部已读 ({unreadCount})
            </Button>
          )}
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
