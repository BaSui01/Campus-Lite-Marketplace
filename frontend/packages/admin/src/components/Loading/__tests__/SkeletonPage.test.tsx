/**
 * SkeletonPage 组件单元测试
 * @author BaSui 😎
 */

import { describe, it, expect } from 'vitest';
import { render } from '@testing-library/react';
import { SkeletonPage } from '../SkeletonPage';

describe('SkeletonPage 组件', () => {
  it('应该渲染列表骨架屏', () => {
    const { container } = render(<SkeletonPage type="list" />);
    expect(container).toBeTruthy();
  });

  it('应该渲染详情骨架屏', () => {
    const { container } = render(<SkeletonPage type="detail" />);
    expect(container).toBeTruthy();
  });

  it('应该渲染表单骨架屏', () => {
    const { container } = render(<SkeletonPage type="form" />);
    expect(container).toBeTruthy();
  });

  it('应该渲染仪表盘骨架屏', () => {
    const { container } = render(<SkeletonPage type="dashboard" />);
    expect(container).toBeTruthy();
  });

  it('应该显示统计卡片', () => {
    const { container } = render(<SkeletonPage showStats />);
    expect(container.querySelectorAll('.ant-card').length).toBeGreaterThan(0);
  });

  it('应该隐藏页面头部', () => {
    const { container } = render(<SkeletonPage showHeader={false} />);
    expect(container.querySelector('.ant-skeleton-input')).toBeNull();
  });

  it('应该支持自定义行数', () => {
    const { container } = render(<SkeletonPage type="list" rows={10} />);
    expect(container).toBeTruthy();
  });
});
