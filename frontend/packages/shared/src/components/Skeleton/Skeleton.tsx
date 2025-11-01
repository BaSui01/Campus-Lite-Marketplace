/**
 * Skeleton 骨架屏组件 - 加载占位符专业户！💀
 * @author BaSui 😎
 * @description 更专业的骨架屏组件，支持多种预设布局（卡片、列表、表单等）
 */

import React from 'react';
import './Skeleton.css';

/**
 * 骨架屏预设类型
 * - text: 文本行（单行或多行）
 * - avatar: 头像（圆形或方形）
 * - image: 图片占位
 * - button: 按钮占位
 * - card: 卡片布局（包含图片+文本）
 * - list: 列表布局（多个列表项）
 * - form: 表单布局（标签+输入框）
 * - custom: 自定义布局
 */
export type SkeletonType = 'text' | 'avatar' | 'image' | 'button' | 'card' | 'list' | 'form' | 'custom';

/**
 * 头像形状
 */
export type AvatarShape = 'circle' | 'square';

/**
 * 动画效果
 */
export type AnimationType = 'wave' | 'pulse' | 'none';

/**
 * Skeleton 组件的 Props 接口
 */
export interface SkeletonProps {
  /**
   * 骨架屏类型
   * @default 'text'
   */
  type?: SkeletonType;

  /**
   * 动画效果
   * @default 'wave'
   */
  animation?: AnimationType;

  /**
   * 是否激活（显示骨架屏）
   * @default true
   */
  active?: boolean;

  /**
   * 文本行数（type='text' 时有效）
   * @default 3
   */
  rows?: number;

  /**
   * 头像形状（type='avatar' 时有效）
   * @default 'circle'
   */
  avatarShape?: AvatarShape;

  /**
   * 头像大小（type='avatar' 时有效）
   * @default 40
   */
  avatarSize?: number;

  /**
   * 列表项数量（type='list' 时有效）
   * @default 3
   */
  count?: number;

  /**
   * 宽度
   */
  width?: string | number;

  /**
   * 高度
   */
  height?: string | number;

  /**
   * 自定义类名
   */
  className?: string;

  /**
   * 自定义样式
   */
  style?: React.CSSProperties;

  /**
   * 子元素（当 active=false 时显示真实内容）
   */
  children?: React.ReactNode;
}

/**
 * 文本行骨架
 */
const TextSkeleton: React.FC<{ rows: number; animation: AnimationType }> = ({ rows, animation }) => {
  return (
    <div className="campus-skeleton-text">
      {Array.from({ length: rows }).map((_, index) => (
        <div
          key={index}
          className={`campus-skeleton-text__line campus-skeleton-text__line--${animation} ${
            index === rows - 1 ? 'campus-skeleton-text__line--last' : ''
          }`}
        />
      ))}
    </div>
  );
};

/**
 * 头像骨架
 */
const AvatarSkeleton: React.FC<{ shape: AvatarShape; size: number; animation: AnimationType }> = ({
  shape,
  size,
  animation,
}) => {
  return (
    <div
      className={`campus-skeleton-avatar campus-skeleton-avatar--${shape} campus-skeleton-avatar--${animation}`}
      style={{ width: size, height: size }}
    />
  );
};

/**
 * 图片骨架
 */
const ImageSkeleton: React.FC<{ width?: string | number; height?: string | number; animation: AnimationType }> = ({
  width,
  height,
  animation,
}) => {
  return (
    <div
      className={`campus-skeleton-image campus-skeleton-image--${animation}`}
      style={{
        width: width || '100%',
        height: height || 200,
      }}
    />
  );
};

/**
 * 按钮骨架
 */
const ButtonSkeleton: React.FC<{ width?: string | number; animation: AnimationType }> = ({ width, animation }) => {
  return (
    <div
      className={`campus-skeleton-button campus-skeleton-button--${animation}`}
      style={{ width: width || 100 }}
    />
  );
};

/**
 * 卡片骨架（图片 + 标题 + 描述）
 */
