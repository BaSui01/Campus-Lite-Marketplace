/**
 * 社区广场管理页面
 * 
 * 功能：
 * - 用户动态流监控
 * - 热门话题展示
 * - 帖子互动统计
 * - 话题帖子管理
 * 
 * @author BaSui 😎
 * @date 2025-11-06
 */

import { useState } from 'react';
import {
  Card,
  Table,
  Space,
  Tag,
  Button,
  Row,
  Col,
  Statistic,
  Select,
  Tabs,
  message,
  Modal,
} from 'antd';
import {
  FireOutlined,
  LikeOutlined,
  StarOutlined,
  CommentOutlined,
  EyeOutlined,
  ReloadOutlined,
  TeamOutlined,
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { communityService } from '@campus/shared/services/community';
import { topicService } from '@campus/shared/services/topic';
import type { UserFeed } from '@campus/shared/services/community';
import type { Topic } from '@campus/shared/services/topic';
import dayjs from 'dayjs';

const { TabPane } = Tabs;
const { Option } = Select;

/**
 * 动作类型映射
 */
const ACTION_TYPE_MAP = {
  POST: { text: '发布帖子', color: 'blue', icon: <CommentOutlined /> },
  LIKE: { text: '点赞', color: 'red', icon: <LikeOutlined /> },
  COLLECT: { text: '收藏', color: 'orange', icon: <StarOutlined /> },
  COMMENT: { text: '评论', color: 'green', icon: <CommentOutlined /> },
};

export const CommunityList: React.FC = () => {
  const queryClient = useQueryClient();

  // 选中的话题
  const [selectedTopicId, setSelectedTopicId] = useState<number | null>(null);

  // 查询用户动态流
  const { data: feeds, isLoading: feedsLoading, refetch: refetchFeeds } = useQuery({
    queryKey: ['community', 'feeds'],
    queryFn: () => communityService.getUserFeed(),
    refetchInterval: 60000, // 60秒自动刷新
  });

  // 查询热门话题
  const { data: hotTopics, isLoading: hotTopicsLoading } = useQuery({
    queryKey: ['community', 'hot-topics'],
    queryFn: () => communityService.getHotTopics(),
  });

  // 查询所有话题
  const { data: allTopics } = useQuery({
    queryKey: ['topics', 'all'],
    queryFn: () => topicService.getAll(),
  });

  // 查询话题下的帖子
  const { data: topicPosts, isLoading: topicPostsLoading } = useQuery({
    queryKey: ['community', 'topic-posts', selectedTopicId],
    queryFn: () => (selectedTopicId ? communityService.getPostsByTopic(selectedTopicId) : Promise.resolve([])),
    enabled: !!selectedTopicId,
  });

  // ==================== 表格列定义 ====================

  /**
   * 动态流表格列
   */
  const feedColumns = [
    {
      title: '用户',
      dataIndex: 'userName',
      key: 'userName',
      width: 150,
      render: (name: string, record: UserFeed) => (
        <div style={{ display: 'flex', alignItems: 'center' }}>
          {record.userAvatar && (
            <img
              src={record.userAvatar}
              alt={name}
              style={{ width: 32, height: 32, borderRadius: '50%', marginRight: 8 }}
            />
          )}
          <span>{name}</span>
        </div>
      ),
    },
    {
      title: '动作类型',
      dataIndex: 'actionType',
      key: 'actionType',
      width: 120,
      render: (type: keyof typeof ACTION_TYPE_MAP) => {
        const config = ACTION_TYPE_MAP[type];
        return (
          <Tag color={config.color} icon={config.icon}>
            {config.text}
          </Tag>
        );
      },
    },
    {
      title: '目标类型',
      dataIndex: 'targetType',
      key: 'targetType',
      width: 100,
      render: (type: string) => (
        <Tag color={type === 'POST' ? 'blue' : 'green'}>{type === 'POST' ? '帖子' : '商品'}</Tag>
      ),
    },
    {
      title: '目标ID',
      dataIndex: 'targetId',
      key: 'targetId',
      width: 100,
    },
    {
      title: '内容',
      dataIndex: 'content',
      key: 'content',
      width: 300,
      ellipsis: true,
      render: (content: string) => content || '-',
    },
    {
      title: '时间',
      dataIndex: 'createdAt',
      key: 'createdAt',
      width: 180,
      render: (date: string) => dayjs(date).format('YYYY-MM-DD HH:mm:ss'),
    },
  ];

  /**
   * 话题帖子表格列
   */
  const topicPostColumns = [
    {
      title: '帖子ID',
      dataIndex: 'postId',
      key: 'postId',
      width: 100,
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_: unknown, postId: number) => (
        <Space>
          <Button size="small" type="link">
            查看详情
          </Button>
        </Space>
      ),
    },
  ];

  // ==================== 统计数据 ====================

  const totalFeeds = feeds?.length || 0;
  const postCount = feeds?.filter((f) => f.actionType === 'POST').length || 0;
  const likeCount = feeds?.filter((f) => f.actionType === 'LIKE').length || 0;
  const collectCount = feeds?.filter((f) => f.actionType === 'COLLECT').length || 0;

  // ==================== 渲染 ====================

  return (
    <div style={{ padding: 24 }}>
      {/* 页面头部 */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <h2>
          <TeamOutlined style={{ marginRight: 8 }} />
          社区广场管理
        </h2>
        <Space>
          <Button icon={<ReloadOutlined />} onClick={() => refetchFeeds()}>
            刷新
          </Button>
        </Space>
      </div>

      {/* 统计卡片 */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card>
            <Statistic
              title="动态总数"
              value={totalFeeds}
              prefix={<TeamOutlined />}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="发布帖子"
              value={postCount}
              prefix={<CommentOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="点赞数"
              value={likeCount}
              prefix={<LikeOutlined />}
              valueStyle={{ color: '#f5222d' }}
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card>
            <Statistic
              title="收藏数"
              value={collectCount}
              prefix={<StarOutlined />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 热门话题卡片 */}
      {hotTopics && hotTopics.length > 0 && (
        <Card title="🔥 热门话题" style={{ marginBottom: 24 }}>
          <Space wrap>
            {hotTopics.map((topic: Topic) => (
              <Tag
                key={topic.id}
                color="red"
                icon={<FireOutlined />}
                style={{ fontSize: 14, padding: '4px 12px', cursor: 'pointer' }}
                onClick={() => setSelectedTopicId(topic.id)}
              >
                {topic.name} ({topic.followerCount || 0})
              </Tag>
            ))}
          </Space>
        </Card>
      )}

      {/* 主内容区域 - Tabs */}
      <Card>
        <Tabs defaultActiveKey="feeds">
          {/* 用户动态流 */}
          <TabPane tab="用户动态流" key="feeds">
            <Table
              dataSource={feeds || []}
              columns={feedColumns}
              rowKey="id"
              loading={feedsLoading}
              pagination={{
                pageSize: 20,
                showTotal: (total) => `共 ${total} 条动态`,
              }}
            />
          </TabPane>

          {/* 话题帖子管理 */}
          <TabPane tab="话题帖子管理" key="topic-posts">
            <div style={{ marginBottom: 16 }}>
              <Space>
                <span>选择话题：</span>
                <Select
                  placeholder="请选择话题"
                  style={{ width: 300 }}
                  value={selectedTopicId}
                  onChange={setSelectedTopicId}
                  allowClear
                >
                  {allTopics?.map((topic) => (
                    <Option key={topic.id} value={topic.id}>
                      {topic.name} ({topic.postCount || 0}个帖子)
                    </Option>
                  ))}
                </Select>
              </Space>
            </div>

            {selectedTopicId ? (
              <div>
                <p style={{ marginBottom: 16, color: '#8c8c8c' }}>
                  共 {topicPosts?.length || 0} 个帖子
                </p>
                <Table
                  dataSource={topicPosts?.map((postId) => ({ postId })) || []}
                  columns={topicPostColumns}
                  rowKey="postId"
                  loading={topicPostsLoading}
                  pagination={{
                    pageSize: 20,
                    showTotal: (total) => `共 ${total} 个帖子`,
                  }}
                />
              </div>
            ) : (
              <div style={{ textAlign: 'center', padding: 100, color: '#8c8c8c' }}>
                请选择一个话题查看相关帖子
              </div>
            )}
          </TabPane>
        </Tabs>
      </Card>
    </div>
  );
};
