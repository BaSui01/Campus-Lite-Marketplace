/**
 * 统计卡片组件
 * @author BaSui 😎
 * @date 2025-11-06
 * @description 用于展示统计数据，支持图标、趋势、加载状态
 */

import React from 'react';
import './StatCard.css';

export interface StatCardProps {
  /** 标题 */
  title: string;
  /** 数值 */
  value: number | string;
  /** 图标（React 节点） */
  icon?: React.ReactNode;
  /** 主题色 */
  color?: string;
  /** 前缀（如 ¥） */
  prefix?: string;
  /** 后缀（如 人、件） */
  suffix?: string;
  /** 趋势（正数上涨，负数下跌，0持平） */
  trend?: number;
  /** 趋势标签（如 "较上月"） */
  trendLabel?: string;
  /** 加载状态 */
  loading?: boolean;
}

export const StatCard: React.FC<StatCardProps> = ({
  title,
  value,
  icon,
  color = '#1677ff',
  prefix,
  suffix,
  trend,
  trendLabel,
  loading = false,
}) => {
  return (
    <div className="stat-card">
      {loading ? (
        <div className="stat-card-skeleton">
          <div className="skeleton-line skeleton-title"></div>
          <div className="skeleton-line skeleton-value"></div>
          <div className="skeleton-line skeleton-trend"></div>
        </div>
      ) : (
        <>
          <div className="stat-card-header">
            <span className="stat-card-title">{title}</span>
            {icon && (
              <div 
                className="stat-card-icon" 
                style={{ backgroundColor: color }}
              >
                {icon}
              </div>
            )}
          </div>
          <div className="stat-card-content">
            <div className="stat-card-value">
              {prefix && <span className="stat-card-prefix">{prefix}</span>}
              <span className="stat-card-number">{value}</span>
              {suffix && <span className="stat-card-suffix">{suffix}</span>}
            </div>
            {trend !== undefined && (
              <div className="stat-card-trend">
                {trend > 0 ? (
                  <span className="trend-up">
                    ↑ {trend}%
                  </span>
                ) : trend < 0 ? (
                  <span className="trend-down">
                    ↓ {Math.abs(trend)}%
                  </span>
                ) : (
                  <span className="trend-neutral">持平</span>
                )}
                {trendLabel && <span className="trend-label">{trendLabel}</span>}
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
};

export default StatCard;
