/**
 * 数据表格组件
 * 
 * 功能：
 * - 集成 useTable Hook
 * - 统一表格样式
 * - 支持列配置
 * - 支持操作列
 * - 支持批量操作
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */

import React from 'react';
import { Table, Card } from 'antd';
import { useTable } from '@/hooks';
import type { ColumnsType, TableProps } from 'antd/es/table';

/**
 * DataTable 组件属性
 */
export interface DataTableProps<T = any> extends Omit<TableProps<T>, 'pagination' | 'onChange'> {
  /** 表格列配置 */
  columns: ColumnsType<T>;
  /** 数据源 */
  dataSource?: T[];
  /** 总条数 */
  total?: number;
  /** 是否加载中 */
  loading?: boolean;
  /** 是否显示边框，默认 false */
  bordered?: boolean;
  /** 是否显示卡片包裹，默认 true */
  showCard?: boolean;
  /** 表格变化回调 */
  onTableChange?: (page: number, size: number) => void;
  /** 默认每页条数，默认 20 */
  defaultPageSize?: number;
  /** 行选择配置 */
  rowSelection?: TableProps<T>['rowSelection'];
}

/**
 * 数据表格组件
 * 
 * @example
 * ```tsx
 * <DataTable
 *   columns={[
 *     { title: 'ID', dataIndex: 'id', key: 'id' },
 *     { title: '名称', dataIndex: 'name', key: 'name' },
 *   ]}
 *   dataSource={data?.content}
 *   total={data?.totalElements}
 *   loading={isLoading}
 *   onTableChange={(page, size) => {
 *     console.log('页码:', page, '每页:', size);
 *   }}
 * />
 * ```
 */
export const DataTable = <T extends Record<string, any>>({
  columns,
  dataSource,
  total = 0,
  loading = false,
  bordered = false,
  showCard = true,
  onTableChange,
  defaultPageSize = 20,
  rowSelection,
  ...restProps
}: DataTableProps<T>) => {
  // 使用 useTable Hook 管理表格状态
  const { page, size, setTotal, handleTableChange, tableParams } = useTable({
    defaultPageSize,
  });

  // 更新总条数
  React.useEffect(() => {
    setTotal(total);
  }, [total, setTotal]);

  // 表格变化事件
  React.useEffect(() => {
    onTableChange?.(page, size);
  }, [page, size, onTableChange]);

  const tableNode = (
    <Table<T>
      columns={columns}
      dataSource={dataSource}
      loading={loading}
      bordered={bordered}
      pagination={tableParams.pagination}
      onChange={handleTableChange}
      rowSelection={rowSelection}
      scroll={{ x: 'max-content' }}
      {...restProps}
    />
  );

  if (showCard) {
    return <Card>{tableNode}</Card>;
  }

  return tableNode;
};
