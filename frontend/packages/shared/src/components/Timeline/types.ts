/**
 * Timeline 组件类型定义
 * @author BaSui 😎
 * @description 时间轴组件的 TypeScript 类型
 */

import type { ReactNode } from 'react';

/**
 * 时间轴节点状态
 */
export type TimelineStatus = 'pending' | 'processing' | 'success' | 'error' | 'default';

/**
 * 时间轴方向
 */
export type TimelineDirection = 'vertical' | 'horizontal';

/**
 * 时间轴节点数据
 */
export interface TimelineItem {
  /**
   * 时间
   */
  time: string;

  /**
   * 标题
   */
  title: string;

  /**
   * 描述
   */
  description?: string;

  /**
   * 自定义图标
   */
  icon?: ReactNode;

  /**
   * 节点状态
   * @default 'default'
   */
  status?: TimelineStatus;

  /**
   * 自定义内容（会替换默认的 title + description）
   */
  content?: ReactNode;
}

/**
 * Timeline 组件 Props
 */
export interface TimelineProps {
  /**
   * 时间轴数据
   */
  items: TimelineItem[];

  /**
   * 方向
   * @default 'vertical'
   */
  direction?: TimelineDirection;

  /**
   * 当前高亮节点索引（-1 表示无高亮）
   * @default -1
   */
  activeIndex?: number;

  /**
   * 是否显示时间
   * @default true
   */
  showTime?: boolean;

  /**
   * 自定义类名
   */
  className?: string;

  /**
   * 是否显示连接线
   * @default true
   */
  showLine?: boolean;
}
