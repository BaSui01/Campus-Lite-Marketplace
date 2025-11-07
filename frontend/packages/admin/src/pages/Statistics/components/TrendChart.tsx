/**
 * 📈 趋势图表组件 - BaSui 搞笑专业版 😎
 *
 * 使用 ECharts 绘制多条折线图，展示用户、物品、订单的增长趋势
 *
 * @author BaSui
 * @date 2025-11-07
 */

import React from 'react';
import ReactECharts from 'echarts-for-react';
import type { TrendStatistics } from '@campus/shared/services/statistics';
import { Empty } from 'antd';

interface TrendChartProps {
  data: TrendStatistics | null;
}

/**
 * 趋势图表组件
 */
const TrendChart: React.FC<TrendChartProps> = ({ data }) => {
  if (!data || !data.userTrend.length) {
    return <Empty description="暂无趋势数据" />;
  }

  // 提取日期和数据
  const dates = data.userTrend.map(item => item.date);
  const userCounts = data.userTrend.map(item => item.value);
  const goodsCounts = data.goodsTrend.map(item => item.value);
  const orderCounts = data.orderTrend.map(item => item.value);

  // ECharts 配置
  const option = {
    title: {
      text: '数据趋势分析',
      left: 'center',
      textStyle: {
        fontSize: 16,
        fontWeight: 'normal',
      },
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985',
        },
      },
    },
    legend: {
      data: ['新增用户', '新增物品', '新增订单'],
      bottom: 10,
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      containLabel: true,
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLabel: {
        rotate: 45,
        fontSize: 10,
      },
    },
    yAxis: {
      type: 'value',
      name: '数量',
    },
    series: [
      {
        name: '新增用户',
        type: 'line',
        smooth: true,
        data: userCounts,
        itemStyle: {
          color: '#3f8600',
        },
        areaStyle: {
          color: 'rgba(63, 134, 0, 0.1)',
        },
      },
      {
        name: '新增物品',
        type: 'line',
        smooth: true,
        data: goodsCounts,
        itemStyle: {
          color: '#1890ff',
        },
        areaStyle: {
          color: 'rgba(24, 144, 255, 0.1)',
        },
      },
      {
        name: '新增订单',
        type: 'line',
        smooth: true,
        data: orderCounts,
        itemStyle: {
          color: '#cf1322',
        },
        areaStyle: {
          color: 'rgba(207, 19, 34, 0.1)',
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

export default TrendChart;
