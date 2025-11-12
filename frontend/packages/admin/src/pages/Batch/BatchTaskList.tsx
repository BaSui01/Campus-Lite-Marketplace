/**
 * 批量任务列表页
 * @author BaSui 😎
 */

import { useState } from 'react';
import { Table, Button, Select, Space, Tag, Card, Progress, Modal, Form, Input, App } from 'antd';
import { PlusOutlined, EyeOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { batchService, BatchTaskStatus, BatchType } from '@campus/shared/services';
import type { BatchTaskResponse } from '@campus/shared/api';

const { Option } = Select;

const STATUS_MAP: Record<string, { text: string; color: string }> = {
  PENDING: { text: '等待中', color: 'default' },
  RUNNING: { text: '执行中', color: 'blue' },
  COMPLETED: { text: '已完成', color: 'green' },
  FAILED: { text: '失败', color: 'red' },
};

const TASK_TYPE_MAP: Record<string, string> = {
  BATCH_APPROVE: '批量审核',
  BATCH_DELETE: '批量删除',
  BATCH_EXPORT: '批量导出',
  BATCH_UPDATE: '批量更新',
};

export const BatchTaskList: React.FC = () => {
  const { message } = App.useApp();
  const queryClient = useQueryClient();
  const [form] = Form.useForm();
  const [status, setStatus] = useState<string | undefined>();
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [createVisible, setCreateVisible] = useState(false);

  const { data, isLoading, refetch } = useQuery({
    queryKey: ['batchTasks', { status, page, size }],
    queryFn: () => batchService.listBatchTasks({
      status: status as BatchTaskStatus,
      page,
      size
    }),
    refetchInterval: 5000,
  });

  const createMutation = useMutation({
    mutationFn: async (values: any) => { await new Promise(r => setTimeout(r, 500)); },
    onSuccess: () => { message.success('创建成功'); setCreateVisible(false); form.resetFields(); refetch(); },
  });

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
    { title: '任务编码', dataIndex: 'taskCode', key: 'taskCode', width: 150 },
    { title: '类型', dataIndex: 'batchType', key: 'batchType', width: 120, render: (t: string) => TASK_TYPE_MAP[t] || t },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (s: string) => <Tag color={STATUS_MAP[s]?.color || 'default'}>{STATUS_MAP[s]?.text || s}</Tag>,
    },
    {
      title: '进度',
      key: 'progress',
      width: 200,
      render: (_: any, record: BatchTaskResponse) => {
        const percent = record.progressPercentage || 0;
        return <Progress percent={Math.round(percent)} size="small" />;
      },
    },
    { title: '总数', dataIndex: 'totalCount', key: 'totalCount', width: 80 },
    { title: '成功', dataIndex: 'successCount', key: 'successCount', width: 80 },
    { title: '失败', dataIndex: 'errorCount', key: 'errorCount', width: 80 },
    { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 180, render: (d: string) => d ? new Date(d).toLocaleString('zh-CN') : '-' },
    {
      title: '操作',
      key: 'actions',
      fixed: 'right' as const,
      width: 100,
      render: () => (
        <Button type="link" size="small" icon={<EyeOutlined />}>查看</Button>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Space style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateVisible(true)}>创建批量任务</Button>
        <Select placeholder="任务状态" value={status} onChange={setStatus} allowClear style={{ width: 120 }}>
          {Object.entries(STATUS_MAP).map(([k, v]) => <Option key={k} value={k}>{v.text}</Option>)}
        </Select>
      </Space>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={data?.content || []}
        loading={isLoading}
        pagination={{
          current: page + 1,
          pageSize: size,
          total: data?.totalElements || 0,
          showSizeChanger: true,
          onChange: (p, s) => { setPage(p - 1); setSize(s); },
        }}
        scroll={{ x: 1200 }}
      />

      <Modal title="创建批量任务" open={createVisible} onOk={() => form.validateFields().then(createMutation.mutate)} onCancel={() => setCreateVisible(false)} confirmLoading={createMutation.isPending}>
        <Form form={form} layout="vertical">
          <Form.Item name="taskName" label="任务名称" rules={[{ required: true }]}>
            <Input placeholder="请输入任务名称" />
          </Form.Item>
          <Form.Item name="taskType" label="任务类型" rules={[{ required: true }]}>
            <Select placeholder="选择任务类型">
              {Object.entries(TASK_TYPE_MAP).map(([k, v]) => <Option key={k} value={k}>{v}</Option>)}
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default BatchTaskList;
