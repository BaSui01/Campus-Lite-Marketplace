/**
 * 状态标签组件
 * 
 * 功能：
 * - 根据状态显示不同颜色和图标
 * - 支持自定义状态映射
 * - 统一状态展示样式
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */

import React from 'react';
import { Tag, type TagProps } from 'antd';

/**
 * 状态映射配置
 */
export interface StatusConfig {
  /** 状态文本 */
  text: string;
  /** 标签颜色 */
  color: string;
  /** 图标 */
  icon?: React.ReactNode;
}

/**
 * StatusTag 组件属性
 */
export interface StatusTagProps extends Omit<TagProps, 'color'> {
  /** 状态值 */
  status: string;
  /** 状态映射 */
  statusMap: Record<string, StatusConfig>;
}

/**
 * 状态标签组件
 * 
 * @example
 * ```tsx
 * const STATUS_MAP = {
 *   ACTIVE: { text: '启用', color: 'green', icon: <CheckCircleOutlined /> },
 *   DISABLED: { text: '禁用', color: 'red', icon: <CloseCircleOutlined /> },
 *   PENDING: { text: '待审核', color: 'orange', icon: <SyncOutlined /> },
 * };
 * 
 * <StatusTag status="ACTIVE" statusMap={STATUS_MAP} />
 * ```
 */
export const StatusTag: React.FC<StatusTagProps> = ({
  status,
  statusMap,
  ...tagProps
}) => {
  const config = statusMap[status];

  if (!config) {
    return <Tag {...tagProps}>{status}</Tag>;
  }

  return (
    <Tag color={config.color} icon={config.icon} {...tagProps}>
      {config.text}
    </Tag>
  );
};
