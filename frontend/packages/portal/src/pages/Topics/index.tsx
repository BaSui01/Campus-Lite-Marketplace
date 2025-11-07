/**
 * 话题列表页面 - 发现热门话题！🔥
 * @author BaSui 😎
 * @description 话题浏览、关注、搜索
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Skeleton, Tabs, Input, Modal } from '@campus/shared/components';
import { topicService, type Topic } from '@campus/shared/services';
import { useAuthStore, useNotificationStore } from '../../store';
import './Topics.css';

/**
 * 话题列表页面组件
 */
const Topics: React.FC = () => {
  const navigate = useNavigate();
  const toast = useNotificationStore();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  // ==================== 状态管理 ====================

  const [activeTab, setActiveTab] = useState<'all' | 'hot' | 'followed'>('all');
  const [topics, setTopics] = useState<Topic[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchKeyword, setSearchKeyword] = useState('');

  // 创建话题弹窗
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [createForm, setCreateForm] = useState({
    name: '',
    description: '',
  });
  const [creating, setCreating] = useState(false);

  // 关注状态管理
  const [followedTopics, setFollowedTopics] = useState<Set<number>>(new Set());

  // ==================== 数据加载 ====================

  /**
   * 加载话题列表
   */
  const loadTopics = async () => {
    setLoading(true);

    try {
      let data: Topic[];

      if (activeTab === 'all') {
        // ✅ 获取所有话题
        data = await topicService.getAll();
      } else if (activeTab === 'hot') {
        // ✅ 获取热门话题
        data = await topicService.getHotTopics();
      } else {
        // ✅ 获取我关注的话题
        if (!isAuthenticated) {
          setTopics([]);
          setLoading(false);
          return;
        }
        data = await topicService.getMyFollowedTopics();
      }

      setTopics(data);
    } catch (err: any) {
      console.error('加载话题列表失败:', err);
      toast.error(err.response?.data?.message || '加载话题列表失败！😭');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 加载关注状态
   */
  const loadFollowedStatus = async () => {
    if (!isAuthenticated) return;

    try {
      const followed = await topicService.getMyFollowedTopics();
      const followedIds = new Set(followed.map((t) => t.id));
      setFollowedTopics(followedIds);
    } catch (err) {
      console.error('加载关注状态失败:', err);
    }
  };

  useEffect(() => {
    loadTopics();
  }, [activeTab]);

  useEffect(() => {
    if (isAuthenticated) {
      loadFollowedStatus();
    }
  }, [isAuthenticated]);

  // ==================== 事件处理 ====================

  /**
   * 创建话题
   */
  const handleCreateTopic = async () => {
    if (!isAuthenticated) {
      toast.warning('请先登录！😰');
      navigate('/login');
      return;
    }

    if (!createForm.name.trim()) {
      toast.warning('请输入话题名称！😰');
      return;
    }

    setCreating(true);

    try {
      // ✅ 调用真实 API 创建话题
      const topicId = await topicService.create({
        name: createForm.name.trim(),
        description: createForm.description.trim() || undefined,
      });

      toast.success('话题创建成功！🎉');
      setShowCreateModal(false);
      setCreateForm({ name: '', description: '' });

      // 重新加载话题列表
      loadTopics();

      // 跳转到话题详情页
      navigate(`/topics/${topicId}`);
    } catch (err: any) {
      console.error('创建话题失败:', err);
      toast.error(err.response?.data?.message || '创建话题失败！😭');
    } finally {
      setCreating(false);
    }
  };

  /**
   * 关注/取消关注话题
   */
  const handleToggleFollow = async (topicId: number, isFollowed: boolean) => {
    if (!isAuthenticated) {
      toast.warning('请先登录！😰');
      navigate('/login');
      return;
    }

    try {
      // 乐观更新 UI
      const newFollowedTopics = new Set(followedTopics);
      if (isFollowed) {
        newFollowedTopics.delete(topicId);
      } else {
        newFollowedTopics.add(topicId);
      }
      setFollowedTopics(newFollowedTopics);

      // ✅ 调用真实 API
      if (isFollowed) {
        await topicService.unfollow(topicId);
        toast.success('取消关注成功！👋');
      } else {
        await topicService.follow(topicId);
        toast.success('关注成功！🎉');
      }

      // 如果在"关注"标签，重新加载列表
      if (activeTab === 'followed') {
        loadTopics();
      }
    } catch (err: any) {
      console.error('关注操作失败:', err);
      toast.error(err.response?.data?.message || '操作失败！😭');

      // 回滚 UI
      setFollowedTopics(new Set(followedTopics));
    }
  };

  /**
   * 查看话题详情
   */
  const handleViewTopic = (topicId: number) => {
    navigate(`/topics/${topicId}`);
  };

  /**
   * 搜索过滤
   */
  const filteredTopics = topics.filter((topic) => {
    if (!searchKeyword.trim()) return true;
    const keyword = searchKeyword.toLowerCase();
    return (
      topic.name.toLowerCase().includes(keyword) ||
      topic.description?.toLowerCase().includes(keyword)
    );
  });

  // ==================== 渲染 ====================

  return (
    <div className="topics-page">
      <div className="topics-container">
        {/* ==================== 头部 ==================== */}
        <div className="topics-header">
          <div className="topics-header__info">
            <h1 className="topics-header__title">🔥 热门话题</h1>
            <p className="topics-header__subtitle">发现感兴趣的话题，参与讨论！</p>
          </div>
          <div className="topics-header__actions">
            <Button
              type="primary"
              size="large"
              onClick={() => setShowCreateModal(true)}
            >
              ➕ 创建话题
            </Button>
          </div>
        </div>

        {/* ==================== 搜索栏 ==================== */}
        <div className="topics-search">
          <Input
            type="text"
            placeholder="🔍 搜索话题..."
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
          />
        </div>

        {/* ==================== 标签页 ==================== */}
        <div className="topics-tabs">
          <Tabs
            value={activeTab}
            onChange={(value) => setActiveTab(value as 'all' | 'hot' | 'followed')}
            tabs={[
              { label: '🌐 全部话题', value: 'all' },
              { label: '🔥 热门话题', value: 'hot' },
              {
                label: '❤️ 我的关注',
                value: 'followed',
                disabled: !isAuthenticated,
              },
            ]}
          />
        </div>

        {/* ==================== 话题列表 ==================== */}
        <div className="topics-content">
          {loading ? (
            <div className="topics-loading">
              <Skeleton type="card" count={6} animation="wave" />
            </div>
          ) : filteredTopics.length === 0 ? (
            <div className="topics-empty">
              <div className="empty-icon">
                {activeTab === 'followed' && !isAuthenticated ? '🔒' : '🔍'}
              </div>
              <h3 className="empty-text">
                {activeTab === 'followed' && !isAuthenticated
                  ? '请先登录'
                  : searchKeyword
                  ? '没有找到相关话题'
                  : '暂无话题'}
              </h3>
              <p className="empty-tip">
                {activeTab === 'followed' && !isAuthenticated
                  ? '登录后可以查看关注的话题'
                  : searchKeyword
                  ? '试试其他关键词吧~'
                  : '快来创建第一个话题吧！'}
              </p>
              {activeTab === 'followed' && !isAuthenticated && (
                <Button type="primary" size="large" onClick={() => navigate('/login')}>
                  去登录 →
                </Button>
              )}
            </div>
          ) : (
            <div className="topics-grid">
              {filteredTopics.map((topic) => {
                const isFollowed = followedTopics.has(topic.id);

                return (
                  <div key={topic.id} className="topic-card">
                    {/* 话题图标 */}
                    <div className="topic-card__icon">
                      {topic.isHot ? '🔥' : '💬'}
                    </div>

                    {/* 话题信息 */}
                    <div
                      className="topic-card__content"
                      onClick={() => handleViewTopic(topic.id)}
                    >
                      <h3 className="topic-card__name">{topic.name}</h3>
                      {topic.description && (
                        <p className="topic-card__description">
                          {topic.description}
                        </p>
                      )}

                      {/* 统计信息 */}
                      <div className="topic-card__stats">
                        <span className="stat-item">
                          📝 {topic.postCount || 0} 帖子
                        </span>
                        <span className="stat-item">
                          👥 {topic.followerCount || 0} 关注
                        </span>
                        <span className="stat-item">
                          👀 {topic.viewCount || 0} 浏览
                        </span>
                      </div>
                    </div>

                    {/* 操作按钮 */}
                    <div className="topic-card__actions">
                      {isAuthenticated && (
                        <Button
                          type={isFollowed ? 'default' : 'primary'}
                          size="small"
                          onClick={(e) => {
                            e.stopPropagation();
                            handleToggleFollow(topic.id, isFollowed);
                          }}
                        >
                          {isFollowed ? '已关注' : '➕ 关注'}
                        </Button>
                      )}
                      <Button
                        type="default"
                        size="small"
                        onClick={() => handleViewTopic(topic.id)}
                      >
                        查看详情 →
                      </Button>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>

      {/* ==================== 创建话题弹窗 ==================== */}
      {showCreateModal && (
        <Modal
          title="➕ 创建话题"
          visible={showCreateModal}
          onClose={() => {
            setShowCreateModal(false);
            setCreateForm({ name: '', description: '' });
          }}
          footer={
            <>
              <Button
                type="default"
                onClick={() => {
                  setShowCreateModal(false);
                  setCreateForm({ name: '', description: '' });
                }}
              >
                取消
              </Button>
              <Button
                type="primary"
                onClick={handleCreateTopic}
                loading={creating}
              >
                创建
              </Button>
            </>
          }
        >
          <div className="create-topic-form">
            <div className="form-group">
              <label className="form-label">
                话题名称<span className="required">*</span>
              </label>
              <Input
                type="text"
                placeholder="例如：考研交流、二手交易..."
                value={createForm.name}
                onChange={(e) =>
                  setCreateForm({ ...createForm, name: e.target.value })
                }
                maxLength={50}
              />
              <div className="char-count">{createForm.name.length}/50</div>
            </div>

            <div className="form-group">
              <label className="form-label">话题描述（可选）</label>
              <textarea
                className="form-textarea"
                placeholder="简单描述这个话题的内容..."
                value={createForm.description}
                onChange={(e) =>
                  setCreateForm({ ...createForm, description: e.target.value })
                }
                maxLength={200}
                rows={4}
              />
              <div className="char-count">
                {createForm.description.length}/200
              </div>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};

export default Topics;
