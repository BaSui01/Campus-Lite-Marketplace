/**
 * Timeline - 时间轴组件
 * @author BaSui 😎
 * @description 支持垂直/水平布局、多种状态、自定义图标的时间轴组件
 */

import React, { useMemo } from 'react';
import type { TimelineProps, TimelineItem, TimelineStatus } from './types';
import './Timeline.css';

/**
 * 默认图标组件（圆点）
 */
const DefaultIcon: React.FC<{ status?: TimelineStatus }> = ({ status = 'default' }) => {
  return <div className={`timeline__node timeline__node--${status}`} />;
};

/**
 * 时间轴组件
 */
export const Timeline: React.FC<TimelineProps> = ({
  items,
  direction = 'vertical',
  activeIndex = -1,
  showTime = true,
  className = '',
  showLine = true,
}) => {
  // 容器类名
  const containerClassName = useMemo(() => {
    const classes = [
      'timeline',
      `timeline--${direction}`,
      className,
    ];
    return classes.filter(Boolean).join(' ');
  }, [direction, className]);

  // 渲染单个节点
  const renderItem = (item: TimelineItem, index: number) => {
    const isActive = index === activeIndex;
    const itemClassName = [
      'timeline__item',
      isActive && 'timeline__item--active',
    ]
      .filter(Boolean)
      .join(' ');

    return (
      <div key={index} className={itemClassName}>
        {/* 节点图标 */}
        <div className="timeline__node-wrapper">
          {item.icon ? (
            <div className="timeline__node timeline__node--custom">
              {item.icon}
            </div>
          ) : (
            <DefaultIcon status={item.status} />
          )}
          
          {/* 连接线 */}
          {showLine && index < items.length - 1 && (
            <div className="timeline__line" />
          )}
        </div>

        {/* 内容区域 */}
        <div className="timeline__content">
          {/* 时间 */}
          {showTime && item.time && (
            <div className="timeline__time">{item.time}</div>
          )}

          {/* 自定义内容 或 默认内容 */}
          {item.content ? (
            <div className="timeline__custom-content">{item.content}</div>
          ) : (
            <>
              {/* 标题 */}
              <div className="timeline__title">{item.title}</div>
              
              {/* 描述 */}
              {item.description && (
                <div className="timeline__description">{item.description}</div>
              )}
            </>
          )}
        </div>
      </div>
    );
  };

  return (
    <div className={containerClassName}>
      {items.map((item, index) => renderItem(item, index))}
    </div>
  );
};

export default Timeline;
