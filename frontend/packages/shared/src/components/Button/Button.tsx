/**
 * Button 组件 - 按钮界的劳模！💪
 * @author BaSui 😎
 * @description 通用按钮组件，支持多种类型、尺寸和状态
 */

import React from 'react';
import './Button.css';

/**
 * 按钮类型枚举
 * - primary: 主要按钮（蓝色，最显眼！）
 * - default: 默认按钮（灰色，低调奢华）
 * - danger: 危险按钮（红色，小心点击！）
 * - link: 链接按钮（像超链接一样）
 */
export type ButtonType = 'primary' | 'default' | 'danger' | 'link';

/**
 * 按钮尺寸枚举
 * - large: 大号按钮（适合重要操作）
 * - medium: 中号按钮（最常用）
 * - small: 小号按钮（适合紧凑布局）
 */
export type ButtonSize = 'large' | 'medium' | 'small';

/**
 * Button 组件的 Props 接口
 */
export interface ButtonProps extends Omit<React.ButtonHTMLAttributes<HTMLButtonElement>, 'type'> {
  /**
   * 按钮类型
   * @default 'default'
   */
  type?: ButtonType;

  /**
   * HTML button type (submit, reset, button)
   */
  htmlType?: 'submit' | 'reset' | 'button';

  /**
   * ��钮尺寸
   * @default 'medium'
   */
  size?: ButtonSize;

  /**
   * 是否禁用（禁用后变灰，不能点击）
   * @default false
   */
  disabled?: boolean;

  /**
   * 是否加载中（显示加载动画）
   * @default false
   */
  loading?: boolean;

  /**
   * 是否块级按钮（占满父容器宽度）
   * @default false
   */
  block?: boolean;

  /**
   * 按钮图标（可选）
   */
  icon?: React.ReactNode;

  /**
   * 子元素（按钮文字）
   */
  children?: React.ReactNode;

  /**
   * 点击事件处理函数
   */
  onClick?: (event: React.MouseEvent<HTMLButtonElement>) => void;

  /**
   * 自定义类名
   */
  className?: string;

  /**
   * 自定义样式
   */
  style?: React.CSSProperties;
}

/**
 * Button 组件
 *
 * @example
 * ```tsx
 * // 主要按钮
 * <Button type="primary" onClick={() => alert('点我干啥！')}>
 *   点击我
 * </Button>
 *
 * // 危险按钮
 * <Button type="danger" loading>
 *   删除中...
 * </Button>
 *
 * // 带图标的按钮
 * <Button icon={<SearchIcon />}>
 *   搜索
 * </Button>
 * ```
 */
export const Button: React.FC<ButtonProps> = ({
  type = 'default',
  htmlType = 'button',
  size = 'medium',
  disabled = false,
  loading = false,
  block = false,
  icon,
  children,
  onClick,
  className = '',
  style,
  ...rest
}) => {
  // 组装 CSS 类名
  const classNames = [
    'campus-button',
    `campus-button--${type}`,
    `campus-button--${size}`,
    block ? 'campus-button--block' : '',
    loading ? 'campus-button--loading' : '',
    disabled ? 'campus-button--disabled' : '',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  // 处理点击事件
  const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
    // 如果正在加载或禁用，就不处理点击事件
    if (loading || disabled) {
      event.preventDefault();
      return;
    }
    onClick?.(event);
  };

  return (
    <button
      className={classNames}
      type={htmlType}
      disabled={disabled || loading}
      onClick={handleClick}
      style={style}
      {...rest}
    >
      {/* 加载动画（转圈圈） */}
      {loading && <span className="campus-button__spinner">⏳</span>}

      {/* 图标 */}
      {icon && !loading && <span className="campus-button__icon">{icon}</span>}

      {/* 按钮文字 */}
      {children && <span className="campus-button__text">{children}</span>}
    </button>
  );
};

export default Button;
