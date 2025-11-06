/**
 * 标签管理页
 * 
 * 功能：
 * - 分页查询标签列表
 * - 关键词搜索
 * - 类型和状态筛选
 * - 添加/编辑/删除标签
 * - 标签合并
 * - 热度排行榜（TOP 20）
 * - 启用/禁用标签
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
  Tag as AntTag,
  message,
  Modal,
  Form,
  Card,
  Statistic,
  Row,
  Col,
  Popconfirm,
  List,
  Badge,
  ColorPicker,
  type Color,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import {
  SearchOutlined,
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  MergeCellsOutlined,
  FireOutlined,
  TagOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { tagService, TagType, TagStatus } from '@campus/shared/services';
import type { Tag, TagRequest, HotTag } from '@campus/shared/services';

const { Option } = Select;
const { TextArea } = Input;

/**
 * 标签类型映射
 */
const TAG_TYPE_MAP: Record<TagType, { text: string; color: string }> = {
  [TagType.GOODS]: { text: '商品', color: 'blue' },
  [TagType.POST]: { text: '帖子', color: 'green' },
  [TagType.COMMON]: { text: '通用', color: 'default' },
};

/**
 * 标签列表页组件
 */
export const TagList: React.FC = () => {
  const queryClient = useQueryClient();
  const [form] = Form.useForm<TagRequest>();

  // 查询参数
  const [keyword, setKeyword] = useState<string>('');
  const [typeFilter, setTypeFilter] = useState<TagType | undefined>();
  const [statusFilter, setStatusFilter] = useState<TagStatus | undefined>();
  const [page, setPage] = useState<number>(0);
  const [size] = useState<number>(20);

  // 弹窗状态
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingTag, setEditingTag] = useState<Tag | null>(null);
  const [isMergeModalVisible, setIsMergeModalVisible] = useState(false);
  
  // 批量操作
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);
  const [targetTagId, setTargetTagId] = useState<number | undefined>();

  // 查询标签列表
  const { data, isLoading, refetch } = useQuery({
    queryKey: ['tags', 'list', { keyword, typeFilter, statusFilter, page, size }],
    queryFn: () =>
      tagService.list({
        keyword,
        type: typeFilter,
        status: statusFilter,
        page,
        size,
      }),
    staleTime: 2 * 60 * 1000,
  });

  // 查询热门标签
  const { data: hotTags } = useQuery({
    queryKey: ['tags', 'hot'],
    queryFn: () => tagService.getHotTags(20),
    staleTime: 5 * 60 * 1000,
  });

  // 添加/编辑标签
  const saveMutation = useMutation({
    mutationFn: (values: TagRequest) =>
      editingTag
        ? tagService.update(editingTag.id, values)
        : tagService.create(values),
    onSuccess: () => {
      message.success(editingTag ? '编辑成功' : '添加成功');
      setIsModalVisible(false);
      form.resetFields();
      setEditingTag(null);
      refetch();
      queryClient.invalidateQueries({ queryKey: ['tags'] });
    },
    onError: (error: any) => {
      message.error(error?.message || '操作失败');
    },
  });

  // 删除标签
  const deleteMutation = useMutation({
    mutationFn: (id: number) => tagService.delete(id),
    onSuccess: () => {
      message.success('删除成功');
      refetch();
      queryClient.invalidateQueries({ queryKey: ['tags'] });
    },
    onError: (error: any) => {
      message.error(error?.message || '删除失败，请确保该标签没有关联内容');
    },
  });

  // 更新状态
  const updateStatusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: TagStatus }) =>
      tagService.updateStatus(id, status),
    onSuccess: () => {
      message.success('状态更新成功');
      refetch();
      queryClient.invalidateQueries({ queryKey: ['tags'] });
    },
    onError: () => {
      message.error('状态更新失败');
    },
  });

  // 合并标签
  const mergeMutation = useMutation({
    mutationFn: (data: { sourceIds: number[]; targetId: number }) =>
      tagService.merge(data),
    onSuccess: () => {
      message.success('标签合并成功');
      setIsMergeModalVisible(false);
      setSelectedRowKeys([]);
      setTargetTagId(undefined);
      refetch();
      queryClient.invalidateQueries({ queryKey: ['tags'] });
    },
    onError: (error: any) => {
      message.error(error?.message || '合并失败');
    },
  });

  // 统计数据
  const totalCount = data?.totalElements || 0;
  const enabledCount = data?.content.filter(t => t.status === TagStatus.ENABLED).length || 0;
  const hotTagsCount = data?.content.filter(t => t.hotCount > 100).length || 0;

  // 搜索处理
  const handleSearch = () => {
    setPage(0);
    refetch();
  };

  // 重置筛选
  const handleReset = () => {
    setKeyword('');
    setTypeFilter(undefined);
    setStatusFilter(undefined);
    setPage(0);
  };

  // 打开添加弹窗
  const handleAdd = () => {
    setEditingTag(null);
    form.resetFields();
    form.setFieldsValue({ 
      status: TagStatus.ENABLED,
      type: TagType.COMMON
    });
    setIsModalVisible(true);
  };

  // 打开编辑弹窗
  const handleEdit = (tag: Tag) => {
    setEditingTag(tag);
    form.setFieldsValue({
      name: tag.name,
      type: tag.type,
      color: tag.color,
      description: tag.description,
      status: tag.status,
    });
    setIsModalVisible(true);
  };

  // 删除标签
  const handleDelete = (id: number) => {
    deleteMutation.mutate(id);
  };

  // 切换状态
  const handleToggleStatus = (tag: Tag) => {
    const newStatus = tag.status === TagStatus.ENABLED 
      ? TagStatus.DISABLED 
      : TagStatus.ENABLED;
    
    updateStatusMutation.mutate({ id: tag.id, status: newStatus });
  };

  // 打开合并弹窗
  const handleOpenMerge = () => {
    if (selectedRowKeys.length < 2) {
      message.warning('请至少选择2个标签进行合并');
      return;
    }
    setIsMergeModalVisible(true);
  };

  // 执行合并
  const handleMerge = () => {
    if (!targetTagId) {
      message.warning('请选择目标标签');
      return;
    }
    if (selectedRowKeys.includes(targetTagId)) {
      message.warning('目标标签不能在源标签列表中');
      return;
    }
    
    mergeMutation.mutate({
      sourceIds: selectedRowKeys as number[],
      targetId: targetTagId
    });
  };

  // 保存表单
  const handleFormSubmit = async () => {
    try {
      const values = await form.validateFields();
      
      // 处理颜色值
      if (values.color && typeof values.color === 'object') {
        values.color = (values.color as Color).toHexString();
      }
      
      saveMutation.mutate(values);
    } catch (error) {
      console.error('表单验证失败:', error);
    }
  };

  // 表格列定义
  const columns: ColumnsType<Tag> = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '标签名',
      dataIndex: 'name',
      key: 'name',
      width: 150,
      render: (name: string, record: Tag) => (
        <AntTag 
          color={record.color || 'default'} 
          icon={<TagOutlined />}
          style={{ fontSize: 14, padding: '4px 12px' }}
        >
          {name}
        </AntTag>
      ),
    },
    {
      title: '类型',
      dataIndex: 'type',
      key: 'type',
      width: 100,
      render: (type: TagType) => {
        const typeInfo = TAG_TYPE_MAP[type];
        return <AntTag color={typeInfo.color}>{typeInfo.text}</AntTag>;
      },
    },
    {
      title: '热度',
      dataIndex: 'hotCount',
      key: 'hotCount',
      width: 120,
      sorter: (a, b) => a.hotCount - b.hotCount,
      render: (count: number) => (
        <Space>
          <FireOutlined style={{ color: count > 100 ? '#ff4d4f' : '#8c8c8c' }} />
          <span style={{ fontWeight: count > 100 ? 'bold' : 'normal' }}>
            {count}
          </span>
        </Space>
      ),
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: TagStatus) => (
        <AntTag 
          color={status === TagStatus.ENABLED ? 'green' : 'red'} 
          icon={status === TagStatus.ENABLED ? <CheckCircleOutlined /> : <CloseCircleOutlined />}
        >
          {status === TagStatus.ENABLED ? '启用' : '禁用'}
        </AntTag>
      ),
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
      width: 200,
      ellipsis: true,
      render: (description: string) => description || '-',
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
      width: 200,
      render: (_: any, record: Tag) => (
        <Space size="small">
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
            {record.status === TagStatus.ENABLED ? '禁用' : '启用'}
          </Button>
          <Popconfirm
            title="确定删除此标签吗？"
            description="删除后无法恢复，请确保该标签没有关联内容。"
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
      <Row gutter={16}>
        {/* 左侧：主内容 */}
        <Col span={18}>
          <h2 style={{ marginBottom: 24 }}>标签管理</h2>

          {/* 统计卡片 */}
          <Row gutter={16} style={{ marginBottom: 24 }}>
            <Col span={8}>
              <Card>
                <Statistic
                  title="标签总数"
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
                  title="热门标签"
                  value={hotTagsCount}
                  valueStyle={{ color: '#ff4d4f' }}
                  prefix={<FireOutlined />}
                  suffix="个(热度>100)"
                />
              </Card>
            </Col>
          </Row>

          {/* 搜索筛选区 */}
          <Card style={{ marginBottom: 16 }}>
            <Space size="middle" wrap>
              <Input
                placeholder="搜索标签名称"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                onPressEnter={handleSearch}
                style={{ width: 200 }}
                prefix={<SearchOutlined />}
              />
              <Select
                placeholder="类型筛选"
                value={typeFilter}
                onChange={setTypeFilter}
                allowClear
                style={{ width: 120 }}
              >
                <Option value={TagType.GOODS}>商品</Option>
                <Option value={TagType.POST}>帖子</Option>
                <Option value={TagType.COMMON}>通用</Option>
              </Select>
              <Select
                placeholder="状态筛选"
                value={statusFilter}
                onChange={setStatusFilter}
                allowClear
                style={{ width: 120 }}
              >
                <Option value={TagStatus.ENABLED}>启用</Option>
                <Option value={TagStatus.DISABLED}>禁用</Option>
              </Select>
              <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
                查询
              </Button>
              <Button onClick={handleReset}>重置</Button>
            </Space>
          </Card>

          {/* 操作按钮 */}
          <div style={{ marginBottom: 16 }}>
            <Space>
              <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
                添加标签
              </Button>
              <Button 
                icon={<MergeCellsOutlined />} 
                onClick={handleOpenMerge}
                disabled={selectedRowKeys.length < 2}
              >
                合并标签 ({selectedRowKeys.length})
              </Button>
            </Space>
          </div>

          {/* 数据表格 */}
          <Table
            columns={columns}
            dataSource={data?.content || []}
            rowKey="id"
            loading={isLoading}
            rowSelection={{
              selectedRowKeys,
              onChange: setSelectedRowKeys,
            }}
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
        </Col>

        {/* 右侧：热度排行榜 */}
        <Col span={6}>
          <Card 
            title={
              <Space>
                <FireOutlined style={{ color: '#ff4d4f' }} />
                <span>热度排行榜 TOP 20</span>
              </Space>
            }
            style={{ position: 'sticky', top: 24 }}
          >
            <List
              dataSource={hotTags || []}
              renderItem={(item: HotTag, index) => (
                <List.Item>
                  <Space style={{ width: '100%', justifyContent: 'space-between' }}>
                    <Space>
                      <Badge 
                        count={index + 1} 
                        style={{ 
                          backgroundColor: index < 3 ? '#ff4d4f' : '#8c8c8c' 
                        }} 
                      />
                      <AntTag icon={<TagOutlined />}>
                        {item.name}
                      </AntTag>
                    </Space>
                    <Space>
                      <AntTag color={TAG_TYPE_MAP[item.type].color}>
                        {TAG_TYPE_MAP[item.type].text}
                      </AntTag>
                      <span style={{ color: '#ff4d4f', fontWeight: 'bold' }}>
                        {item.hotCount}
                      </span>
                    </Space>
                  </Space>
                </List.Item>
              )}
              size="small"
            />
          </Card>
        </Col>
      </Row>

      {/* 添加/编辑弹窗 */}
      <Modal
        title={editingTag ? '编辑标签' : '添加标签'}
        open={isModalVisible}
        onOk={handleFormSubmit}
        onCancel={() => {
          setIsModalVisible(false);
          form.resetFields();
          setEditingTag(null);
        }}
        width={600}
        confirmLoading={saveMutation.isPending}
      >
        <Form
          form={form}
          layout="vertical"
          initialValues={{ status: TagStatus.ENABLED, type: TagType.COMMON }}
        >
          <Form.Item
            label="标签名称"
            name="name"
            rules={[
              { required: true, message: '请输入标签名称' },
              { min: 2, max: 20, message: '标签名称长度为2-20个字符' },
            ]}
          >
            <Input placeholder="请输入标签名称" />
          </Form.Item>

          <Form.Item
            label="标签类型"
            name="type"
            rules={[{ required: true, message: '请选择标签类型' }]}
          >
            <Select>
              <Option value={TagType.GOODS}>商品标签</Option>
              <Option value={TagType.POST}>帖子标签</Option>
              <Option value={TagType.COMMON}>通用标签</Option>
            </Select>
          </Form.Item>

          <Form.Item
            label="标签颜色"
            name="color"
            tooltip="用于前端展示"
          >
            <ColorPicker showText />
          </Form.Item>

          <Form.Item
            label="标签描述"
            name="description"
            rules={[
              { max: 100, message: '描述长度不能超过100个字符' },
            ]}
          >
            <TextArea 
              placeholder="请输入标签描述（选填）" 
              rows={3}
              maxLength={100}
              showCount
            />
          </Form.Item>

          <Form.Item
            label="状态"
            name="status"
            rules={[{ required: true, message: '请选择状态' }]}
          >
            <Select>
              <Option value={TagStatus.ENABLED}>启用</Option>
              <Option value={TagStatus.DISABLED}>禁用</Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>

      {/* 标签合并弹窗 */}
      <Modal
        title="合并标签"
        open={isMergeModalVisible}
        onOk={handleMerge}
        onCancel={() => {
          setIsMergeModalVisible(false);
          setTargetTagId(undefined);
        }}
        confirmLoading={mergeMutation.isPending}
      >
        <div>
          <p>将选中的 <strong>{selectedRowKeys.length}</strong> 个标签合并到目标标签</p>
          <p style={{ color: '#ff4d4f', marginBottom: 16 }}>
            ⚠️ 注意：合并后源标签将被删除，所有关联数据将迁移到目标标签
          </p>
          <Form.Item label="目标标签" required>
            <Select
              placeholder="请选择目标标签"
              value={targetTagId}
              onChange={setTargetTagId}
              style={{ width: '100%' }}
              showSearch
              filterOption={(input, option) =>
                (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
              }
              options={data?.content
                .filter(tag => !selectedRowKeys.includes(tag.id))
                .map(tag => ({
                  value: tag.id,
                  label: `${tag.name} (${TAG_TYPE_MAP[tag.type].text})`,
                }))}
            />
          </Form.Item>
        </div>
      </Modal>
    </div>
  );
};

export default TagList;
