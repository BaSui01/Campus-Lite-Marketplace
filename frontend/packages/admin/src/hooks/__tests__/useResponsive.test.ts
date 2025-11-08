/**
 * useResponsive Hook 单元测试
 * @author BaSui 😎
 */

import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useResponsive } from '../useResponsive';

describe('useResponsive Hook', () => {
  // 保存原始 innerWidth
  const originalInnerWidth = window.innerWidth;

  beforeEach(() => {
    // 模拟窗口尺寸
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 1920,
    });

    Object.defineProperty(window, 'innerHeight', {
      writable: true,
      configurable: true,
      value: 1080,
    });
  });

  afterEach(() => {
    // 恢复原始值
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: originalInnerWidth,
    });

    vi.clearAllTimers();
  });

  it('应该返回正确的初始断点（桌面端）', () => {
    const { result } = renderHook(() => useResponsive());

    expect(result.current.breakpoint).toBe('xxl');
    expect(result.current.isDesktop).toBe(true);
    expect(result.current.isMobile).toBe(false);
    expect(result.current.isTablet).toBe(false);
  });

  it('应该检测移动端断点', () => {
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 500,
    });

    const { result } = renderHook(() => useResponsive());

    expect(result.current.breakpoint).toBe('xs');
    expect(result.current.isMobile).toBe(true);
    expect(result.current.isDesktop).toBe(false);
  });

  it('应该检测平板断点', () => {
    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 768,
    });

    const { result } = renderHook(() => useResponsive());

    expect(result.current.breakpoint).toBe('md');
    expect(result.current.isTablet).toBe(true);
    expect(result.current.isMobile).toBe(false);
    expect(result.current.isDesktop).toBe(false);
  });

  it('应该返回正确的宽度和高度', () => {
    const { result } = renderHook(() => useResponsive());

    expect(result.current.width).toBe(1920);
    expect(result.current.height).toBe(1080);
  });

  it('应该返回各断点的激活状态', () => {
    const { result } = renderHook(() => useResponsive());

    expect(result.current.breakpoints.xxl).toBe(true);
    expect(result.current.breakpoints.xl).toBe(false);
    expect(result.current.breakpoints.lg).toBe(false);
    expect(result.current.breakpoints.md).toBe(false);
    expect(result.current.breakpoints.sm).toBe(false);
    expect(result.current.breakpoints.xs).toBe(false);
  });

  it('应该支持自定义断点配置', () => {
    const customBreakpoints = {
      md: 800,
      lg: 1000,
    };

    Object.defineProperty(window, 'innerWidth', {
      writable: true,
      configurable: true,
      value: 900,
    });

    const { result } = renderHook(() => useResponsive(customBreakpoints));

    expect(result.current.breakpoint).toBe('md');
  });
});
