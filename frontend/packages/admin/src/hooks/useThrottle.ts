/**
 * 节流 Hook
 * 
 * 功能：
 * - 限制函数执行频率
 * - 适用于滚动、resize 等高频事件
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */

import { useRef, useCallback } from 'react';

/**
 * 节流 Hook - 限制函数执行频率
 * 
 * @param callback - 要节流的函数
 * @param delay - 节流间隔（毫秒），默认 300ms
 * @returns 节流后的函数
 * 
 * @example
 * ```tsx
 * const handleScroll = useThrottle(() => {
 *   console.log('滚动事件');
 * }, 200);
 * 
 * useEffect(() => {
 *   window.addEventListener('scroll', handleScroll);
 *   return () => window.removeEventListener('scroll', handleScroll);
 * }, [handleScroll]);
 * ```
 */
export const useThrottle = <T extends (...args: any[]) => any>(
  callback: T,
  delay: number = 300
): ((...args: Parameters<T>) => void) => {
  const lastRun = useRef<number>(Date.now());

  return useCallback(
    (...args: Parameters<T>) => {
      const now = Date.now();

      if (now - lastRun.current >= delay) {
        callback(...args);
        lastRun.current = now;
      }
    },
    [callback, delay]
  );
};
