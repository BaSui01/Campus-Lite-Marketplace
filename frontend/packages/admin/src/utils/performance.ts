/**
 * 性能优化工具集
 * @author BaSui 😎
 * @date 2025-11-02
 */

import React, { useCallback, useMemo, useRef, useEffect } from 'react';

// ===== 防抖函数 =====
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  delay: number
): (...args: Parameters<T>) => void {
  let timeoutId: NodeJS.Timeout;
  
  return (...args: Parameters<T>) => {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => func(...args), delay);
  };
}

// ===== 节流函数 =====
export function throttle<T extends (...args: any[]) => any>(
  func: T,
  delay: number
): (...args: Parameters<T>) => void {
  let lastCallTime = 0;
  
  return (...args: Parameters<T>) => {
    const now = Date.now();
    if (now - lastCallTime >= delay) {
      lastCallTime = now;
      func(...args);
    }
  };
}

// ===== 虚拟化列表Hook =====
export function useVirtualList<T>({
  items = [],
  containerHeight = 400,
  itemHeight = 40,
  overscan = 5,
}: {
  items: T[];
  containerHeight?: number;
  itemHeight?: number;
  overscan?: number;
}) {
  const [scrollTop, setScrollTop] = React.useState(0);
  
  const containerRef = useRef<HTMLDivElement>(null);
  
  const visibleRange = useMemo(() => {
    const startIndex = Math.max(0, Math.floor(scrollTop / itemHeight) - overscan);
    const endIndex = Math.min(
      items.length - 1,
      Math.ceil((scrollTop + containerHeight) / itemHeight) + overscan
    );
    
    return {
      start: startIndex,
      end: endIndex,
    };
  }, [scrollTop, itemHeight, containerHeight, overscan, items.length]);
  
  const visibleItems = useMemo(() => {
    return items.slice(visibleRange.start, visibleRange.end + 1).map((item, index) => ({
      item,
      index: visibleRange.start + index,
      top: (visibleRange.start + index) * itemHeight,
    }));
  }, [items, visibleRange.start, visibleRange.end, itemHeight]);
  
  const totalHeight = items.length * itemHeight;
  
  const handleScroll = useCallback((e: React.UIEvent<HTMLDivElement>) => {
    setScrollTop(e.currentTarget.scrollTop);
  }, []);
  
  return {
    containerRef,
    visibleItems,
    totalHeight,
    handleScroll,
  };
}

// ===== 组件懒加载Hook =====
export function useLazyLoadComponent<T extends React.ComponentType<any>>(
  componentFactory: () => Promise<{ default: T }>,
  options: { timeout?: number } = {}
) {
  const [Component, setComponent] = React.useState<T | null>(null);
  const [isLoading, setIsLoading] = React.useState(false);
  const [error, setError] = React.useState<Error | null>(null);
  
  const loadComponent = React.useCallback(async () => {
    setIsLoading(true);
    setError(null);
    
    try {
      const timeoutPromise = new Promise((_, reject) => {
        if (options.timeout) {
          setTimeout(() => reject(new Error('加载超时')), options.timeout);
        }
      });
      
      const result = await Promise.race([
        componentFactory(),
        timeoutPromise,
      ]);
      
      setComponent(result.default);
    } catch (err) {
      setError(err as Error);
    } finally {
      setIsLoading(false);
    }
  }, [componentFactory, options.timeout]);
  
  React.useEffect(() => {
    loadComponent();
  }, [loadComponent]);
  
  return { Component, isLoading, error, retry: loadComponent };
}

// ===== 内存使用监控 =====
export function useMemoryMonitor() {
  const [memoryInfo, setMemoryInfo] = React.useState({
    used: 0,
    total: 0,
    percentage: 0,
  });
  
  useEffect(() => {
    if ('memory' in performance) {
      const updateMemoryInfo = () => {
        const memory = (performance as any).memory;
        const used = memory.usedJSHeapSize;
        const total = memory.totalJSHeapSize;
        const percentage = (used / total) * 100;
        
        setMemoryInfo({ used, total, percentage });
      };
      
      updateMemoryInfo();
      const interval = setInterval(updateMemoryInfo, 5000);
      
      return () => clearInterval(interval);
    }
  }, []);
  
  return memoryInfo;
}

// ===== 组件渲染性能监控 =====
export function useRenderMonitor(componentName: string) {
  const renderCount = React.useRef(0);
  const startTime = React.useRef(Date.now());
  
  React.useEffect(() => {
    renderCount.current += 1;
    const endTime = Date.now();
    const renderTime = endTime - startTime.current;
    
    if (renderTime > 16) { // 超过16ms可能影响帧率
      console.warn(
        `组件 ${componentName} 渲染耗时: ${renderTime}ms (第${renderCount.current}次渲染)`
      );
    }
    
    startTime.current = Date.now();
  });
  
  return {
    renderCount: renderCount.current,
  };
}

