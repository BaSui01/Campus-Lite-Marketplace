/**
 * useWebSocketService Hook - WebSocket 服务管理大师！🔌
 * @author BaSui 😎
 * @description 封装 websocketService，管理连接生命周期
 * @fixed 修复无限重连bug - 拆分useEffect避免依赖项循环
 */

import { useState, useEffect, useCallback, useRef } from 'react';
import { websocketService, WebSocketReadyState } from '../utils/websocket';
import { getAccessToken } from '../utils/tokenUtils';

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
 *       console.log('WebSocket 已连接');
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

  // 标记本 Hook 是否曾主动发起连接（用于避免 React StrictMode 下的重复挂载导致的无意义断开）
  const didConnectRef = useRef(false);

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
    didConnectRef.current = true;
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
   * 初始化 WebSocket 事件监听器（只在组件挂载时执行一次）
   * 🔧 BaSui 修复：拆分为独立的effect，避免依赖项循环导致无限重连
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

    // 清理函数（组件卸载时断开连接）
    return () => {
      // 仅当本 Hook 曾主动建立连接时，才在卸载时断开，减少 StrictMode 双调用造成的噪音
      if (didConnectRef.current) {
        disconnect();
        didConnectRef.current = false;
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // ✅ 只在挂载时执行一次，避免无限循环

  /**
   * 响应 autoConnect 变化（独立的 effect）
   * 🔧 BaSui 修复：将autoConnect逻辑拆分到独立effect中
   */
  useEffect(() => {
    if (autoConnect) {
      // ✅ 检查 Token 是否存在，避免未登录时尝试连接
      const token = getAccessToken();
      if (token && !websocketService.isConnected()) {
        console.log('🔌 [useWebSocketService] 自动连接 WebSocket...');
        connect();
      } else if (!token) {
        console.log('⚠️ [useWebSocketService] Token 不存在，跳过 WebSocket 连接');
      }
    } else {
      // autoConnect 为 false 时断开连接
      if (websocketService.isConnected()) {
        console.log('⚠️ [useWebSocketService] 自动断开 WebSocket...');
        disconnect();
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [autoConnect]); // ✅ 只响应 autoConnect 变化

  return {
    isConnected,
    readyState,
    connect,
    disconnect,
  };
};

export default useWebSocketService;
