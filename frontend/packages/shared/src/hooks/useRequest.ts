/**
 * useRequest Hook - API 请求封装专家！🚀
 * @author BaSui 😎
 * @description 封装 API 请求，提供加载状态、错误处理、自动重试等功能
 */

import { useState, useCallback, useEffect, useRef } from 'react';

/**
 * useRequest 配置接口
 */
export interface UseRequestOptions<T, P extends any[]> {
  /**
   * 是否立即执行
   * @default true
   */
  immediate?: boolean;

  /**
   * 成功回调
   */
  onSuccess?: (data: T) => void;

  /**
   * 失败回调
   */
  onError?: (error: Error) => void;

  /**
   * 重试次数
   * @default 0
   */
  retryCount?: number;

  /**
   * 重试延迟（毫秒）
   * @default 1000
   */
  retryDelay?: number;

  /**
   * 防抖延迟（毫秒）
   * @default 0
   */
  debounceDelay?: number;

  /**
   * 节流延迟（毫秒）
   * @default 0
   */
  throttleDelay?: number;

  /**
   * 请求依赖项（类似 useEffect 的依赖数组）
   * 当依赖项变化时，自动重新请求
   */
  deps?: any[];
}

/**
 * useRequest 返回值接口
 */
export interface UseRequestResult<T, P extends any[]> {
  /**
   * 响应数据
   */
  data: T | null;

  /**
   * 错误信息
   */
  error: Error | null;

  /**
   * 是否正在加载
   */
  loading: boolean;

  /**
   * 手动执行请求
   */
  run: (...params: P) => Promise<void>;

  /**
   * 取消请求
   */
  cancel: () => void;

  /**
   * 刷新（使用上次的参数重新请求）
   */
  refresh: () => Promise<void>;

  /**
   * 重置状态
   */
  reset: () => void;
}

/**
 * useRequest Hook
 *
 * @description API 请求封装 Hook，提供加载状态、错误处理、自动重试、防抖节流等功能
 *
 * @example
 * ```tsx
 * // 基础用法
 * function UserList() {
 *   const { data, loading, error, run } = useRequest(
 *     async () => {
 *       const response = await api.listUsers();
 *       return response.data.data;
 *     },
 *     { immediate: true }
 *   );
 *
 *   if (loading) return <Loading />;
 *   if (error) return <div>加载失败：{error.message}</div>;
 *
 *   return (
 *     <div>
 *       {data?.map(user => <div key={user.id}>{user.name}</div>)}
 *       <Button onClick={run}>刷新</Button>
 *     </div>
 *   );
 * }
 * ```
 *
 * @example
 * ```tsx
 * // 带参数的请求
 * function UserDetail({ userId }: { userId: number }) {
 *   const { data, loading, run } = useRequest(
 *     async (id: number) => {
 *       const response = await api.getUserProfile({ userId: id });
 *       return response.data.data;
 *     },
 *     { immediate: false }
 *   );
 *
 *   useEffect(() => {
 *     run(userId);
 *   }, [userId]);
 *
 *   return <div>{data?.name}</div>;
 * }
 * ```
 *
 * @example
 * ```tsx
 * // 自动重试
 * function DataFetch() {
 *   const { data, loading, error } = useRequest(
 *     async () => {
 *       const response = await api.fetchData();
 *       return response.data.data;
 *     },
 *     {
 *       immediate: true,
 *       retryCount: 3,
 *       retryDelay: 1000,
 *       onSuccess: (data) => toast.success('加载成功！'),
 *       onError: (error) => toast.error('加载失败！'),
 *     }
 *   );
 *
 *   return <div>{data}</div>;
 * }
 * ```
 */
