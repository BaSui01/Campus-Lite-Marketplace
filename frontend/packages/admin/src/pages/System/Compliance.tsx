/**
 * 合规审计页面
 * @author BaSui 😎
 * @date 2025-11-02
 */

import React, { useState } from 'react';
import {
  Card,
  Table,
  Button,
  Input,
  Select,
  Space,
  Form,
  Typography,
  Tag,
  Popconfirm,
  Tooltip,
  App,
  Modal,
} from 'antd';
import {
  PlusOutlined,
  DeleteOutlined,
  EyeOutlined,
  SafetyOutlined,
  FileSearchOutlined,
} from '@ant-design/icons';
import type { ColumnsType } from 'antd/es/table';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { complianceService } from '@/services';
import { PermissionGuard } from '@/components';
import { PERMISSION_CODES } from '@campus/shared';
import type { ComplianceWhitelistItem, ComplianceAuditLog } from '@campus/shared';

const { Title } = Typography;
const { Option } = Select;
const { Search } = Input;

const Compliance: React.FC = () => {
  const queryClient = useQueryClient();
  const { message, modal } = App.useApp();
  const [whitelistModalVisible, setWhitelistModalVisible] = useState(false);
  const [auditModalVisible, setAuditModalVisible] = useState(false);
  const [selectedTargetType, setSelectedTargetType] = useState<string>();
  const [selectedTargetId, setSelectedTargetId] = useState<string>();
  const [whitelistForm] = Form.useForm();

  // ===== 白名单查询 =====
  const { data: whitelist, isLoading: whitelistLoading } = useQuery({
    queryKey: ['compliance-whitelist'],
    queryFn: () => complianceService.listWhitelist(),
  });

  // ===== 审计日志查询 =====
  const { data: auditLogs, isLoading: auditLoading } = useQuery({
    queryKey: ['compliance-audit', selectedTargetType, selectedTargetId],
    queryFn: async () => {
      if (!selectedTargetType || !selectedTargetId) {
        return { content: [], totalElements: 0 };
      }
      const res = await complianceService.listAudit(
        selectedTargetType,
        parseInt(selectedTargetId)
      );
      return res;
    },
    enabled: !!(selectedTargetType && selectedTargetId),
  });

  // ===== 添加白名单 Mutation =====
  const addWhitelistMutation = useMutation({
    mutationFn: (data: { type: string; targetId: number }) => {
      return complianceService.addWhitelist(data.type, data.targetId);
    },
    onSuccess: () => {
      message.success('已加入白名单！🎉');
      queryClient.invalidateQueries({ queryKey: ['compliance-whitelist'] });
      setWhitelistModalVisible(false);
      whitelistForm.resetFields();
    },
    onError: (error: any) => {
      message.error(`操作失败：${error.message} 😰`);
    },
  });

  // ===== 移除白名单 Mutation =====
  const removeWhitelistMutation = useMutation({
    mutationFn: (id: number) => {
      return complianceService.removeWhitelist(id);
    },
    onSuccess: () => {
      message.success('已从白名单移除！🎉');
      queryClient.invalidateQueries({ queryKey: ['compliance-whitelist'] });
    },
    onError: (error: any) => {
      message.error(`操作失败：${error.message} 😰`);
    },
  });

  // ===== 白名单表格列 =====
  const whitelistColumns: ColumnsType<ComplianceWhitelistItem> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      render: (type) => {
        const typeColors: Record<string, string> = {
          USER: 'blue',
          GOODS: 'green',
          POST: 'orange',
          COMMENT: 'purple',
        };
        return <Tag color={typeColors[type] || 'default'}>{type}</Tag>;
      },
    },
    {
      title: '目标ID',
      dataIndex: 'targetId',
      key: 'targetId',
      render: (targetId, record) => (
        <div>
          <span>ID: {targetId}</span>
          <br />
          <small style={{ color: '#999' }}>{record.type}数据</small>
        </div>
      ),
    },
    {
      title: '添加时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (time) => time ? new Date(time).toLocaleString() : '-',
    },
    {
      title: '操作',
      key: 'actions',
      render: (_, record) => (
        <PermissionGuard permission={PERMISSION_CODES.SYSTEM_COMPLIANCE_REVIEW}>
          <Popconfirm
            title="确定要移除白名单吗？"
            onConfirm={() => removeWhitelistMutation.mutate(record.id)}
            okText="确认"
            cancelText="取消"
          >
            <Button
              type="link"
              danger
              size="small"
              icon={<DeleteOutlined />}
              loading={removeWhitelistMutation.isPending}
            >
              移除
            </Button>
          </Popconfirm>
        </PermissionGuard>
      ),
    },
  ];

  // ===== 审计日志表格列 =====
  const auditLogColumns: ColumnsType<ComplianceAuditLog> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '目标',
      key: 'target',
      render: (_, record) => (
        <div>
          <Tag color="blue">{record.targetType}</Tag>
          <span style={{ marginLeft: 8 }}>ID: {record.targetId}</span>
        </div>
      ),
    },
    {
      title: '操作',
      dataIndex: 'action',
      key: 'action',
      render: (action) => {
        const actionColors: Record<string, string> = {
          READ: 'blue',
          WRITE: 'green',
          DELETE: 'red',
          APPROVE: 'orange',
        };
        return <Tag color={actionColors[action] || 'default'}>{action}</Tag>;
      },
    },
    {
      title: '操作人',
      key: 'operator',
      render: (_, record) => (
        <div>
          <div>{record.operatorName || `用户${record.operatorId}`}</div>
          <small style={{ color: '#999' }}>ID: {record.operatorId}</small>
        </div>
      ),
    },
    {
      title: '备注',
      dataIndex: 'remark',
      key: 'remark',
      render: (remark) => (
        <Tooltip title={remark} placement="topLeft">
          <div style={{ maxWidth: 150, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            {remark || '-'}
          </div>
        </Tooltip>
      ),
    },
    {
      title: '操作时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (time) => new Date(time).toLocaleString(),
      width: 180,
    },
  ];

  // ===== 添加到白名单 =====
  const handleAddWhitelist = () => {
    setWhitelistModalVisible(true);
  };

  // ===== 查看审计日志 =====
  const handleViewAudit = (type: string, targetId: number) => {
    setSelectedTargetType(type);
    setSelectedTargetId(targetId.toString());
    setAuditModalVisible(true);
  };

  // ===== 表单提交 =====
  const handleWhitelistSubmit = () => {
    whitelistForm.validateFields().then((values) => {
      addWhitelistMutation.mutate(values);
    });
  };

  return (
    <div className="compliance" style={{ padding: '24px' }}>
      <Card>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <Title level={2}>⚖️ 合规审计管理</Title>

          {/* 白名单管理 */}
          <Card 
            title={
              <Space>
                <SafetyOutlined />
                <span>合规白名单</span>
                <PermissionGuard permission={PERMISSION_CODES.SYSTEM_COMPLIANCE_REVIEW}>
                  <Button
                    type="primary"
                    size="small"
                    icon={<PlusOutlined />}
                    onClick={handleAddWhitelist}
                  >
                    添加白名单
                  </Button>
                </PermissionGuard>
              </Space>
            }
          >
            <Table
              columns={whitelistColumns}
              dataSource={whitelist}
              rowKey="id"
              loading={whitelistLoading}
              pagination={{
                showSizeChanger: true,
                showQuickJumper: true,
                showTotal: (total) => `共 ${total} 条记录`,
              }}
              locale={{ emptyText: '暂无白名单记录' }}
            />
          </Card>

          {/* 审计日志 */}
          <Card 
            title={
              <Space>
                <FileSearchOutlined />
                <span>审计日志</span>
                <Search
                  placeholder="输入类型和ID查看日志 (例如: USER/123)"
                  style={{ width: 300 }}
                  onSearch={(value) => {
                    if (value) {
                      const [type, id] = value.split('/');
                      if (type && id) {
                        setSelectedTargetType(type);
                        setSelectedTargetId(id);
                      }
                    }
                  }}
                  allowClear
                />
              </Space>
            }
          >
            <Table
              columns={auditLogColumns}
              dataSource={auditLogs?.content || []}
              rowKey="id"
              loading={auditLoading}
              pagination={{
                showSizeChanger: true,
                showQuickJumper: true,
                showTotal: (total) => `共 ${total} 条记录`,
              }}
              locale={{ emptyText: '请选择类型和ID查看审计日志' }}
            />
          </Card>
        </Space>
      </Card>

      {/* 添加白名单弹窗 */}
      <Modal
        title="添加到白名单"
        open={whitelistModalVisible}
        onOk={handleWhitelistSubmit}
        onCancel={() => {
          setWhitelistModalVisible(false);
          whitelistForm.resetFields();
        }}
        confirmLoading={addWhitelistMutation.isPending}
      >
        <Form form={whitelistForm} layout="vertical">
          <Form.Item
            name="type"
            label="目标类型"
            rules={[{ required: true, message: '请选择目标类型！' }]}
          >
            <Select placeholder="请选择目标类型">
              <Option value="USER">用户</Option>
              <Option value="GOODS">商品</Option>
              <Option value="POST">帖子</Option>
              <Option value="COMMENT">评论</Option>
            </Select>
          </Form.Item>
          <Form.Item
            name="targetId"
            label="目标ID"
            rules={[
              { required: true, message: '请输入目标ID！' },
              { type: 'number', message: '请输入有效的数字ID！' },
            ]}
          >
            <Input placeholder="请输入对象ID" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 审计日志弹窗 */}
      <Modal
        title={`审计日志：${selectedTargetType} ID:${selectedTargetId}`}
        open={auditModalVisible}
        onCancel={() => {
          setAuditModalVisible(false);
          setSelectedTargetType(undefined);
          setSelectedTargetId(undefined);
        }}
        width={1000}
        footer={null}
      >
        <Table
          columns={auditLogColumns}
          dataSource={auditLogs?.content || []}
          rowKey="id"
          loading={auditLoading}
          pagination={{
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`,
          }}
        />
      </Modal>
    </div>
  );
};

export default Compliance;
