/**
 * 功能开关管理页
 * 
 * 功能：
 * - 分页查询功能开关列表
 * - 关键词搜索
 * - 状态和环境筛选
 * - 添加/编辑/删除功能开关
 * - 灰度策略配置（按用户/校园/百分比）
 * - 使用日志查看
 * - 快速启用/禁用
 * 
 * @author BaSui 😎
 * @date 2025-11-06
 */

import { useState } from 'react';
import {
  Table,
  Button,
  Input,
  Select,
  Space,
  Tag,
  message,
  Modal,
  Form,
  Card,
  Statistic,
  Row,
  Col,
  Popconfirm,
  Drawer,
  List,
  InputNumber,
  Slider,
  Badge,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  SearchOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ExperimentOutlined,
  FileTextOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { featureFlagService, FeatureFlagStatus, GrayStrategy, Environment } from '@campus/shared/services';
import type { FeatureFlag, FeatureFlagRequest, GrayRuleConfig } from '@campus/shared/services';

const { Option } = Select;
const { TextArea } = Input;

/**
 * 状态映射
 */
const STATUS_MAP: Record<FeatureFlagStatus, { text: string; color: string; icon: React.ReactNode }> = {
  [FeatureFlagStatus.ENABLED]: { 
    text: '全量启用', 
    color: 'green', 
    icon: <CheckCircleOutlined /> 
  },
  [FeatureFlagStatus.DISABLED]: { 
    text: '全量禁用', 
    color: 'red', 
    icon: <CloseCircleOutlined /> 
  },
  [FeatureFlagStatus.GRAY]: { 
    text: '灰度发布', 
    color: 'orange', 
    icon: <ExperimentOutlined /> 
  },
};

/**
 * 环境映射
 */
const ENV_MAP: Record<Environment, { text: string; color: string }> = {
  [Environment.DEV]: { text: '开发', color: 'blue' },
  [Environment.TEST]: { text: '测试', color: 'cyan' },
  [Environment.PROD]: { text: '生产', color: 'red' },
};

/**
 * 功能开关列表页组件
 */
export const FeatureFlagList: React.FC = () => {
  const queryClient = useQueryClient();
  const [form] = Form.useForm<FeatureFlagRequest>();

  // 查询参数
  const [keyword, setKeyword] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<FeatureFlagStatus | undefined>();
  const [envFilter, setEnvFilter] = useState<Environment | undefined>();
  const [page, setPage] = useState<number>(0);
  const [size] = useState<number>(20);

  // 弹窗状态
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingFlag, setEditingFlag] = useState<FeatureFlag | null>(null);
  const [logsDrawerVisible, setLogsDrawerVisible] = useState(false);
  const [selectedFlagId, setSelectedFlagId] = useState<number | null>(null);

  // 表单状态
  const [currentStatus, setCurrentStatus] = useState<FeatureFlagStatus>(FeatureFlagStatus.ENABLED);
  const [currentStrategy, setCurrentStrategy] = useState<GrayStrategy | undefined>();

  // 查询功能开关列表
  const { data, isLoading, refetch } = useQuery({
    queryKey: ['feature-flags', 'list', { keyword, statusFilter, envFilter, page, size }],
    queryFn: () =>
      featureFlagService.list({
        keyword,
        status: statusFilter,
        environment: envFilter,
        page,
        size,
      }),
    staleTime: 2 * 60 * 1000,
  });

  // 查询使用日志
  const { data: logs } = useQuery({
    queryKey: ['feature-flags', 'logs', selectedFlagId],
    queryFn: () => featureFlagService.getLogs(selectedFlagId!),
    enabled: !!selectedFlagId && logsDrawerVisible,
  });

  // 添加/编辑功能开关
  const saveMutation = useMutation({
    mutationFn: (values: FeatureFlagRequest) =>
      editingFlag
        ? featureFlagService.update(editingFlag.id, values)
        : featureFlagService.create(values),
    onSuccess: () => {
      message.success(editingFlag ? '编辑成功' : '添加成功');
      setIsModalVisible(false);
      form.resetFields();
      setEditingFlag(null);
      refetch();
      queryClient.invalidateQueries({ queryKey: ['feature-flags'] });
    },
    onError: (error: any) => {
      message.error(error?.message || '操作失败');
    },
  });

  // 删除功能开关
  const deleteMutation = useMutation({
    mutationFn: (id: number) => featureFlagService.delete(id),
    onSuccess: () => {
      message.success('删除成功');
      refetch();
      queryClient.invalidateQueries({ queryKey: ['feature-flags'] });
    },
    onError: (error: any) => {
      message.error(error?.message || '删除失败');
    },
  });

  // 更新状态
  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: FeatureFlagStatus }) =>
      featureFlagService.updateStatus(id, status),
    onSuccess: () => {
      message.success('状态更新成功');
      refetch();
      queryClient.invalidateQueries({ queryKey: ['feature-flags'] });
    },
    onError: () => {
      message.error('状态更新失败');
    },
  });

  // 统计数据
  const totalCount = data?.totalElements || 0;
  const enabledCount = data?.content.filter(f => f.status === FeatureFlagStatus.ENABLED).length || 0;
  const grayCount = data?.content.filter(f => f.status === FeatureFlagStatus.GRAY).length || 0;

  // 搜索处理
  const handleSearch = () => {
    setPage(0);
    refetch();
  };

  // 重置筛选
  const handleReset = () => {
    setKeyword('');
    setStatusFilter(undefined);
    setEnvFilter(undefined);
    setPage(0);
  };

  // 打开添加弹窗
  const handleAdd = () => {
    setEditingFlag(null);
    form.resetFields();
    form.setFieldsValue({ 
      status: FeatureFlagStatus.ENABLED,
      environment: Environment.DEV
    });
    setCurrentStatus(FeatureFlagStatus.ENABLED);
    setCurrentStrategy(undefined);
    setIsModalVisible(true);
  };

  // 打开编辑弹窗
  const handleEdit = (flag: FeatureFlag) => {
    setEditingFlag(flag);
    
    // 解析灰度规则
    const grayRule = featureFlagService.parseGrayRule(flag.grayRule);
    
    form.setFieldsValue({
      name: flag.name,
      key: flag.key,
      description: flag.description,
      status: flag.status,
      strategy: flag.strategy,
      environment: flag.environment,
      ...grayRule
    });
    
    setCurrentStatus(flag.status);
    setCurrentStrategy(flag.strategy);
    setIsModalVisible(true);
  };

  // 删除功能开关
  const handleDelete = (id: number) => {
    deleteMutation.mutate(id);
  };

  // 快速切换状态
  const handleQuickToggle = (flag: FeatureFlag) => {
    const newStatus = flag.status === FeatureFlagStatus.ENABLED 
      ? FeatureFlagStatus.DISABLED 
      : FeatureFlagStatus.ENABLED;
    
    updateStatusMutation.mutate({ id: flag.id, status: newStatus });
  };

  // 查看使用日志
  const handleViewLogs = (flagId: number) => {
    setSelectedFlagId(flagId);
    setLogsDrawerVisible(true);
  };

  // 保存表单
  const handleFormSubmit = async () => {
    try {
      const values = await form.validateFields();
      
      // 构建灰度规则
      let grayRule: GrayRuleConfig | undefined;
      if (values.status === FeatureFlagStatus.GRAY && values.strategy) {
        if (values.strategy === GrayStrategy.USER && values.userIds) {
          grayRule = { userIds: values.userIds };
        } else if (values.strategy === GrayStrategy.CAMPUS && values.campusIds) {
          grayRule = { campusIds: values.campusIds };
        } else if (values.strategy === GrayStrategy.PERCENTAGE && values.percentage !== undefined) {
          grayRule = { percentage: values.percentage };
        }
      }
      
      const requestData: FeatureFlagRequest = {
        name: values.name,
        key: values.key,
        description: values.description,
        status: values.status,
        strategy: values.status === FeatureFlagStatus.GRAY ? values.strategy : undefined,
        grayRule,
        environment: values.environment,
      };
      
      saveMutation.mutate(requestData);
    } catch (error) {
      console.error('表单验证失败:', error);
    }
  };

  // 渲染灰度规则
  const renderGrayRule = (flag: FeatureFlag) => {
    if (flag.status !== FeatureFlagStatus.GRAY || !flag.grayRule) {
      return '-';
    }
    
    const rule = featureFlagService.parseGrayRule(flag.grayRule);
    if (!rule) return '-';
    
    if (rule.userIds && rule.userIds.length > 0) {
      return `用户ID: ${rule.userIds.slice(0, 3).join(', ')}${rule.userIds.length > 3 ? '...' : ''}`;
    } else if (rule.campusIds && rule.campusIds.length > 0) {
      return `校园ID: ${rule.campusIds.slice(0, 3).join(', ')}${rule.campusIds.length > 3 ? '...' : ''}`;
    } else if (rule.percentage !== undefined) {
      return `百分比: ${rule.percentage}%`;
    }
    
    return '-';
  };

  // 表格列定义
  const columns: ColumnsType<FeatureFlag> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '功能名称',
      dataIndex: 'name',
      key: 'name',
      width: 150,
    },
    {
      title: '功能Key',
      dataIndex: 'key',
      key: 'key',
      width: 180,
      render: (key: string) => (
        <Tag icon={<ThunderboltOutlined />} color="purple">{key}</Tag>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 120,
      render: (status: FeatureFlagStatus) => {
        const statusInfo = STATUS_MAP[status];
        return (
          <Tag color={statusInfo.color} icon={statusInfo.icon}>
            {statusInfo.text}
          </Tag>
        );
      },
    },
    {
      title: '灰度策略',
      dataIndex: 'strategy',
      key: 'strategy',
      width: 200,
      render: (_: any, record: FeatureFlag) => renderGrayRule(record),
    },
    {
      title: '环境',
      dataIndex: 'environment',
      key: 'environment',
      width: 100,
      render: (env: Environment) => {
        const envInfo = ENV_MAP[env];
        return <Tag color={envInfo.color}>{envInfo.text}</Tag>;
      },
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      width: 200,
      ellipsis: true,
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (date: string) => new Date(date).toLocaleString('zh-CN'),
    },
    {
      title: '操作',
      key: 'actions',
      fixed: 'right',
      width: 260,
      render: (_: any, record: FeatureFlag) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<FileTextOutlined />}
            onClick={() => handleViewLogs(record.id)}
          >
            日志
          </Button>
          <Button
            type="link"
            size="small"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Button
            type="link"
            size="small"
            onClick={() => handleQuickToggle(record)}
          >
            {record.status === FeatureFlagStatus.ENABLED ? '禁用' : '启用'}
          </Button>
          <Popconfirm
            title="确定删除此功能开关吗？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button
              type="link"
              size="small"
              danger
              icon={<DeleteOutlined />}
            >
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <h2 style={{ marginBottom: 24 }}>功能开关管理</h2>

      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={8}>
          <Card>
            <Statistic
              title="功能总数"
              value={totalCount}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="全量启用"
              value={enabledCount}
              valueStyle={{ color: '#52c41a' }}
              prefix={<CheckCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="灰度发布中"
              value={grayCount}
              valueStyle={{ color: '#faad14' }}
              prefix={<ExperimentOutlined />}
            />
          </Card>
        </Col>
      </Row>

      {/* 搜索筛选区 */}
      <Card style={{ marginBottom: 16 }}>
        <Space size="middle" wrap>
          <Input
            placeholder="搜索功能名称或Key"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onPressEnter={handleSearch}
            style={{ width: 250 }}
            prefix={<SearchOutlined />}
          />
          <Select
            placeholder="状态筛选"
            value={statusFilter}
            onChange={setStatusFilter}
            allowClear
            style={{ width: 150 }}
          >
            <Option value={FeatureFlagStatus.ENABLED}>全量启用</Option>
            <Option value={FeatureFlagStatus.DISABLED}>全量禁用</Option>
            <Option value={FeatureFlagStatus.GRAY}>灰度发布</Option>
          </Select>
          <Select
            placeholder="环境筛选"
            value={envFilter}
            onChange={setEnvFilter}
            allowClear
            style={{ width: 120 }}
          >
            <Option value={Environment.DEV}>开发</Option>
            <Option value={Environment.TEST}>测试</Option>
            <Option value={Environment.PROD}>生产</Option>
          </Select>
          <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
            查询
          </Button>
          <Button onClick={handleReset}>重置</Button>
        </Space>
      </Card>

      {/* 操作按钮 */}
      <div style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          添加功能开关
        </Button>
      </div>

      {/* 数据表格 */}
      <Table
        columns={columns}
        dataSource={data?.content || []}
        rowKey="id"
        loading={isLoading}
        pagination={{
          current: page + 1,
          pageSize: size,
          total: totalCount,
          showSizeChanger: false,
          showTotal: (total) => `共 ${total} 条`,
          onChange: (newPage) => setPage(newPage - 1),
        }}
        scroll={{ x: 1600 }}
      />

      {/* 添加/编辑弹窗 */}
      <Modal
        title={editingFlag ? '编辑功能开关' : '添加功能开关'}
        open={isModalVisible}
        onOk={handleFormSubmit}
        onCancel={() => {
          setIsModalVisible(false);
          form.resetFields();
          setEditingFlag(null);
        }}
        width={700}
        confirmLoading={saveMutation.isPending}
      >
        <Form
          form={form}
          layout="vertical"
          initialValues={{ 
            status: FeatureFlagStatus.ENABLED,
            environment: Environment.DEV
          }}
        >
          <Form.Item
            label="功能名称"
            name="name"
            rules={[
              { required: true, message: '请输入功能名称' },
              { min: 2, max: 50, message: '功能名称长度为2-50个字符' },
            ]}
          >
            <Input placeholder="请输入功能名称" />
          </Form.Item>

          <Form.Item
            label="功能Key"
            name="key"
            tooltip="全局唯一标识，只能包含字母、数字和下划线"
            rules={[
              { required: true, message: '请输入功能Key' },
              { pattern: /^[A-Za-z0-9_]+$/, message: '只能包含字母、数字和下划线' },
            ]}
          >
            <Input placeholder="例如: FEATURE_NEW_PAYMENT" disabled={!!editingFlag} />
          </Form.Item>

          <Form.Item
            label="功能描述"
            name="description"
            rules={[
              { required: true, message: '请输入功能描述' },
              { max: 200, message: '描述长度不能超过200个字符' },
            ]}
          >
            <TextArea 
              placeholder="请输入功能描述" 
              rows={3}
              maxLength={200}
              showCount
            />
          </Form.Item>

          <Form.Item
            label="状态"
            name="status"
            rules={[{ required: true, message: '请选择状态' }]}
          >
            <Select onChange={setCurrentStatus}>
              <Option value={FeatureFlagStatus.ENABLED}>全量启用</Option>
              <Option value={FeatureFlagStatus.DISABLED}>全量禁用</Option>
              <Option value={FeatureFlagStatus.GRAY}>灰度发布</Option>
            </Select>
          </Form.Item>

          {currentStatus === FeatureFlagStatus.GRAY && (
            <>
              <Form.Item
                label="灰度策略"
                name="strategy"
                rules={[{ required: true, message: '请选择灰度策略' }]}
              >
                <Select onChange={setCurrentStrategy} placeholder="请选择灰度策略">
                  <Option value={GrayStrategy.USER}>按用户ID</Option>
                  <Option value={GrayStrategy.CAMPUS}>按校园ID</Option>
                  <Option value={GrayStrategy.PERCENTAGE}>按百分比</Option>
                </Select>
              </Form.Item>

              {currentStrategy === GrayStrategy.USER && (
                <Form.Item
                  label="用户ID列表"
                  name="userIds"
                  tooltip="输入用户ID，用逗号分隔"
                  rules={[{ required: true, message: '请输入用户ID列表' }]}
                >
                  <Select mode="tags" placeholder="输入用户ID后按回车">
                    {/* 动态输入 */}
                  </Select>
                </Form.Item>
              )}

              {currentStrategy === GrayStrategy.CAMPUS && (
                <Form.Item
                  label="校园ID列表"
                  name="campusIds"
                  tooltip="输入校园ID，用逗号分隔"
                  rules={[{ required: true, message: '请输入校园ID列表' }]}
                >
                  <Select mode="tags" placeholder="输入校园ID后按回车">
                    {/* 动态输入 */}
                  </Select>
                </Form.Item>
              )}

              {currentStrategy === GrayStrategy.PERCENTAGE && (
                <Form.Item
                  label="灰度百分比"
                  name="percentage"
                  rules={[{ required: true, message: '请设置灰度百分比' }]}
                >
                  <Slider 
                    min={0} 
                    max={100} 
                    marks={{ 0: '0%', 25: '25%', 50: '50%', 75: '75%', 100: '100%' }}
                  />
                </Form.Item>
              )}
            </>
          )}

          <Form.Item
            label="环境"
            name="environment"
            rules={[{ required: true, message: '请选择环境' }]}
          >
            <Select>
              <Option value={Environment.DEV}>开发环境</Option>
              <Option value={Environment.TEST}>测试环境</Option>
              <Option value={Environment.PROD}>生产环境</Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>

      {/* 使用日志抽屉 */}
      <Drawer
        title="使用日志"
        open={logsDrawerVisible}
        onClose={() => {
          setLogsDrawerVisible(false);
          setSelectedFlagId(null);
        }}
        width={720}
      >
        <List
          dataSource={logs?.content || []}
          renderItem={(log) => (
            <List.Item>
              <List.Item.Meta
                title={
                  <Space>
                    <span>{log.userName} ({log.userId})</span>
                    <Badge 
                      status={log.result ? 'success' : 'error'} 
                      text={log.result ? '通过' : '拒绝'}
                    />
                  </Space>
                }
                description={
                  <Space direction="vertical" size="small">
                    <div>操作: {log.action}</div>
                    <div>时间: {new Date(log.createdAt).toLocaleString('zh-CN')}</div>
                  </Space>
                }
              />
            </List.Item>
          )}
          pagination={{
            pageSize: 20,
            total: logs?.totalElements || 0,
          }}
        />
      </Drawer>
    </div>
  );
};

export default FeatureFlagList;
