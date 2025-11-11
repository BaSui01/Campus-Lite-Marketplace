/**
 * WebSocket 工具与服务封装
 * @description 替代原 services 目录下的 WebSocket 封装，提供统一接入点
 */

import {
  WEBSOCKET_URL,
  WEBSOCKET_HEARTBEAT_INTERVAL,
  WEBSOCKET_RECONNECT_INTERVAL,
  WEBSOCKET_MAX_RECONNECT,
  WEBSOCKET_RECONNECT_BACKOFF,
} from '../constants';
import { getAccessToken } from './tokenUtils';

// ==================== 基础类型 ====================

export enum WebSocketReadyState {
  CONNECTING = 0,
  OPEN = 1,
  CLOSING = 2,
  CLOSED = 3,
}

export enum WebSocketMessageType {
  HEARTBEAT = 'HEARTBEAT',
  TEXT = 'TEXT',
  IMAGE = 'IMAGE',
  SYSTEM = 'SYSTEM',
  ERROR = 'ERROR',
  CHAT = 'chat',
  NOTIFICATION = 'notification',
  ORDER_UPDATE = 'order_update',
  USER_ONLINE = 'user_online',
  USER_OFFLINE = 'user_offline',
}

export interface WebSocketMessage<T = any> {
  type: WebSocketMessageType | string;
  content?: string;
  data?: T;
  toUserId?: number;
  fromUserId?: number;
  messageId?: number;
  timestamp?: number;
  extra?: string;
}

export interface WebSocketClientOptions {
  url?: string;
  heartbeatInterval?: number;
  reconnectInterval?: number;
  maxReconnect?: number;
  autoConnect?: boolean;
  onOpen?: (event: Event) => void;
  onClose?: (event: CloseEvent) => void;
  onError?: (event: Event) => void;
  onMessage?: (message: WebSocketMessage) => void;
  onReconnect?: (attempt: number) => void;
}

// ==================== WebSocket 客户端 ====================

export class WebSocketClient {
  private ws: WebSocket | null = null;
  private url: string;
  private heartbeatTimer: ReturnType<typeof setInterval> | null = null;
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null;
  private heartbeatInterval: number;
  private reconnectInterval: number;
  private maxReconnect: number;
  private reconnectCount = 0;
  private manualClose = false;
  private messageQueue: WebSocketMessage[] = [];
  private listeners: {
    onOpen?: (event: Event) => void;
    onClose?: (event: CloseEvent) => void;
    onError?: (event: Event) => void;
    onMessage?: (message: WebSocketMessage) => void;
    onReconnect?: (attempt: number) => void;
  } = {};

  constructor(options: WebSocketClientOptions = {}) {
    this.url = options.url || WEBSOCKET_URL;
    this.heartbeatInterval = options.heartbeatInterval || WEBSOCKET_HEARTBEAT_INTERVAL;
    this.reconnectInterval = options.reconnectInterval || WEBSOCKET_RECONNECT_INTERVAL;
    this.maxReconnect = options.maxReconnect || WEBSOCKET_MAX_RECONNECT;
    this.listeners = {
      onOpen: options.onOpen,
      onClose: options.onClose,
      onError: options.onError,
      onMessage: options.onMessage,
      onReconnect: options.onReconnect,
    };
    if (options.autoConnect !== false) {
      this.connect();
    }
  }

