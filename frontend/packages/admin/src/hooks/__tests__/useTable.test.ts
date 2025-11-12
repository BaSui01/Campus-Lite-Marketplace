/**
 * useTable Hook 单元测试
 * @author BaSui 😎
 */

import { describe, it, expect } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useTable } from '../useTable';

describe('useTable Hook', () => {
  it('应该使用默认配置初始化', () => {
    const { result } = renderHook(() => useTable());

    expect(result.current.page).toBe(0); // 后端格式从 0 开始
    expect(result.current.size).toBe(20);
    expect(result.current.total).toBe(0);
  });

  it('应该支持自定义默认配置', () => {
    const { result } = renderHook(() =>
      useTable({
        defaultCurrent: 2,
        defaultPageSize: 50,
      })
    );

    expect(result.current.page).toBe(1); // 前端 2 转后端 1
    expect(result.current.size).toBe(50);
  });

  it('应该正确设置总条数', () => {
    const { result } = renderHook(() => useTable());

    act(() => {
      result.current.setTotal(100);
    });

    expect(result.current.total).toBe(100);
    expect(result.current.tableParams.pagination.total).toBe(100);
  });

  it('应该正确处理表格变化', () => {
    const { result } = renderHook(() => useTable());

    act(() => {
      result.current.handleTableChange(
        { current: 3, pageSize: 30 },
        {},
        {} as any
      );
    });

    expect(result.current.page).toBe(2); // 前端 3 转后端 2
    expect(result.current.size).toBe(30);
  });

  it('应该正确重置表格状态', () => {
    const { result } = renderHook(() => useTable());

    // 先修改状态
    act(() => {
      result.current.setTotal(100);
      result.current.handleTableChange(
        { current: 3, pageSize: 30 },
        {},
        {} as any
      );
    });

    // 重置
    act(() => {
      result.current.resetTable();
    });

    expect(result.current.page).toBe(0);
    expect(result.current.size).toBe(20);
    expect(result.current.total).toBe(0);
  });

  it('应该正确管理筛选条件', () => {
    const { result } = renderHook(() => useTable());

    const filters = { status: 'ACTIVE', type: 'USER' };

    act(() => {
      result.current.setFilters(filters);
    });

    expect(result.current.filters).toEqual(filters);
  });

  it('应该支持排序', () => {
    const { result } = renderHook(() => useTable());

    act(() => {
      result.current.handleTableChange(
        { current: 1, pageSize: 20 },
        {},
        { field: 'createdAt', order: 'descend' } as any
      );
    });

    expect(result.current.sortField).toBe('createdAt');
    expect(result.current.sortOrder).toBe('descend');
  });
});
