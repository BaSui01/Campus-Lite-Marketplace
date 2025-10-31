/**
 * Tag 组件 - 标签标记专家！🏷���
 * @author BaSui 😎
 * @description 通用标签组件，支持多种颜色、尺寸、可关闭
 */

import React, { useState } from 'react';
import './Tag.css';

/**
 * Tag 颜色类型
 */
export type TagColor =
  | 'default'
  | 'primary'
  | 'success'
  | 'warning'
  | 'error'
  | 'processing'
  | string;

/**
 * Tag 尺寸
 */
export type TagSize = 'small' | 'medium' | 'large';

/**
 * Tag 组件的 Props 接口
 */
export interface TagProps {
  /**
   * 标签颜色（预设颜色或自定义颜色值）
   */
  color?: TagColor;

  /**
   * 标签尺寸
   * @default 'medium'
   */
  size?: TagSize;

  /**
   * 是否可关闭
   * @default false
   */
  closable?: boolean;

  /**
   * 是否显示边框
   * @default true
   */
  bordered?: boolean;

  /**
   * 图标
   */
  icon?: React.ReactNode;

  /**
   * 是否可见（受控）
   */
  visible?: boolean;

  /**
   * 关闭回调
   */
  onClose?: (e: React.MouseEvent) => void;

  /**
   * 点击回调
   */
  onClick?: (e: React.MouseEvent) => void;

  /**
   * 标签内容
   */
  children: React.ReactNode;

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
 * 预设颜色映射
 */
const PRESET_COLORS: Record<string, { bg: string; border: string; color: string }> = {
  default: { bg: '#fafafa', border: '#d9d9d9', color: '#00000073' },
  primary: { bg: '#e6f7ff', border: '#91d5ff', color: '#1890ff' },
  success: { bg: '#f6ffed', border: '#b7eb8f', color: '#52c41a' },
  warning: { bg: '#fffbe6', border: '#ffe58f', color: '#faad14' },
  error: { bg: '#fff2f0', border: '#ffccc7', color: '#ff4d4f' },
  processing: { bg: '#e6f7ff', border: '#91d5ff', color: '#1890ff' },
};

/**
 * Tag 组件
 *
 * @example
 * ```tsx
 * // 基础用法
 * <Tag>默认标签</Tag>
 *
 * // 不同颜色
 * <Tag color="primary">主要</Tag>
 * <Tag color="success">成功</Tag>
 * <Tag color="warning">警告</Tag>
 * <Tag color="error">错误</Tag>
 *
 * // 自定义颜色
 * <Tag color="#f50">#f50</Tag>
 * <Tag color="purple">紫色</Tag>
 *
 * // 可关闭
 * <Tag closable onClose={() => console.log('关闭')}>
 *   可关闭标签
 * </Tag>
 *
 * // 带图标
 * <Tag icon={<CheckIcon />} color="success">
 *   已完成
 * </Tag>
 *
 * // 不同尺寸
 * <Tag size="small">小号</Tag>
 * <Tag size="medium">中号</Tag>
 * <Tag size="large">大号</Tag>
 * ```
 */
export const Tag: React.FC<TagProps> = ({
  color = 'default',
  size = 'medium',
  closable = false,
  bordered = true,
  icon,
  visible: controlledVisible,
  onClose,
  onClick,
  children,
  className = '',
  style,
}) => {
  // 内部可见状态
  const [internalVisible, setInternalVisible] = useState(true);

  // 实际可见状态（受控/非受控）
  const visible = controlledVisible !== undefined ? controlledVisible : internalVisible;

  /**
   * 处理关闭
   */
  const handleClose = (e: React.MouseEvent) => {
    e.stopPropagation();

    if (controlledVisible === undefined) {
      setInternalVisible(false);
    }

    onClose?.(e);
  };

  /**
   * 获取颜色样式
   */
  const getColorStyle = (): React.CSSProperties => {
    const presetColor = PRESET_COLORS[color];

    if (presetColor) {
      return {
        backgroundColor: presetColor.bg,
        borderColor: presetColor.border,
        color: presetColor.color,
      };
    }

    // 自定义颜色
    return {
      backgroundColor: color,
      borderColor: color,
      color: '#ffffff',
    };
  };

  if (!visible) {
    return null;
  }

  // 组装 CSS 类名
  const classNames = [
    'campus-tag',
    `campus-tag--${size}`,
    bordered ? 'campus-tag--bordered' : '',
    onClick ? 'campus-tag--clickable' : '',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  const tagStyle: React.CSSProperties = {
    ...getColorStyle(),
    ...style,
  };

  return (
    <span className={classNames} style={tagStyle} onClick={onClick}>
      {icon && <span className="campus-tag__icon">{icon}</span>}
      <span className="campus-tag__content">{children}</span>
      {closable && (
        <span className="campus-tag__close" onClick={handleClose}>
          ×
        </span>
      )}
    </span>
  );
};

export default Tag;
