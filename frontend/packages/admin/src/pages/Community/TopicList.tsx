/**
 * 话题管理页面
 * 
 * 功能：
 * - 话题列表展示
 * - 添加/编辑/删除话题
 * - 热度排行榜
 * - 话题统计查看
 * 
 * @author BaSui 😎
 * @date 2025-11-06
 */

import { useState } from 'react';
import {
  Table,
  Button,
  Input,
  Space,
  Tag,
  Form,
  Card,
  Row,
  Col,
  Statistic,
  Tooltip,
  App,
  Modal,
} from 'antd';
import {
  PlusOutlined,
  SearchOutlined,
  FireOutlined,
  TeamOutlined,
  EyeOutlined,
  EditOutlined,
  DeleteOutlined,
  ReloadOutlined,
  FileTextOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { topicService } from '@campus/shared/services/topic';
import type { Topic } from '@campus/shared/services/topic';
import dayjs from 'dayjs';

const { TextArea } = Input;

export const TopicList: React.FC = () => {
  const { message, modal } = App.useApp();
  const queryClient = useQueryClient();

  // 搜索关键词
  const [keyword, setKeyword] = useState<string>('');

  // 添加/编辑弹窗
  const [modalVisible, setModalVisible] = useState(false);
  const [editingTopic, setEditingTopic] = useState<Topic | null>(null);
  const [form] = Form.useForm();

  // 统计弹窗
  const [statsModalVisible, setStatsModalVisible] = useState(false);
  const [selectedTopic, setSelectedTopic] = useState<Topic | null>(null);

  // 查询话题列表
  const { data: topics, isLoading, refetch } = useQuery({
    queryKey: ['topics', 'list'],
    queryFn: () => topicService.getAll(),
  });

  // 查询热门话题
  const { data: hotTopics } = useQuery({
    queryKey: ['topics', 'hot'],
    queryFn: () => topicService.getHotTopics(),
  });

  // 创建话题
  const createMutation = useMutation({
    mutationFn: (values: { name: string; description?: string }) =>
      topicService.create(values),
    onSuccess: () => {
      message.success('话题创建成功');
      setModalVisible(false);
      form.resetFields();
      refetch();
    },
    onError: () => {
      message.error('话题创建失败');
    },
  });

  // 更新话题
  const updateMutation = useMutation({
    mutationFn: ({ id, values }: { id: number; values: { name?: string; description?: string } }) =>
      topicService.update(id, values),
    onSuccess: () => {
      message.success('话题更新成功');
      setModalVisible(false);
      form.resetFields();
      setEditingTopic(null);
      refetch();
    },
    onError: () => {
      message.error('话题更新失败');
    },
  });

  // 删除话题
  const deleteMutation = useMutation({
    mutationFn: (topicId: number) => topicService.delete(topicId),
    onSuccess: () => {
      message.success('话题删除成功');
      refetch();
    },
    onError: () => {
      message.error('话题删除失败');
    },
  });

  // ==================== 事件处理 ====================

  /**
   * 打开添加话题弹窗
   */
  const handleAdd = () => {
    setEditingTopic(null);
    form.resetFields();
    setModalVisible(true);
  };

  /**
   * 打开编辑话题弹窗
   */
  const handleEdit = (topic: Topic) => {
    setEditingTopic(topic);
    form.setFieldsValue({
      name: topic.name,
      description: topic.description,
    });
    setModalVisible(true);
  };

  /**
   * 确认添加/编辑
   */
  const handleModalOk = async () => {
    try {
      const values = await form.validateFields();
      if (editingTopic) {
        updateMutation.mutate({ id: editingTopic.id, values });
      } else {
        createMutation.mutate(values);
      }
    } catch (error) {
      // 表单验证失败
    }
  };

  /**
   * 删除话题
   */
  const handleDelete = (topicId: number) => {
    modal.confirm({
      title: '确认删除',
      content: '删除话题后无法恢复，确定要删除吗？',
      onOk: () => deleteMutation.mutate(topicId),
    });
  };

  /**
   * 打开统计弹窗
   */
  const handleViewStats = async (topic: Topic) => {
    setSelectedTopic(topic);
    setStatsModalVisible(true);
  };

  // ==================== 表格列定义 ====================

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
    },
    {
      title: '话题名称',
      dataIndex: 'name',
      key: 'name',
      width: 200,
      filteredValue: keyword ? [keyword] : null,
      onFilter: (value: string | number | boolean, record: Topic) =>
        record.name.toLowerCase().includes(String(value).toLowerCase()),
      render: (name: string, record: Topic) => (
        <div>
          <div style={{ fontWeight: 500 }}>{name}</div>
          {record.description && (
            <div style={{ fontSize: 12, color: '#8c8c8c', marginTop: 4 }}>
              {record.description}
            </div>
          )}
        </div>
      ),
    },
    {
      title: '帖子数',
      dataIndex: 'postCount',
      key: 'postCount',
      width: 100,
      sorter: (a: Topic, b: Topic) => (a.postCount || 0) - (b.postCount || 0),
      render: (count: number) => (
        <span>
          <FileTextOutlined style={{ marginRight: 4 }} />
          {count || 0}
        </span>
      ),
    },
    {
      title: '关注人数',
      dataIndex: 'followerCount',
      key: 'followerCount',
      width: 120,
      sorter: (a: Topic, b: Topic) => (a.followerCount || 0) - (b.followerCount || 0),
      render: (count: number) => (
        <span>
          <TeamOutlined style={{ marginRight: 4 }} />
          {count || 0}
        </span>
      ),
    },
    {
      title: '浏览量',
      dataIndex: 'viewCount',
      key: 'viewCount',
      width: 120,
      sorter: (a: Topic, b: Topic) => (a.viewCount || 0) - (b.viewCount || 0),
      render: (count: number) => (
        <span>
          <EyeOutlined style={{ marginRight: 4 }} />
          {count || 0}
        </span>
      ),
    },
    {
      title: '是否热门',
      dataIndex: 'isHot',
      key: 'isHot',
      width: 100,
      render: (isHot: boolean) =>
        isHot ? (
          <Tag color="red" icon={<FireOutlined />}>
            热门
          </Tag>
        ) : (
          <Tag>普通</Tag>
        ),
    },
    {
      title: '创建时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (date: string) => dayjs(date).format('YYYY-MM-DD HH:mm'),
    },
    {
      title: '操作',
      key: 'action',
      width: 250,
      fixed: 'right' as const,
      render: (_: unknown, record: Topic) => (
        <Space>
          <Button size="small" icon={<EyeOutlined />} onClick={() => handleViewStats(record)}>
            统计
          </Button>
          <Button size="small" icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            编辑
          </Button>
          <Button
            size="small"
            danger
            icon={<DeleteOutlined />}
            onClick={() => handleDelete(record.id)}
          >
            删除
          </Button>
        </Space>
      ),
    },
  ];

  // ==================== 统计数据 ====================

  const totalTopics = topics?.length || 0;
  const totalPosts = topics?.reduce((sum, t) => sum + (t.postCount || 0), 0) || 0;
  const totalFollowers = topics?.reduce((sum, t) => sum + (t.followerCount || 0), 0) || 0;
  const hotTopicCount = topics?.filter((t) => t.isHot).length || 0;

  // ==================== 渲染 ====================

  return (
    <div style={{ padding: 24 }}>
      {/* 页面头部 */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <h2>
          <FireOutlined style={{ marginRight: 8 }} />
          话题管理
        </h2>
        <Space>
          <Input
            placeholder="搜索话题名称"
            prefix={<SearchOutlined />}
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            style={{ width: 200 }}
          />
          <Button icon={<ReloadOutlined />} onClick={() => refetch()}>
            刷新
          </Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            添加话题
          </Button>
        </Space>
      </div>

      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="话题总数"
              value={totalTopics}
              prefix={<FireOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="热门话题"
              value={hotTopicCount}
              valueStyle={{ color: '#cf1322' }}
              prefix={<FireOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="总帖子数"
              value={totalPosts}
              prefix={<FileTextOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="总关注数"
              value={totalFollowers}
              prefix={<TeamOutlined />}
            />
          </Card>
        </Col>
      </Row>

      {/* 热门话题排行榜 */}
      {hotTopics && hotTopics.length > 0 && (
        <Card title="🔥 热门话题 TOP 10" style={{ marginBottom: 24 }}>
          <Space wrap>
            {hotTopics.slice(0, 10).map((topic) => (
              <Tag
                key={topic.id}
                color="red"
                icon={<FireOutlined />}
                style={{ fontSize: 14, padding: '4px 12px' }}
              >
                {topic.name} ({topic.followerCount || 0})
              </Tag>
            ))}
          </Space>
        </Card>
      )}

      {/* 话题列表 */}
      <Card>
        <Table
          dataSource={topics || []}
          columns={columns}
          rowKey="id"
          loading={isLoading}
          pagination={{
            pageSize: 20,
            showTotal: (total) => `共 ${total} 个话题`,
          }}
        />
      </Card>

      {/* 添加/编辑话题弹窗 */}
      <Modal
        title={editingTopic ? '编辑话题' : '添加话题'}
        open={modalVisible}
        onOk={handleModalOk}
        onCancel={() => {
          setModalVisible(false);
          form.resetFields();
          setEditingTopic(null);
        }}
        confirmLoading={createMutation.isPending || updateMutation.isPending}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            label="话题名称"
            name="name"
            rules={[
              { required: true, message: '请输入话题名称' },
              { max: 50, message: '话题名称不能超过50个字符' },
            ]}
          >
            <Input placeholder="请输入话题名称" />
          </Form.Item>
          <Form.Item
            label="话题描述"
            name="description"
            rules={[{ max: 200, message: '话题描述不能超过200个字符' }]}
          >
            <TextArea rows={4} placeholder="请输入话题描述（可选）" />
          </Form.Item>
        </Form>
      </Modal>

      {/* 话题统计弹窗 */}
      <Modal
        title="话题统计"
        open={statsModalVisible}
        onCancel={() => setStatsModalVisible(false)}
        footer={[
          <Button key="close" onClick={() => setStatsModalVisible(false)}>
            关闭
          </Button>,
        ]}
        width={600}
      >
        {selectedTopic && (
          <div>
            <h3>{selectedTopic.name}</h3>
            {selectedTopic.description && <p style={{ color: '#8c8c8c' }}>{selectedTopic.description}</p>}
            <Row gutter={16} style={{ marginTop: 24 }}>
              <Col span={8}>
                <Statistic
                  title="帖子数"
                  value={selectedTopic.postCount || 0}
                  prefix={<FileTextOutlined />}
                />
              </Col>
              <Col span={8}>
                <Statistic
                  title="关注人数"
                  value={selectedTopic.followerCount || 0}
                  prefix={<TeamOutlined />}
                />
              </Col>
              <Col span={8}>
                <Statistic
                  title="浏览量"
                  value={selectedTopic.viewCount || 0}
                  prefix={<EyeOutlined />}
                />
              </Col>
            </Row>
          </div>
        )}
      </Modal>
    </div>
  );
};

// ✅ 添加默认导出
export default TopicList;
