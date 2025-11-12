/**
 * Timeline 组件单元测试
 * @author BaSui 😎
 * @description 测试时间轴组件的各种功能
 */

import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Timeline } from './Timeline';
import type { TimelineItem } from './types';

describe('Timeline 组件测试', () => {
  // 测试数据
  const mockItems: TimelineItem[] = [
    {
      time: '2024-01-01 10:00',
      title: '订单创建',
      description: '买家下单成功',
      status: 'success',
    },
    {
      time: '2024-01-01 11:00',
      title: '商品发货',
      description: '卖家已发货',
      status: 'success',
    },
    {
      time: '2024-01-02 09:00',
      title: '运输中',
      description: '快递运输中',
      status: 'processing',
    },
    {
      time: '2024-01-03 14:00',
      title: '待签收',
      description: '快递已到达',
      status: 'pending',
    },
  ];

  // ==================== 基础渲染测试 ====================

  describe('基础渲染', () => {
    it('应该正确渲染时间轴', () => {
      const { container } = render(<Timeline items={mockItems} />);
      const timeline = container.querySelector('.timeline');
      expect(timeline).toBeInTheDocument();
    });

    it('应该渲染所有节点', () => {
      const { container } = render(<Timeline items={mockItems} />);
      const items = container.querySelectorAll('.timeline__item');
      expect(items).toHaveLength(4);
    });

    it('应该渲染节点标题', () => {
      render(<Timeline items={mockItems} />);
      expect(screen.getByText('订单创建')).toBeInTheDocument();
      expect(screen.getByText('商品发货')).toBeInTheDocument();
    });

    it('应该渲染节点描述', () => {
      render(<Timeline items={mockItems} />);
      expect(screen.getByText('买家下单成功')).toBeInTheDocument();
      expect(screen.getByText('卖家已发货')).toBeInTheDocument();
    });

    it('应该默认显示时间', () => {
      render(<Timeline items={mockItems} />);
      expect(screen.getByText('2024-01-01 10:00')).toBeInTheDocument();
    });
  });

  // ==================== 方向测试 ====================

  describe('方向', () => {
    it('应该默认使用垂直布局', () => {
      const { container } = render(<Timeline items={mockItems} />);
      const timeline = container.querySelector('.timeline--vertical');
      expect(timeline).toBeInTheDocument();
    });

    it('应该应用垂直布局', () => {
      const { container } = render(
        <Timeline items={mockItems} direction="vertical" />
      );
      const timeline = container.querySelector('.timeline--vertical');
      expect(timeline).toBeInTheDocument();
    });

    it('应该应用水平布局', () => {
      const { container } = render(
        <Timeline items={mockItems} direction="horizontal" />
      );
      const timeline = container.querySelector('.timeline--horizontal');
      expect(timeline).toBeInTheDocument();
    });
  });

  // ==================== 状态测试 ====================

  describe('节点状态', () => {
    it('应该渲染 success 状态', () => {
      const { container } = render(<Timeline items={mockItems} />);
      const successNodes = container.querySelectorAll(
        '.timeline__node--success'
      );
      expect(successNodes.length).toBeGreaterThanOrEqual(2);
    });

    it('应该渲染 processing 状态', () => {
      const { container } = render(<Timeline items={mockItems} />);
      const processingNodes = container.querySelectorAll(
        '.timeline__node--processing'
      );
      expect(processingNodes.length).toBeGreaterThanOrEqual(1);
    });

    it('应该渲染 pending 状态', () => {
      const { container } = render(<Timeline items={mockItems} />);
      const pendingNodes = container.querySelectorAll(
        '.timeline__node--pending'
      );
      expect(pendingNodes.length).toBeGreaterThanOrEqual(1);
    });

    it('应该渲染 error 状态', () => {
      const errorItems: TimelineItem[] = [
        {
          time: '2024-01-01 10:00',
          title: '发货失败',
          status: 'error',
        },
      ];
      const { container } = render(<Timeline items={errorItems} />);
      const errorNode = container.querySelector('.timeline__node--error');
      expect(errorNode).toBeInTheDocument();
    });

    it('应该渲染默认状态', () => {
      const defaultItems: TimelineItem[] = [
        {
          time: '2024-01-01 10:00',
          title: '普通节点',
        },
      ];
      const { container } = render(<Timeline items={defaultItems} />);
      const defaultNode = container.querySelector('.timeline__node--default');
      expect(defaultNode).toBeInTheDocument();
    });
  });

  // ==================== 高亮测试 ====================

  describe('高亮节点', () => {
    it('应该高亮指定节点', () => {
      const { container } = render(
        <Timeline items={mockItems} activeIndex={1} />
      );
      const activeNode = container.querySelector('.timeline__item--active');
      expect(activeNode).toBeInTheDocument();
    });

    it('应该在 activeIndex=-1 时无高亮', () => {
      const { container } = render(
        <Timeline items={mockItems} activeIndex={-1} />
      );
      const activeNode = container.querySelector('.timeline__item--active');
      expect(activeNode).not.toBeInTheDocument();
    });

    it('应该在未设置 activeIndex 时无高亮', () => {
      const { container } = render(<Timeline items={mockItems} />);
      const activeNode = container.querySelector('.timeline__item--active');
      expect(activeNode).not.toBeInTheDocument();
    });
  });

  // ==================== 显示/隐藏时间测试 ====================

  describe('显示时间', () => {
    it('应该默认显示时间', () => {
      render(<Timeline items={mockItems} />);
      expect(screen.getByText('2024-01-01 10:00')).toBeInTheDocument();
    });

    it('应该在 showTime=true 时显示时间', () => {
      render(<Timeline items={mockItems} showTime />);
      expect(screen.getByText('2024-01-01 10:00')).toBeInTheDocument();
    });

    it('应该在 showTime=false 时隐藏时间', () => {
      const { container } = render(<Timeline items={mockItems} showTime={false} />);
      const timeElements = container.querySelectorAll('.timeline__time');
      expect(timeElements).toHaveLength(0);
    });
  });

  // ==================== 自定义图标测试 ====================

  describe('自定义图标', () => {
    it('应该渲染自定义图标', () => {
      const customIcon = <span data-testid="custom-icon">📦</span>;
      const itemsWithIcon: TimelineItem[] = [
        {
          time: '2024-01-01 10:00',
          title: '订单创建',
          icon: customIcon,
        },
      ];
      render(<Timeline items={itemsWithIcon} />);
      expect(screen.getByTestId('custom-icon')).toBeInTheDocument();
    });
  });

  // ==================== 自定义内容测试 ====================

  describe('自定义内容', () => {
    it('应该渲染自定义内容', () => {
      const customContent = (
        <div data-testid="custom-content">
          <h3>自定义标题</h3>
          <p>自定义描述</p>
        </div>
      );
      const itemsWithContent: TimelineItem[] = [
        {
          time: '2024-01-01 10:00',
          title: '默认标题',
          content: customContent,
        },
      ];
      render(<Timeline items={itemsWithContent} />);
      expect(screen.getByTestId('custom-content')).toBeInTheDocument();
      expect(screen.getByText('自定义标题')).toBeInTheDocument();
    });

    it('应该在有自定义内容时不显示默认 title', () => {
      const customContent = <div>自定义内容</div>;
      const itemsWithContent: TimelineItem[] = [
        {
          time: '2024-01-01 10:00',
          title: '默认标题',
          description: '默认描述',
          content: customContent,
        },
      ];
      const { container } = render(<Timeline items={itemsWithContent} />);
      const titleElement = container.querySelector('.timeline__title');
      expect(titleElement).not.toBeInTheDocument();
    });
  });

  // ==================== 连接线测试 ====================

  describe('连接线', () => {
    it('应该默认显示连接线', () => {
      const { container } = render(<Timeline items={mockItems} />);
      const lines = container.querySelectorAll('.timeline__line');
      expect(lines.length).toBeGreaterThanOrEqual(1);
    });

    it('应该在 showLine=false 时隐藏连接线', () => {
      const { container } = render(<Timeline items={mockItems} showLine={false} />);
      const lines = container.querySelectorAll('.timeline__line');
      expect(lines).toHaveLength(0);
    });
  });

  // ==================== 空状态测试 ====================

  describe('空状态', () => {
    it('应该处理空数组', () => {
      const { container } = render(<Timeline items={[]} />);
      const items = container.querySelectorAll('.timeline__item');
      expect(items).toHaveLength(0);
    });
  });

  // ==================== 自定义类名测试 ====================

  describe('自定义类名', () => {
    it('应该应用自定义类名', () => {
      const { container } = render(
        <Timeline items={mockItems} className="custom-timeline" />
      );
      const timeline = container.querySelector('.custom-timeline');
      expect(timeline).toBeInTheDocument();
    });
  });

  // ==================== 边界情况测试 ====================

  describe('边界情况', () => {
    it('应该处理没有描述的节点', () => {
      const itemsWithoutDesc: TimelineItem[] = [
        {
          time: '2024-01-01 10:00',
          title: '只有标题',
        },
      ];
      render(<Timeline items={itemsWithoutDesc} />);
      expect(screen.getByText('只有标题')).toBeInTheDocument();
    });

    it('应该处理所有字段都有值的节点', () => {
      const fullItems: TimelineItem[] = [
        {
          time: '2024-01-01 10:00',
          title: '完整节点',
          description: '包含所有字段',
          status: 'success',
          icon: <span>🎉</span>,
        },
      ];
      render(<Timeline items={fullItems} />);
      expect(screen.getByText('完整节点')).toBeInTheDocument();
      expect(screen.getByText('包含所有字段')).toBeInTheDocument();
    });

    it('应该处理超出范围的 activeIndex', () => {
      const { container } = render(
        <Timeline items={mockItems} activeIndex={999} />
      );
      const timeline = container.querySelector('.timeline');
      expect(timeline).toBeInTheDocument();
    });
  });
});
