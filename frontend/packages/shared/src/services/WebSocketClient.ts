/**
 * WebSocket 客户端类
 * @author BaSui 😎
 * @description 封装 WebSocket 连接，提供自动重连、心跳检测、消息队列等功能
 */

import {
  WEBSOCKET_URL,
  WEBSOCKET_HEARTBEAT_INTERVAL,
  WEBSOCKET_RECONNECT_INTERVAL,
  WEBSOCKET_MAX_RECONNECT,
  TOKEN_KEY,
} from '../constants';
import { getItem } from '../utils/storage';

/**
 * WebSocket 连接状态
 */
export enum WebSocketReadyState {
  /** 正在连接 */
  CONNECTING = 0,
  /** 已连接 */
  OPEN = 1,
  /** 正在关闭 */
  CLOSING = 2,
  /** 已关闭 */
  CLOSED = 3,
}

/**
 * WebSocket 消息类型
 */
export enum WebSocketMessageType {
  /** 心跳消息 */
  HEARTBEAT = 'heartbeat',
  /** 聊天消息 */
  CHAT = 'chat',
  /** 系统通知 */
  NOTIFICATION = 'notification',
  /** 订单更新 */
  ORDER_UPDATE = 'order_update',
  /** 用户上线 */
  USER_ONLINE = 'user_online',
  /** 用户下线 */
  USER_OFFLINE = 'user_offline',
}

/**
 * WebSocket 消息结构
 */
export interface WebSocketMessage<T = any> {
  /**
   * 消息类型
   */
  type: WebSocketMessageType | string;

  /**
   * 消息数据
   */
  data: T;

  /**
   * 消息ID（可选）
   */
  id?: string;

  /**
   * 时间戳
   */
  timestamp?: number;
}

/**
 * WebSocket 配置选项
 */
export interface WebSocketClientOptions {
  /**
   * WebSocket 服务器地址
   */
  url?: string;

  /**
   * 心跳间隔（毫秒）
   */
  heartbeatInterval?: number;

  /**
   * 重连间隔（毫秒）
   */
  reconnectInterval?: number;

  /**
   * 最大重连次数
   */
  maxReconnect?: number;

  /**
   * 是否自动连接
   */
  autoConnect?: boolean;

  /**
   * 连接成功回调
   */
  onOpen?: (event: Event) => void;

  /**
   * 连接关闭回调
   */
  onClose?: (event: CloseEvent) => void;

  /**
   * 连接错误回调
   */
  onError?: (event: Event) => void;

  /**
   * 收到消息回调
   */
  onMessage?: (message: WebSocketMessage) => void;

  /**
   * 重连成功回调
   */
  onReconnect?: (attempt: number) => void;
}

/**
 * WebSocket 客户端类
 */
export class WebSocketClient {
  /**
   * WebSocket 实例
   */
  private ws: WebSocket | null = null;

  /**
   * WebSocket 服务器地址
   */
  private url: string;

  /**
   * 心跳定时器
   */
  private heartbeatTimer: NodeJS.Timeout | null = null;

  /**
   * 重连定时器
   */
  private reconnectTimer: NodeJS.Timeout | null = null;

  /**
   * 心跳间隔（毫秒）
   */
  private heartbeatInterval: number;

  /**
   * 重连间隔（毫秒）
   */
  private reconnectInterval: number;

  /**
   * 最大重连次数
   */
  private maxReconnect: number;

  /**
   * 当前重连次数
   */
  private reconnectCount = 0;

  /**
   * 是否手动关闭
   */
  private manualClose = false;

  /**
   * 消息队列（连接前发送的消息）
   */
  private messageQueue: WebSocketMessage[] = [];

  /**
   * 事件监听器
   */
  private listeners: {
    onOpen?: (event: Event) => void;
    onClose?: (event: CloseEvent) => void;
    onError?: (event: Event) => void;
    onMessage?: (message: WebSocketMessage) => void;
    onReconnect?: (attempt: number) => void;
  } = {};

  /**
   * 构造函数
   */
  constructor(options: WebSocketClientOptions = {}) {
    this.url = options.url || WEBSOCKET_URL;
    this.heartbeatInterval = options.heartbeatInterval || WEBSOCKET_HEARTBEAT_INTERVAL;
    this.reconnectInterval = options.reconnectInterval || WEBSOCKET_RECONNECT_INTERVAL;
    this.maxReconnect = options.maxReconnect || WEBSOCKET_MAX_RECONNECT;

    // 保存事件监听器
    this.listeners = {
      onOpen: options.onOpen,
      onClose: options.onClose,
      onError: options.onError,
      onMessage: options.onMessage,
      onReconnect: options.onReconnect,
    };

    // 自动连接
    if (options.autoConnect !== false) {
      this.connect();
    }
  }

  /**
   * 连接 WebSocket
   */
  connect(): void {
    // 如果已经连接，直接返回
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      console.log('[WebSocket] 已经连接，无需重复连接');
      return;
    }

    // 获取 Token
    const token = getItem(TOKEN_KEY);
    if (!token) {
      console.error('[WebSocket] Token 不存在，无法连接');
      return;
    }

    // 构建 WebSocket URL（带 Token）
    const wsUrl = `${this.url}?token=${token}`;