  connect(): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      console.log('[WebSocket] 已连接，无需重复连接');
      return;
    }

    const token = getAccessToken();
    if (!token) {
      console.error('[WebSocket] ❌ Token 不存在，无法建立连接');
      console.log('[WebSocket] 💡 请确保已登录并且 Token 已保存到 localStorage');
      return;
    }

    const wsUrl = `${this.url}?token=${encodeURIComponent(token)}`;

    try {
      console.log('[WebSocket] 🔌 正在连接...', wsUrl);
      console.log('[WebSocket] 📍 Token 前20字符:', token.substring(0, 20) + '...');
      this.ws = new WebSocket(wsUrl);

      this.ws.onopen = (event) => {
        console.log('[WebSocket] 连接成功');
        this.reconnectCount = 0;
        this.manualClose = false;
        this.startHeartbeat();
        this.flushMessageQueue();
        this.listeners.onOpen?.(event);
      };

      this.ws.onclose = (event) => {
        console.log('[WebSocket] 🔴 连接关闭', {
          code: event.code,
          reason: event.reason || '无原因',
          wasClean: event.wasClean,
        });
        
        this.stopHeartbeat();
        this.listeners.onClose?.(event);
        
        // 错误码处理和重连策略
        if (!this.manualClose) {
          if (event.code === 1006) {
            // 1006: 异常关闭 - 可能是后端未启动或网络问题
            console.warn('[WebSocket] ⚠️ 连接异常关闭 (1006)');
            if (this.reconnectCount === 0) {
              console.warn('[WebSocket] 💡 提示：请确保后端服务已启动 (http://localhost:8200)');
            }
            this.reconnect();
          } else if (event.code === 1003) {
            // 1003: 不可接受的数据 - Token 验证失败
            console.error('[WebSocket] ❌ Token 验证失败 (1003)，停止重连');
            console.error('[WebSocket] 💡 提示：请重新登录获取有效 Token');
            // Token 失败不重连
          } else if (event.code === 1000 || event.code === 1001) {
            // 1000/1001: 正常关闭 - 不重连
            console.log('[WebSocket] ✅ 正常关闭，不重连');
          } else {
            // 其他错误码 - 尝试重连
            this.reconnect();
          }
        }
      };

      this.ws.onerror = (event) => {
        console.error('[WebSocket] 连接错误', event);
        this.listeners.onError?.(event);
      };

      this.ws.onmessage = (event) => {
        try {
          const message: WebSocketMessage = JSON.parse(event.data);
          this.listeners.onMessage?.(message);
        } catch (error) {
          console.error('[WebSocket] 消息解析失败', event.data, error);
        }
      };
    } catch (error) {
      console.error('[WebSocket] 连接异常', error);
      this.reconnect();
    }
  }

  disconnect(): void {
    this.manualClose = true;
    this.stopHeartbeat();
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }

  isConnected(): boolean {
    return !!this.ws && this.ws.readyState === WebSocket.OPEN;
  }

  getReadyState(): number {
    return this.ws?.readyState ?? WebSocket.CLOSED;
  }

  send<T>(message: WebSocketMessage<T>): void {
    if (this.isConnected() && this.ws) {
      this.ws.send(JSON.stringify(message));
      return;
    }
    console.warn('[WebSocket] 未连接，消息加入队列', message);
    this.messageQueue.push(message);
  }

  on(event: 'open', handler: (event: Event) => void): void;
  on(event: 'close', handler: (event: CloseEvent) => void): void;
  on(event: 'error', handler: (event: Event) => void): void;
  on(event: 'message', handler: (message: WebSocketMessage) => void): void;
  on(event: 'reconnect', handler: (attempt: number) => void): void;
  on(event: string, handler: (...args: any[]) => void): void {
    switch (event) {
      case 'open':
        this.listeners.onOpen = handler as (event: Event) => void;
        break;
      case 'close':
        this.listeners.onClose = handler as (event: CloseEvent) => void;
        break;
      case 'error':
        this.listeners.onError = handler as (event: Event) => void;
        break;
      case 'message':
        this.listeners.onMessage = handler as (message: WebSocketMessage) => void;
        break;
      case 'reconnect':
        this.listeners.onReconnect = handler as (attempt: number) => void;
        break;
      default:
        console.warn('[WebSocket] 未知事件类型', event);
    }
  }

  private startHeartbeat(): void {
    this.stopHeartbeat();
    this.heartbeatTimer = setInterval(() => {
      this.send({
        type: WebSocketMessageType.HEARTBEAT,
        content: 'PING',
        timestamp: Date.now(),
      });
    }, this.heartbeatInterval);
  }

  private stopHeartbeat(): void {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  private flushMessageQueue(): void {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      return;
    }
    while (this.messageQueue.length > 0) {
      const message = this.messageQueue.shift();
      if (message) {
        this.ws.send(JSON.stringify(message));
      }
    }
  }

  private reconnect(): void {
    if (this.reconnectCount >= this.maxReconnect) {
      console.error(`[WebSocket] ❌ 达到最大重连次数 (${this.maxReconnect})，停止重连`);
      console.error('[WebSocket] 💡 提示：请检查后端服务是否启动，或手动刷新页面重新连接');
      return;
    }
    
    this.reconnectCount += 1;
    
    // 指数退避策略：每次重连间隔翻倍，最大30秒
    const backoffDelay = Math.min(
      this.reconnectInterval * Math.pow(WEBSOCKET_RECONNECT_BACKOFF, this.reconnectCount - 1),
      30000
    );
    
    console.log(
      `[WebSocket] 🔄 ${Math.round(backoffDelay / 1000)}秒后尝试第 ${this.reconnectCount}/${this.maxReconnect} 次重连`
    );
    
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
    }
    
    this.reconnectTimer = setTimeout(() => {
      this.listeners.onReconnect?.(this.reconnectCount);
      this.connect();
    }, backoffDelay);
  }
}