export const useRequest = <T, P extends any[] = any[]>(
  requestFn: (...params: P) => Promise<T>,
  options: UseRequestOptions<T, P> = {}
): UseRequestResult<T, P> => {
  const {
    immediate = true,
    onSuccess,
    onError,
    retryCount = 0,
    retryDelay = 1000,
    debounceDelay = 0,
    throttleDelay = 0,
    deps = [],
  } = options;

  // 响应数据状态
  const [data, setData] = useState<T | null>(null);

  // 错误状态
  const [error, setError] = useState<Error | null>(null);

  // 加载状态
  const [loading, setLoading] = useState(false);

  // 记录上次请求的参数（用于 refresh）
  const lastParamsRef = useRef<P | null>(null);

  // 取消标志
  const canceledRef = useRef(false);

  // 防抖定时器
  const debounceTimerRef = useRef<NodeJS.Timeout | null>(null);

  // 节流定时器
  const throttleTimerRef = useRef<NodeJS.Timeout | null>(null);

  /**
   * 执行请求（带重试逻辑）
   */
  const executeRequest = useCallback(
    async (...params: P): Promise<void> => {
      // 如果已取消，直接返回
      if (canceledRef.current) {
        return;
      }

      setLoading(true);
      setError(null);
      lastParamsRef.current = params;

      let currentRetry = 0;

      while (currentRetry <= retryCount) {
        try {
          const result = await requestFn(...params);

          // 检查是否已取消
          if (canceledRef.current) {
            return;
          }

          setData(result);
          setError(null);
          onSuccess?.(result);
          break; // 成功，跳出循环
        } catch (err) {
          const error = err instanceof Error ? err : new Error(String(err));

          // 如果是最后一次重试，设置错误状态
          if (currentRetry === retryCount) {
            if (!canceledRef.current) {
              setError(error);
              onError?.(error);
            }
          } else {
            // 否则，等待后重试
            await new Promise((resolve) => setTimeout(resolve, retryDelay));
          }

          currentRetry++;
        } finally {
          if (!canceledRef.current && currentRetry > retryCount) {
            setLoading(false);
          }
        }
      }
    },
    [requestFn, retryCount, retryDelay, onSuccess, onError]
  );

  /**
   * 防抖执行
   */
  const debouncedRun = useCallback(
    (...params: P) => {
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current);
      }

      return new Promise<void>((resolve) => {
        debounceTimerRef.current = setTimeout(() => {
          executeRequest(...params).then(resolve);
        }, debounceDelay);
      });
    },
    [executeRequest, debounceDelay]
  );

  /**
   * 节流执行
   */
  const throttledRun = useCallback(
    (...params: P) => {
      if (throttleTimerRef.current) {
        return Promise.resolve();
      }

      throttleTimerRef.current = setTimeout(() => {
        throttleTimerRef.current = null;
      }, throttleDelay);

      return executeRequest(...params);
    },
    [executeRequest, throttleDelay]
  );

  /**
   * 手动执行请求
   */
  const run = useCallback(
    (...params: P): Promise<void> => {
      canceledRef.current = false;

      if (debounceDelay > 0) {
        return debouncedRun(...params);
      }

      if (throttleDelay > 0) {
        return throttledRun(...params);
      }

      return executeRequest(...params);
    },
    [executeRequest, debouncedRun, throttledRun, debounceDelay, throttleDelay]
  );

  /**
   * 取消请求
   */
  const cancel = useCallback(() => {
    canceledRef.current = true;
    setLoading(false);

    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
      debounceTimerRef.current = null;
    }

    if (throttleTimerRef.current) {
      clearTimeout(throttleTimerRef.current);
      throttleTimerRef.current = null;
    }
  }, []);

  /**
   * 刷新（使用上次的参数重新请求）
   */
  const refresh = useCallback((): Promise<void> => {
    if (lastParamsRef.current) {
      return run(...lastParamsRef.current);
    }
    return run(...([] as any));
  }, [run]);

  /**
   * 重置状态
   */
  const reset = useCallback(() => {
    setData(null);
    setError(null);
    setLoading(false);
    lastParamsRef.current = null;
    canceledRef.current = false;
  }, []);

  /**
   * 立即执行或依赖变化时执行
   */
  useEffect(() => {
    if (immediate) {
      run(...([] as any));
    }

    return () => {
      cancel();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...deps, immediate]);

  return {
    data,
    error,
    loading,
    run,
    cancel,
    refresh,
    reset,
  };
};

export default useRequest;
