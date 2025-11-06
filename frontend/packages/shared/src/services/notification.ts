/**
 * 通知 API 服务
 * @author BaSui 😎
 * @description 站内通知查询、标记已读、删除等接口
 */

import { getApi } from '../utils/apiClient';
import type { NotificationResponse } from '../api/models';
import { NotificationType } from './notificationPreference';

/**
 * 通知状态枚举
 */
export enum NotificationStatus {
  UNREAD = 'UNREAD',
  READ = 'READ',
}

/**
 * 通知列表查询参数
 */
export interface NotificationListParams {
  status?: NotificationStatus;  // 通知状态筛选
  type?: NotificationType;      // 通知类型筛选
  page?: number;                // 页码（从 0 开始）
  size?: number;                // 每页大小
}

/**
 * 通知分页响应
 */
export interface PageNotificationResponse {
  content: NotificationResponse[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

/**
 * 通知 API 服务类
 */
export class NotificationService {
  /**
   * 查询通知列表（分页）
   * @param params 查询参数
   * @returns 通知列表（分页）
   */
  async listNotifications(params?: NotificationListParams): Promise<PageNotificationResponse> {
    const api = getApi();
    const response = await api.listNotifications(
      params?.status as any,
      params?.page ?? 0,
      params?.size ?? 20
    );
    
    let content = response.data.data?.content || [];
    
    // 前端类型筛选（如果后端不支持）
    if (params?.type) {
      content = content.filter((item) => 
        item.type?.toLowerCase().includes(params.type!.toLowerCase())
      );
    }
    
    return {
      content,
      totalElements: response.data.data?.totalElements || 0,
      totalPages: response.data.data?.totalPages || 0,
      number: response.data.data?.number || 0,
      size: response.data.data?.size || 20,
    };
  }

  /**
   * 获取未读通知数量
   * @returns 未读数量
   */
  async getUnreadCount(): Promise<number> {
    const api = getApi();
    const response = await api.getUnreadCount();
    return (response.data.data as number) || 0;
  }

  /**
   * 标记通知为已读（批量）
   * @param notificationIds 通知 ID 列表
   */
  async markAsRead(notificationIds: number[]): Promise<void> {
    const api = getApi();
    await api.markAsRead({ requestBody: notificationIds });
  }

  /**
   * 标记所有通知为已读
   */
  async markAllAsRead(): Promise<void> {
    const api = getApi();
    await api.markAllAsRead();
  }

  /**
   * 删除通知（批量，软删除）
   * @param notificationIds 通知 ID 列表
   */
  async deleteNotifications(notificationIds: number[]): Promise<void> {
    const api = getApi();
    await api.deleteNotifications({ requestBody: notificationIds });
  }

  /**
   * 标记单条通知为已读（快捷方法）
   * @param notificationId 通知 ID
   */
  async markOneAsRead(notificationId: number): Promise<void> {
    await this.markAsRead([notificationId]);
  }

  /**
   * 删除单条通知（快捷方法）
   * @param notificationId 通知 ID
   */
  async deleteOne(notificationId: number): Promise<void> {
    await this.deleteNotifications([notificationId]);
  }

  /**
   * 获取通知统计（按类型分组，前端计算）
   * @returns 各类型通知数量
   */
  async getNotificationStats(): Promise<Record<string, number>> {
    const notifications = await this.listNotifications({ page: 0, size: 1000 });
    const stats: Record<string, number> = {
      all: notifications.totalElements,
      unread: 0,
      system: 0,
      order: 0,
      message: 0,
      like: 0,
      comment: 0,
      follow: 0,
      priceAlert: 0,
    };

    notifications.content.forEach((item) => {
      if (item.status === 'UNREAD') stats.unread++;
      
      const type = item.type?.toLowerCase() || '';
      if (type.includes('order')) stats.order++;
      else if (type.includes('message')) stats.message++;
      else if (type.includes('like')) stats.like++;
      else if (type.includes('comment')) stats.comment++;
      else if (type.includes('follow')) stats.follow++;
      else if (type.includes('price')) stats.priceAlert++;
      else stats.system++;
    });

    return stats;
  }
}

/**
 * 导出单例实例
 */
export const notificationService = new NotificationService();
