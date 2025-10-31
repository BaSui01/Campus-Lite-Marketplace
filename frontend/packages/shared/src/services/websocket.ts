/**
 * WebSocket 服务
 * @author BaSui 😎
 * @description WebSocket 高级封装服务，提供实时通信功能
 */

import {
  WebSocketClient,
  WebSocketMessage,
  WebSocketMessageType,
  WebSocketClientOptions,
} from './WebSocketClient';
import type {
  Message,
  Notification,
  OrderStatus,
} from '../types';

/**
 * 聊天消息数据
 */
export interface ChatMessageData {
  conversationId: number;
  content: string;
  messageType: 'text' | 'image' | 'file';
  receiver: number;
}

/**
 * 订单状态更新数据
 */
export interface OrderUpdateData {
  orderId: number;
  orderNo: string;
  status: OrderStatus;
  message?: string;
}

/**
 * 用户在线状态数据
 */
export interface UserOnlineStatusData {
  userId: number;
  username: string;
  status: 'online' | 'offline';
  timestamp: number;
}

/**
 * WebSocket 服务类
 */
export class WebSocketService {
  /**
   * WebSocket 客户端实例
   */
  private client: WebSocketClient;

  /**
   * 消息回调映射
   */
  private messageHandlers: Map<string, Set<(data: any) => void>> = new Map();

  /**
   * 构造函数
   */
  constructor(options?: WebSocketClientOptions) {
    this.client = new WebSocketClient({
      ...options,
      autoConnect: false, // 手动控制连接时机
    });

    // 注册消息监听器
    this.client.on('message', this.handleMessage.bind(this));
  }

  /**
   * 连接 WebSocket
   */
  connect(): void {
    this.client.connect();
  }

  /**
   * 断开连接
   */
  disconnect(): void {
    this.client.disconnect();
  }

  /**
   * 是否已连接
   */
  isConnected(): boolean {
    return this.client.isConnected();
  }

  /**
   * 获取连接状态
   */
  getReadyState(): number {
    return this.client.getReadyState();
  }

  // ==================== 事件监听器 ====================

  /**
   * 监听连接打开事件
   */
  onOpen(callback: (event: Event) => void): void {
    this.client.on('open', callback);
  }

  /**
   * 监听连接关闭事件
   */
  onClose(callback: (event: CloseEvent) => void): void {
    this.client.on('close', callback);
  }

  /**
   * 监听连接错误事件
   */
  onError(callback: (event: Event) => void): void {
    this.client.on('error', callback);
  }

  /**
   * 监听重连事件
   */
  onReconnect(callback: (attempt: number) => void): void {
    this.client.on('reconnect', callback);
  }

  // ==================== 聊天消息相关 ====================

  /**
   * 发送聊天消息
   * @param data 聊天消息数据
   */
  sendChatMessage(data: ChatMessageData): void {
    this.client.send<ChatMessageData>({
      type: WebSocketMessageType.CHAT,
      data,
      id: `chat-${Date.now()}`,
    });
  }

  /**
   * 订阅聊天消息
   * @param callback 消息回调
   */
  onChatMessage(callback: (message: Message) => void): void {
    this.subscribe(WebSocketMessageType.CHAT, callback);
  }

  /**
   * 取消订阅聊天消息
   * @param callback 消息回调
   */
  offChatMessage(callback: (message: Message) => void): void {
    this.unsubscribe(WebSocketMessageType.CHAT, callback);
  }

  // ==================== 通知相关 ====================

  /**
   * 订阅系统通知
   * @param callback 通知回调
   */
  onNotification(callback: (notification: Notification) => void): void {
    this.subscribe(WebSocketMessageType.NOTIFICATION, callback);
  }

  /**
   * 取消订阅系统通知
   * @param callback 通知回调
   */
  offNotification(callback: (notification: Notification) => void): void {
    this.unsubscribe(WebSocketMessageType.NOTIFICATION, callback);
  }

  // ==================== 订单更新相关 ====================

  /**
   * 订阅订单状态更新
   * @param callback 订单更新回调
   */
  onOrderUpdate(callback: (data: OrderUpdateData) => void): void {
    this.subscribe(WebSocketMessageType.ORDER_UPDATE, callback);
  }

