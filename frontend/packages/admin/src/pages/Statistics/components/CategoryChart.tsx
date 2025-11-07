/**
 * 📂 分类统计饼图组件 - BaSui 搞笑专业版 😎
 *
 * 使用 ECharts 绘制饼图，展示各分类下的物品数量占比
 *
 * @author BaSui
 * @date 2025-11-07
 */

import React from 'react';
import ReactECharts from 'echarts-for-react';
import { Empty } from 'antd';
import type { CategoryStat } from '@campus/shared/services/statistics';

interface CategoryChartProps {
  data: CategoryStat[];
}

/**
 * 分类统计饼图组件
 */
const CategoryChart: React.FC<CategoryChartProps> = ({ data }) => {
  if (!data || data.length === 0) {
    return <Empty description="暂无分类数据" />;
  }

  // 转换数据格式
  const chartData = data.map(item => ({
    name: item.categoryName,
    value: item.count,
  }));

  // ECharts 配置
  const option = {
    title: {
      text: '分类物品分布',
      left: 'center',
      textStyle: {
        fontSize: 14,
        fontWeight: 'normal',
      },
    },
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)',
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'middle',
      data: data.map(item => item.categoryName),
    },
    series: [
      {
        name: '分类统计',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2,
        },
        label: {
          show: true,
          formatter: '{b}\n{d}%',
          fontSize: 10,
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold',
          },
        },
        data: chartData,
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

export default CategoryChart;
