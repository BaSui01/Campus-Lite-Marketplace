/**
 * 通知 API 服务
 * @author BaSui 😎
 * @description 系统通知、消息推送等接口
 */

import { http } from '../utils/http';
import type {
  ApiResponse,
  PageInfo,
  Notification,
  NotificationListQuery,
  MarkNotificationReadRequest,
} from '../types';

/**
 * 通知 API 服务类
 */
class NotificationService {
  /**
   * 获取通知列表
   * @param params 查询参数
   * @returns 通知列表
   */
  async getNotifications(params?: NotificationListQuery): Promise<ApiResponse<PageInfo<Notification>>> {
    return http.get('/notifications', { params });
  }

  /**
   * 获取通知详情
   * @param notificationId 通知ID
   * @returns 通知详情
   */
  async getNotificationById(notificationId: number): Promise<ApiResponse<Notification>> {
    return http.get(`/notifications/${notificationId}`);
  }

  /**
   * 标记通知已读
   * @param data 标记通知已读请求参数
   * @returns 标记结果
   */
  async markNotificationsAsRead(data: MarkNotificationReadRequest): Promise<ApiResponse<void>> {
    return http.post('/notifications/mark-read', data);
  }

  /**
   * 标记所有通知已读
   * @returns 标记结果
   */
  async markAllAsRead(): Promise<ApiResponse<void>> {
    return http.post('/notifications/mark-all-read');
  }

  /**
   * 删除通知
   * @param notificationId 通知ID
   * @returns 删除结果
   */
  async deleteNotification(notificationId: number): Promise<ApiResponse<void>> {
    return http.delete(`/notifications/${notificationId}`);
  }

  /**
   * 清空所有通知
   * @returns 清空结果
   */
  async clearAll(): Promise<ApiResponse<void>> {
    return http.delete('/notifications/clear-all');
  }

  /**
   * 获取未读通知数
   * @returns 未读通知数
   */
  async getUnreadCount(): Promise<ApiResponse<number>> {
    return http.get('/notifications/unread-count');
  }
}

// 导出单例
export const notificationService = new NotificationService();
export default notificationService;
