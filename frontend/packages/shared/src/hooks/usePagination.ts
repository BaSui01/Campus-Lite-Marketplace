/**
 * usePagination Hook - 分页状态管理专家！📄
 * @author BaSui 😎
 * @description 管理分页状态、页码切换、每页条数变更等功能
 */

import { useState, useCallback, useMemo } from 'react';

/**
 * 分页参数接口
 */
export interface PaginationParams {
  /**
   * 当前页码（从 0 开始）
   */
  page: number;

  /**
   * 每页条数
   */
  size: number;

  /**
   * 排序字段
   */
  sort?: string;

  /**
   * 排序方向
   */
  direction?: 'ASC' | 'DESC';
}

/**
 * 分页响应接口
 */
export interface PaginationResponse<T> {
  /**
   * 数据列表
   */
  content: T[];

  /**
   * 总条数
   */
  totalElements: number;

  /**
   * 总页数
   */
  totalPages: number;

  /**
   * 当前页码
   */
  number: number;

  /**
   * 每页条数
   */
  size: number;

  /**
   * 是否第一页
   */
  first: boolean;

  /**
   * 是否最后一页
   */
  last: boolean;
}

/**
 * usePagination 配置接口
 */
export interface UsePaginationOptions {
  /**
   * 初始页码（从 0 开始）
   * @default 0
   */
  initialPage?: number;

  /**
   * 初始每页条数
   * @default 10
   */
  initialSize?: number;

  /**
   * 初始排序字段
   */
  initialSort?: string;

  /**
   * 初始排序方向
   * @default 'DESC'
   */
  initialDirection?: 'ASC' | 'DESC';

  /**
   * 每页条数选项
   * @default [10, 20, 50, 100]
   */
  sizeOptions?: number[];
}

/**
 * usePagination 返回值接口
 */
export interface UsePaginationResult {
  /**
   * 当前页码（从 0 开始）
   */
  page: number;

  /**
   * 每页条数
   */
  size: number;

  /**
   * 排序字段
   */
  sort?: string;

  /**
   * 排序方向
   */
  direction?: 'ASC' | 'DESC';

  /**
   * 总条数
   */
  total: number;

  /**
   * 总页数
   */
  totalPages: number;

  /**
   * 是否第一页
   */
  isFirst: boolean;

  /**
   * 是否最后一页
   */
  isLast: boolean;

  /**
   * 每页条数选项
   */
  sizeOptions: number[];

  /**
   * 分页参数（用于 API 请求）
   */
  params: PaginationParams;

  /**
   * 跳转到指定页
   */
  goToPage: (page: number) => void;

  /**
   * 上一页
   */
  prevPage: () => void;

  /**
   * 下一页
   */
  nextPage: () => void;

  /**
   * 跳转到第一页
   */
  firstPage: () => void;

  /**
   * 跳转到最后一页
   */
  lastPage: () => void;

  /**
   * 修改每页条数
   */
  changeSize: (size: number) => void;

  /**
   * 修改排序
   */
  changeSort: (sort: string, direction?: 'ASC' | 'DESC') => void;

  /**
   * 更新分页数据（从 API 响应更新）
   */
  updatePagination: (response: PaginationResponse<any>) => void;

  /**
   * 重置分页
   */
  reset: () => void;
}

/**
 * usePagination Hook
 *
 * @description 分页状态管理 Hook，提供页码切换、每页条数变更、排序等功能
 *
 * @example
 * ```tsx
 * function GoodsList() {
 *   const pagination = usePagination({
 *     initialSize: 20,
 *     initialSort: 'createdAt',
 *     initialDirection: 'DESC',
 *   });
 *
 *   const { data, loading } = useRequest(
 *     async () => {
 *       const response = await api.listGoods(pagination.params);
 *       pagination.updatePagination(response.data.data);
 *       return response.data.data.content;
 *     },
 *     { deps: [pagination.page, pagination.size, pagination.sort, pagination.direction] }
 *   );
 *
 *   return (
 *     <div>
 *       {data?.map(item => <div key={item.id}>{item.title}</div>)}
 *
 *       <Pagination
 *         current={pagination.page + 1}
 *         pageSize={pagination.size}
 *         total={pagination.total}
 *         onChange={(page) => pagination.goToPage(page - 1)}
 *         onShowSizeChange={(_, size) => pagination.changeSize(size)}
 *       />
 *     </div>
 *   );
 * }
 * ```
 */
export const usePagination = (
  options: UsePaginationOptions = {}
): UsePaginationResult => {
  const {
    initialPage = 0,
    initialSize = 10,
    initialSort,
    initialDirection = 'DESC',
    sizeOptions = [10, 20, 50, 100],
  } = options;

  // 当前页码
  const [page, setPage] = useState(initialPage);

  // 每页条数
  const [size, setSize] = useState(initialSize);

  // 排序字段
  const [sort, setSort] = useState(initialSort);

  // 排序方向
  const [direction, setDirection] = useState<'ASC' | 'DESC'>(initialDirection);

  // 总条数
  const [total, setTotal] = useState(0);

  // 总页数
  const [totalPages, setTotalPages] = useState(0);

  // 是否第一页
  const isFirst = page === 0;

  // 是否最后一页
  const isLast = page === totalPages - 1 || totalPages === 0;

  /**
   * 分页参数（用于 API 请求）
   */
  const params = useMemo<PaginationParams>(() => {
    const params: PaginationParams = {
      page,
      size,
    };

    if (sort) {
      params.sort = sort;
      params.direction = direction;
    }

    return params;
  }, [page, size, sort, direction]);

  /**
   * 跳转到指定页
   */
  const goToPage = useCallback((newPage: number) => {
    setPage(Math.max(0, newPage));
  }, []);

  /**
   * 上一页
   */
  const prevPage = useCallback(() => {
    setPage((prev) => Math.max(0, prev - 1));
  }, []);

  /**
   * 下一页
   */
  const nextPage = useCallback(() => {
    setPage((prev) => prev + 1);
  }, []);

  /**
   * 跳转到第一页
   */
  const firstPage = useCallback(() => {
    setPage(0);
  }, []);

  /**
   * 跳转到最后一页
   */
  const lastPage = useCallback(() => {
    setPage((totalPages) => Math.max(0, totalPages - 1));
  }, []);

  /**
   * 修改每页条数
   */
  const changeSize = useCallback((newSize: number) => {
    setSize(newSize);
    setPage(0); // 修改每页条数时，重置到第一页
  }, []);

  /**
   * 修改排序
   */
  const changeSort = useCallback((newSort: string, newDirection?: 'ASC' | 'DESC') => {
    setSort(newSort);
    if (newDirection) {
      setDirection(newDirection);
    }
    setPage(0); // 修改排序时，重置到第一页
  }, []);

  /**
   * 更新分页数据（从 API 响应更新）
   */
  const updatePagination = useCallback((response: PaginationResponse<any>) => {
    setTotal(response.totalElements);
    setTotalPages(response.totalPages);
  }, []);

  /**
   * 重置分页
   */
  const reset = useCallback(() => {
    setPage(initialPage);
    setSize(initialSize);
    setSort(initialSort);
    setDirection(initialDirection);
    setTotal(0);
    setTotalPages(0);
  }, [initialPage, initialSize, initialSort, initialDirection]);

  return {
    page,
    size,
    sort,
    direction,
    total,
    totalPages,
    isFirst,
    isLast,
    sizeOptions,
    params,
    goToPage,
    prevPage,
    nextPage,
    firstPage,
    lastPage,
    changeSize,
    changeSort,
    updatePagination,
    reset,
  };
};

export default usePagination;
