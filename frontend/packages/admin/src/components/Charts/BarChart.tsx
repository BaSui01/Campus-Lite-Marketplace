/**
 * 柱状图组件
 * @author BaSui 😎
 * @date 2025-11-02
 */

import React from 'react';
import * as echarts from 'echarts/core';
import { BarChart as EChartsBarChart } from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
} from 'echarts/components';
import { CanvasRenderer } from 'echarts/renderers';

// 注册必要组件
echarts.use([
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  EChartsBarChart,
  CanvasRenderer,
]);

export interface BarChartProps {
  data: Array<{ name: string; value: number }>;
  title?: string;
  height?: number;
  color?: string;
}

const BarChart: React.FC<BarChartProps> = ({ data, title, height = 300, color = '#1890ff' }) => {
  const chartRef = React.useRef<HTMLDivElement>(null);
  const chartInstance = React.useRef<echarts.ECharts>();

  React.useEffect(() => {
    if (!chartRef.current) return;

    // 初始化图表
    chartInstance.current = echarts.init(chartRef.current);

    // 配置项
    const option = {
      title: {
        text: title,
        left: 'center',
        textStyle: {
          fontSize: 16,
          fontWeight: 'bold',
        },
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow',
        },
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        data: data.map(item => item.name),
        axisLabel: {
          rotate: 45,
        },
      },
      yAxis: {
        type: 'value',
        axisLabel: {
          formatter: '{value}',
        },
      },
      series: [
        {
          data: data.map(item => item.value),
          type: 'bar',
          barWidth: '60%',
          itemStyle: {
            color,
            borderRadius: [4, 4, 0, 0],
          },
        },
      ],
    };

    chartInstance.current.setOption(option);

    // 响应式
    const handleResize = () => {
      chartInstance.current?.resize();
    };

    window.addEventListener('resize', handleResize);

    return () => {
      window.removeEventListener('resize', handleResize);
      chartInstance.current?.dispose();
    };
  }, [data, title, color]);

  return (
    <div
      ref={chartRef}
      style={{ width: '100%', height: `${height}px` }}
    />
  );
};

export default BarChart;
