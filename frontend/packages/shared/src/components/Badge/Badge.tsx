/**
 * Badge 组件 - 徽标数字专家！🔴
 * @author BaSui 😎
 * @description 通用徽标组件，支持数字徽标、小红点、状态点
 */

import React from 'react';
import './Badge.css';

/**
 * Badge 状态类型
 */
export type BadgeStatus = 'success' | 'error' | 'warning' | 'processing' | 'default';

/**
 * Badge 组件的 Props 接口
 */
export interface BadgeProps {
  /**
   * 徽标数字（超过 overflowCount 显示为 overflowCount+）
   */
  count?: number;

  /**
   * 展示封顶数字
   * @default 99
   */
  overflowCount?: number;

  /**
   * 是否显示小红点
   * @default false
   */
  dot?: boolean;

  /**
   * 是否显示数字为 0 的徽标
   * @default false
   */
  showZero?: boolean;

  /**
   * 状态点模式
   */
  status?: BadgeStatus;

  /**
   * 状态点文本
   */
  text?: React.ReactNode;

  /**
   * 徽标颜色（自定义颜色）
   */
  color?: string;

  /**
   * 徽标内容（自定义内容，优先级高于 count）
   */
  content?: React.ReactNode;

  /**
   * 设置徽标的 z-index
   * @default 10
   */
  zIndex?: number;

  /**
   * 子元素（徽标挂载的元素）
   */
  children?: React.ReactNode;

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
 * Badge 组件
 *
 * @example
 * ```tsx
 * // 基础数字徽标
 * <Badge count={5}>
 *   <Button>消息</Button>
 * </Badge>
 *
 * // 小红点
 * <Badge dot>
 *   <BellIcon />
 * </Badge>
 *
 * // 封顶数字
 * <Badge count={100} overflowCount={99}>
 *   <MailIcon />
 * </Badge>
 *
 * // 状态点
 * <Badge status="success" text="成功" />
 * <Badge status="error" text="错误" />
 * <Badge status="processing" text="进行中" />
 *
 * // 独立使用
 * <Badge count={5} />
 * ```
 */
export const Badge: React.FC<BadgeProps> = ({
  count,
  overflowCount = 99,
  dot = false,
  showZero = false,
  status,
  text,
  color,
  content,
  zIndex = 10,
  children,
  className = '',
  style,
}) => {
  /**
   * 获取显示的徽标内容
   */
  const getBadgeContent = (): React.ReactNode => {
    if (content !== undefined) {
      return content;
    }

    if (dot) {
      return null;
    }

    if (count !== undefined) {
      if (count === 0 && !showZero) {
        return null;
      }
      return count > overflowCount ? `${overflowCount}+` : count;
    }

    return null;
  };

  const badgeContent = getBadgeContent();
  const shouldShowBadge = dot || badgeContent !== null || status;

  // 状态点模式（无子元素）
  if (status && !children) {
    return (
      <span
        className={`campus-badge-status ${className}`}
        style={style}
      >
        <span className={`campus-badge-status__dot campus-badge-status__dot--${status}`} />
        {text && <span className="campus-badge-status__text">{text}</span>}
      </span>
    );
  }

  // 独立徽标模式（无子元素）
  if (!children) {
    if (!shouldShowBadge) return null;

    return (
      <span
        className={`campus-badge-standalone ${dot ? 'campus-badge-standalone--dot' : ''} ${className}`}
        style={{
          backgroundColor: color,
          ...style,
        }}
      >
        {badgeContent}
      </span>
    );
  }

  // 带子元素的徽标模式
  return (
    <span className={`campus-badge ${className}`} style={style}>
      {children}
      {shouldShowBadge && (
        <span
          className={`campus-badge__content ${
            dot ? 'campus-badge__content--dot' : ''
          } ${status ? `campus-badge__content--${status}` : ''}`}
          style={{
            backgroundColor: color,
            zIndex,
          }}
        >
          {badgeContent}
        </span>
      )}
    </span>
  );
};

export default Badge;