    try {
      console.log('[WebSocket] 正在连接...', wsUrl);
      this.ws = new WebSocket(wsUrl);

      // 连接成功
      this.ws.onopen = (event) => {
        console.log('[WebSocket] 连接成功');
        this.reconnectCount = 0;
        this.manualClose = false;

        // 启动心跳
        this.startHeartbeat();

        // 发送队列中的消息
        this.flushMessageQueue();

        // 触发回调
        this.listeners.onOpen?.(event);
      };

      // 连接关闭
      this.ws.onclose = (event) => {
        console.log('[WebSocket] 连接关闭', event.code, event.reason);

        // 停止心跳
        this.stopHeartbeat();

        // 触发回调
        this.listeners.onClose?.(event);

        // 自动重连（非手动关闭）
        if (!this.manualClose) {
          this.reconnect();
        }
      };

      // 连接错误
      this.ws.onerror = (event) => {
        console.error('[WebSocket] 连接错误', event);

        // 触发回调
        this.listeners.onError?.(event);
      };

      // 收到消息
      this.ws.onmessage = (event) => {
        try {
          const message: WebSocketMessage = JSON.parse(event.data);
          console.log('[WebSocket] 收到消息', message);

          // 心跳响应，不触发回调
          if (message.type === WebSocketMessageType.HEARTBEAT) {
            return;
          }

          // 触发回调
          this.listeners.onMessage?.(message);
        } catch (error) {
          console.error('[WebSocket] 消息解析失败', error);
        }
      };
    } catch (error) {
      console.error('[WebSocket] 连接失败', error);
    }
  }

  /**
   * 断开连接
   */
  disconnect(): void {
    console.log('[WebSocket] 手动断开连接');
    this.manualClose = true;

    // 停止心跳
    this.stopHeartbeat();

    // 停止重连
    this.stopReconnect();

    // 关闭连接
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }

  /**
   * 发送消息
   */
  send<T = any>(message: WebSocketMessage<T>): void {
    // 添加时间戳
    if (!message.timestamp) {
      message.timestamp = Date.now();
    }

    // 如果未连接，加入队列
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      console.log('[WebSocket] 未连接，消息加入队列', message);
      this.messageQueue.push(message);
      return;
    }

    // 发送消息
    try {
      this.ws.send(JSON.stringify(message));
      console.log('[WebSocket] 发送消息', message);
    } catch (error) {
      console.error('[WebSocket] 发送消息失败', error);
    }
  }

  /**
   * 获取连接状态
   */
  getReadyState(): WebSocketReadyState {
    return this.ws?.readyState ?? WebSocketReadyState.CLOSED;
  }

  /**
   * 是否已连接
   */
  isConnected(): boolean {
    return this.ws?.readyState === WebSocket.OPEN;
  }

  /**
   * 启动心跳
   */
  private startHeartbeat(): void {
    this.stopHeartbeat();

    this.heartbeatTimer = setInterval(() => {
      if (this.isConnected()) {
        this.send({
          type: WebSocketMessageType.HEARTBEAT,
          data: { timestamp: Date.now() },
        });
      }
    }, this.heartbeatInterval);
  }

  /**
   * 停止心跳
   */
  private stopHeartbeat(): void {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  /**
   * 重连
   */
  private reconnect(): void {
    // 超过最大重连次数
    if (this.reconnectCount >= this.maxReconnect) {
      console.error('[WebSocket] 超过最大重连次数', this.maxReconnect);
      return;
    }

    // 停止之前的重连定时器
    this.stopReconnect();

    // 延迟重连
    this.reconnectTimer = setTimeout(() => {
      this.reconnectCount++;
      console.log(`[WebSocket] 第 ${this.reconnectCount} 次重连...`);

      // 重新连接
      this.connect();

      // 触发重连回调
      this.listeners.onReconnect?.(this.reconnectCount);
    }, this.reconnectInterval);
  }

  /**
   * 停止重连
   */
  private stopReconnect(): void {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
  }

  /**
   * 发送队列中的消息
   */
  private flushMessageQueue(): void {
    if (this.messageQueue.length === 0) {
      return;
    }

    console.log(`[WebSocket] 发送队列中的 ${this.messageQueue.length} 条消息`);

    while (this.messageQueue.length > 0) {
      const message = this.messageQueue.shift();
      if (message) {
        this.send(message);
      }
    }
  }

  /**
   * 更新事件监听器
   */
  on(
    event: 'open' | 'close' | 'error' | 'message' | 'reconnect',
    callback: (...args: any[]) => void
  ): void {
    switch (event) {
      case 'open':
        this.listeners.onOpen = callback;
        break;
      case 'close':
        this.listeners.onClose = callback;
        break;
      case 'error':
        this.listeners.onError = callback;
        break;
      case 'message':
        this.listeners.onMessage = callback;
        break;
      case 'reconnect':
        this.listeners.onReconnect = callback;
        break;
    }
  }

  /**
   * 移除事件监听器
   */
  off(event: 'open' | 'close' | 'error' | 'message' | 'reconnect'): void {
    switch (event) {
      case 'open':
        this.listeners.onOpen = undefined;
        break;
      case 'close':
        this.listeners.onClose = undefined;
        break;
      case 'error':
        this.listeners.onError = undefined;
        break;
      case 'message':
        this.listeners.onMessage = undefined;
        break;
      case 'reconnect':
        this.listeners.onReconnect = undefined;
        break;
    }
  }
}

export default WebSocketClient;
