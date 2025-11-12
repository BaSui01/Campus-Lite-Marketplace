/**
 * 校园管理页
 * 
 * 功能：
 * - 分页查询校园列表
 * - 关键词搜索（名称、代码）
 * - 状态筛选（启用/禁用）
 * - 添加/编辑/删除校园
 * - 启用/禁用校园
 * - 查看校园统计（用户数、商品数、订单数）
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
  Form,
  Card,
  Statistic,
  Row,
  Col,
  Descriptions,
  Popconfirm,
  App,
  Modal,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  SearchOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  BarChartOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { campusService, CampusStatus } from '@campus/shared/services';
import type { Campus, CampusRequest, CampusStatistics } from '@campus/shared/services';

const { Option } = Select;

/**
 * 校园列表页组件
 */
export const CampusList: React.FC = () => {
  const queryClient = useQueryClient();
  const { message, modal } = App.useApp();
  const [form] = Form.useForm<CampusRequest>();

  // 查询参数
  const [keyword, setKeyword] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<CampusStatus | undefined>();
  const [page, setPage] = useState<number>(0);
  const [size] = useState<number>(20);

  // 弹窗状态
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingCampus, setEditingCampus] = useState<Campus | null>(null);
  const [statisticsVisible, setStatisticsVisible] = useState(false);
  const [selectedCampusId, setSelectedCampusId] = useState<number | null>(null);

  // 查询校园列表
  const { data, isLoading, refetch } = useQuery({
    queryKey: ['campuses', 'list', { keyword, statusFilter, page, size }],
    queryFn: () =>
      campusService.list({
        keyword,
        status: statusFilter,
        page,
        size,
      }),
    staleTime: 2 * 60 * 1000, // 缓存2分钟
  });

  // 查询校园统计
  const { data: statistics } = useQuery({
    queryKey: ['campuses', 'statistics', selectedCampusId],
    queryFn: () => campusService.statistics(selectedCampusId!),
    enabled: !!selectedCampusId && statisticsVisible,
  });

  // 添加/编辑校园
  const saveMutation = useMutation({
    mutationFn: (values: CampusRequest) =>
      editingCampus
        ? campusService.update(editingCampus.id, values)
        : campusService.create(values),
    onSuccess: () => {
      message.success(editingCampus ? '编辑成功' : '添加成功');
      setIsModalVisible(false);
      form.resetFields();
      setEditingCampus(null);
      refetch();
      queryClient.invalidateQueries({ queryKey: ['campuses'] });
    },
    onError: (error: any) => {
      message.error(error?.message || '操作失败');
    },
  });

  // 删除校园
  const deleteMutation = useMutation({
    mutationFn: (id: number) => campusService.delete(id),
    onSuccess: () => {
      message.success('删除成功');
      refetch();
      queryClient.invalidateQueries({ queryKey: ['campuses'] });
    },
    onError: (error: any) => {
      message.error(error?.message || '删除失败，请确保该校园没有关联数据');
    },
  });

  // 更新状态
  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: CampusStatus }) =>
      campusService.updateStatus(id, status),
    onSuccess: () => {
      message.success('状态更新成功');
      refetch();
      queryClient.invalidateQueries({ queryKey: ['campuses'] });
    },
    onError: () => {
      message.error('状态更新失败');
    },
  });

  // 统计数据
  const totalCount = data?.totalElements || 0;
  const enabledCount = data?.content.filter(c => c.status === CampusStatus.ENABLED).length || 0;
  const disabledCount = data?.content.filter(c => c.status === CampusStatus.DISABLED).length || 0;

  // 搜索处理
  const handleSearch = () => {
    setPage(0);
    refetch();
  };

  // 重置筛选
  const handleReset = () => {
    setKeyword('');
    setStatusFilter(undefined);
    setPage(0);
  };

  // 打开添加弹窗
  const handleAdd = () => {
    setEditingCampus(null);
    form.resetFields();
    form.setFieldsValue({ status: CampusStatus.ENABLED });
    setIsModalVisible(true);
  };

  // 打开编辑弹窗
  const handleEdit = (campus: Campus) => {
    setEditingCampus(campus);
    form.setFieldsValue({
      name: campus.name,
      code: campus.code,
      address: campus.address,
      phone: campus.phone,
      status: campus.status,
    });
    setIsModalVisible(true);
  };

  // 删除校园
  const handleDelete = (id: number) => {
    deleteMutation.mutate(id);
  };

  // 切换状态
  const handleToggleStatus = (campus: Campus) => {
    const newStatus = campus.status === CampusStatus.ENABLED 
      ? CampusStatus.DISABLED 
      : CampusStatus.ENABLED;
    
    modal.confirm({
      title: `确认${newStatus === CampusStatus.ENABLED ? '启用' : '禁用'}校园`,
      content: `${newStatus === CampusStatus.DISABLED ? '禁用后该校园的用户将无法登录，' : ''}确定要继续吗？`,
      onOk: () => {
        updateStatusMutation.mutate({ id: campus.id, status: newStatus });
      },
    });
  };

  // 查看统计
  const handleViewStatistics = (campusId: number) => {
    setSelectedCampusId(campusId);
    setStatisticsVisible(true);
  };

  // 保存表单
  const handleFormSubmit = async () => {
    try {
      const values = await form.validateFields();
      saveMutation.mutate(values);
    } catch (error) {
      console.error('表单验证失败:', error);
    }
  };

  // 表格列定义
  const columns: ColumnsType<Campus> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '校园名称',
      dataIndex: 'name',
      key: 'name',
      width: 200,
      ellipsis: true,
    },
    {
      title: '校园代码',
      dataIndex: 'code',
      key: 'code',
      width: 150,
    },
    {
      title: '地址',
      dataIndex: 'address',
      key: 'address',
      width: 250,
      ellipsis: true,
      render: (address: string) => address || '-',
    },
    {
      title: '联系电话',
      dataIndex: 'phone',
      key: 'phone',
      width: 150,
      render: (phone: string) => phone || '-',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: CampusStatus) => (
        <Tag color={status === CampusStatus.ENABLED ? 'green' : 'red'} icon={
          status === CampusStatus.ENABLED ? <CheckCircleOutlined /> : <CloseCircleOutlined />
        }>
          {status === CampusStatus.ENABLED ? '启用' : '禁用'}
        </Tag>
      ),
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
      render: (_: any, record: Campus) => (
        <Space size="small">
          <Button
            type="link"
            size="small"
            icon={<BarChartOutlined />}
            onClick={() => handleViewStatistics(record.id)}
          >
            统计
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
            onClick={() => handleToggleStatus(record)}
          >
            {record.status === CampusStatus.ENABLED ? '禁用' : '启用'}
          </Button>
          <Popconfirm
            title="确定删除此校园吗？"
            description="删除后无法恢复，请确保该校园没有关联数据。"
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
      {/* 页面标题 */}
      <h2 style={{ marginBottom: 24 }}>校园管理</h2>

      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={8}>
          <Card>
            <Statistic
              title="校园总数"
              value={totalCount}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="启用中"
              value={enabledCount}
              valueStyle={{ color: '#52c41a' }}
              prefix={<CheckCircleOutlined />}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="已禁用"
              value={disabledCount}
              valueStyle={{ color: '#ff4d4f' }}
              prefix={<CloseCircleOutlined />}
            />
          </Card>
        </Col>
      </Row>

      {/* 搜索筛选区 */}
      <Card style={{ marginBottom: 16 }}>
        <Space size="middle" wrap>
          <Input
            placeholder="搜索校园名称或代码"
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
            <Option value={CampusStatus.ENABLED}>启用</Option>
            <Option value={CampusStatus.DISABLED}>禁用</Option>
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
          添加校园
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
        scroll={{ x: 1400 }}
      />

      {/* 添加/编辑弹窗 */}
      <Modal
        title={editingCampus ? '编辑校园' : '添加校园'}
        open={isModalVisible}
        onOk={handleFormSubmit}
        onCancel={() => {
          setIsModalVisible(false);
          form.resetFields();
          setEditingCampus(null);
        }}
        width={600}
        confirmLoading={saveMutation.isPending}
      >
        <Form
          form={form}
          layout="vertical"
          initialValues={{ status: CampusStatus.ENABLED }}
        >
          <Form.Item
            label="校园名称"
            name="name"
            rules={[
              { required: true, message: '请输入校园名称' },
              { min: 2, max: 50, message: '校园名称长度为2-50个字符' },
            ]}
          >
            <Input placeholder="请输入校园名称" />
          </Form.Item>

          <Form.Item
            label="校园代码"
            name="code"
            rules={[
              { required: true, message: '请输入校园代码' },
              { min: 2, max: 20, message: '校园代码长度为2-20个字符' },
              { pattern: /^[A-Za-z0-9_-]+$/, message: '只能包含字母、数字、下划线和连字符' },
            ]}
          >
            <Input placeholder="请输入校园代码（唯一标识）" disabled={!!editingCampus} />
          </Form.Item>

          <Form.Item
            label="校园地址"
            name="address"
            rules={[
              { max: 100, message: '地址长度不能超过100个字符' },
            ]}
          >
            <Input.TextArea 
              placeholder="请输入校园地址（选填）" 
              rows={2}
              maxLength={100}
              showCount
            />
          </Form.Item>

          <Form.Item
            label="联系电话"
            name="phone"
            rules={[
              { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码' },
            ]}
          >
            <Input placeholder="请输入联系电话（选填）" />
          </Form.Item>

          <Form.Item
            label="状态"
            name="status"
            rules={[{ required: true, message: '请选择状态' }]}
          >
            <Select>
              <Option value={CampusStatus.ENABLED}>启用</Option>
              <Option value={CampusStatus.DISABLED}>禁用</Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>

      {/* 统计弹窗 */}
      <Modal
        title="校园统计数据"
        open={statisticsVisible}
        onCancel={() => {
          setStatisticsVisible(false);
          setSelectedCampusId(null);
        }}
        footer={[
          <Button key="close" onClick={() => {
            setStatisticsVisible(false);
            setSelectedCampusId(null);
          }}>
            关闭
          </Button>
        ]}
        width={600}
      >
        {statistics ? (
          <>
            <Descriptions bordered column={2}>
              <Descriptions.Item label="校园名称" span={2}>
                {statistics.campusName}
              </Descriptions.Item>
              <Descriptions.Item label="用户总数">
                {statistics.userCount}
              </Descriptions.Item>
              <Descriptions.Item label="活跃用户数">
                {statistics.activeUserCount}
              </Descriptions.Item>
              <Descriptions.Item label="商品总数">
                {statistics.goodsCount}
              </Descriptions.Item>
              <Descriptions.Item label="订单总数">
                {statistics.orderCount}
              </Descriptions.Item>
            </Descriptions>
            
            <Row gutter={16} style={{ marginTop: 24 }}>
              <Col span={8}>
                <Card>
                  <Statistic
                    title="用户总数"
                    value={statistics.userCount}
                    valueStyle={{ color: '#1890ff' }}
                  />
                </Card>
              </Col>
              <Col span={8}>
                <Card>
                  <Statistic
                    title="商品总数"
                    value={statistics.goodsCount}
                    valueStyle={{ color: '#52c41a' }}
                  />
                </Card>
              </Col>
              <Col span={8}>
                <Card>
                  <Statistic
                    title="订单总数"
                    value={statistics.orderCount}
                    valueStyle={{ color: '#faad14' }}
                  />
                </Card>
              </Col>
            </Row>
          </>
        ) : (
          <div style={{ textAlign: 'center', padding: 40 }}>加载中...</div>
        )}
      </Modal>
    </div>
  );
};

export default CampusList;
