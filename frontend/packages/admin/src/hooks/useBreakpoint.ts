/**
 * 响应式断点Hook
 * @author BaSui 😎
 * @date 2025-11-06
 */

import { Grid } from 'antd';

const { useBreakpoint: useAntBreakpoint } = Grid;

/**
 * 响应式断点检测
 * 
 * @returns 断点信息和便捷判断
 * 
 * @example
 * const { isMobile, isTablet, isDesktop } = useBreakpoint();
 * 
 * if (isMobile) {
 *   // 手机端逻辑：使用Drawer
 * } else if (isTablet) {
 *   // 平板端逻辑：默认收起Sider
 * } else {
 *   // 桌面端逻辑：展开Sider
 * }
 */
export const useBreakpoint = () => {
  const screens = useAntBreakpoint();

  return {
    screens,
    // 手机端：< 768px
    isMobile: !screens.md,
    // 平板端：768px - 991px
    isTablet: screens.md && !screens.lg,
    // 桌面端：≥ 992px
    isDesktop: screens.lg || false,
  };
};
