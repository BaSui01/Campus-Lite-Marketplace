/**
 * RightSidebar 组件 - 右侧边栏（Linux.do 风格）
 * @author BaSui 😎
 * @description 热门话题、热门标签、活动公告
 */

import React from 'react';
import { useNavigate } from 'react-router-dom';
import type { Topic } from '@campus/shared/services/topic';
import type { Tag } from '@campus/shared/services/tag';
import './RightSidebar.css';

interface RightSidebarProps {
  hotTopics: Topic[];
  hotTags: Tag[];
  onSelectTopic?: (topicId: number) => void;
  onSelectTag?: (tagId: number) => void;
}

const RightSidebar: React.FC<RightSidebarProps> = ({
  hotTopics,
  hotTags,
  onSelectTopic,
  onSelectTag,
}) => {
  const navigate = useNavigate();

  return (
    <aside className="right-sidebar">
      {/* 热门话题 */}
      {hotTopics.length > 0 && (
        <section className="right-sidebar__section">
          <div className="right-sidebar__header">
            <h3 className="right-sidebar__title">🔥 热门话题</h3>
          </div>
          <div className="right-sidebar__list">
            {hotTopics.slice(0, 8).map((topic, index) => (
              <button
                key={topic.id}
                className="right-sidebar__item"
                onClick={() => onSelectTopic?.(topic.id)}
              >
                <span className="right-sidebar__rank">{index + 1}</span>
                <div className="right-sidebar__item-content">
                  <div className="right-sidebar__item-title">{topic.name}</div>
                  <div className="right-sidebar__item-meta">
                    {topic.postCount > 0 && (
                      <span>{topic.postCount} 帖子</span>
                    )}
                    {topic.followerCount > 0 && (
                      <span className="right-sidebar__divider">•</span>
                    )}
                    {topic.followerCount > 0 && (
                      <span>{topic.followerCount} 关注</span>
                    )}
                  </div>
                </div>
              </button>
            ))}
          </div>
        </section>
      )}

      {/* 热门标签 */}
      {hotTags.length > 0 && (
        <section className="right-sidebar__section">
          <div className="right-sidebar__header">
            <h3 className="right-sidebar__title">🏷️ 热门标签</h3>
          </div>
          <div className="right-sidebar__tags">
            {hotTags.slice(0, 12).map((tag) => (
              <button
                key={tag.id}
                className="right-sidebar__tag"
                onClick={() => onSelectTag?.(tag.id)}
              >
                <span className="right-sidebar__tag-name">#{tag.name}</span>
                {tag.hotCount > 0 && (
                  <span className="right-sidebar__tag-count">{tag.hotCount}</span>
                )}
              </button>
            ))}
          </div>
        </section>
      )}

      {/* 快捷入口 */}
      <section className="right-sidebar__section">
        <div className="right-sidebar__header">
          <h3 className="right-sidebar__title">⚡ 快捷入口</h3>
        </div>
        <div className="right-sidebar__shortcuts">
          <button
            className="right-sidebar__shortcut"
            onClick={() => navigate('/goods')}
          >
            <span className="right-sidebar__shortcut-icon">🛍️</span>
            <span className="right-sidebar__shortcut-label">商品市场</span>
          </button>
          <button
            className="right-sidebar__shortcut"
            onClick={() => navigate('/events')}
          >
            <span className="right-sidebar__shortcut-icon">🎉</span>
            <span className="right-sidebar__shortcut-label">校园活动</span>
          </button>
          <button
            className="right-sidebar__shortcut"
            onClick={() => navigate('/resources')}
          >
            <span className="right-sidebar__shortcut-icon">🎓</span>
            <span className="right-sidebar__shortcut-label">学习资源</span>
          </button>
        </div>
      </section>

      {/* 校园公告（预留） */}
      <section className="right-sidebar__section right-sidebar__section--notice">
        <div className="right-sidebar__header">
          <h3 className="right-sidebar__title">📢 校园公告</h3>
        </div>
        <div className="right-sidebar__notice">
          <p className="right-sidebar__notice-text">
            🎉 欢迎来到校园社区！分享你的生活点滴吧~
          </p>
        </div>
      </section>
    </aside>
  );
};

export default RightSidebar;
