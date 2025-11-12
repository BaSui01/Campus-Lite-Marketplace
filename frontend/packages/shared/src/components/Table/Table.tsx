/**
 * Table 组件 - 数据表格专家！📊
 * @author BaSui 😎
 * @description 通用数据表格组件，支持排序、选择、分页、自定义渲染
 */

import React, { useState, useMemo, useCallback } from 'react';
import { Pagination, type PaginationProps } from '../Pagination';
import './Table.css';

/**
 * 表格列配置接口
 */
export interface TableColumn<T = any> {
  /**
   * 列键值（对应数据字段）
   */
  key: string;

  /**
   * 列标题
   */
  title: React.ReactNode;

  /**
   * 数据索引（支持嵌套，如 'user.name'）
   */
  dataIndex?: string;

  /**
   * 列宽度
   */
  width?: number | string;

  /**
   * 列对齐方式
   * @default 'left'
   */
  align?: 'left' | 'center' | 'right';

  /**
   * 是否可排序
   * @default false
   */
  sortable?: boolean;

  /**
   * 是否固定列
   */
  fixed?: 'left' | 'right';

  /**
   * 自定义渲染函数
   */
  render?: (value: any, record: T, index: number) => React.ReactNode;

  /**
   * 自定义类名
   */
  className?: string;
}

/**
 * 排序方向
 */
export type SortDirection = 'ascend' | 'descend' | null;

/**
 * 排序信息
 */
export interface SortInfo {
  /**
   * 排序列键值
   */
  key: string;

  /**
   * 排序方向
   */
  direction: SortDirection;
}

/**
 * 表格行选择配置
 */
export interface TableRowSelection<T = any> {
  /**
   * 选中的行键值数组
   */
  selectedRowKeys?: React.Key[];

  /**
   * 选中改变��调
   */
  onChange?: (selectedRowKeys: React.Key[], selectedRows: T[]) => void;

  /**
   * 获取行键值的函数
   * @default (record) => record.id
   */
  getRowKey?: (record: T) => React.Key;

  /**
   * 是否显示全选复选框
   * @default true
   */
  showSelectAll?: boolean;

  /**
   * 单选或多选
   * @default 'checkbox'
   */
  type?: 'checkbox' | 'radio';
}

/**
 * Table 组件的 Props 接口
 */
export interface TableProps<T = any> {
  /**
   * 表格数据
   */
  dataSource: T[];

  /**
   * 表格列配置
   */
  columns: TableColumn<T>[];

  /**
   * 获取行键值的函数
   * @default (record) => record.id
   */
  rowKey?: string | ((record: T) => React.Key);

  /**
   * 是否显示边框
   * @default true
   */
  bordered?: boolean;

  /**
   * 是否显示斑马纹
   * @default true
   */
  striped?: boolean;

  /**
   * 是否显示悬浮效果
   * @default true
   */
  hover?: boolean;

  /**
   * 表格大小
   * @default 'medium'
   */
  size?: 'small' | 'medium' | 'large';

  /**
   * 是否加载中
   * @default false
   */
  loading?: boolean;

  /**
   * 空数据提示
   * @default '暂无数据'
   */
  emptyText?: React.ReactNode;

  /**
   * 行选择配置
   */
  rowSelection?: TableRowSelection<T>;

  /**
   * 分页配置（false 表示不显示分页）
   */
  pagination?: false | PaginationProps;

  /**
   * 排序改变回调
   */
  onSortChange?: (sortInfo: SortInfo | null) => void;

  /**
   * 行点击回调
   */
  onRowClick?: (record: T, index: number) => void;

  /**
   * 自定义类名
   */
  className?: string;

  /**
   * 自定义样式
   */
  style?: React.CSSProperties;
}

/**
 * Table 组件
 *
 * @example
 * ```tsx
 * // 基础用法
 * <Table
 *   dataSource={users}
 *   columns={[
 *     { key: 'name', title: '姓名', dataIndex: 'name' },
 *     { key: 'age', title: '年龄', dataIndex: 'age', sortable: true },
 *     { key: 'email', title: '邮箱', dataIndex: 'email' },
 *   ]}
 * />
 *
 * // 带选择和分页
 * <Table
 *   dataSource={users}
 *   columns={columns}
 *   rowSelection={{
 *     selectedRowKeys,
 *     onChange: (keys, rows) => setSelectedRowKeys(keys),
 *   }}
 *   pagination={{
 *     current: page,
 *     pageSize: 20,
 *     total: total,
 *     onChange: (page) => setPage(page),
 *   }}
 * />
 * ```
 */
