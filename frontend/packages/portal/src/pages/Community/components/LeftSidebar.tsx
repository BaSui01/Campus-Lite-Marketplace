/**
 * 社区左侧边栏 - Linux.do 风格导航
 * @author BaSui 😎
 * @description 话题分类、个人导航、资源分类
 */

import React from 'react';
import { useNavigate } from 'react-router-dom';
import type { Topic } from '@campus/shared/services/topic';
import './LeftSidebar.css';

interface LeftSidebarProps {
  topics: Topic[];
  selectedTopicId: number | null;
  onSelectTopic: (topicId: number | null) => void;
  isAuthenticated: boolean;
}

const LeftSidebar: React.FC<LeftSidebarProps> = ({
  topics,
  selectedTopicId,
  onSelectTopic,
  isAuthenticated,
}) => {
  const navigate = useNavigate();

  // 快捷导航项
  const quickNavItems = [
    { icon: '🏠', label: '全部话题', key: 'all', onClick: () => onSelectTopic(null) },
    { icon: '🔥', label: '热门', key: 'hot', onClick: () => navigate('/community?tab=hot') },
    { icon: '📝', label: '最新', key: 'new', onClick: () => navigate('/community?tab=new') },
    { icon: '📌', label: '精华', key: 'featured', onClick: () => navigate('/community?tab=featured') },
  ];

  // 用户导航项（需要登录）
  const userNavItems = isAuthenticated
    ? [
        { icon: '⭐', label: '我的关注', onClick: () => navigate('/community?tab=followed') },
        { icon: '📬', label: '我的帖子', onClick: () => navigate('/profile/posts') },
        { icon: '💬', label: '我的评论', onClick: () => navigate('/profile/comments') },
        { icon: '🔖', label: '我的收藏', onClick: () => navigate('/profile/favorites') },
      ]
    : [];

  // 资源导航项
  const resourceNavItems = [
    { icon: '🛍️', label: '商品市场', onClick: () => navigate('/goods') },
    { icon: '🎉', label: '校园活动', onClick: () => navigate('/events') },
    { icon: '🎓', label: '学习资源', onClick: () => navigate('/resources') },
  ];

  return (
    <aside className="left-sidebar">
      {/* 快捷导航 */}
      <section className="sidebar-section">
        <div className="sidebar-section__title">快速导航</div>
        <nav className="sidebar-nav">
          {quickNavItems.map((item) => (
            <button
              key={item.key}
              className={`sidebar-nav__item ${item.key === 'all' && selectedTopicId === null ? 'active' : ''}`}
              onClick={item.onClick}
            >
              <span className="sidebar-nav__icon">{item.icon}</span>
              <span className="sidebar-nav__label">{item.label}</span>
            </button>
          ))}
        </nav>
      </section>

      {/* 话题分类 */}
      {topics.length > 0 && (
        <section className="sidebar-section">
          <div className="sidebar-section__title">话题分类</div>
          <nav className="sidebar-nav">
            {topics.slice(0, 15).map((topic) => (
              <button
                key={topic.id}
                className={`sidebar-nav__item ${selectedTopicId === topic.id ? 'active' : ''}`}
                onClick={() => onSelectTopic(topic.id)}
              >
                <span className="sidebar-nav__icon">{topic.isHot ? '🔥' : '💬'}</span>
                <span className="sidebar-nav__label">{topic.name}</span>
                {topic.postCount > 0 && (
                  <span className="sidebar-nav__count">{topic.postCount}</span>
                )}
              </button>
            ))}
          </nav>
        </section>
      )}

      {/* 个人导航（需要登录） */}
      {isAuthenticated && userNavItems.length > 0 && (
        <section className="sidebar-section">
          <div className="sidebar-section__title">我的</div>
          <nav className="sidebar-nav">
            {userNavItems.map((item, index) => (
              <button
                key={index}
                className="sidebar-nav__item"
                onClick={item.onClick}
              >
                <span className="sidebar-nav__icon">{item.icon}</span>
                <span className="sidebar-nav__label">{item.label}</span>
              </button>
            ))}
          </nav>
        </section>
      )}

      {/* 资源导航 */}
      <section className="sidebar-section">
        <div className="sidebar-section__title">更多</div>
        <nav className="sidebar-nav">
          {resourceNavItems.map((item, index) => (
            <button
              key={index}
              className="sidebar-nav__item"
              onClick={item.onClick}
            >
              <span className="sidebar-nav__icon">{item.icon}</span>
              <span className="sidebar-nav__label">{item.label}</span>
            </button>
          ))}
        </nav>
      </section>
    </aside>
  );
};

export default LeftSidebar;
