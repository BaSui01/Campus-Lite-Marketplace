/**
 * useThrottle Hook - 节流大师！⚡
 * @author BaSui 😎
 * @description 节流 Hook，限制值更新的频率
 */

import { useState, useEffect, useRef } from 'react';

/**
 * useThrottle Hook
 *
 * @description
 * 节流 Hook，用于限制值更新的频率，在指定时间内最多更新一次。
 * 常用于滚动事件、鼠标移动事件等高频触发场景，提升性能。
 *
 * @template T 值的类型
 * @param value 需要节流的值
 * @param delay 节流间隔时间（毫秒）
 * @returns 节流后的值
 *
 * @example
 * ```tsx
 * // 滚动位置节流
 * function ScrollPosition() {
 *   const [scrollY, setScrollY] = useState(0);
 *   const throttledScrollY = useThrottle(scrollY, 200);
 *
 *   useEffect(() => {
 *     const handleScroll = () => {
 *       setScrollY(window.scrollY);
 *     };
 *
 *     window.addEventListener('scroll', handleScroll);
 *     return () => window.removeEventListener('scroll', handleScroll);
 *   }, []);
 *
 *   return (
 *     <div className={throttledScrollY > 100 ? 'header-fixed' : ''}>
 *       滚动位置: {throttledScrollY}px
 *     </div>
 *   );
 * }
 * ```
 *
 * @example
 * ```tsx
 * // 鼠标位置节流
 * function MouseTracker() {
 *   const [mousePos, setMousePos] = useState({ x: 0, y: 0 });
 *   const throttledPos = useThrottle(mousePos, 100);
 *
 *   useEffect(() => {
 *     const handleMouseMove = (e: MouseEvent) => {
 *       setMousePos({ x: e.clientX, y: e.clientY });
 *     };
 *
 *     window.addEventListener('mousemove', handleMouseMove);
 *     return () => window.removeEventListener('mousemove', handleMouseMove);
 *   }, []);
 *
 *   return (
 *     <div>
 *       鼠标位置: X: {throttledPos.x}, Y: {throttledPos.y}
 *     </div>
 *   );
 * }
 * ```
 *
 * @example
 * ```tsx
 * // 实时搜索节流
 * function LiveSearch() {
 *   const [query, setQuery] = useState('');
 *   const throttledQuery = useThrottle(query, 300);
 *
 *   useEffect(() => {
 *     if (throttledQuery) {
 *       // 执行搜索，但不会太频繁
 *       api.search(throttledQuery).then(setResults);
 *     }
 *   }, [throttledQuery]);
 *
 *   return (
 *     <Input
 *       value={query}
 *       onChange={(e) => setQuery(e.target.value)}
 *       placeholder="实时搜索..."
 *     />
 *   );
 * }
 * ```
 *
 * @example
 * ```tsx
 * // 按钮点击节流（防止重复提交）
 * function SubmitButton() {
 *   const [clickCount, setClickCount] = useState(0);
 *   const throttledCount = useThrottle(clickCount, 1000);
 *
 *   useEffect(() => {
 *     if (throttledCount > 0) {
 *       // 真正执行提交操作
 *       api.submit().then(() => {
 *         toast.success('提交成功！');
 *       });
 *     }
 *   }, [throttledCount]);
 *
 *   return (
 *     <Button onClick={() => setClickCount((c) => c + 1)}>
 *       提交（点击: {clickCount}，执行: {throttledCount}）
 *     </Button>
 *   );
 * }
 * ```
 */
export const useThrottle = <T,>(value: T, delay: number = 500): T => {
  // 节流后的值
  const [throttledValue, setThrottledValue] = useState<T>(value);

  // 上次更新的时间
  const lastUpdateTime = useRef<number>(Date.now());

  // 定时器引用
  const timerRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    const now = Date.now();
    const timeSinceLastUpdate = now - lastUpdateTime.current;

    if (timeSinceLastUpdate >= delay) {
      // 距离上次更新已超过延迟时间，立即更新
      setThrottledValue(value);
      lastUpdateTime.current = now;
    } else {
      // 距离上次更新未超过延迟时间，设置定时器在剩余时间后更新
      if (timerRef.current) {
        clearTimeout(timerRef.current);
      }

      timerRef.current = setTimeout(() => {
        setThrottledValue(value);
        lastUpdateTime.current = Date.now();
      }, delay - timeSinceLastUpdate);
    }

    // 清理函数：清除定时器
    return () => {
      if (timerRef.current) {
        clearTimeout(timerRef.current);
      }
    };
  }, [value, delay]);

  return throttledValue;
};

export default useThrottle;
