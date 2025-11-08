/**
 * 防抖 Hook
 * 
 * 功能：
 * - 延迟执行函数
 * - 自动取消未执行的调用
 * - 适用于搜索、输入等场景
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */

import { useState, useEffect } from 'react';

/**
 * 防抖 Hook - 延迟更新值
 * 
 * @param value - 原始值
 * @param delay - 延迟时间（毫秒），默认 300ms
 * @returns 防抖后的值
 * 
 * @example
 * ```tsx
 * const [keyword, setKeyword] = useState('');
 * const debouncedKeyword = useDebounce(keyword, 500);
 * 
 * // 只有当用户停止输入 500ms 后，才会触发查询
 * useEffect(() => {
 *   fetchData(debouncedKeyword);
 * }, [debouncedKeyword]);
 * 
 * <Input
 *   value={keyword}
 *   onChange={(e) => setKeyword(e.target.value)}
 *   placeholder="搜索..."
 * />
 * ```
 */
export const useDebounce = <T>(value: T, delay: number = 300): T => {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    // 设置定时器
    const timer = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    // 清理函数：组件卸载或 value 变化时取消定时器
    return () => {
      clearTimeout(timer);
    };
  }, [value, delay]);

  return debouncedValue;
};
