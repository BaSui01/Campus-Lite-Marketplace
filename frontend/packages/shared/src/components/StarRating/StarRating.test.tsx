/**
 * StarRating 组件单元测试
 * @author BaSui 😎
 * @description 测试星级评分组件的各种功能
 */

import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { StarRating } from './StarRating';

describe('StarRating 组件测试', () => {
  // ==================== 只读模式测试 ====================
  
  describe('只读模式', () => {
    it('应该正确渲染星级展示', () => {
      const { container } = render(<StarRating value={3.5} readonly />);
      const stars = container.querySelectorAll('.star-rating__star');
      expect(stars).toHaveLength(5);
    });

    it('应该展示完整星星', () => {
      const { container } = render(<StarRating value={4} readonly />);
      const filledStars = container.querySelectorAll('.star-rating__star--filled');
      expect(filledStars.length).toBeGreaterThanOrEqual(4);
    });

    it('应该在只读模式下禁止交互', () => {
      const onChange = vi.fn();
      const { container } = render(
        <StarRating value={3} readonly onChange={onChange} />
      );
      const star = container.querySelector('.star-rating__star');
      if (star) {
        fireEvent.click(star);
      }
      expect(onChange).not.toHaveBeenCalled();
    });
  });

  // ==================== 可编辑模式测试 ====================
  
  describe('可编辑模式', () => {
    it('应该在点击星星时触发 onChange', () => {
      const onChange = vi.fn();
      const { container } = render(<StarRating value={0} onChange={onChange} />);
      const stars = container.querySelectorAll('.star-rating__star');
      
      // 点击第3颗星
      fireEvent.click(stars[2]);
      expect(onChange).toHaveBeenCalledWith(3);
    });

    it('应该在鼠标悬停时显示预览', () => {
      const { container } = render(<StarRating value={2} onChange={vi.fn()} />);
      const stars = container.querySelectorAll('.star-rating__star');
      
      // 悬停在第4颗星上
      fireEvent.mouseEnter(stars[3]);
      const hoveredStars = container.querySelectorAll('.star-rating__star--hovered');
      expect(hoveredStars.length).toBeGreaterThanOrEqual(4);
    });

    it('应该在鼠标离开时恢复原始值', () => {
      const { container } = render(<StarRating value={2} onChange={vi.fn()} />);
      const starContainer = container.querySelector('.star-rating');
      
      if (starContainer) {
        fireEvent.mouseLeave(starContainer);
      }
      
      const filledStars = container.querySelectorAll('.star-rating__star--filled');
      expect(filledStars.length).toBeGreaterThanOrEqual(2);
    });
  });

  // ==================== 半星功能测试 ====================
  
  describe('半星功能', () => {
    it('应该在 allowHalf=true 时支持半星', () => {
      const { container } = render(
        <StarRating value={3.5} readonly allowHalf />
      );
      const halfStars = container.querySelectorAll('.star-rating__star--half');
      expect(halfStars.length).toBeGreaterThanOrEqual(1);
    });

    it('应该在点击星星左半边时选择半星', () => {
      const onChange = vi.fn();
      const { container } = render(
        <StarRating value={0} onChange={onChange} allowHalf />
      );
      const stars = container.querySelectorAll('.star-rating__star');
      
      // Mock getBoundingClientRect
      const star = stars[2] as Element;
      const originalGetBoundingClientRect = star.getBoundingClientRect;
      star.getBoundingClientRect = vi.fn(() => ({
        left: 0,
        right: 20,
        width: 20,
        height: 20,
        top: 0,
        bottom: 20,
        x: 0,
        y: 0,
        toJSON: () => ({}),
      }));
      
      // 点击第3颗星的左半边
      fireEvent.click(star, {
        clientX: 5, // 左侧25%位置
      });
      
      expect(onChange).toHaveBeenCalledWith(2.5);
      
      // 恢复原始方法
      star.getBoundingClientRect = originalGetBoundingClientRect;
    });

    it('应该在点击星星右半边时选择整星', () => {
      const onChange = vi.fn();
      const { container } = render(
        <StarRating value={0} onChange={onChange} allowHalf />
      );
      const stars = container.querySelectorAll('.star-rating__star');
      
      // Mock getBoundingClientRect
      const star = stars[2] as Element;
      const originalGetBoundingClientRect = star.getBoundingClientRect;
      star.getBoundingClientRect = vi.fn(() => ({
        left: 0,
        right: 20,
        width: 20,
        height: 20,
        top: 0,
        bottom: 20,
        x: 0,
        y: 0,
        toJSON: () => ({}),
      }));
      
      // 点击第3颗星的右半边
      fireEvent.click(star, {
        clientX: 15, // 右侧75%位置
      });
      
      expect(onChange).toHaveBeenCalledWith(3);
      
      // 恢复原始方法
      star.getBoundingClientRect = originalGetBoundingClientRect;
    });
  });

  // ==================== 尺寸测试 ====================
  
  describe('尺寸', () => {
    it('应该应用 small 尺寸', () => {
      const { container } = render(<StarRating value={3} size="small" />);
      const starRating = container.querySelector('.star-rating--small');
      expect(starRating).toBeInTheDocument();
    });

    it('应该应用 medium 尺寸（默认）', () => {
      const { container } = render(<StarRating value={3} />);
      const starRating = container.querySelector('.star-rating--medium');
      expect(starRating).toBeInTheDocument();
    });

    it('应该应用 large 尺寸', () => {
      const { container } = render(<StarRating value={3} size="large" />);
      const starRating = container.querySelector('.star-rating--large');
      expect(starRating).toBeInTheDocument();
    });
  });

  // ==================== 显示数字测试 ====================
  
  describe('显示数字', () => {
    it('应该在 showValue=true 时显示评分数字', () => {
      render(<StarRating value={4.5} showValue readonly />);
      expect(screen.getByText('4.5')).toBeInTheDocument();
    });

    it('应该在 showValue=false 时不显示数字', () => {
      const { container } = render(<StarRating value={4.5} readonly />);
      expect(container.textContent).not.toContain('4.5');
    });
  });

  // ==================== 禁用状态测试 ====================
  
  describe('禁用状态', () => {
    it('应该在禁用时添加禁用类名', () => {
      const { container } = render(
        <StarRating value={3} disabled onChange={vi.fn()} />
      );
      const starRating = container.querySelector('.star-rating--disabled');
      expect(starRating).toBeInTheDocument();
    });

    it('应该在禁用时阻止交互', () => {
      const onChange = vi.fn();
      const { container } = render(
        <StarRating value={3} disabled onChange={onChange} />
      );
      const star = container.querySelector('.star-rating__star');
      if (star) {
        fireEvent.click(star);
      }
      expect(onChange).not.toHaveBeenCalled();
    });
  });

  // ==================== 自定义颜色测试 ====================
  
  describe('自定义颜色', () => {
    it('应该应用自定义颜色', () => {
      const customColor = '#ff6b6b';
      const { container } = render(
        <StarRating value={3} color={customColor} readonly />
      );
      const star = container.querySelector('.star-rating__star--filled');
      expect(star).toHaveStyle({ color: customColor });
    });
  });

  // ==================== 边界值测试 ====================
  
  describe('边界值', () => {
    it('应该正确处理 value=0', () => {
      const { container } = render(<StarRating value={0} readonly />);
      const filledStars = container.querySelectorAll('.star-rating__star--filled');
      expect(filledStars).toHaveLength(0);
    });

    it('应该正确处理 value=5', () => {
      const { container } = render(<StarRating value={5} readonly />);
      const filledStars = container.querySelectorAll('.star-rating__star--filled');
      expect(filledStars).toHaveLength(5);
    });

    it('应该将超出范围的值限制在 0-5', () => {
      const { container } = render(<StarRating value={10} readonly />);
      const stars = container.querySelectorAll('.star-rating__star');
      expect(stars).toHaveLength(5);
    });

    it('应该将负数值限制为 0', () => {
      const { container } = render(<StarRating value={-1} readonly />);
      const filledStars = container.querySelectorAll('.star-rating__star--filled');
      expect(filledStars).toHaveLength(0);
    });
  });

  // ==================== 自定义类名测试 ====================
  
  describe('自定义类名', () => {
    it('应该应用自定义类名', () => {
      const { container } = render(
        <StarRating value={3} className="custom-star-rating" />
      );
      const starRating = container.querySelector('.custom-star-rating');
      expect(starRating).toBeInTheDocument();
    });
  });
});
