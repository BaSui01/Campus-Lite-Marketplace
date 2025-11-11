/**
 * 学习资源页面
 * @author BaSui 😎
 * @date 2025-11-11
 */

import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Card, Skeleton, Input } from '@campus/shared/components';
import { resourceService, type Resource } from '@campus/shared/services';
import { useNotificationStore } from '../../store';
import './Resources.css';

const Resources: React.FC = () => {
  const toast = useNotificationStore();
  const [keyword, setKeyword] = useState('');
  const [typeFilter, setTypeFilter] = useState<string>('');

  // 获取资源列表
  const { data, isLoading } = useQuery({
    queryKey: ['resources', typeFilter, keyword],
    queryFn: () =>
      resourceService.list({
        page: 0,
        size: 20,
        type: typeFilter || undefined,
        keyword: keyword || undefined,
      }),
  });

  // 下载资源
  const handleDownload = async (resource: Resource) => {
    if (!resource.fileUrl) {
      toast.warning('该资源暂无下载链接');
      return;
    }

    try {
      await resourceService.recordDownload(resource.id);
      window.open(resource.fileUrl, '_blank');
      toast.success('开始下载...');
    } catch (error: any) {
      toast.error('下载失败！');
    }
  };

  // 获取类型图标
  const getTypeIcon = (type: string) => {
    const iconMap: Record<string, string> = {
      DOCUMENT: '📄',
      VIDEO: '🎬',
      AUDIO: '🎵',
      LINK: '🔗',
      CODE: '💻',
      OTHER: '📦',
    };
    return iconMap[type] || '📦';
  };

  // 格式化文件大小
  const formatFileSize = (bytes?: number) => {
    if (!bytes) return '-';
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  };

  return (
    <div className="resources-page">
      <div className="resources-header">
        <h1>🎓 学习资源</h1>
        <p>共享知识，共同成长</p>
      </div>

      {/* 搜索和筛选 */}
      <div className="resources-controls">
        <Input
          type="text"
          placeholder="搜索资源..."
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          className="search-input"
        />
        <div className="type-filters">
          <button className={`type-btn ${typeFilter === '' ? 'active' : ''}`} onClick={() => setTypeFilter('')}>
            全部
          </button>
          <button
            className={`type-btn ${typeFilter === 'DOCUMENT' ? 'active' : ''}`}
            onClick={() => setTypeFilter('DOCUMENT')}
          >
            📄 文档
          </button>
          <button
            className={`type-btn ${typeFilter === 'VIDEO' ? 'active' : ''}`}
            onClick={() => setTypeFilter('VIDEO')}
          >
            🎬 视频
          </button>
          <button
            className={`type-btn ${typeFilter === 'LINK' ? 'active' : ''}`}
            onClick={() => setTypeFilter('LINK')}
          >
            🔗 链接
          </button>
          <button
            className={`type-btn ${typeFilter === 'CODE' ? 'active' : ''}`}
            onClick={() => setTypeFilter('CODE')}
          >
            💻 代码
          </button>
        </div>
      </div>

      {/* 资源列表 */}
      <div className="resources-list">
        {isLoading ? (
          <Skeleton type="card" count={6} />
        ) : data?.content && data.content.length > 0 ? (
          data.content.map((resource) => (
            <Card key={resource.id} className="resource-card">
              <div className="resource-icon">{getTypeIcon(resource.type)}</div>
              <div className="resource-content">
                <h3>{resource.title}</h3>
                {resource.category && <span className="resource-category">{resource.category}</span>}
                <p className="resource-desc">{resource.description?.substring(0, 150)}</p>
                <div className="resource-meta">
                  <span>📥 {resource.downloadCount} 次下载</span>
                  <span>👁️ {resource.viewCount} 次浏览</span>
                  <span>💾 {formatFileSize(resource.fileSize)}</span>
                </div>
                {resource.tags && (
                  <div className="resource-tags">
                    {resource.tags.split(',').map((tag, idx) => (
                      <span key={idx} className="tag">
                        {tag.trim()}
                      </span>
                    ))}
                  </div>
                )}
              </div>
              <div className="resource-actions">
                <button className="download-btn" onClick={() => handleDownload(resource)}>
                  {resource.type === 'LINK' ? '打开链接' : '下载'}
                </button>
              </div>
            </Card>
          ))
        ) : (
          <div className="empty-state">
            <p>📭 暂无资源</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default Resources;