  /**
   * 取消订阅订单状态更新
   * @param callback 订单更新回调
   */
  offOrderUpdate(callback: (data: OrderUpdateData) => void): void {
    this.unsubscribe(WebSocketMessageType.ORDER_UPDATE, callback);
  }

  // ==================== 用户在线状态相关 ====================

  /**
   * 订阅用户上线事件
   * @param callback 用户上线回调
   */
  onUserOnline(callback: (data: UserOnlineStatusData) => void): void {
    this.subscribe(WebSocketMessageType.USER_ONLINE, callback);
  }

  /**
   * 取消订阅用户上线事件
   * @param callback 用户上线回调
   */
  offUserOnline(callback: (data: UserOnlineStatusData) => void): void {
    this.unsubscribe(WebSocketMessageType.USER_ONLINE, callback);
  }

  /**
   * 订阅用户下线事件
   * @param callback 用户下线回调
   */
  onUserOffline(callback: (data: UserOnlineStatusData) => void): void {
    this.subscribe(WebSocketMessageType.USER_OFFLINE, callback);
  }

  /**
   * 取消订阅用户下线事件
   * @param callback 用户下线回调
   */
  offUserOffline(callback: (data: UserOnlineStatusData) => void): void {
    this.unsubscribe(WebSocketMessageType.USER_OFFLINE, callback);
  }

  // ==================== 自定义消息类型 ====================

  /**
   * 发送自定义消息
   * @param type 消息类型
   * @param data 消息数据
   * @param id 消息ID（可选）
   */
  sendCustomMessage<T = any>(type: string, data: T, id?: string): void {
    this.client.send<T>({
      type,
      data,
      id: id || `custom-${Date.now()}`,
    });
  }

  /**
   * 订阅自定义消息类型
   * @param type 消息类型
   * @param callback 消息回调
   */
  onCustomMessage<T = any>(type: string, callback: (data: T) => void): void {
    this.subscribe(type, callback);
  }

  /**
   * 取消订阅自定义消息类型
   * @param type 消息类型
   * @param callback 消息回调
   */
  offCustomMessage<T = any>(type: string, callback: (data: T) => void): void {
    this.unsubscribe(type, callback);
  }

  // ==================== 私有方法 ====================

  /**
   * 处理收到的消息
   */
  private handleMessage(message: WebSocketMessage): void {
    const handlers = this.messageHandlers.get(message.type);
    if (!handlers || handlers.size === 0) {
      console.warn(`[WebSocket] 未找到消息类型 "${message.type}" 的处理器`);
      return;
    }

    // 触发所有订阅该消息类型的回调
    handlers.forEach((handler) => {
      try {
        handler(message.data);
      } catch (error) {
        console.error(`[WebSocket] 消息处理器执行失败`, error);
      }
    });
  }

  /**
   * 订阅消息类型
   */
  private subscribe<T = any>(type: string, callback: (data: T) => void): void {
    if (!this.messageHandlers.has(type)) {
      this.messageHandlers.set(type, new Set());
    }

    const handlers = this.messageHandlers.get(type)!;
    handlers.add(callback);

    console.log(`[WebSocket] 订阅消息类型: ${type}，当前订阅数: ${handlers.size}`);
  }

  /**
   * 取消订阅消息类型
   */
  private unsubscribe<T = any>(type: string, callback: (data: T) => void): void {
    const handlers = this.messageHandlers.get(type);
    if (!handlers) {
      return;
    }

    handlers.delete(callback);
    console.log(`[WebSocket] 取消订阅消息类型: ${type}，剩余订阅数: ${handlers.size}`);

    // 如果没有订阅者了，删除该类型
    if (handlers.size === 0) {
      this.messageHandlers.delete(type);
    }
  }

  /**
   * 移除所有订阅
   */
  clearAllSubscriptions(): void {
    this.messageHandlers.clear();
    console.log('[WebSocket] 已清除所有订阅');
  }
}

/**
 * WebSocket 服务单例
 */
export const websocketService = new WebSocketService();

/**
 * 默认导出
 */
export default websocketService;
