/**
 * 💰 收入趋势图表组件 - BaSui 搞笑专业版 😎
 *
 * 使用 ECharts 绘制柱状图，展示按月统计的收入趋势
 *
 * @author BaSui
 * @date 2025-11-07
 */

import React from 'react';
import ReactECharts from 'echarts-for-react';
import { Empty } from 'antd';

interface RevenueChartProps {
  data: { name: string; value: number }[];
}

/**
 * 收入趋势图表组件
 */
const RevenueChart: React.FC<RevenueChartProps> = ({ data }) => {
  if (!data || data.length === 0) {
    return <Empty description="暂无收入数据" />;
  }

  // 提取月份和收入数据
  const months = data.map(item => item.name);
  const revenues = data.map(item => item.value);

  // ECharts 配置
  const option = {
    title: {
      text: '月度收入统计',
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 'normal',
      },
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow',
      },
      formatter: (params: any) => {
        const item = params[0];
        return `${item.name}<br/>收入: ¥${item.value.toFixed(2)}`;
      },
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      data: months,
      axisLabel: {
        rotate: 45,
        fontSize: 10,
      },
    },
    yAxis: {
      type: 'value',
      name: '收入(¥)',
      axisLabel: {
        formatter: '¥{value}',
      },
    },
    series: [
      {
        name: '收入',
        type: 'bar',
        data: revenues,
        itemStyle: {
          color: '#fa8c16',
          borderRadius: [4, 4, 0, 0],
        },
        label: {
          show: true,
          position: 'top',
          formatter: (params: any) => `¥${params.value.toFixed(2)}`,
          fontSize: 10,
        },
      },
    ],
  };

  return (
    <ReactECharts
      option={option}
      style={{ height: '400px', width: '100%' }}
      opts={{ renderer: 'canvas' }}
    />
  );
};

export default RevenueChart;
