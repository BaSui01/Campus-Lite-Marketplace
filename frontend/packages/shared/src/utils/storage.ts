/**
 * 📦 BaSui 的本地存储工具
 * @description 封装 localStorage 和 sessionStorage，支持过期时间
 */

import { TOKEN_KEY, REFRESH_TOKEN_KEY, USER_INFO_KEY } from '../constants/config';

// ==================== 类型定义 ====================

interface StorageItem<T> {
  value: T;
  expireAt?: number; // 过期时间戳
}

type StorageType = 'local' | 'session';

// ==================== 核心方法 ====================

/**
 * 通用存储方法
 */
const setStorage = <T>(
  key: string,
  value: T,
  type: StorageType = 'local',
  expireInMinutes?: number
): void => {
  const storage = type === 'local' ? localStorage : sessionStorage;

  const item: StorageItem<T> = {
    value,
    expireAt: expireInMinutes ? Date.now() + expireInMinutes * 60 * 1000 : undefined,
  };

  storage.setItem(key, JSON.stringify(item));
};

/**
 * 通用获取方法
 */
const getStorage = <T>(key: string, type: StorageType = 'local'): T | null => {
  const storage = type === 'local' ? localStorage : sessionStorage;

  const itemStr = storage.getItem(key);
  if (!itemStr) return null;

  try {
    const item: StorageItem<T> = JSON.parse(itemStr);

    // 检查是否过期
    if (item.expireAt && Date.now() > item.expireAt) {
      storage.removeItem(key);
      return null;
    }

    return item.value;
  } catch {
    return null;
  }
};

/**
 * 通用删除方法
 */
const removeStorage = (key: string, type: StorageType = 'local'): void => {
  const storage = type === 'local' ? localStorage : sessionStorage;
  storage.removeItem(key);
};

/**
 * 清空存储
 */
const clearStorage = (type: StorageType = 'local'): void => {
  const storage = type === 'local' ? localStorage : sessionStorage;
  storage.clear();
};

// ==================== LocalStorage ====================

export const storage = {
  /**
   * 保存到 LocalStorage
   * @param key 键名
   * @param value 值
   * @param expireInMinutes 过期时间（分钟），不传则永久有效
   */
  set: <T>(key: string, value: T, expireInMinutes?: number): void => {
    setStorage(key, value, 'local', expireInMinutes);
  },

  /**
   * 从 LocalStorage 获取
   */
  get: <T>(key: string): T | null => {
    return getStorage<T>(key, 'local');
  },

  /**
   * 从 LocalStorage 删除
   */
  remove: (key: string): void => {
    removeStorage(key, 'local');
  },

  /**
   * 清空 LocalStorage
   */
  clear: (): void => {
    clearStorage('local');
  },
};

// ==================== SessionStorage ====================

export const sessionStorage_ = {
  /**
   * 保存到 SessionStorage
   */
  set: <T>(key: string, value: T, expireInMinutes?: number): void => {
    setStorage(key, value, 'session', expireInMinutes);
  },

  /**
   * 从 SessionStorage 获取
   */
  get: <T>(key: string): T | null => {
    return getStorage<T>(key, 'session');
  },

  /**
   * 从 SessionStorage 删除
   */
  remove: (key: string): void => {
    removeStorage(key, 'session');
  },

  /**
   * 清空 SessionStorage
   */
  clear: (): void => {
    clearStorage('session');
  },
};

// ==================== 业务快捷方法 ====================

/**
 * Token 管理
 */
export const tokenStorage = {
  /** 保存 Token */
  setTokens: (accessToken: string, refreshToken?: string): void => {
    storage.set(TOKEN_KEY, accessToken);
    if (refreshToken) {
      storage.set(REFRESH_TOKEN_KEY, refreshToken);
    }
  },

  /** 获取 Access Token */
  getAccessToken: (): string | null => {
    return storage.get<string>(TOKEN_KEY);
  },

  /** 获取 Refresh Token */
  getRefreshToken: (): string | null => {
    return storage.get<string>(REFRESH_TOKEN_KEY);
  },

  /** 清除 Token */
  clearTokens: (): void => {
    storage.remove(TOKEN_KEY);
    storage.remove(REFRESH_TOKEN_KEY);
  },

  /** 检查是否已登录 */
  hasToken: (): boolean => {
    return !!tokenStorage.getAccessToken();
  },
};

/**
 * 用户信息管理
 */
export const userStorage = {
  /** 保存用户信息 */
  setUserInfo: <T>(userInfo: T): void => {
    storage.set(USER_INFO_KEY, userInfo);
  },

  /** 获取用户信息 */
  getUserInfo: <T>(): T | null => {
    return storage.get<T>(USER_INFO_KEY);
  },

  /** 清除用户信息 */
  clearUserInfo: (): void => {
    storage.remove(USER_INFO_KEY);
  },
};

// ==================== 独立导出（兼容旧API）====================

/**
 * 保存数据到 LocalStorage
 * @param key 键名
 * @param value 值
 * @param expireInMinutes 过期时间（分钟）
 */
export const setItem = <T>(key: string, value: T, expireInMinutes?: number): void => {
  storage.set(key, value, expireInMinutes);
};

/**
 * 从 LocalStorage 获取数据
 * @param key 键名
 */
export const getItem = <T>(key: string): T | null => {
  return storage.get<T>(key);
};

/**
 * 从 LocalStorage 删除数据
 * @param key 键名
 */
export const removeItem = (key: string): void => {
  storage.remove(key);
};

/**
 * 清空 LocalStorage
 */
export const clearAll = (): void => {
  storage.clear();
};
