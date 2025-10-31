/**
 * Loading 组件 - 转圈圈专业户！⏳
 * @author BaSui 😎
 * @description 加载动画组件，支持 Spinner 和 Skeleton 两种模式
 */

import React from 'react';
import './Loading.css';

/**
 * 加载动画类型
 * - spinner: 旋转加载动画（转圈圈）
 * - skeleton: 骨架屏加载动画（灰色占位）
 */
export type LoadingType = 'spinner' | 'skeleton';

/**
 * 加载动画尺寸
 */
export type LoadingSize = 'large' | 'medium' | 'small';

/**
 * Loading 组件的 Props 接口
 */
export interface LoadingProps {
  /**
   * 加载动画类型
   * @default 'spinner'
   */
  type?: LoadingType;

  /**
   * 加载动画尺寸
   * @default 'medium'
   */
  size?: LoadingSize;

  /**
   * 是否全屏加载（覆盖整个页面）
   * @default false
   */
  fullscreen?: boolean;

  /**
   * 加载提示文字
   */
  tip?: string;

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
 * Spinner 加载动画组件（转圈圈）
 */
const Spinner: React.FC<{ size: LoadingSize }> = ({ size }) => {
  return (
    <div className={`campus-loading-spinner campus-loading-spinner--${size}`}>
      <div className="campus-loading-spinner__circle"></div>
    </div>
  );
};

/**
 * Skeleton 骨架屏组件
 */
const Skeleton: React.FC<{ size: LoadingSize }> = ({ size }) => {
  return (
    <div className={`campus-loading-skeleton campus-loading-skeleton--${size}`}>
      <div className="campus-loading-skeleton__line"></div>
      <div className="campus-loading-skeleton__line"></div>
      <div className="campus-loading-skeleton__line campus-loading-skeleton__line--short"></div>
    </div>
  );
};

/**
 * Loading 组件
 *
 * @example
 * ```tsx
 * // Spinner 加载动画
 * <Loading type="spinner" tip="加载中..." />
 *
 * // 全屏加载
 * <Loading fullscreen tip="拼命加载中，请稍候..." />
 *
 * // 骨架屏加载
 * <Loading type="skeleton" />
 *
 * // 大号加载动画
 * <Loading size="large" tip="正在加载数据..." />
 * ```
 */
export const Loading: React.FC<LoadingProps> = ({
  type = 'spinner',
  size = 'medium',
  fullscreen = false,
  tip,
  className = '',
  style,
}) => {
  // 组装 CSS 类名
  const classNames = [
    'campus-loading',
    fullscreen ? 'campus-loading--fullscreen' : '',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <div className={classNames} style={style}>
      <div className="campus-loading__content">
        {/* 加载动画 */}
        {type === 'spinner' ? <Spinner size={size} /> : <Skeleton size={size} />}

        {/* 加载提示文字 */}
        {tip && <div className="campus-loading__tip">{tip}</div>}
      </div>
    </div>
  );
};

export default Loading;
