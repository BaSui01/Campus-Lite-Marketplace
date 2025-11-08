/**
 * useModal Hook 单元测试
 * @author BaSui 😎
 */

import { describe, it, expect } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useModal } from '../useModal';

interface TestData {
  id: number;
  name: string;
}

describe('useModal Hook', () => {
  it('应该初始化为关闭状态', () => {
    const { result } = renderHook(() => useModal<TestData>());

    expect(result.current.visible).toBe(false);
    expect(result.current.data).toBeNull();
  });

  it('应该打开弹窗（新增模式）', () => {
    const { result } = renderHook(() => useModal<TestData>());

    act(() => {
      result.current.open();
    });

    expect(result.current.visible).toBe(true);
    expect(result.current.data).toBeNull();
  });

  it('应该打开弹窗（编辑模式）', () => {
    const { result } = renderHook(() => useModal<TestData>());

    const testData: TestData = { id: 1, name: 'Test' };

    act(() => {
      result.current.open(testData);
    });

    expect(result.current.visible).toBe(true);
    expect(result.current.data).toEqual(testData);
  });

  it('应该关闭弹窗并清空数据', () => {
    const { result } = renderHook(() => useModal<TestData>());

    const testData: TestData = { id: 1, name: 'Test' };

    act(() => {
      result.current.open(testData);
    });

    expect(result.current.visible).toBe(true);
    expect(result.current.data).toEqual(testData);

    act(() => {
      result.current.close();
    });

    expect(result.current.visible).toBe(false);
    expect(result.current.data).toBeNull();
  });

  it('应该支持手动设置数据', () => {
    const { result } = renderHook(() => useModal<TestData>());

    const testData: TestData = { id: 2, name: 'Updated' };

    act(() => {
      result.current.setData(testData);
    });

    expect(result.current.data).toEqual(testData);
  });
});
