/**
 * 可撤销操作列表组件
 * @author BaSui 😎
 * @description 展示用户可以撤销的操作列表（表格形式）
 */

import React, { useState, useCallback } from 'react';
import { Table, type TableColumn } from '../Table';
import { Button } from '../Button';
import { Tag, type TagColor } from '../Tag';
import { Badge } from '../Badge';
import { Pagination } from '../Pagination';
import { Loading } from '../Loading';
import { Empty } from '../Empty';
import type { 
  RevertRequest, 
  RevertRequestStatus 
} from '../../types/revert';
import './RevertOperationsList.css';

/**
 * 实体类型枚举
 */
export type EntityType = 'Goods' | 'Order' | 'User' | 'BatchOperation';

/**
 * 操作类型枚举
 */
export type ActionType = 'DELETE' | 'UPDATE' | 'CREATE';

/**
 * 可撤销操作数据
 */
export interface RevertableOperation {
  auditLogId: number;
  entityType: EntityType;
  entityId: number;
  entityName?: string;
  actionType: ActionType;
  actionDescription: string;
  actionTime: string;
  revertDeadline: string;
  remainingDays: number;
  isReversible: boolean;
  requiresApproval: boolean;
  existingRequest?: {
    requestId: number;
    status: RevertRequestStatus;
    requestedAt: string;
  };
}

/**
 * 列表查询参数
 */
export interface RevertListParams {
  entityType?: EntityType;
  actionType?: ActionType;
  page?: number;
  size?: number;
}

/**
 * 列表响应数据
 */
