/**
 * Avatar 组件 - 头像显示专家！👤
 * @author BaSui 😎
 * @description 通用头像组件，支持图片、文字、图标、形状、尺寸
 */

import React, { useState } from 'react';
import './Avatar.css';

/**
 * Avatar 尺寸
 */
export type AvatarSize = 'small' | 'medium' | 'large' | number;

/**
 * Avatar 形状
 */
export type AvatarShape = 'circle' | 'square';

/**
 * Avatar 组件的 Props 接口
 */
export interface AvatarProps {
  /**
   * 图片地址
   */
  src?: string;

  /**
   * 图片无法显示时的替代文本
   */
  alt?: string;

  /**
   * 图标
   */
  icon?: React.ReactNode;

  /**
   * 文本（通常是用户名首字母）
   */
  text?: string;

  /**
   * 尺寸
   * @default 'medium'
   */
  size?: AvatarSize;

  /**
   * 形状
   * @default 'circle'
   */
  shape?: AvatarShape;

  /**
   * 背景颜色
   */
  backgroundColor?: string;

  /**
   * 文字颜色
   */
  color?: string;

  /**
   * 图片加载失败回调
   */
  onError?: () => void;

  /**
   * 点击回调
   */
  onClick?: () => void;

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
 * 默认颜色列表（用于根据文本生成背景色）
 */
const DEFAULT_COLORS = [
  '#1890ff', '#52c41a', '#faad14', '#f5222d', '#722ed1',
  '#13c2c2', '#eb2f96', '#fa8c16', '#a0d911', '#2f54eb',
];

/**
 * 根据文本生成颜色
 */
const getColorByText = (text: string): string => {
  let hash = 0;
  for (let i = 0; i < text.length; i++) {
    hash = text.charCodeAt(i) + ((hash << 5) - hash);
  }
  const color = DEFAULT_COLORS[Math.abs(hash) % DEFAULT_COLORS.length];
  return color || DEFAULT_COLORS[0] || '#1890ff'; // fallback 到第一个颜色或默认蓝色
};

/**
 * Avatar 组件
 *
 * @example
 * ```tsx
 * // 图片头像
 * <Avatar src="/avatar.jpg" alt="用户头像" />
 *
 * // 文字头像
 * <Avatar text="张三" />
 * <Avatar text="AB" backgroundColor="#1890ff" />
 *
 * // 图标头像
 * <Avatar icon={<UserIcon />} />
 *
 * // 不同尺寸
 * <Avatar src="/avatar.jpg" size="small" />
 * <Avatar src="/avatar.jpg" size="medium" />
 * <Avatar src="/avatar.jpg" size="large" />
 * <Avatar src="/avatar.jpg" size={64} />
 *
 * // 不同形状
 * <Avatar src="/avatar.jpg" shape="circle" />
 * <Avatar src="/avatar.jpg" shape="square" />
 * ```
 */
export const Avatar: React.FC<AvatarProps> = ({
  src,
  alt,
  icon,
  text,
  size = 'medium',
  shape = 'circle',
  backgroundColor,
  color,
  onError,
  onClick,
  className = '',
  style,
}) => {
  // 图片加载失败状态
  const [imageLoadFailed, setImageLoadFailed] = useState(false);

  /**
   * 处理图片加载失败
   */
  const handleImageError = () => {
    setImageLoadFailed(true);
    onError?.();
  };

  /**
   * 获取头像内容
   */
  const getAvatarContent = (): React.ReactNode => {
    // 优先显示图片
    if (src && !imageLoadFailed) {
      return <img src={src} alt={alt} onError={handleImageError} className="campus-avatar__image" />;
    }

    // 其次显示图标
    if (icon) {
      return <span className="campus-avatar__icon">{icon}</span>;
    }

    // 最后显示文字
    if (text) {
      // 取前两个字符
      const displayText = text.length > 2 ? text.substring(0, 2) : text;
      return <span className="campus-avatar__text">{displayText}</span>;
    }

    // 默认显示用户图标
    return (
      <span className="campus-avatar__icon">
        <svg viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z" />
        </svg>
      </span>
    );
  };

  // 获取尺寸样式
  const getSizeStyle = (): React.CSSProperties => {
    if (typeof size === 'number') {
      return {
        width: `${size}px`,
        height: `${size}px`,
        fontSize: `${size / 2}px`,
      };
    }
    return {};
  };

  // 获取背景颜色
  const getBackgroundColor = (): string => {
    if (backgroundColor) {
      return backgroundColor;
    }
    if (text) {
      return getColorByText(text);
    }
    return '#d9d9d9';
  };

  // 组装 CSS 类名
  const classNames = [
    'campus-avatar',
    typeof size === 'string' ? `campus-avatar--${size}` : '',
    `campus-avatar--${shape}`,
    onClick ? 'campus-avatar--clickable' : '',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  const avatarStyle: React.CSSProperties = {
    backgroundColor: src && !imageLoadFailed ? 'transparent' : getBackgroundColor(),
    color: color || '#ffffff',
    ...getSizeStyle(),
    ...style,
  };

  return (
    <div className={classNames} style={avatarStyle} onClick={onClick}>
      {getAvatarContent()}
    </div>
  );
};

export default Avatar;