const CardSkeleton: React.FC<{ animation: AnimationType }> = ({ animation }) => {
  return (
    <div className="campus-skeleton-card">
      <ImageSkeleton animation={animation} height={180} />
      <div className="campus-skeleton-card__content">
        <div className={`campus-skeleton-card__title campus-skeleton-card__title--${animation}`} />
        <TextSkeleton rows={2} animation={animation} />
      </div>
    </div>
  );
};

/**
 * 列表骨架（头像 + 文本）
 */
const ListSkeleton: React.FC<{ count: number; animation: AnimationType }> = ({ count, animation }) => {
  return (
    <div className="campus-skeleton-list">
      {Array.from({ length: count }).map((_, index) => (
        <div key={index} className="campus-skeleton-list__item">
          <AvatarSkeleton shape="circle" size={40} animation={animation} />
          <div className="campus-skeleton-list__content">
            <TextSkeleton rows={2} animation={animation} />
          </div>
        </div>
      ))}
    </div>
  );
};

/**
 * 表单骨架（标签 + 输入框）
 */
const FormSkeleton: React.FC<{ count: number; animation: AnimationType }> = ({ count, animation }) => {
  return (
    <div className="campus-skeleton-form">
      {Array.from({ length: count }).map((_, index) => (
        <div key={index} className="campus-skeleton-form__item">
          <div className={`campus-skeleton-form__label campus-skeleton-form__label--${animation}`} />
          <div className={`campus-skeleton-form__input campus-skeleton-form__input--${animation}`} />
        </div>
      ))}
    </div>
  );
};

/**
 * Skeleton 骨架屏组件
 *
 * @example
 * ```tsx
 * // 文本骨架（3行）
 * <Skeleton type="text" rows={3} />
 *
 * // 头像骨架
 * <Skeleton type="avatar" avatarShape="circle" avatarSize={64} />
 *
 * // 图片骨架
 * <Skeleton type="image" width={300} height={200} />
 *
 * // 卡片骨架
 * <Skeleton type="card" />
 *
 * // 列表骨架（5项）
 * <Skeleton type="list" count={5} />
 *
 * // 表单骨架（4个字段）
 * <Skeleton type="form" count={4} />
 *
 * // 带真实内容的骨架屏（加载完成后显示子元素）
 * <Skeleton active={loading} type="card">
 *   <YourRealContent />
 * </Skeleton>
 *
 * // 脉冲动画
 * <Skeleton animation="pulse" />
 *
 * // 无动画
 * <Skeleton animation="none" />
 * ```
 */
export const Skeleton: React.FC<SkeletonProps> = ({
  type = 'text',
  animation = 'wave',
  active = true,
  rows = 3,
  avatarShape = 'circle',
  avatarSize = 40,
  count = 3,
  width,
  height,
  className = '',
  style,
  children,
}) => {
  // 如果不激活，直接显示子元素
  if (!active) {
    return <>{children}</>;
  }

  // 组装 CSS 类名
  const classNames = ['campus-skeleton', className].filter(Boolean).join(' ');

  // 渲染不同类型的骨架屏
  const renderSkeleton = () => {
    switch (type) {
      case 'text':
        return <TextSkeleton rows={rows} animation={animation} />;
      case 'avatar':
        return <AvatarSkeleton shape={avatarShape} size={avatarSize} animation={animation} />;
      case 'image':
        return <ImageSkeleton width={width} height={height} animation={animation} />;
      case 'button':
        return <ButtonSkeleton width={width} animation={animation} />;
      case 'card':
        return <CardSkeleton animation={animation} />;
      case 'list':
        return <ListSkeleton count={count} animation={animation} />;
      case 'form':
        return <FormSkeleton count={count} animation={animation} />;
      case 'custom':
        return children;
      default:
        return <TextSkeleton rows={rows} animation={animation} />;
    }
  };

  return (
    <div className={classNames} style={style}>
      {renderSkeleton()}
    </div>
  );
};

export default Skeleton;
