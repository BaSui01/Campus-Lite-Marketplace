/**
 * useWebSocketService Hook - WebSocket 服务管理大师！🔌
 * @author BaSui 😎
 * @description 封装 websocketService，管理连接生命周期
 */

import { useState, useEffect, useCallback } from 'react';
import { websocketService } from '../services/websocket';
import { WebSocketReadyState } from '../services/WebSocketClient';

/**
 * useWebSocketService 配置选项
 */
export interface UseWebSocketServiceOptions {
  /**
   * 是否自动连接
   * @default true
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
   * 重连成功回调
   */
  onReconnect?: (attempt: number) => void;
}

/**
 * useWebSocketService 返回值
 */
export interface UseWebSocketServiceResult {
  /**
   * 是否已连接
   */
  isConnected: boolean;

  /**
   * 连接状态
   */
  readyState: WebSocketReadyState;

  /**
   * 手动连接
   */
  connect: () => void;

  /**
   * 手动断开
   */
  disconnect: () => void;
}

/**
 * useWebSocketService Hook
 *
 * @description
 * 封装 websocketService，管理 WebSocket 连接的生命周期。
 * 这个 Hook 负责在组件挂载时自动连接，卸载时自动断开。
 *
 * @param options 配置选项
 * @returns WebSocket 服务管理结果
 *
 * @example
 * ```tsx
 * // 基础用法（自动连接）
 * function App() {
 *   const { isConnected } = useWebSocketService();
 *
 *   return (
 *     <div>
 *       <p>连接状态: {isConnected ? '已连接' : '未连接'}</p>
 *     </div>
 *   );
 * }
 * ```
 *
 * @example
 * ```tsx
 * // 手动连接
 * function ChatPage() {
 *   const { isConnected, connect, disconnect } = useWebSocketService({
 *     autoConnect: false,
 *     onOpen: () => {
 *       console.log('WebSocket ���连接');
 *     },
 *   });
 *
 *   return (
 *     <div>
 *       <Button onClick={connect} disabled={isConnected}>
 *         连接
 *       </Button>
 *       <Button onClick={disconnect} disabled={!isConnected}>
 *         断开
 *       </Button>
 *     </div>
 *   );
 * }
 * ```
 */
export const useWebSocketService = (
  options: UseWebSocketServiceOptions = {}
): UseWebSocketServiceResult => {
  const { autoConnect = true, onOpen, onClose, onError, onReconnect } = options;

  // 连接状态
  const [readyState, setReadyState] = useState<WebSocketReadyState>(
    websocketService.getReadyState()
  );

  // 是否已连接
  const isConnected = readyState === WebSocketReadyState.OPEN;

  /**
   * 更新连接状态
   */
  const updateReadyState = useCallback(() => {
    setReadyState(websocketService.getReadyState());
  }, []);

  /**
   * 连接 WebSocket
   */
  const connect = useCallback(() => {
    websocketService.connect();
    updateReadyState();
  }, [updateReadyState]);

  /**
   * 断开 WebSocket
   */
  const disconnect = useCallback(() => {
    websocketService.disconnect();
    updateReadyState();
  }, [updateReadyState]);

  /**
   * 初始化 WebSocket 事件监听器
   */
  useEffect(() => {
    // 注册事件监听器
    if (onOpen) {
      websocketService.onOpen((event) => {
        updateReadyState();
        onOpen(event);
      });
    } else {
      websocketService.onOpen(updateReadyState);
    }

    if (onClose) {
      websocketService.onClose((event) => {
        updateReadyState();
        onClose(event);
      });
    } else {
      websocketService.onClose(updateReadyState);
    }

    if (onError) {
      websocketService.onError((event) => {
        updateReadyState();
        onError(event);
      });
    } else {
      websocketService.onError(updateReadyState);
    }

    if (onReconnect) {
      websocketService.onReconnect((attempt) => {
        updateReadyState();
        onReconnect(attempt);
      });
    } else {
      websocketService.onReconnect(updateReadyState);
    }

    // 自动连接（响应 autoConnect 变化！🎯）
    if (autoConnect) {
      if (!websocketService.isConnected()) {
        console.log('🔌 [useWebSocketService] 自动连接 WebSocket...');
        connect();
      }
    } else {
      // autoConnect 为 false 时断开连接
      if (websocketService.isConnected()) {
        console.log('⚠️ [useWebSocketService] 自动断开 WebSocket...');
        disconnect();
      }
    }

    // 清理函数（组件卸载时断开连接）
    return () => {
      disconnect();
    };
  }, [autoConnect, onOpen, onClose, onError, onReconnect, connect, disconnect, updateReadyState]);

  return {
    isConnected,
    readyState,
    connect,
    disconnect,
  };
};

export default useWebSocketService;