export interface RevertListResponse {
  content: RevertableOperation[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

/**
 * RevertOperationsList 组件属性
 */
export interface RevertOperationsListProps {
  /** 加载状态 */
  loading?: boolean;
  
  /** 数据列表 */
  data: RevertableOperation[];
  
  /** 总记录数 */
  total?: number;
  
  /** 当前页码（从1开始） */
  currentPage?: number;
  
  /** 每页大小 */
  pageSize?: number;
  
  /** 分页变化回调 */
  onPageChange?: (page: number, pageSize: number) => void;
  
  /** 预览操作回调 */
  onPreview?: (operation: RevertableOperation) => void;
  
  /** 申请撤销回调 */
  onRequestRevert?: (operation: RevertableOperation) => void;
  
  /** 自定义空状态 */
  emptyText?: string;
  
  /** 自定义类名 */
  className?: string;
}

/**
 * 实体类型文本映射
 */
const ENTITY_TYPE_TEXT: Record<EntityType, string> = {
  Goods: '商品',
  Order: '订单',
  User: '用户',
  BatchOperation: '批量操作'
};

/**
 * 实体类型颜色映射
 */
const ENTITY_TYPE_COLOR: Record<EntityType, TagColor> = {
  Goods: 'blue',
  Order: 'green',
  User: 'orange',
  BatchOperation: 'purple'
};

/**
 * 操作类型文本映射
 */
const ACTION_TYPE_TEXT: Record<ActionType, string> = {
  DELETE: '删除',
  UPDATE: '更新',
  CREATE: '创建'
};

/**
 * 操作类型颜色映射
 */
const ACTION_TYPE_COLOR: Record<ActionType, TagColor> = {
  DELETE: 'red',
  UPDATE: 'orange',
  CREATE: 'green'
};

/**
 * 获取剩余时间的颜色
 */
const getRemainingDaysColor = (days: number): 'success' | 'warning' | 'error' => {
  if (days > 7) return 'success';
  if (days > 3) return 'warning';
  return 'error';
};

/**
 * 格式化时间显示
 */
const formatDateTime = (dateStr: string): string => {
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

/**
 * 可撤销操作列表组件
 */
export const RevertOperationsList: React.FC<RevertOperationsListProps> = ({
  loading = false,
  data = [],
  total = 0,
  currentPage = 1,
  pageSize = 10,
  onPageChange,
  onPreview,
  onRequestRevert,
  emptyText = '暂无可撤销的操作',
  className = ''
}) => {
  // 定义表格列
  const columns: TableColumn<RevertableOperation>[] = [
    {
      key: 'entityType',
      title: '实体类型',
      dataIndex: 'entityType',
      width: 100,
      align: 'center',
      render: (value: EntityType) => (
        <Tag color={ENTITY_TYPE_COLOR[value]} size="medium">
          {ENTITY_TYPE_TEXT[value]}
        </Tag>
      )
    },
    {
      key: 'actionType',
      title: '操作类型',
      dataIndex: 'actionType',
      width: 100,
      align: 'center',
      render: (value: ActionType) => (
        <Tag color={ACTION_TYPE_COLOR[value]} size="medium">
          {ACTION_TYPE_TEXT[value]}
        </Tag>
      )
    },
    {
      key: 'entityName',
      title: '实体信息',
      dataIndex: 'entityName',
      width: 200,
      render: (value: string | undefined, record: RevertableOperation) => (
        <div className="revert-entity-info">
          <div className="revert-entity-name">
            {value || `ID: ${record.entityId}`}
          </div>
          <div className="revert-entity-description">
            {record.actionDescription}
          </div>
        </div>
      )
    },
    {
      key: 'actionTime',
      title: '操作时间',
      dataIndex: 'actionTime',
      width: 160,
      render: (value: string) => (
        <span className="revert-time">{formatDateTime(value)}</span>
      )
    },
    {
      key: 'remainingDays',
      title: '剩余时限',
      dataIndex: 'remainingDays',
      width: 120,
      align: 'center',
      render: (value: number) => {
        const status = getRemainingDaysColor(value);
        return (
          <Badge status={status} text={`${value} 天`} />
        );
      }
    },
    {
      key: 'requiresApproval',
      title: '需要审批',
      dataIndex: 'requiresApproval',
      width: 100,
      align: 'center',
      render: (value: boolean) => (
        <Tag color={value ? 'orange' : 'gray'} size="small">
          {value ? '是' : '否'}
        </Tag>
      )
    },
    {
      key: 'status',
      title: '状态',
      dataIndex: 'existingRequest',
      width: 120,
      align: 'center',
      render: (existingRequest?: RevertableOperation['existingRequest']) => {
        if (!existingRequest) {
          return <Tag color="gray" size="small">未申请</Tag>;
        }
        
        const statusColors: Record<RevertRequestStatus, TagColor> = {
          PENDING: 'orange',
          APPROVED: 'blue',
          REJECTED: 'red',
          EXECUTED: 'green',
          FAILED: 'red',
          CANCELLED: 'gray'
        };
        
        const statusText: Record<RevertRequestStatus, string> = {
          PENDING: '待处理',
          APPROVED: '已批准',
          REJECTED: '已拒绝',
          EXECUTED: '已执行',
          FAILED: '执行失败',
          CANCELLED: '已取消'
        };
        
        return (
          <Tag 
            color={statusColors[existingRequest.status]} 
            size="small"
          >
            {statusText[existingRequest.status]}
          </Tag>
        );
      }
    },
    {
      key: 'actions',
      title: '操作',
      width: 180,
      align: 'center',
      render: (_: any, record: RevertableOperation) => {
        const hasRequest = !!record.existingRequest;
        const canApply = record.isReversible && !hasRequest;
        
        return (
          <div className="revert-actions">
            {onPreview && (
              <Button
                type="link"
                size="small"
                onClick={() => onPreview(record)}
              >
                预览
              </Button>
            )}
            
            {onRequestRevert && canApply && (
              <Button
                type="primary"
                size="small"
                onClick={() => onRequestRevert(record)}
              >
                申请撤销
              </Button>
            )}
            
            {hasRequest && (
              <span className="revert-status-hint">
                已申请
              </span>
            )}
          </div>
        );
      }
    }
  ];

  // 处理分页变化
  const handlePaginationChange = useCallback((page: number, size: number) => {
    onPageChange?.(page, size);
  }, [onPageChange]);

  // 加载状态
  if (loading) {
    return (
      <div className={`revert-operations-list ${className}`}>
        <Loading type="spinner" size="large" />
      </div>
    );
  }

  // 空状态
  if (!data || data.length === 0) {
    return (
      <div className={`revert-operations-list ${className}`}>
        <Empty description={emptyText} />
      </div>
    );
  }

  return (
    <div className={`revert-operations-list ${className}`}>
      <Table<RevertableOperation>
        columns={columns}
        dataSource={data}
        rowKey="auditLogId"
        pagination={false}
      />
      
      {total > 0 && onPageChange && (
        <div className="revert-pagination">
          <Pagination
            current={currentPage}
            pageSize={pageSize}
            total={total}
            onChange={handlePaginationChange}
            showSizeChanger
            showTotal={(total) => `共 ${total} 条记录`}
          />
        </div>
      )}
    </div>
  );
};

// 类型导出
export type { 
  RevertableOperation, 
  RevertListParams, 
  RevertListResponse,
  EntityType,
  ActionType
};
