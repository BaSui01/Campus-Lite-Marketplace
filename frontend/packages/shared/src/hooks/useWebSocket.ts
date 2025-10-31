/**
 * useWebSocket Hook - WebSocket 连接大师！🔌
 * @author BaSui 😎
 * @description WebSocket 封装 Hook，支持自动重连、心跳检测
 */

import { useState, useEffect, useRef, useCallback } from 'react';

/**
 * WebSocket 连接状态
 */
export enum WebSocketReadyState {
  /**
   * 正在连接
   */
  CONNECTING = 0,
  /**
   * 已连接
   */
  OPEN = 1,
  /**
   * 正在关闭
   */
  CLOSING = 2,
  /**
   * 已关闭
   */
  CLOSED = 3,
}

/**
 * useWebSocket 配置选项
 */
export interface UseWebSocketOptions {
  /**
   * 是否自动连接
   * @default true
   */
  autoConnect?: boolean;

  /**
   * 是否自动重连
   * @default true
   */
  reconnect?: boolean;

  /**
   * 重连延迟（毫秒）
   * @default 5000
   */
  reconnectDelay?: number;

  /**
   * 最大重连次数（0 表示无限制）
   * @default 0
   */
  maxReconnectAttempts?: number;

  /**
   * 心跳间隔（毫秒，0 表示禁用心跳）
   * @default 30000
   */
  heartbeatInterval?: number;

  /**
   * 心跳消息
   * @default 'ping'
   */
  heartbeatMessage?: string;

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
  onMessage?: (data: any) => void;
}

/**
 * useWebSocket 返回值
 */
export interface UseWebSocketResult {
  /**
   * 最新收到的消息
   */
  lastMessage: any;

  /**
   * 连接状态
   */
  readyState: WebSocketReadyState;

  /**
   * 发送消息
   */
  send: (data: string | object) => void;

  /**
   * 手动连接
   */
  connect: () => void;

  /**
   * 手动断开连接
   */
  disconnect: () => void;

  /**
   * 重连次数
   */
  reconnectCount: number;
}

/**
 * useWebSocket Hook
 *
 * @description
 * WebSocket 封装 Hook，提供自动重连、心跳检测、消息收发等功能。
 *
 * @param url WebSocket 服务器 URL
 * @param options 配置选项
 * @returns WebSocket 连接结果
 *
 * @example
 * ```tsx
 * // 基础用法
 * function ChatRoom() {
 *   const { lastMessage, send, readyState } = useWebSocket('ws://localhost:8080/chat');
 *
 *   const sendMessage = () => {
 *     send({ type: 'message', content: 'Hello!' });
 *   };
 *
 *   return (
 *     <div>
 *       <p>连接状态: {readyState === WebSocketReadyState.OPEN ? '已连接' : '未连接'}</p>
 *       <p>最新消息: {lastMessage?.content}</p>
 *       <Button onClick={sendMessage}>发送消息</Button>
 *     </div>
 *   );
 * }
 * ```
 *
 * @example
 * ```tsx
 * // 手动连接和断开
 * function RealtimeData() {
 *   const { lastMessage, connect, disconnect, readyState } = useWebSocket(
 *     'ws://localhost:8080/data',
 *     {
 *       autoConnect: false,
 *       onMessage: (data) => {
 *         console.log('收到数据:', data);
 *       },
 *     }
 *   );
 *
 *   return (
 *     <div>
 *       <Button onClick={connect} disabled={readyState === WebSocketReadyState.OPEN}>
 *         连接
 *       </Button>
 *       <Button onClick={disconnect} disabled={readyState !== WebSocketReadyState.OPEN}>
 *         断开
 *       </Button>
 *       <p>数据: {JSON.stringify(lastMessage)}</p>
 *     </div>
 *   );
 * }
 * ```
 */
