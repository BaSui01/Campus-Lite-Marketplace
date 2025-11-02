/**
 * 统计卡片组件
 *
 * 用于显示单个统计指标（如总用户数、总商品数等）
 *
 * @author BaSui 😎
 * @date 2025-11-01
 */

import React from 'react';
import { Card, Statistic, Typography } from 'antd';
import { ArrowUpOutlined, ArrowDownOutlined } from '@ant-design/icons';
import type { ReactNode } from 'react';
import './StatCard.css';

const { Text } = Typography;

// ========== 类型定义 ==========

export interface StatCardProps {
  title: string;
  value: number | string;
  icon?: ReactNode;
  suffix?: string;
  prefix?: string;
  trend?: number; // 增长率（百分比）
  trendLabel?: string; // 增长率标签（如 "较昨日"）
  color?: string; // 主题色
  loading?: boolean;
}

// ========== 组件 ==========

const StatCard: React.FC<StatCardProps> = ({
  title,
  value,
  icon,
  suffix,
  prefix,
  trend,
  trendLabel = '较昨日',
  color = '#1677ff',
  loading = false,
}) => {
  const isPositive = trend !== undefined && trend > 0;
  const isNegative = trend !== undefined && trend < 0;

  return (
    <Card bordered={false} loading={loading} className="stat-card">
      <div className="stat-card-content">
        <div className="stat-card-left">
          <Text type="secondary" className="stat-card-title">
            {title}
          </Text>
          <Statistic
            value={value}
            suffix={suffix}
            prefix={prefix}
            valueStyle={{ color, fontSize: 28, fontWeight: 600 }}
          />
          {trend !== undefined && (
            <div className="stat-card-trend">
              <Text
                type={isPositive ? 'success' : isNegative ? 'danger' : 'secondary'}
                className="trend-value"
              >
                {isPositive && <ArrowUpOutlined />}
                {isNegative && <ArrowDownOutlined />}
                {Math.abs(trend)}%
              </Text>
              <Text type="secondary" className="trend-label">
                {trendLabel}
              </Text>
            </div>
          )}
        </div>
        {icon && (
          <div className="stat-card-icon" style={{ color }}>
            {icon}
          </div>
        )}
      </div>
    </Card>
  );
};

export default StatCard;
