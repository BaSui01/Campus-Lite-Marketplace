/**
 * 多 Tab 登录状态同步
 * @author BaSui 😎
 * @description 使用 BroadcastChannel 实现多标签页登录状态同步
 */

// ==================== 类型定义 ====================

export type SyncEventType = 'LOGIN' | 'LOGOUT' | 'TOKEN_REFRESH' | 'PERMISSION_UPDATE';

export interface SyncEvent {
  type: SyncEventType;
  payload?: any;
  timestamp: number;
}

export interface TabSyncConfig {
  /**
   * BroadcastChannel 名称
   */
  channelName?: string;

  /**
   * 登录回调
   */
  onLogin?: (user: any, token: string) => void;

  /**
   * 登出回调
   */
  onLogout?: () => void;

  /**
   * Token 刷新回调
   */
  onTokenRefresh?: (token: string) => void;

  /**
   * 权限更新回调
   */
  onPermissionUpdate?: (permissions: string[]) => void;

  /**
   * 是否启用调试日志
   */
  debug?: boolean;
}

// ==================== Tab 同步管理器 ====================

class TabSyncManager {
  private channel: BroadcastChannel | null = null;
  private config: Required<TabSyncConfig>;

  constructor(config: TabSyncConfig) {
    this.config = {
      channelName: config.channelName ?? 'auth-sync',
      onLogin: config.onLogin ?? (() => {}),
      onLogout: config.onLogout ?? (() => {}),
      onTokenRefresh: config.onTokenRefresh ?? (() => {}),
      onPermissionUpdate: config.onPermissionUpdate ?? (() => {}),
      debug: config.debug ?? false,
    };
  }

  /**
   * 初始化（创建 BroadcastChannel）
   */
  init(): void {
    if (!this.isBroadcastChannelSupported()) {
      console.warn('[Tab Sync] BroadcastChannel 不支持，多 Tab 同步已禁用');
      return;
    }

    try {
      this.channel = new BroadcastChannel(this.config.channelName);
      this.channel.onmessage = this.handleMessage.bind(this);
      this.log('初始化成功');
    } catch (error) {
      console.error('[Tab Sync] 初始化失败:', error);
    }
  }

  /**
   * 检查浏览器是否支持 BroadcastChannel
   */
  private isBroadcastChannelSupported(): boolean {
    return typeof BroadcastChannel !== 'undefined';
  }

  /**
   * 处理接收到的消息
   */
  private handleMessage(event: MessageEvent<SyncEvent>): void {
    const { type, payload, timestamp } = event.data;

    this.log(`收到消息: ${type}`, payload);

    // 忽略过旧的消息（超过 5 秒）
    if (Date.now() - timestamp > 5000) {
      this.log('忽略过旧的消息', { type, timestamp });
      return;
    }

    switch (type) {
      case 'LOGIN':
        this.config.onLogin(payload.user, payload.token);
        break;

      case 'LOGOUT':
        this.config.onLogout();
        break;

      case 'TOKEN_REFRESH':
        this.config.onTokenRefresh(payload.token);
        break;

      case 'PERMISSION_UPDATE':
        this.config.onPermissionUpdate(payload.permissions);
        break;

      default:
        this.log('未知消息类型', { type });
    }
  }

  /**
   * 广播登录事件
   */
  broadcastLogin(user: any, token: string): void {
    this.postMessage({
      type: 'LOGIN',
      payload: { user, token },
      timestamp: Date.now(),
    });
    this.log('广播登录事件', { user, token });
  }

  /**
   * 广播登出事件
   */
  broadcastLogout(): void {
    this.postMessage({
      type: 'LOGOUT',
      timestamp: Date.now(),
    });
    this.log('广播登出事件');
  }

  /**
   * 广播 Token 刷新事件
   */
  broadcastTokenRefresh(token: string): void {
    this.postMessage({
      type: 'TOKEN_REFRESH',
      payload: { token },
      timestamp: Date.now(),
    });
    this.log('广播 Token 刷新事件', { token });
  }

  /**
   * 广播权限更新事件
   */
  broadcastPermissionUpdate(permissions: string[]): void {
    this.postMessage({
      type: 'PERMISSION_UPDATE',
      payload: { permissions },
      timestamp: Date.now(),
    });
    this.log('广播权限更新事件', { permissions });
  }

  /**
   * 发送消息
   */
  private postMessage(event: SyncEvent): void {
    if (!this.channel) {
      this.log('BroadcastChannel 未初始化，无法发送消息');
      return;
    }

    try {
      this.channel.postMessage(event);
    } catch (error) {
      console.error('[Tab Sync] 发送消息失败:', error);
    }
  }

  /**
   * 调试日志
   */
  private log(message: string, data?: any): void {
    if (this.config.debug) {
      console.log(`[Tab Sync] ${message}`, data ?? '');
    }
  }

  /**
   * 销毁（关闭 BroadcastChannel）
   */
  destroy(): void {
    if (this.channel) {
      this.channel.close();
      this.channel = null;
      this.log('已销毁');
    }
  }
}

// ==================== 单例模式 ====================

let tabSyncInstance: TabSyncManager | null = null;

/**
 * 初始化 Tab 同步
 */
export const initTabSync = (config: TabSyncConfig): TabSyncManager => {
  if (tabSyncInstance) {
    console.warn('[Tab Sync] 已初始化，销毁旧实例');
    tabSyncInstance.destroy();
  }

  tabSyncInstance = new TabSyncManager(config);
  tabSyncInstance.init();

  return tabSyncInstance;
};

/**
 * 获取 Tab 同步实例
 */
export const getTabSync = (): TabSyncManager | null => {
  return tabSyncInstance;
};

/**
 * 销毁 Tab 同步
 */
export const destroyTabSync = (): void => {
  if (tabSyncInstance) {
    tabSyncInstance.destroy();
    tabSyncInstance = null;
  }
};

// ==================== 导出 ====================

export { TabSyncManager };
export default initTabSync;
