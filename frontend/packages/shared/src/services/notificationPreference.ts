/**
 * ⚠️ 警告：此文件仍使用手写 API 路径（http.get/post/put/delete）
 * 🔧 需要重构：将所有 http. 调用替换为 getApi() + DefaultApi 方法
 * 📋 参考：frontend/packages/shared/src/services/order.ts（已完成重构）
 * 👉 重构步骤：
 *    1. 找到对应的 OpenAPI 生成的方法名（在 api/api/default-api.ts）
 *    2. 替换为：const api = getApi(); api.methodName(...)
 *    3. 更新返回值类型
 */
/**
 * 通知偏好 API 服务
 * @author BaSui 😎
 * @description 通知渠道开关、免打扰时段、通知类型订阅管理
 */

import { getApi } from '../utils/apiClient';

/**
 * 通知渠道枚举
 */
export enum NotificationChannel {
  EMAIL = 'EMAIL',       // 邮件
  SMS = 'SMS',           // 短信
  IN_APP = 'IN_APP',     // 站内信
}

/**
 * 通知类型枚举
 */
export enum NotificationType {
  ORDER = 'ORDER',               // 订单通知（不可退订）
  PAYMENT = 'PAYMENT',           // 支付通知（不可退订）
  MESSAGE = 'MESSAGE',           // 站内消息
  LIKE = 'LIKE',                 // 点赞通知
  COMMENT = 'COMMENT',           // 评论通知
  FOLLOW = 'FOLLOW',             // 关注通知
  PRICE_ALERT = 'PRICE_ALERT',   // 价格提醒
  SYSTEM = 'SYSTEM',             // 系统公告（不可退订）
}

/**
 * 免打扰时段配置
 */
export interface QuietHoursConfig {
  enabled: boolean;        // 是否启用
  startTime: string;       // 开始时间 "22:00"
  endTime: string;         // 结束时间 "08:00"
}

/**
 * 通知偏好配置
 */
export interface NotificationPreference {
  userId: number;                          // 用户ID
  channels: {
    email: boolean;                        // 邮件开关
    sms: boolean;                          // 短信开关
    inApp: boolean;                        // 站内信开关（固定为true）
  };
  quietHours?: QuietHoursConfig;           // 免打扰时段
  unsubscribedTypes: NotificationType[];   // 已退订的通知类型
  updatedAt: string;                       // 最后更新时间
}

/**
 * 通知类型信息
 */
export interface NotificationTypeInfo {
  type: NotificationType;
  name: string;
  description: string;
  icon: string;
  canUnsubscribe: boolean;  // 是否可以退订
}

/**
 * 通知偏好 API 服务类
 */
export class NotificationPreferenceService {
  private BASE_PATH = '/api/notifications/preferences';

  /**
   * 开关通知渠道
   * 
   * @param channel 渠道类型
   * @param enabled 是否启用
   */
  async toggleChannel(channel: NotificationChannel, enabled: boolean): Promise<void> {
    await http.post(`${this.BASE_PATH}/channel/${channel}/enabled/${enabled}`);
  }

  /**
   * 设置免打扰时段
   * 
   * @param channel 渠道（通常为 EMAIL 或 IN_APP）
   * @param config 免打扰配置
   */
  async setQuietHours(channel: NotificationChannel, config: QuietHoursConfig): Promise<void> {
    await http.post(`${this.BASE_PATH}/channel/${channel}/quiet-hours`, {
      startTime: config.startTime,
      endTime: config.endTime,
    });
  }

  /**
   * 退订通知类型
   * 
   * @param channel 渠道
   * @param type 通知类型
   */
  async unsubscribe(channel: NotificationChannel, type: NotificationType): Promise<void> {
    await http.post(`${this.BASE_PATH}/unsubscribe/${channel}/${type}`);
  }

  /**
   * 重新订阅通知类型
   * 
   * @param channel 渠道
   * @param type 通知类型
   */
  async resubscribe(channel: NotificationChannel, type: NotificationType): Promise<void> {
    await http.delete(`${this.BASE_PATH}/unsubscribe/${channel}/${type}`);
  }

  /**
   * 查询通知偏好状态
   * 
   * @returns 通知偏好配置
   */
  async getStatus(): Promise<NotificationPreference> {
    const response = await http.get(`${this.BASE_PATH}/status`);
    return response.data.data;
  }

  /**
   * 获取所有通知类型信息（前端定义）
   * 
   * @returns 通知类型列表
   */
  getNotificationTypes(): NotificationTypeInfo[] {
    return [
      {
        type: NotificationType.ORDER,
        name: '订单通知',
        description: '订单状态变更、发货、收货等通知',
        icon: '🛒',
        canUnsubscribe: false,
      },
      {
        type: NotificationType.PAYMENT,
        name: '支付通知',
        description: '支付成功、失败、退款等通知',
        icon: '💰',
        canUnsubscribe: false,
      },
      {
        type: NotificationType.MESSAGE,
        name: '站内消息',
        description: '其他用户发送的私信',
        icon: '💬',
        canUnsubscribe: true,
      },
      {
        type: NotificationType.LIKE,
        name: '点赞通知',
        description: '帖子、评论被点赞时通知',
        icon: '👍',
        canUnsubscribe: true,
      },
      {
        type: NotificationType.COMMENT,
        name: '评论通知',
        description: '帖子被评论、评论被回复时通知',
        icon: '💬',
        canUnsubscribe: true,
      },
      {
        type: NotificationType.FOLLOW,
        name: '关注通知',
        description: '被其他用户关注时通知',
        icon: '👤',
        canUnsubscribe: true,
      },
      {
        type: NotificationType.PRICE_ALERT,
        name: '价格提醒',
        description: '关注的商品降价时通知',
        icon: '💲',
        canUnsubscribe: true,
      },
      {
        type: NotificationType.SYSTEM,
        name: '系统公告',
        description: '系统维护、更新等重要公告',
        icon: '📢',
        canUnsubscribe: false,
      },
    ];
  }

  /**
   * 检查通知类型是否已退订
   * 
   * @param type 通知类型
   * @param unsubscribedTypes 已退订列表
   * @returns 是否已退订
   */
  isUnsubscribed(type: NotificationType, unsubscribedTypes: NotificationType[]): boolean {
    return unsubscribedTypes.includes(type);
  }

  /**
   * 批量退订通知类型
   * 
   * @param channel 渠道
   * @param types 通知类型列表
   */
  async batchUnsubscribe(channel: NotificationChannel, types: NotificationType[]): Promise<void> {
    const promises = types.map((type) => this.unsubscribe(channel, type));
    await Promise.all(promises);
  }

  /**
   * 批量重新订阅通知类型
   * 
   * @param channel 渠道
   * @param types 通知类型列表
   */
  async batchResubscribe(channel: NotificationChannel, types: NotificationType[]): Promise<void> {
    const promises = types.map((type) => this.resubscribe(channel, type));
    await Promise.all(promises);
  }
}

/**
 * 导出单例实例
 */
export const notificationPreferenceService = new NotificationPreferenceService();
