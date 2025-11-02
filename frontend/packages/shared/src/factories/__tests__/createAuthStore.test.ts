import { describe, it, expect, beforeEach, vi } from 'vitest';
import { createAuthStore, type AuthLoginResult } from '../createAuthStore';
import { TOKEN_KEY, REFRESH_TOKEN_KEY } from '../../constants';

interface TestUser {
  id: number;
  name: string;
  permissions?: string[];
  roles?: string[];
}

interface LoginPayload {
  username: string;
  password: string;
}

const createStore = () => {
  const login = vi.fn<[
    LoginPayload
  ], Promise<AuthLoginResult<TestUser>>>(async () => ({
    accessToken: 'mock-token',
    refreshToken: 'mock-refresh',
    user: {
      id: 1,
      name: 'BaSui',
      permissions: ['system:user:view', 'system:user:ban'],
      roles: ['admin'],
    },
  }));

  const logout = vi.fn(async () => {});

  const store = createAuthStore<TestUser, LoginPayload>({
    storageKey: 'test-auth-store',
    login,
    logout,
    permissionConfig: {
      getPermissions: (user) => user?.permissions ?? [],
      getRoles: (user) => user?.roles ?? [],
    },
  });

  return { store, login, logout };
};

describe('createAuthStore', () => {
  beforeEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  it('登录成功后应更新令牌、用户信息与权限', async () => {
    const { store, login } = createStore();

    await store.getState().login({ username: 'admin', password: '123456' });

    expect(login).toHaveBeenCalledTimes(1);

    const state = store.getState();
    expect(state.isAuthenticated).toBe(true);
    expect(state.user?.name).toBe('BaSui');
    expect(state.accessToken).toBe('mock-token');
    expect(state.refreshToken).toBe('mock-refresh');
    expect(state.hasPermission('system:user:view')).toBe(true);
    expect(state.hasAnyPermission(['system:user:ban', 'system:user:create'])).toBe(true);
    expect(state.hasRole('admin')).toBe(true);
    expect(localStorage.getItem(TOKEN_KEY)).toBe('mock-token');
    expect(localStorage.getItem(REFRESH_TOKEN_KEY)).toBe('mock-refresh');
  });

  it('登出应清理状态与 Token', async () => {
    const { store, logout } = createStore();
    await store.getState().login({ username: 'admin', password: '123456' });

    await store.getState().logout();

    const state = store.getState();
    expect(logout).toHaveBeenCalledTimes(1);
    expect(state.isAuthenticated).toBe(false);
    expect(state.user).toBeNull();
    expect(localStorage.getItem(TOKEN_KEY)).toBeNull();
    expect(localStorage.getItem(REFRESH_TOKEN_KEY)).toBeNull();
  });

  it('initFromStorage 应基于现有 Token 恢复用户信息', async () => {
    localStorage.setItem(TOKEN_KEY, 'persisted-token');

    const fetchProfile = vi.fn(async () => ({
      id: 7,
      name: 'PersistedUser',
      permissions: ['system:audit:view'],
      roles: ['auditor'],
    } satisfies TestUser));

    const login: (payload: LoginPayload) => Promise<AuthLoginResult<TestUser>> = async () => {
      throw new Error('login should not be called during init');
    };

    const store = createAuthStore<TestUser, LoginPayload>({
      storageKey: 'test-auth-restore',
      login,
      fetchProfile,
      permissionConfig: {
        getPermissions: (user) => user?.permissions ?? [],
        getRoles: (user) => user?.roles ?? [],
      },
    });

    await store.getState().initFromStorage();

    const state = store.getState();
    expect(fetchProfile).toHaveBeenCalledTimes(1);
    expect(state.user?.id).toBe(7);
    expect(state.isAuthenticated).toBe(true);
    expect(state.hasPermission('system:audit:view')).toBe(true);
    expect(state.hasRole('auditor')).toBe(true);
  });
});
