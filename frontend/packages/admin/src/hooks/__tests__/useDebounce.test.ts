/**
 * useDebounce Hook 单元测试
 * @author BaSui 😎
 */

import { describe, it, expect, vi } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useDebounce } from '../useDebounce';

describe('useDebounce Hook', () => {
  it('应该返回初始值', () => {
    const { result } = renderHook(() => useDebounce('initial', 500));

    expect(result.current).toBe('initial');
  });

  it('应该在延迟后更新值', async () => {
    vi.useFakeTimers();

    const { result, rerender } = renderHook(
      ({ value }) => useDebounce(value, 500),
      {
        initialProps: { value: 'initial' },
      }
    );

    expect(result.current).toBe('initial');

    // 更新值
    rerender({ value: 'updated' });

    // 立即检查，应该还是旧值
    expect(result.current).toBe('initial');

    // 快进时间 500ms
    act(() => {
      vi.advanceTimersByTime(500);
    });

    // 应该更新为新值
    expect(result.current).toBe('updated');

    vi.useRealTimers();
  });

  it('应该在值频繁变化时只保留最后一次', async () => {
    vi.useFakeTimers();

    const { result, rerender } = renderHook(
      ({ value }) => useDebounce(value, 500),
      {
        initialProps: { value: 'initial' },
      }
    );

    // 快速更新多次
    rerender({ value: 'value1' });
    act(() => {
      vi.advanceTimersByTime(100);
    });

    rerender({ value: 'value2' });
    act(() => {
      vi.advanceTimersByTime(100);
    });

    rerender({ value: 'value3' });
    act(() => {
      vi.advanceTimersByTime(100);
    });

    // 此时应该还是初始值
    expect(result.current).toBe('initial');

    // 快进剩余时间
    act(() => {
      vi.advanceTimersByTime(300);
    });

    // 应该更新为最后一次的值
    expect(result.current).toBe('value3');

    vi.useRealTimers();
  });

  it('应该支持自定义延迟时间', async () => {
    vi.useFakeTimers();

    const { result, rerender } = renderHook(
      ({ value }) => useDebounce(value, 1000),
      {
        initialProps: { value: 'initial' },
      }
    );

    rerender({ value: 'updated' });

    // 500ms 后还应该是旧值
    act(() => {
      vi.advanceTimersByTime(500);
    });
    expect(result.current).toBe('initial');

    // 再过 500ms（总共 1000ms）应该更新
    act(() => {
      vi.advanceTimersByTime(500);
    });
    expect(result.current).toBe('updated');

    vi.useRealTimers();
  });
});
