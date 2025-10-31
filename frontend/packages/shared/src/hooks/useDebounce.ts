/**
 * useDebounce Hook - 防抖大师！⏱️
 * @author BaSui 😎
 * @description 防抖 Hook，延迟更新值直到停止变化一段时间
 */

import { useState, useEffect } from 'react';

/**
 * useDebounce Hook
 *
 * @description
 * 防抖 Hook，用于延迟更新值直到值停止变化一段时间。
 * 常用于搜索输入框、窗口大小调整等场景，避免频繁触发操作。
 *
 * @template T 值的类型
 * @param value 需要防抖的值
 * @param delay 延迟时间（毫秒）
 * @returns 防抖后的值
 *
 * @example
 * ```tsx
 * // 搜索输入框防抖
 * function SearchInput() {
 *   const [searchTerm, setSearchTerm] = useState('');
 *   const debouncedSearchTerm = useDebounce(searchTerm, 500);
 *
 *   useEffect(() => {
 *     if (debouncedSearchTerm) {
 *       // 执行搜索请求
 *       api.search(debouncedSearchTerm).then(setResults);
 *     }
 *   }, [debouncedSearchTerm]);
 *
 *   return (
 *     <Input
 *       value={searchTerm}
 *       onChange={(e) => setSearchTerm(e.target.value)}
 *       placeholder="搜索..."
 *     />
 *   );
 * }
 * ```
 *
 * @example
 * ```tsx
 * // 窗口大小调整防抖
 * function WindowSize() {
 *   const [windowSize, setWindowSize] = useState({
 *     width: window.innerWidth,
 *     height: window.innerHeight,
 *   });
 *   const debouncedSize = useDebounce(windowSize, 200);
 *
 *   useEffect(() => {
 *     const handleResize = () => {
 *       setWindowSize({
 *         width: window.innerWidth,
 *         height: window.innerHeight,
 *       });
 *     };
 *
 *     window.addEventListener('resize', handleResize);
 *     return () => window.removeEventListener('resize', handleResize);
 *   }, []);
 *
 *   return (
 *     <div>
 *       窗口大小: {debouncedSize.width} x {debouncedSize.height}
 *     </div>
 *   );
 * }
 * ```
 *
 * @example
 * ```tsx
 * // 表单字段验证防抖
 * function EmailInput() {
 *   const [email, setEmail] = useState('');
 *   const debouncedEmail = useDebounce(email, 300);
 *   const [isValid, setIsValid] = useState(true);
 *
 *   useEffect(() => {
 *     if (debouncedEmail) {
 *       // 验证邮箱格式
 *       const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
 *       setIsValid(emailRegex.test(debouncedEmail));
 *     }
 *   }, [debouncedEmail]);
 *
 *   return (
 *     <FormItem label="邮箱" error={!isValid ? '邮箱格式不正确' : undefined}>
 *       <Input
 *         value={email}
 *         onChange={(e) => setEmail(e.target.value)}
 *         placeholder="请输入邮箱"
 *       />
 *     </FormItem>
 *   );
 * }
 * ```
 */
export const useDebounce = <T,>(value: T, delay: number = 500): T => {
  // 防抖后的值
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    // 设置定时器，延迟更新防抖值
    const timer = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    // 清理函数：值变化时清除之前的定时器
    return () => {
      clearTimeout(timer);
    };
  }, [value, delay]);

  return debouncedValue;
};

export default useDebounce;
