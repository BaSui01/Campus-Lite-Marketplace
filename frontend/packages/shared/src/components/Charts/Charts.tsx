/**
 * 图表组件集合
 * @author BaSui 😎
 * @date 2025-11-06
 * @description 简化版图表组件，使用纯 CSS 实现，无需外部图表库依赖
 */

import React from 'react';
import './Charts.css';

export interface ChartData {
  name: string;
  value: number;
}

export interface ChartProps {
  /** 图表数据 */
  data: ChartData[];
  /** 图表高度 */
  height?: number;
  /** 主题色 */
  color?: string;
}

/**
 * 折线图组件（使用柱状图模拟）
 * @description 简化版折线图，使用 CSS 实现
 */
export const LineChart: React.FC<ChartProps> = ({ 
  data, 
  height = 300, 
  color = '#1677ff' 
}) => {
  if (!data || data.length === 0) {
    return (
      <div className="chart-empty" style={{ height }}>
        <div className="empty-icon">📊</div>
        <div className="empty-text">暂无数据</div>
      </div>
    );
  }

  const maxValue = Math.max(...data.map(item => item.value), 1);

  return (
    <div className="chart-container" style={{ height }}>
      <div className="line-chart">
        <div className="line-chart-bars">
          {data.map((item, index) => {
            const barHeight = (item.value / maxValue) * 100;
            return (
              <div key={index} className="line-chart-bar">
                <div className="line-chart-value-label">{item.value}</div>
                <div 
                  className="line-chart-bar-fill"
                  style={{ 
                    height: `${barHeight}%`,
                    backgroundColor: color
                  }}
                />
              </div>
            );
          })}
        </div>
        <div className="line-chart-labels">
          {data.map((item, index) => (
            <div key={index} className="line-chart-label">
              {item.name}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

/**
 * 柱状图组件
 * @description 水平柱状图，使用 CSS 实现
 */
export const BarChart: React.FC<ChartProps> = ({ 
  data, 
  height = 300, 
  color = '#1677ff' 
}) => {
  if (!data || data.length === 0) {
    return (
      <div className="chart-empty" style={{ height }}>
        <div className="empty-icon">📊</div>
        <div className="empty-text">暂无数据</div>
      </div>
    );
  }

  const maxValue = Math.max(...data.map(item => item.value), 1);

  return (
    <div className="chart-container" style={{ height }}>
      <div className="bar-chart">
        {data.map((item, index) => {
          const barWidth = (item.value / maxValue) * 100;
          return (
            <div key={index} className="bar-chart-row">
              <div className="bar-chart-label">{item.name}</div>
              <div className="bar-chart-bar-container">
                <div 
                  className="bar-chart-bar"
                  style={{ 
                    width: `${barWidth}%`,
                    backgroundColor: color
                  }}
                >
                  <span className="bar-chart-value">{item.value}</span>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