// ==================== WebSocket 高级封装 ====================

export interface ChatMessageData {
  conversationId: number;
  content: string;
  messageType: 'text' | 'image' | 'file';
  receiver: number;
}

export interface OrderUpdateData {
  orderId: number;
  orderNo: string;
  status: string;
  message?: string;
}

export interface UserOnlineStatusData {
  userId: number;
  username: string;
  status: 'online' | 'offline';
  timestamp: number;
}

export class WebSocketService {
  private client: WebSocketClient;
  private handlers: Map<string, Set<(data: any) => void>> = new Map();

  constructor(options?: WebSocketClientOptions) {
    this.client = new WebSocketClient({
      ...options,
      autoConnect: options?.autoConnect ?? false,
      onMessage: this.handleMessage.bind(this),
    });
  }

  connect(): void {
    this.client.connect();
  }

  disconnect(): void {
    this.client.disconnect();
  }

  isConnected(): boolean {
    return this.client.isConnected();
  }

  getReadyState(): number {
    return this.client.getReadyState();
  }

  onOpen(callback: (event: Event) => void): void {
    this.client.on('open', callback);
  }

  onClose(callback: (event: CloseEvent) => void): void {
    this.client.on('close', callback);
  }

  onError(callback: (event: Event) => void): void {
    this.client.on('error', callback);
  }

  onReconnect(callback: (attempt: number) => void): void {
    this.client.on('reconnect', callback);
  }

  sendChatMessage(data: ChatMessageData): void {
    this.client.send({
      type: WebSocketMessageType.CHAT,
      data,
      id: `chat-${Date.now()}`,
    });
  }

  onChatMessage(callback: (message: any) => void): void {
    this.subscribe(WebSocketMessageType.CHAT, callback);
  }

  offChatMessage(callback: (message: any) => void): void {
    this.unsubscribe(WebSocketMessageType.CHAT, callback);
  }

  onNotification(callback: (notification: any) => void): void {
    this.subscribe(WebSocketMessageType.NOTIFICATION, callback);
  }

  offNotification(callback: (notification: any) => void): void {
    this.unsubscribe(WebSocketMessageType.NOTIFICATION, callback);
  }

  onOrderUpdate(callback: (data: OrderUpdateData) => void): void {
    this.subscribe(WebSocketMessageType.ORDER_UPDATE, callback);
  }

  offOrderUpdate(callback: (data: OrderUpdateData) => void): void {
    this.unsubscribe(WebSocketMessageType.ORDER_UPDATE, callback);
  }

  onUserOnline(callback: (data: UserOnlineStatusData) => void): void {
    this.subscribe(WebSocketMessageType.USER_ONLINE, callback);
  }

  offUserOnline(callback: (data: UserOnlineStatusData) => void): void {
    this.unsubscribe(WebSocketMessageType.USER_ONLINE, callback);
  }

  onUserOffline(callback: (data: UserOnlineStatusData) => void): void {
    this.subscribe(WebSocketMessageType.USER_OFFLINE, callback);
  }

  offUserOffline(callback: (data: UserOnlineStatusData) => void): void {
    this.unsubscribe(WebSocketMessageType.USER_OFFLINE, callback);
  }

  sendCustomMessage<T = any>(type: string, data: T, id?: string): void {
    this.client.send({
      type,
      data,
      id: id || `custom-${Date.now()}`,
    });
  }

  onCustomMessage<T = any>(type: string, callback: (data: T) => void): void {
    this.subscribe(type, callback);
  }

  offCustomMessage<T = any>(type: string, callback: (data: T) => void): void {
    this.unsubscribe(type, callback);
  }

  clearAllSubscriptions(): void {
    this.handlers.clear();
  }

  private handleMessage(message: WebSocketMessage): void {
    const callbacks = this.handlers.get(message.type);
    callbacks?.forEach((cb) => cb(message.data));
  }

  private subscribe(type: string, callback: (data: any) => void): void {
    if (!this.handlers.has(type)) {
      this.handlers.set(type, new Set());
    }
    this.handlers.get(type)!.add(callback);
  }

  private unsubscribe(type: string, callback: (data: any) => void): void {
    this.handlers.get(type)?.delete(callback);
  }
}

export const websocketService = new WebSocketService();