// ===== 图片懒加载Hook =====
export function useLazyImage(src: string | undefined) {
  const [loaded, setLoaded] = React.useState(false);
  const [error, setError] = React.useState(false);
  const [imageSrc, setImageSrc] = React.useState<string | undefined>();
  
  useEffect(() => {
    if (!src) {
      setLoaded(false);
      setError(false);
      setImageSrc(undefined);
      return;
    }
    
    const img = new Image();
    
    const onLoad = () => {
      setImageSrc(src);
      setLoaded(true);
      setError(false);
    };
    
    const onError = () => {
      setImageSrc(undefined);
      setLoaded(true);
      setError(true);
    };
    
    img.onload = onLoad;
    img.onerror = onError;
    img.src = src;
    
    return () => {
      img.onload = null;
      img.onerror = null;
    };
  }, [src]);
  
  return { loaded, error, imageSrc };
}

// ===== 数据缓存Hook =====
export function useCache<T>(
  key: string,
  fetcher: () => Promise<T>,
  ttl: number = 300000 // 5分钟
) {
  const cacheRef = useRef<Map<string, { data: T; timestamp: number }>>(new Map());
  
  const getFromCache = React.useCallback(() => {
    const cached = cacheRef.current.get(key);
    if (!cached) return null;
    
    const isExpired = Date.now() - cached.timestamp > ttl;
    if (isExpired) {
      cacheRef.current.delete(key);
      return null;
    }
    
    return cached.data;
  }, [key, ttl]);
  
  const setCache = React.useCallback((data: T) => {
    cacheRef.current.set(key, {
      data,
      timestamp: Date.now(),
    });
  }, [key]);
  
  const fetchAndCache = React.useCallback(async () => {
    const cached = getFromCache();
    if (cached) return cached;
    
    try {
      const data = await fetcher();
      setCache(data);
      return data;
    } catch (error) {
      throw error;
    }
  }, [fetcher, getFromCache, setCache]);
  
  return {
    getFromCache,
    fetchAndCache,
    clearCache: () => cacheRef.current.delete(key),
  };
}

// ===== 批量操作优化 =====
export function useBatchProcessor<T>(
  processor: (batch: T[]) => Promise<void>,
  batchSize: number = 10,
  delay: number = 100
) {
  const [queue, setQueue] = React.useState<T[]>([]);
  const [isProcessing, setIsProcessing] = React.useState(false);
  
  const addToQueue = React.useCallback((item: T) => {
    setQueue(prev => [...prev, item]);
  }, []);
  
  const processBatch = React.useCallback(async (batch: T[]) => {
    setIsProcessing(true);
    try {
      await processor(batch);
      setQueue(prev => prev.slice(batch.length));
    } finally {
      setIsProcessing(false);
    }
  }, [processor]);
  
  React.useEffect(() => {
    if (queue.length === 0 || isProcessing) return;
    
    const timer = setTimeout(() => {
      const batch = queue.slice(0, batchSize);
      if (batch.length > 0) {
        processBatch(batch);
      }
    }, delay);
    
    return () => clearTimeout(timer);
  }, [queue, batchSize, delay, isProcessing, processBatch]);
  
  return {
    addToQueue,
    isProcessing,
    queueLength: queue.length,
  };
}

// ===== 请求重试机制 =====
export function useRetryableRequest<T>(
  requestFn: () => Promise<T>,
  maxRetries: number = 3,
  delay: number = 1000
) {
  const [isRetrying, setIsRetrying] = React.useState(false);
  const [retryCount, setRetryCount] = React.useState(0);
  
  const executeWithRetry = React.useCallback(async (): Promise<T> => {
    for (let i = 0; i <= maxRetries; i++) {
      try {
        const result = await requestFn();
        setRetryCount(0);
        setIsRetrying(false);
        return result;
      } catch (error) {
        if (i === maxRetries) {
          setRetryCount(0);
          setIsRetrying(false);
          throw error;
        }
        
        setRetryCount(i + 1);
        setIsRetrying(true);
        await new Promise(resolve => setTimeout(resolve, delay * Math.pow(2, i)));
      }
    }
    
    throw new Error('请求失败');
  }, [requestFn, maxRetries, delay]);
  
  return {
    executeWithRetry,
    isRetrying,
    retryCount,
  };
}
