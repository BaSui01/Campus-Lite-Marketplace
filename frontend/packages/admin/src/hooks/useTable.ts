/**
 * 表格状态管理 Hook
 * 
 * 功能：
 * - 分页状态管理
 * - 搜索参数管理
 * - 排序状态管理
 * - 自动触发数据查询
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */

import { useState, useCallback } from 'react';
import type { TablePaginationConfig, SorterResult } from 'antd/es/table/interface';

/**
 * 表格参数接口
 */
export interface TableParams<T = any> {
  pagination: {
    current: number;
    pageSize: number;
    total?: number;
  };
  sortField?: string;
  sortOrder?: 'ascend' | 'descend' | null;
  filters?: Record<string, any>;
}

/**
 * useTable Hook 参数
 */
export interface UseTableOptions {
  /** 默认页码，默认 1 */
  defaultCurrent?: number;
  /** 默认每页条数，默认 20 */
  defaultPageSize?: number;
  /** 每页条数选项，默认 [10, 20, 50, 100] */
  pageSizeOptions?: number[];
}

/**
 * useTable Hook 返回值
 */
export interface UseTableResult<T = any> {
  /** 当前页码（从 0 开始，后端格式） */
  page: number;
  /** 每页条数 */
  size: number;
  /** 总条数 */
  total: number;
  /** 排序字段 */
  sortField?: string;
  /** 排序顺序 */
  sortOrder?: 'ascend' | 'descend' | null;
  /** 筛选条件 */
  filters: Record<string, any>;
  /** 表格参数（Ant Design 格式） */
  tableParams: TableParams<T>;
  /** 设置总条数 */
  setTotal: (total: number) => void;
  /** 设置筛选条件 */
  setFilters: (filters: Record<string, any>) => void;
  /** 处理表格变化（分页、排序、筛选） */
  handleTableChange: (
    pagination: TablePaginationConfig,
    filters: Record<string, any>,
    sorter: SorterResult<T> | SorterResult<T>[]
  ) => void;
  /** 重置表格状态 */
  resetTable: () => void;
  /** 刷新当前页 */
  refresh: () => void;
}

/**
 * 表格状态管理 Hook
 * 
 * @example
 * ```tsx
 * const { page, size, total, setTotal, handleTableChange, tableParams } = useTable();
 * 
 * const { data } = useQuery({
 *   queryKey: ['list', page, size],
 *   queryFn: () => api.list(page, size)
 * });
 * 
 * useEffect(() => {
 *   if (data) setTotal(data.totalElements);
 * }, [data]);
 * 
 * <Table
 *   dataSource={data?.content}
 *   pagination={tableParams.pagination}
 *   onChange={handleTableChange}
 * />
 * ```
 */
export const useTable = <T = any>(options: UseTableOptions = {}): UseTableResult<T> => {
  const {
    defaultCurrent = 1,
    defaultPageSize = 20,
    pageSizeOptions = [10, 20, 50, 100],
  } = options;

  // 表格参数状态
  const [tableParams, setTableParams] = useState<TableParams<T>>({
    pagination: {
      current: defaultCurrent,
      pageSize: defaultPageSize,
    },
  });

  // 筛选条件状态
  const [filters, setFilters] = useState<Record<string, any>>({});

  /**
   * 设置总条数
   */
  const setTotal = useCallback((total: number) => {
    setTableParams((prev) => ({
      ...prev,
      pagination: {
        ...prev.pagination,
        total,
      },
    }));
  }, []);

  /**
   * 处理表格变化
   */
  const handleTableChange = useCallback(
    (
      pagination: TablePaginationConfig,
      filters: Record<string, any>,
      sorter: SorterResult<T> | SorterResult<T>[]
    ) => {
      // 处理分页
      setTableParams({
        pagination: {
          current: pagination.current || 1,
          pageSize: pagination.pageSize || defaultPageSize,
          total: tableParams.pagination.total,
        },
        // 处理排序
        sortField: Array.isArray(sorter) ? undefined : (sorter.field as string),
        sortOrder: Array.isArray(sorter) ? undefined : sorter.order,
        // 处理筛选
        filters,
      });

      // 更新筛选条件
      setFilters(filters);
    },
    [defaultPageSize, tableParams.pagination.total]
  );

  /**
   * 重置表格状态
   */
  const resetTable = useCallback(() => {
    setTableParams({
      pagination: {
        current: defaultCurrent,
        pageSize: defaultPageSize,
      },
    });
    setFilters({});
  }, [defaultCurrent, defaultPageSize]);

  /**
   * 刷新当前页
   */
  const refresh = useCallback(() => {
    setTableParams((prev) => ({ ...prev }));
  }, []);

  return {
    // 后端格式（从 0 开始）
    page: (tableParams.pagination.current || 1) - 1,
    size: tableParams.pagination.pageSize || defaultPageSize,
    total: tableParams.pagination.total || 0,
    sortField: tableParams.sortField,
    sortOrder: tableParams.sortOrder,
    filters,
    // Ant Design 格式
    tableParams: {
      ...tableParams,
      pagination: {
        ...tableParams.pagination,
        showSizeChanger: true,
        showQuickJumper: true,
        showTotal: (total) => `共 ${total} 条`,
        pageSizeOptions,
      },
    },
    setTotal,
    setFilters,
    handleTableChange,
    resetTable,
    refresh,
  };
};
