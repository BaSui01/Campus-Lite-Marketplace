/**
 * 回收站页面
 * @author BaSui 😎
 * @date 2025-11-02
 */

import React, { useState } from 'react';
import {
  Card,
  Table,
  Button,
  Select,
  Space,
  Modal,
  message,
  Typography,
  Tag,
  Tooltip,
} from 'antd';
import {
  UndoOutlined,
  DeleteOutlined,
  ReloadOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { softDeleteService } from '@campus/shared';
import { PermissionGuard } from '@/components';
import { PERMISSION_CODES } from '@campus/shared';

const { Title } = Typography;
const { Option } = Select;

interface SoftDeleteRecord {
  id: number;
  entityName: string;
  entityType: string;
  deleteReason: string;
  deletedAt: string;
  deleterName?: string;
}

const RecycleBin: React.FC = () => {
  const queryClient = useQueryClient();
  const [selectedEntity, setSelectedEntity] = useState<string>();
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(20);

  // ===== 查询可管理的实体类型 =====
  const { data: targetEntities, isLoading: targetsLoading } = useQuery({
    queryKey: ['soft-delete-targets'],
    queryFn: () => softDeleteService.listTargets(),
  });

  // ===== 查询软删除记录 =====
  const { data: records, isLoading: recordsLoading } = useQuery({
    queryKey: ['soft-delete-records', selectedEntity, currentPage, pageSize],
    queryFn: async () => {
      if (!selectedEntity) return { content: [], totalElements: 0 };
      
      const res = await fetch(`/api/admin/soft-delete/records?entity=${selectedEntity}&page=${currentPage - 1}&size=${pageSize}`);
      const data = await res.json();
      return data;
    },
    enabled: !!selectedEntity,
  });

  // ===== 恢复记录 Mutation =====
  const restoreMutation = useMutation({
    mutationFn: ({ entity, id }: { entity: string; id: number }) => {
      return softDeleteService.restore(entity, id);
    },
    onSuccess: () => {
      message.success('数据已恢复！🎉');
      queryClient.invalidateQueries({ queryKey: ['soft-delete-records'] });
    },
    onError: (error: any) => {
      message.error(`恢复失败：${error.message} 😰`);
    },
  });

  // ===== 彻底删除 Mutation =====
  const purgeMutation = useMutation({
    mutationFn: ({ entity, id }: { entity: string; id: number }) => {
      return softDeleteService.purge(entity, id);
    },
    onSuccess: () => {
      message.success('数据已彻底删除！🎉');
      queryClient.invalidateQueries({ queryKey: ['soft-delete-records'] });
    },
    onError: (error: any) => {
      message.error(`删除失败：${error.message} 😰`);
    },
  });

  // ===== 表格列定义 =====
  const columns: ColumnsType<SoftDeleteRecord> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '实体信息',
      key: 'entity',
      render: (_, record) => (
        <div>
          <div>{record.entityName}</div>
          <Tag color="blue" size="small">{record.entityType}</Tag>
        </div>
      ),
    },
    {
      title: '删除原因',
      dataIndex: 'deleteReason',
      key: 'deleteReason',
      render: (reason) => (
        <Tooltip title={reason} placement="topLeft">
          <div style={{ maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            {reason}
          </div>
        </Tooltip>
      ),
    },
    {
      title: '删除者',
      dataIndex: 'deleterName',
      key: 'deleterName',
      render: (name) => name || '系统',
    },
    {
      title: '删除时间',
      dataIndex: 'deletedAt',
      key: 'deletedAt',
      render: (time) => new Date(time).toLocaleString(),
    },
    {
      title: '操作',
      key: 'actions',
      fixed: 'right',
      width: 200,
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            size="small"
            icon={<UndoOutlined />}
            onClick={() => handleRestore(record)}
          >
            恢复
          </Button>
          <Button
            type="link"
            danger
            size="small"
            icon={<DeleteOutlined />}
            onClick={() => handlePurge(record)}
          >
            彻底删除
          </Button>
        </Space>
      ),
    },
  ];

  // ===== 恢复数据 =====
  const handleRestore = (record: SoftDeleteRecord) => {
    if (!selectedEntity) return;
    
    Modal.confirm({
      title: '恢复数据？',
      content: `确定要恢复 "${record.entityName}" 吗？数据将恢复到删除前的状态。`,
      onOk: () => restoreMutation.mutate({
        entity: selectedEntity,
        id: record.id,
      }),
    });
  };

  // ===== 彻底删除 =====
  const handlePurge = (record: SoftDeleteRecord) => {
    if (!selectedEntity) return;
    
    Modal.confirm({
      title: '彻底删除？',
      content: `确定要彻底删除 "${record.entityName}" 吗？此操作不可撤销！`,
      okText: '确认删除',
      cancelText: '取消',
      okType: 'danger',
      onOk: () => purgeMutation.mutate({
        entity: selectedEntity,
        id: record.id,
      }),
    });
  };

  // ===== 处理页码变化 =====
  const handleTableChange = (pagination: any) => {
    setCurrentPage(pagination.current);
    if (pagination.size !== pageSize) {
      setPageSize(pagination.size);
    }
  };

  return (
    <div className="recycle-bin" style={{ padding: '24px' }}>
      <Card>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Title level={2}>🗑️ 回收站</Title>
          </div>

          {/* 实体类型选择 */}
          <Card size="small" title="选择要管理的实体类型">
            <Space>
              <span>实体类型：</span>
              <Select
                placeholder="请选择实体类型"
                style={{ width: 200 }}
                value={selectedEntity}
                onChange={setSelectedEntity}
                loading={targetsLoading}
              >
                {targetEntities?.map((entity) => (
                  <Option key={entity} value={entity}>
                    {entity}
                  </Option>
                ))}
              </Select>
              <Button
                icon={<ReloadOutlined />}
                onClick={() => {
                  if (selectedEntity) {
                    queryClient.invalidateQueries({ queryKey: ['soft-delete-records'] });
                  }
                }}
                disabled={!selectedEntity}
              >
                刷新
              </Button>
            </Space>
          </Card>

          {/* 软删除记录列表 */}
          {selectedEntity && (
            <Card
              title={`${selectedEntity} 垃圾记录`}
              size="small"
              extra={
                <Tag color="orange">
                  共 {records?.totalElements || 0} 条记录
                </Tag>
              }
            >
              <Table
                columns={columns}
                dataSource={records?.content || []}
                rowKey="id"
                loading={recordsLoading}
                pagination={{
                  current: currentPage,
                  pageSize,
                  total: records?.totalElements || 0,
                  showSizeChanger: true,
                  showQuickJumper: true,
                  showTotal: (total) => `共 ${total} 条记录`,
                  onChange: handleTableChange,
                  onShowSizeChange: handleTableChange,
                }}
                scroll={{ x: 1000 }}
              />
            </Card>
          )}

          {/* 空状态提示 */}
          {!selectedEntity && !targetsLoading && (
            <Card size="small" style={{ textAlign: 'center', padding: '40px' }}>
              <div style={{ fontSize: '48px', color: '#ccc', marginBottom: '16px' }}>🗂️</div>
              <div style={{ fontSize: '16px', color: '#999' }}>请先选择要管理的实体类型</div>
            </Card>
          )}

          {selectedEntity && !recordsLoading && records?.content?.length === 0 && (
            <Card size="small" style={{ textAlign: 'center', padding: '40px' }}>
              <div style={{ fontSize: '48px', color: '#ccc', marginBottom: '16px' }}>📦</div>
              <div style={{ fontSize: '16px', color: '#999' }}>该实体暂无软删除记录</div>
            </Card>
          )}
        </Space>
      </Card>
    </div>
  );
};

export default RecycleBin;
