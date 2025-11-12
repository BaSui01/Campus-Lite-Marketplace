/**
 * Tab 同步机制单元测试
 * @author BaSui 😎
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { initTabSync, getTabSync, destroyTabSync, type TabSyncConfig } from '../tabSync';

describe('Tab Sync Manager', () => {
  let mockOnLogin: ReturnType<typeof vi.fn>;
  let mockOnLogout: ReturnType<typeof vi.fn>;
  let mockOnTokenRefresh: ReturnType<typeof vi.fn>;
  let mockOnPermissionUpdate: ReturnType<typeof vi.fn>;

  beforeEach(() => {
    // 创建 mock 函数
    mockOnLogin = vi.fn();
    mockOnLogout = vi.fn();
    mockOnTokenRefresh = vi.fn();
    mockOnPermissionUpdate = vi.fn();

    // 清理之前的实例
    destroyTabSync();
  });

  afterEach(() => {
    vi.clearAllMocks();
    destroyTabSync();
  });

  it('应该成功初始化 Tab 同步', () => {
    const tabSync = initTabSync({
      channelName: 'test-sync',
      onLogin: mockOnLogin,
      onLogout: mockOnLogout,
    });

    expect(tabSync).toBeDefined();
    expect(getTabSync()).toBe(tabSync);
  });

  it('应该在重复初始化时销毁旧实例', () => {
    const firstSync = initTabSync({
      channelName: 'test-sync-1',
      onLogin: mockOnLogin,
    });

    const secondSync = initTabSync({
      channelName: 'test-sync-2',
      onLogin: mockOnLogin,
    });

    expect(firstSync).not.toBe(secondSync);
    expect(getTabSync()).toBe(secondSync);
  });

  it('应该正确广播登录事件', () => {
    const tabSync = initTabSync({
      channelName: 'test-sync',
      onLogin: mockOnLogin,
      debug: true,
    });

    const mockUser = { id: 1, username: 'test' };
    const mockToken = 'test-token';

    tabSync.broadcastLogin(mockUser, mockToken);

    // 注意：在同一个进程中无法真正测试 BroadcastChannel
    // 这里只是验证方法可以正常调用
    expect(mockOnLogin).not.toHaveBeenCalled(); // 不会触发自己的回调
  });

  it('应该正确广播登出事件', () => {
    const tabSync = initTabSync({
      channelName: 'test-sync',
      onLogout: mockOnLogout,
      debug: true,
    });

    tabSync.broadcastLogout();

    // 同样，只验证方法可以正常调用
    expect(mockOnLogout).not.toHaveBeenCalled();
  });

  it('应该正确广播 Token 刷新事件', () => {
    const tabSync = initTabSync({
      channelName: 'test-sync',
      onTokenRefresh: mockOnTokenRefresh,
    });

    const mockToken = 'new-token';
    tabSync.broadcastTokenRefresh(mockToken);

    expect(mockOnTokenRefresh).not.toHaveBeenCalled();
  });

  it('应该正确广播权限更新事件', () => {
    const tabSync = initTabSync({
      channelName: 'test-sync',
      onPermissionUpdate: mockOnPermissionUpdate,
    });

    const mockPermissions = ['read', 'write'];
    tabSync.broadcastPermissionUpdate(mockPermissions);

    expect(mockOnPermissionUpdate).not.toHaveBeenCalled();
  });

  it('应该正确销毁 Tab 同步实例', () => {
    initTabSync({
      channelName: 'test-sync',
      onLogin: mockOnLogin,
    });

    expect(getTabSync()).toBeDefined();

    destroyTabSync();

    expect(getTabSync()).toBeNull();
  });

  it('应该使用默认配置', () => {
    const tabSync = initTabSync({});

    expect(tabSync).toBeDefined();
    
    // 测试默认回调不会抛出错误
    tabSync.broadcastLogin({}, 'token');
    tabSync.broadcastLogout();
    tabSync.broadcastTokenRefresh('token');
    tabSync.broadcastPermissionUpdate([]);
  });

  it('应该支持调试模式', () => {
    const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {});

    const tabSync = initTabSync({
      channelName: 'test-sync',
      debug: true,
    });

    tabSync.broadcastLogin({ id: 1 }, 'token');

    // 在调试模式下应该有日志输出
    expect(consoleSpy).toHaveBeenCalled();

    consoleSpy.mockRestore();
  });
});

describe('BroadcastChannel 支持检测', () => {
  it('应该在不支持 BroadcastChannel 时优雅降级', () => {
    // 暂时删除 BroadcastChannel
    const originalBroadcastChannel = (global as any).BroadcastChannel;
    (global as any).BroadcastChannel = undefined;

    const consoleSpy = vi.spyOn(console, 'warn').mockImplementation(() => {});

    const tabSync = initTabSync({
      channelName: 'test-sync',
      onLogin: vi.fn(),
    });

    expect(tabSync).toBeDefined();
    expect(consoleSpy).toHaveBeenCalledWith(
      expect.stringContaining('BroadcastChannel 不支持')
    );

    // 恢复 BroadcastChannel
    (global as any).BroadcastChannel = originalBroadcastChannel;
    consoleSpy.mockRestore();
  });
});
