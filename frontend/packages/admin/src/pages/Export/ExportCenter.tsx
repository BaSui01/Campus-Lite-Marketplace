/**
 * 导出中心
 * 
 * 功能：
 * - 创建导出任务
 * - 查看导出任务列表
 * - 查看导出任务状态
 * - 下载导出文件
 * - 取消导出任务
 * - 导出类型选择（订单、用户、商品、评价、纠纷、退款）
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */

import { useState } from 'react';
import {
  Table,
  Button,
  Space,
  Tag,
  Card,
  Row,
  Col,
  Statistic,
  message,
  Modal,
  Form,
  Select,
  DatePicker,
  Input,
  Progress,
} from 'antd';
import {
  DownloadOutlined,
  FileExcelOutlined,
  CloseCircleOutlined,
  CheckCircleOutlined,
  SyncOutlined,
  PlusOutlined,
  DeleteOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { exportService, ExportType } from '@campus/shared';
import type { ExportJob } from '@campus/shared';
import dayjs, { Dayjs } from 'dayjs';

const { Option } = Select;
const { RangePicker } = DatePicker;

/**
 * 任务状态映射
 */
const STATUS_MAP: Record<string, { text: string; color: string; icon: React.ReactNode }> = {
  PENDING: { 
    text: '待处理', 
    color: 'default',
    icon: <SyncOutlined spin />
  },
  PROCESSING: { 
    text: '处理中', 
    color: 'blue',
    icon: <SyncOutlined spin />
  },
  COMPLETED: { 
    text: '已完成', 
    color: 'green',
    icon: <CheckCircleOutlined />
  },
  FAILED: { 
    text: '失败', 
    color: 'red',
    icon: <CloseCircleOutlined />
  },
  CANCELLED: { 
    text: '已取消', 
    color: 'default',
    icon: <DeleteOutlined />
  },
};

/**
 * 导出类型配置
 */
const EXPORT_TYPE_CONFIG: Record<string, { text: string; icon: React.ReactNode }> = {
  orders: { text: '订单数据', icon: <FileExcelOutlined /> },
  users: { text: '用户数据', icon: <FileExcelOutlined /> },
  goods: { text: '商品数据', icon: <FileExcelOutlined /> },
  reviews: { text: '评价数据', icon: <FileExcelOutlined /> },
  disputes: { text: '纠纷数据', icon: <FileExcelOutlined /> },
  refunds: { text: '退款数据', icon: <FileExcelOutlined /> },
};

export const ExportCenter: React.FC = () => {
  const queryClient = useQueryClient();
  const [form] = Form.useForm();
  const [isModalVisible, setIsModalVisible] = useState(false);

  // 查询导出任务列表
  const { data, isLoading, refetch } = useQuery({
    queryKey: ['exports', 'list'],
    queryFn: () => exportService.listMyExports(),
    refetchInterval: 5000, // 每5秒刷新一次（查看任务进度）
  });

  // 创建导出任务
  const createMutation = useMutation({
    mutationFn: exportService.requestExport,
    onSuccess: () => {
      message.success('导出任务创建成功');
      setIsModalVisible(false);
      form.resetFields();
      refetch();
    },
    onError: () => {
      message.error('导出任务创建失败');
    },
  });

  // 取消导出任务
  const cancelMutation = useMutation({
    mutationFn: (id: number) => exportService.cancelExport(id),
    onSuccess: () => {
      message.success('导出任务已取消');
      refetch();
    },
    onError: () => {
      message.error('取消导出任务失败');
    },
  });

  // 显示创建对话框
  const showCreateModal = () => {
    setIsModalVisible(true);
  };

  // 提交创建
  const handleCreate = async () => {
    try {
      const values = await form.validateFields();
      
      // 构建导出参数
      const params: any = {};
      if (values.dateRange) {
        params.dateFrom = values.dateRange[0].format('YYYY-MM-DD');
        params.dateTo = values.dateRange[1].format('YYYY-MM-DD');
      }
      if (values.keyword) {
        params.keyword = values.keyword;
      }

      createMutation.mutate({
        type: values.type,
        params: JSON.stringify(params),
      });
    } catch (error) {
      console.error('表单验证失败', error);
    }
  };

  // 下载文件
  const handleDownload = (token: string, fileName: string) => {
    const downloadUrl = exportService.downloadExport(token);
    // 创建隐藏的a标签触发下载
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = fileName;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    message.success('开始下载');
  };

  // 取消任务
  const handleCancel = (id: number) => {
    Modal.confirm({
      title: '确认取消',
      content: '确定要取消该导出任务吗？',
      onOk: () => cancelMutation.mutate(id),
    });
  };

  // 计算统计数据
  const stats = {
    total: data?.length || 0,
    pending: data?.filter(j => j.status === 'PENDING' || j.status === 'PROCESSING').length || 0,
    completed: data?.filter(j => j.status === 'COMPLETED').length || 0,
    failed: data?.filter(j => j.status === 'FAILED').length || 0,
  };

  // 表格列定义
  const columns = [
    {
      title: '任务ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '导出类型',
      dataIndex: 'type',
      key: 'type',
      width: 120,
      render: (type: string) => {
        const config = EXPORT_TYPE_CONFIG[type] || { text: type, icon: null };
        return (
          <Space>
            {config.icon}
            <span>{config.text}</span>
          </Space>
        );
      },
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status: string) => {
        const config = STATUS_MAP[status] || { text: status, color: 'default', icon: null };
        return (
          <Tag color={config.color} icon={config.icon}>
            {config.text}
          </Tag>
        );
      },
    },
    {
      title: '文件名',
      dataIndex: 'fileName',
      key: 'fileName',
      width: 200,
      ellipsis: true,
      render: (fileName: string) => fileName || '-',
    },
    {
      title: '文件大小',
      dataIndex: 'fileSize',
      key: 'fileSize',
      width: 120,
      render: (size: number) => {
        if (!size) return '-';
        if (size < 1024) return `${size} B`;
        if (size < 1024 * 1024) return `${(size / 1024).toFixed(2)} KB`;
        return `${(size / 1024 / 1024).toFixed(2)} MB`;
      },
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (createdAt: string) => dayjs(createdAt).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '完成时间',
      dataIndex: 'completedAt',
      key: 'completedAt',
      width: 180,
      render: (completedAt: string) => completedAt ? dayjs(completedAt).format('YYYY-MM-DD HH:mm:ss') : '-',
    },
    {
      title: '过期时间',
      dataIndex: 'expiredAt',
      key: 'expiredAt',
      width: 180,
      render: (expiredAt: string) => {
        if (!expiredAt) return '-';
        const isExpired = dayjs(expiredAt).isBefore(dayjs());
        return (
          <span style={{ color: isExpired ? '#f5222d' : undefined }}>
            {dayjs(expiredAt).format('YYYY-MM-DD HH:mm:ss')}
            {isExpired && ' (已过期)'}
          </span>
        );
      },
    },
    {
      title: '操作',
      key: 'action',
      fixed: 'right' as const,
      width: 180,
      render: (_: unknown, record: ExportJob) => (
        <Space size="small">
          {record.status === 'COMPLETED' && record.downloadToken && (
            <Button
              type="link"
              size="small"
              icon={<DownloadOutlined />}
              onClick={() => handleDownload(record.downloadToken!, record.type || 'export')}
              disabled={record.expireAt ? dayjs(record.expireAt).isBefore(dayjs()) : false}
            >
              下载
            </Button>
          )}
          {(record.status === 'PENDING' || record.status === 'PROCESSING') && (
            <Button
              type="link"
              size="small"
              danger
              icon={<CloseCircleOutlined />}
              onClick={() => handleCancel(record.id!)}
            >
              取消
            </Button>
          )}
          {record.status === 'FAILED' && (
            <Button
              type="link"
              size="small"
              onClick={() => {
                Modal.error({
                  title: '错误信息',
                  content: record.message || '导出失败',
                });
              }}
            >
              查看错误
            </Button>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: '24px' }}>
      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="总任务数"
              value={stats.total}
              prefix={<FileExcelOutlined />}
              suffix="个"
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="进行中"
              value={stats.pending}
              prefix={<SyncOutlined spin />}
              suffix="个"
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="已完成"
              value={stats.completed}
              prefix={<CheckCircleOutlined />}
              suffix="个"
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="失败"
              value={stats.failed}
              prefix={<CloseCircleOutlined />}
              suffix="个"
              valueStyle={{ color: '#cf1322' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 操作区域 */}
      <Card style={{ marginBottom: 24 }}>
        <Space>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={showCreateModal}
          >
            创建导出任务
          </Button>
          <Button icon={<SyncOutlined />} onClick={() => refetch()}>
            刷新
          </Button>
        </Space>
      </Card>

      {/* 数据表格 */}
      <Card>
        <Table
          columns={columns}
          dataSource={data || []}
          loading={isLoading}
          rowKey="id"
          scroll={{ x: 1500 }}
          pagination={{
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`,
          }}
        />
      </Card>

      {/* 创建导出任务对话框 */}
      <Modal
        title="创建导出任务"
        open={isModalVisible}
        onOk={handleCreate}
        onCancel={() => {
          setIsModalVisible(false);
          form.resetFields();
        }}
        confirmLoading={createMutation.isPending}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="type"
            label="导出类型"
            rules={[{ required: true, message: '请选择导出类型' }]}
          >
            <Select placeholder="请选择导出类型">
              <Option value={ExportType.ORDERS}>订单数据</Option>
              <Option value={ExportType.USERS}>用户数据</Option>
              <Option value={ExportType.GOODS}>商品数据</Option>
              <Option value={ExportType.REVIEWS}>评价数据</Option>
              <Option value={ExportType.DISPUTES}>纠纷数据</Option>
              <Option value={ExportType.REFUNDS}>退款数据</Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="dateRange"
            label="时间范围"
          >
            <RangePicker
              style={{ width: '100%' }}
              format="YYYY-MM-DD"
              placeholder={['开始日期', '结束日期']}
            />
          </Form.Item>

          <Form.Item
            name="keyword"
            label="关键词"
          >
            <Input placeholder="可选，用于筛选数据" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default ExportCenter;