export const Table = <T extends Record<string, any> = any>({
  dataSource,
  columns,
  rowKey = 'id',
  bordered = true,
  striped = true,
  hover = true,
  size = 'medium',
  loading = false,
  emptyText = '暂无数据',
  rowSelection,
  pagination,
  onSortChange,
  onRowClick,
  className = '',
  style,
}: TableProps<T>) => {
  // 排序状态
  const [sortInfo, setSortInfo] = useState<SortInfo | null>(null);

  // 选中的行键值
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>(
    rowSelection?.selectedRowKeys || []
  );

  /**
   * 获取行键值
   */
  const getRowKey = useCallback(
    (record: T, index: number): React.Key => {
      if (typeof rowKey === 'string') {
        return record[rowKey] ?? index;
      }
      return rowKey(record);
    },
    [rowKey]
  );

  /**
   * 获取嵌套字段值
   */
  const getNestedValue = (obj: any, path: string): any => {
    return path.split('.').reduce((acc, part) => acc?.[part], obj);
  };

  /**
   * 处理排序
   */
  const handleSort = (column: TableColumn<T>) => {
    if (!column.sortable) return;

    let newDirection: SortDirection = 'ascend';

    if (sortInfo?.key === column.key) {
      if (sortInfo.direction === 'ascend') {
        newDirection = 'descend';
      } else if (sortInfo.direction === 'descend') {
        newDirection = null;
      }
    }

    const newSortInfo = newDirection ? { key: column.key, direction: newDirection } : null;
    setSortInfo(newSortInfo);
    onSortChange?.(newSortInfo);
  };

  /**
   * 排序后的数据
   */
  const sortedData = useMemo(() => {
    if (!sortInfo) return dataSource;

    const { key, direction } = sortInfo;
    const column = columns.find((col) => col.key === key);
    if (!column) return dataSource;

    return [...dataSource].sort((a, b) => {
      const aValue = column.dataIndex ? getNestedValue(a, column.dataIndex) : a[key];
      const bValue = column.dataIndex ? getNestedValue(b, column.dataIndex) : b[key];

      if (aValue === bValue) return 0;

      const compareResult = aValue > bValue ? 1 : -1;
      return direction === 'ascend' ? compareResult : -compareResult;
    });
  }, [dataSource, sortInfo, columns]);

  /**
   * 处理全选
   */
  const handleSelectAll = (checked: boolean) => {
    if (!rowSelection) return;

    const newSelectedRowKeys = checked
      ? sortedData.map((record, index) => getRowKey(record, index))
      : [];

    setSelectedRowKeys(newSelectedRowKeys);
    rowSelection.onChange?.(newSelectedRowKeys, checked ? sortedData : []);
  };

  /**
   * 处理单行选择
   */
  const handleSelectRow = (record: T, index: number, checked: boolean) => {
    if (!rowSelection) return;

    const key = getRowKey(record, index);
    let newSelectedRowKeys: React.Key[];

    if (rowSelection.type === 'radio') {
      newSelectedRowKeys = checked ? [key] : [];
    } else {
      newSelectedRowKeys = checked
        ? [...selectedRowKeys, key]
        : selectedRowKeys.filter((k) => k !== key);
    }

    setSelectedRowKeys(newSelectedRowKeys);

    const selectedRows = sortedData.filter((row, idx) =>
      newSelectedRowKeys.includes(getRowKey(row, idx))
    );
    rowSelection.onChange?.(newSelectedRowKeys, selectedRows);
  };

  /**
   * 是否全选
   */
  const isAllSelected = useMemo(() => {
    if (!rowSelection || sortedData.length === 0) return false;
    return sortedData.every((record, index) => selectedRowKeys.includes(getRowKey(record, index)));
  }, [sortedData, selectedRowKeys, rowSelection, getRowKey]);

  /**
   * 是否部分选中
   */
  const isIndeterminate = useMemo(() => {
    if (!rowSelection || sortedData.length === 0) return false;
    const selectedCount = sortedData.filter((record, index) =>
      selectedRowKeys.includes(getRowKey(record, index))
    ).length;
    return selectedCount > 0 && selectedCount < sortedData.length;
  }, [sortedData, selectedRowKeys, rowSelection, getRowKey]);

  // 组装 CSS 类名
  const classNames = [
    'campus-table',
    `campus-table--${size}`,
    bordered ? 'campus-table--bordered' : '',
    striped ? 'campus-table--striped' : '',
    hover ? 'campus-table--hover' : '',
    loading ? 'campus-table--loading' : '',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <div className="campus-table-wrapper" style={style}>
      {/* 表格容器 */}
      <div className={classNames}>
        <table className="campus-table__table">
          {/* 表头 */}
          <thead className="campus-table__thead">
            <tr>
              {/* 选择列 */}
              {rowSelection && (
                <th className="campus-table__th campus-table__th--selection">
                  {rowSelection.type !== 'radio' &&
                    rowSelection.showSelectAll !== false && (
                      <input
                        type="checkbox"
                        checked={isAllSelected}
                        ref={(input) => {
                          if (input) {
                            input.indeterminate = isIndeterminate;
                          }
                        }}
                        onChange={(e) => handleSelectAll(e.target.checked)}
                        className="campus-table__checkbox"
                      />
                    )}
                </th>
              )}

              {/* 数据列 */}
              {columns.map((column) => (
                <th
                  key={column.key}
                  className={`campus-table__th ${
                    column.align ? `campus-table__th--${column.align}` : ''
                  } ${column.sortable ? 'campus-table__th--sortable' : ''} ${
                    column.fixed ? `campus-table__th--fixed-${column.fixed}` : ''
                  } ${column.className || ''}`}
                  style={{ width: column.width }}
                  onClick={() => handleSort(column)}
                >
                  <div className="campus-table__th-content">
                    <span>{column.title}</span>
                    {column.sortable && (
                      <span className="campus-table__sort-icon">
                        <span
                          className={`campus-table__sort-arrow campus-table__sort-arrow--up ${
                            sortInfo?.key === column.key && sortInfo.direction === 'ascend'
                              ? 'campus-table__sort-arrow--active'
                              : ''
                          }`}
                        >
                          ▲
                        </span>
                        <span
                          className={`campus-table__sort-arrow campus-table__sort-arrow--down ${
                            sortInfo?.key === column.key && sortInfo.direction === 'descend'
                              ? 'campus-table__sort-arrow--active'
                              : ''
                          }`}
                        >
                          ▼
                        </span>
                      </span>
                    )}
                  </div>
                </th>
              ))}
            </tr>
          </thead>

          {/* 表体 */}
          <tbody className="campus-table__tbody">
            {sortedData.length === 0 ? (
              <tr>
                <td
                  colSpan={columns.length + (rowSelection ? 1 : 0)}
                  className="campus-table__empty"
                >
                  {emptyText}
                </td>
              </tr>
            ) : (
              sortedData.map((record, index) => {
                const key = getRowKey(record, index);
                const isSelected = selectedRowKeys.includes(key);

                return (
                  <tr
                    key={key}
                    className={`campus-table__tr ${
                      isSelected ? 'campus-table__tr--selected' : ''
                    }`}
                    onClick={() => onRowClick?.(record, index)}
                  >
                    {/* 选择列 */}
                    {rowSelection && (
                      <td className="campus-table__td campus-table__td--selection">
                        <input
                          type={rowSelection.type || 'checkbox'}
                          checked={isSelected}
                          onChange={(e) => handleSelectRow(record, index, e.target.checked)}
                          onClick={(e) => e.stopPropagation()}
                          className="campus-table__checkbox"
                        />
                      </td>
                    )}

                    {/* 数据列 */}
                    {columns.map((column) => {
                      const value = column.dataIndex
                        ? getNestedValue(record, column.dataIndex)
                        : record[column.key];

                      return (
                        <td
                          key={column.key}
                          className={`campus-table__td ${
                            column.align ? `campus-table__td--${column.align}` : ''
                          } ${column.fixed ? `campus-table__td--fixed-${column.fixed}` : ''} ${
                            column.className || ''
                          }`}
                        >
                          {column.render ? column.render(value, record, index) : value}
                        </td>
                      );
                    })}
                  </tr>
                );
              })
            )}
          </tbody>
        </table>

        {/* 加载蒙层 */}
        {loading && (
          <div className="campus-table__loading">
            <div className="campus-table__loading-spinner" />
          </div>
        )}
      </div>

      {/* 分页器 */}
      {pagination !== false && pagination && (
        <div className="campus-table__pagination">
          <Pagination
            {...pagination}
            current={pagination.current ?? 1}
            total={pagination.total ?? 0}
          />
        </div>
      )}
    </div>
  );
};

export default Table;
