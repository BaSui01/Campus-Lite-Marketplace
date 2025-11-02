/**
 * 统计卡片组件
 * @author BaSui 😎
 * @date 2025-11-02
 */

import React from 'react';
import { Card, Badge } from '@campus/shared';

export interface StatCardProps {
  title: string;
  value: number | string;
  icon?: React.ReactNode;
  color?: string;
  prefix?: string;
  suffix?: string;
  trend?: number;
  trendLabel?: string;
  loading?: boolean;
}

const StatCard: React.FC<StatCardProps> = ({
  title,
  value,
  icon,
  color = '#1890ff',
  prefix,
  suffix,
  trend,
  trendLabel,
  loading,
}) => {
  const trendColor = trend && trend > 0 ? '#52c41a' : '#f5222d';
  const trendIcon = trend && trend > 0 ? '↗' : '↘';
  
  return (
    <Card loading={loading}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <div>
          <div style={{ fontSize: '14px', color: '#999', marginBottom: '8px' }}>{title}</div>
          <div style={{ fontSize: '24px', fontWeight: 'bold', color }}>
            {prefix}
            {value}
            {suffix}
          </div>
          {trend !== undefined && (
            <div style={{ fontSize: '12px', color: trendColor, marginTop: '4px' }}>
              <span>{trendIcon}</span> {Math.abs(trend)}% {trendLabel}
            </div>
          )}
        </div>
        <div style={{ fontSize: '32px', color, opacity: 0.2 }}>
          {icon}
        </div>
      </div>
    </Card>
  );
};

export default StatCard;
