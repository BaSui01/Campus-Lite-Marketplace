/**
 * 🌓 主题切换组件
 * @author BaSui 😎
 * @description 亮色/暗色主题切换按钮，带平滑动画效果
 * @date 2025-11-09
 */

import React from 'react';
import { useTheme } from '../../hooks/useTheme';
import './index.css';

export interface ThemeToggleProps {
  /** 自定义类名 */
  className?: string;
  /** 自定义样式 */
  style?: React.CSSProperties;
  /** 是否显示文字提示 */
  showLabel?: boolean;
}

/**
 * 主题切换组件
 *
 * @example
 * ```tsx
 * // 基础用法
 * <ThemeToggle />
 *
 * // 带文字提示
 * <ThemeToggle showLabel />
 * ```
 */
export const ThemeToggle: React.FC<ThemeToggleProps> = ({
  className = '',
  style,
  showLabel = false,
}) => {
  const { actualTheme, toggleTheme } = useTheme();
  const isDark = actualTheme === 'dark';

  return (
    <button
      className={`theme-toggle ${isDark ? 'theme-toggle--dark' : 'theme-toggle--light'} ${className}`}
      onClick={toggleTheme}
      style={style}
      aria-label={isDark ? '切换到亮色模式' : '切换到暗色模式'}
      title={isDark ? '切换到亮色模式' : '切换到暗色模式'}
    >
      {/* 🌙 月亮图标（暗色模式） */}
      <span className={`theme-toggle__icon theme-toggle__icon--moon ${isDark ? 'theme-toggle__icon--active' : ''}`}>
        🌙
      </span>

      {/* ☀️ 太阳图标（亮色模式） */}
      <span className={`theme-toggle__icon theme-toggle__icon--sun ${!isDark ? 'theme-toggle__icon--active' : ''}`}>
        ☀️
      </span>

      {/* 滑块 */}
      <span className="theme-toggle__slider" />

      {/* 文字提示（可选） */}
      {showLabel && (
        <span className="theme-toggle__label">
          {isDark ? '暗色' : '亮色'}
        </span>
      )}
    </button>
  );
};

export default ThemeToggle;