export const useWebSocket = (
  url: string,
  options: UseWebSocketOptions = {}
): UseWebSocketResult => {
  const {
    autoConnect = true,
    reconnect = true,
    reconnectDelay = 5000,
    maxReconnectAttempts = 0,
    heartbeatInterval = 30000,
    heartbeatMessage = 'ping',
    onOpen,
    onClose,
    onError,
    onMessage,
  } = options;

  // WebSocket 实例引用
  const wsRef = useRef<WebSocket | null>(null);

  // 心跳定时器引用
  const heartbeatTimerRef = useRef<NodeJS.Timeout | null>(null);

  // 重连定时器引用
  const reconnectTimerRef = useRef<NodeJS.Timeout | null>(null);

  // 最新消息
  const [lastMessage, setLastMessage] = useState<any>(null);

  // 连接状态
  const [readyState, setReadyState] = useState<WebSocketReadyState>(
    WebSocketReadyState.CLOSED
  );

  // 重连次数
  const [reconnectCount, setReconnectCount] = useState(0);

  /**
   * 启动心跳
   */
  const startHeartbeat = useCallback(() => {
    if (heartbeatInterval === 0) return;

    heartbeatTimerRef.current = setInterval(() => {
      if (wsRef.current?.readyState === WebSocketReadyState.OPEN) {
        wsRef.current.send(heartbeatMessage);
      }
    }, heartbeatInterval);
  }, [heartbeatInterval, heartbeatMessage]);

  /**
   * 停止心跳
   */
  const stopHeartbeat = useCallback(() => {
    if (heartbeatTimerRef.current) {
      clearInterval(heartbeatTimerRef.current);
      heartbeatTimerRef.current = null;
    }
  }, []);

  /**
   * 连接 WebSocket
   */
  const connect = useCallback(() => {
    // 如果已经连接或正在连接，直接返回
    if (
      wsRef.current &&
      (wsRef.current.readyState === WebSocketReadyState.OPEN ||
        wsRef.current.readyState === WebSocketReadyState.CONNECTING)
    ) {
      return;
    }

    try {
      // 创建 WebSocket 连接
      const ws = new WebSocket(url);
      wsRef.current = ws;

      // 连接打开
      ws.onopen = (event) => {
        setReadyState(WebSocketReadyState.OPEN);
        setReconnectCount(0);
        startHeartbeat();
        onOpen?.(event);
      };

      // 收到消息
      ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data);
          setLastMessage(data);
          onMessage?.(data);
        } catch {
          // 如果不是 JSON，直接使用原始数据
          setLastMessage(event.data);
          onMessage?.(event.data);
        }
      };

      // 连接关闭
      ws.onclose = (event) => {
        setReadyState(WebSocketReadyState.CLOSED);
        stopHeartbeat();
        onClose?.(event);

        // 自动重连
        if (
          reconnect &&
          (maxReconnectAttempts === 0 || reconnectCount < maxReconnectAttempts)
        ) {
          reconnectTimerRef.current = setTimeout(() => {
            setReconnectCount((prev) => prev + 1);
            connect();
          }, reconnectDelay);
        }
      };

      // 连接错误
      ws.onerror = (event) => {
        setReadyState(WebSocketReadyState.CLOSED);
        stopHeartbeat();
        onError?.(event);
      };
    } catch (error) {
      console.error('WebSocket 连接失败:', error);
    }
  }, [
    url,
    reconnect,
    reconnectDelay,
    maxReconnectAttempts,
    reconnectCount,
    onOpen,
    onClose,
    onError,
    onMessage,
    startHeartbeat,
    stopHeartbeat,
  ]);

  /**
   * 断开 WebSocket 连接
   */
  const disconnect = useCallback(() => {
    // 清除重连定时器
    if (reconnectTimerRef.current) {
      clearTimeout(reconnectTimerRef.current);
      reconnectTimerRef.current = null;
    }

    // 停止心跳
    stopHeartbeat();

    // 关闭连接
    if (wsRef.current) {
      wsRef.current.close();
      wsRef.current = null;
    }

    setReadyState(WebSocketReadyState.CLOSED);
    setReconnectCount(0);
  }, [stopHeartbeat]);

  /**
   * 发送消息
   */
  const send = useCallback((data: string | object) => {
    if (wsRef.current?.readyState === WebSocketReadyState.OPEN) {
      const message = typeof data === 'string' ? data : JSON.stringify(data);
      wsRef.current.send(message);
    } else {
      console.warn('WebSocket 未连接，无法发送消息');
    }
  }, []);

  /**
   * 自动连接
   */
  useEffect(() => {
    if (autoConnect) {
      connect();
    }

    // 清理函数
    return () => {
      disconnect();
    };
  }, []);

  return {
    lastMessage,
    readyState,
    send,
    connect,
    disconnect,
    reconnectCount,
  };
};

export default useWebSocket;
