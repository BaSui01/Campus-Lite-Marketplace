/**
 * 响应式布局 Hook
 * 
 * 功能：
 * - 监听窗口尺寸变化
 * - 返回当前断点
 * - 支持自定义断点
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */

import { useState, useEffect } from 'react';

/**
 * 屏幕断点类型
 */
export type Breakpoint = 'xs' | 'sm' | 'md' | 'lg' | 'xl' | 'xxl';

/**
 * 断点配置
 */
export interface BreakpointConfig {
  xs: number;  // < 576px
  sm: number;  // >= 576px
  md: number;  // >= 768px
  lg: number;  // >= 992px
  xl: number;  // >= 1200px
  xxl: number; // >= 1600px
}

/**
 * 默认断点配置（Ant Design 断点）
 */
const DEFAULT_BREAKPOINTS: BreakpointConfig = {
  xs: 0,
  sm: 576,
  md: 768,
  lg: 992,
  xl: 1200,
  xxl: 1600,
};

/**
 * useResponsive Hook 返回值
 */
export interface UseResponsiveResult {
  /** 当前断点 */
  breakpoint: Breakpoint;
  /** 当前窗口宽度 */
  width: number;
  /** 当前窗口高度 */
  height: number;
  /** 是否为移动端 */
  isMobile: boolean;
  /** 是否为平板 */
  isTablet: boolean;
  /** 是否为桌面端 */
  isDesktop: boolean;
  /** 各断点是否激活 */
  breakpoints: Record<Breakpoint, boolean>;
}

/**
 * 获取当前断点
 */
const getBreakpoint = (width: number, config: BreakpointConfig): Breakpoint => {
  if (width >= config.xxl) return 'xxl';
  if (width >= config.xl) return 'xl';
  if (width >= config.lg) return 'lg';
  if (width >= config.md) return 'md';
  if (width >= config.sm) return 'sm';
  return 'xs';
};

/**
 * 响应式布局 Hook
 * 
 * @param customBreakpoints - 自定义断点配置
 * @returns 响应式状态
 * 
 * @example
 * ```tsx
 * const { breakpoint, isMobile, isDesktop } = useResponsive();
 * 
 * if (isMobile) {
 *   return <MobileLayout />;
 * }
 * 
 * if (isDesktop) {
 *   return <DesktopLayout />;
 * }
 * 
 * // 根据断点渲染不同布局
 * const columns = breakpoint === 'xs' ? 1 : breakpoint === 'sm' ? 2 : 4;
 * ```
 */
export const useResponsive = (
  customBreakpoints?: Partial<BreakpointConfig>
): UseResponsiveResult => {
  const breakpoints = { ...DEFAULT_BREAKPOINTS, ...customBreakpoints };

  const [state, setState] = useState<{
    width: number;
    height: number;
    breakpoint: Breakpoint;
  }>(() => {
    const width = window.innerWidth;
    const height = window.innerHeight;
    return {
      width,
      height,
      breakpoint: getBreakpoint(width, breakpoints),
    };
  });

  useEffect(() => {
    let timeoutId: NodeJS.Timeout;

    const handleResize = () => {
      // 使用防抖优化性能
      clearTimeout(timeoutId);
      timeoutId = setTimeout(() => {
        const width = window.innerWidth;
        const height = window.innerHeight;
        setState({
          width,
          height,
          breakpoint: getBreakpoint(width, breakpoints),
        });
      }, 150);
    };

    window.addEventListener('resize', handleResize);
    return () => {
      window.removeEventListener('resize', handleResize);
      clearTimeout(timeoutId);
    };
  }, [breakpoints]);

  return {
    breakpoint: state.breakpoint,
    width: state.width,
    height: state.height,
    isMobile: state.breakpoint === 'xs',
    isTablet: state.breakpoint === 'sm' || state.breakpoint === 'md',
    isDesktop: state.breakpoint === 'lg' || state.breakpoint === 'xl' || state.breakpoint === 'xxl',
    breakpoints: {
      xs: state.breakpoint === 'xs',
      sm: state.breakpoint === 'sm',
      md: state.breakpoint === 'md',
      lg: state.breakpoint === 'lg',
      xl: state.breakpoint === 'xl',
      xxl: state.breakpoint === 'xxl',
    },
  };
};
