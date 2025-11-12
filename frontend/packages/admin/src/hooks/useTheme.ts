/**
 * 主题切换 Hook
 * 
 * 功能：
 * - 明亮/暗黑主题切换
 * - 主题持久化
 * - 系统主题检测
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */

import { useState, useEffect, useCallback } from 'react';

/**
 * 主题类型
 */
export type Theme = 'light' | 'dark' | 'auto';

/**
 * useTheme Hook 返回值
 */
export interface UseThemeResult {
  /** 当前主题 */
  theme: Theme;
  /** 实际应用的主题（auto会自动转换为light或dark） */
  actualTheme: 'light' | 'dark';
  /** 切换主题 */
  setTheme: (theme: Theme) => void;
  /** 切换到明亮主题 */
  setLight: () => void;
  /** 切换到暗黑主题 */
  setDark: () => void;
  /** 切换到自动主题 */
  setAuto: () => void;
  /** 切换主题（在light和dark之间） */
  toggleTheme: () => void;
}

const THEME_STORAGE_KEY = 'admin-theme';

/**
 * 获取系统主题偏好
 */
const getSystemTheme = (): 'light' | 'dark' => {
  if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
    return 'dark';
  }
  return 'light';
};

/**
 * 获取存储的主题
 */
const getStoredTheme = (): Theme => {
  const stored = localStorage.getItem(THEME_STORAGE_KEY);
  if (stored === 'light' || stored === 'dark' || stored === 'auto') {
    return stored;
  }
  return 'light';
};

/**
 * 保存主题到 localStorage
 */
const storeTheme = (theme: Theme) => {
  localStorage.setItem(THEME_STORAGE_KEY, theme);
};

/**
 * 应用主题到 document
 */
const applyTheme = (theme: 'light' | 'dark') => {
  document.documentElement.setAttribute('data-theme', theme);
  
  // 同时更新 body 类名，方便 CSS 使用
  if (theme === 'dark') {
    document.body.classList.add('dark-theme');
    document.body.classList.remove('light-theme');
  } else {
    document.body.classList.add('light-theme');
    document.body.classList.remove('dark-theme');
  }
};

/**
 * 主题切换 Hook
 * 
 * @returns 主题状态和切换方法
 * 
 * @example
 * ```tsx
 * const { theme, actualTheme, setTheme, toggleTheme } = useTheme();
 * 
 * // 显示当前主题
 * <div>当前主题：{actualTheme}</div>
 * 
 * // 切换主题
 * <Button onClick={toggleTheme}>
 *   {actualTheme === 'dark' ? '☀️' : '🌙'}
 * </Button>
 * 
 * // 选择主题
 * <Select value={theme} onChange={setTheme}>
 *   <Option value="light">明亮</Option>
 *   <Option value="dark">暗黑</Option>
 *   <Option value="auto">自动</Option>
 * </Select>
 * ```
 */
export const useTheme = (): UseThemeResult => {
  const [theme, setThemeState] = useState<Theme>(() => getStoredTheme());
  const [systemTheme, setSystemTheme] = useState<'light' | 'dark'>(() => getSystemTheme());

  // 计算实际应用的主题
  const actualTheme = theme === 'auto' ? systemTheme : theme;

  /**
   * 设置主题
   */
  const setTheme = useCallback((newTheme: Theme) => {
    setThemeState(newTheme);
    storeTheme(newTheme);
  }, []);

  /**
   * 切换到明亮主题
   */
  const setLight = useCallback(() => {
    setTheme('light');
  }, [setTheme]);

  /**
   * 切换到暗黑主题
   */
  const setDark = useCallback(() => {
    setTheme('dark');
  }, [setTheme]);

  /**
   * 切换到自动主题
   */
  const setAuto = useCallback(() => {
    setTheme('auto');
  }, [setTheme]);

  /**
   * 在明亮和暗黑之间切换
   */
  const toggleTheme = useCallback(() => {
    setTheme(actualTheme === 'dark' ? 'light' : 'dark');
  }, [actualTheme, setTheme]);

  /**
   * 监听系统主题变化
   */
  useEffect(() => {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');

    const handleChange = (e: MediaQueryListEvent) => {
      setSystemTheme(e.matches ? 'dark' : 'light');
    };

    // 现代浏览器使用 addEventListener
    if (mediaQuery.addEventListener) {
      mediaQuery.addEventListener('change', handleChange);
      return () => mediaQuery.removeEventListener('change', handleChange);
    }
    // 旧浏览器使用 addListener
    else if (mediaQuery.addListener) {
      mediaQuery.addListener(handleChange);
      return () => mediaQuery.removeListener(handleChange);
    }
  }, []);

  /**
   * 应用主题到 DOM
   */
  useEffect(() => {
    applyTheme(actualTheme);
  }, [actualTheme]);

  return {
    theme,
    actualTheme,
    setTheme,
    setLight,
    setDark,
    setAuto,
    toggleTheme,
  };
};
