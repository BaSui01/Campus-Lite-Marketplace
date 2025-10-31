/**
 * Card 组件 - 卡片容器专家！🎴
 * @author BaSui 😎
 * @description 通用卡片组件，支持标题、封面、操作按钮
 */

import React from 'react';
import './Card.css';

/**
 * Card 组件的 Props 接口
 */
export interface CardProps {
  /**
   * 卡片标题
   */
  title?: React.ReactNode;

  /**
   * 卡片副标题
   */
  subtitle?: React.ReactNode;

  /**
   * 卡片封面图片
   */
  cover?: React.ReactNode;

  /**
   * 卡片操作按钮区域
   */
  actions?: React.ReactNode[];

  /**
   * 卡片额外内容（显示在右上角）
   */
  extra?: React.ReactNode;

  /**
   * 是否显示边框
   * @default true
   */
  bordered?: boolean;

  /**
   * 是否显示悬浮效果
   * @default false
   */
  hoverable?: boolean;

  /**
   * 是否加载中
   * @default false
   */
  loading?: boolean;

  /**
   * 卡片内边距大小
   * @default 'default'
   */
  bodyPadding?: 'none' | 'small' | 'default' | 'large';

  /**
   * 卡片点击回调
   */
  onClick?: () => void;

  /**
   * 卡片内容
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
 * Card 组件
 *
 * @example
 * ```tsx
 * // 基础用法
 * <Card title="卡片标题">
 *   <p>这是卡片内容</p>
 * </Card>
 *
 * // 带封面和操作按钮
 * <Card
 *   cover={<img src="/cover.jpg" alt="封面" />}
 *   title="商品名称"
 *   subtitle="¥99.00"
 *   actions={[
 *     <Button type="primary">立即购买</Button>,
 *     <Button>加入购物车</Button>,
 *   ]}
 * >
 *   <p>商品描述信息...</p>
 * </Card>
 *
 * // 可悬浮可点击
 * <Card
 *   title="可点击卡片"
 *   hoverable
 *   onClick={() => navigate('/detail')}
 * >
 *   <p>点击查看详情</p>
 * </Card>
 * ```
 */
export const Card: React.FC<CardProps> = ({
  title,
  subtitle,
  cover,
  actions,
  extra,
  bordered = true,
  hoverable = false,
  loading = false,
  bodyPadding = 'default',
  onClick,
  children,
  className = '',
  style,
}) => {
  // 组装 CSS 类名
  const classNames = [
    'campus-card',
    bordered ? 'campus-card--bordered' : '',
    hoverable ? 'campus-card--hoverable' : '',
    loading ? 'campus-card--loading' : '',
    onClick ? 'campus-card--clickable' : '',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  const bodyClassNames = [
    'campus-card__body',
    `campus-card__body--padding-${bodyPadding}`,
  ].join(' ');

  return (
    <div className={classNames} style={style} onClick={onClick}>
      {/* 封面 */}
      {cover && <div className="campus-card__cover">{cover}</div>}

      {/* 头部 */}
      {(title || subtitle || extra) && (
        <div className="campus-card__header">
          <div className="campus-card__header-content">
            {title && <div className="campus-card__title">{title}</div>}
            {subtitle && <div className="campus-card__subtitle">{subtitle}</div>}
          </div>
          {extra && <div className="campus-card__extra">{extra}</div>}
        </div>
      )}

      {/* 主体内容 */}
      {children && (
        <div className={bodyClassNames}>
          {loading ? (
            <div className="campus-card__loading">
              <div className="campus-card__loading-spinner" />
              <div className="campus-card__loading-text">加载中...</div>
            </div>
          ) : (
            children
          )}
        </div>
      )}

      {/* 操作按钮区域 */}
      {actions && actions.length > 0 && (
        <div className="campus-card__actions">
          {actions.map((action, index) => (
            <div key={index} className="campus-card__action">
              {action}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Card;
