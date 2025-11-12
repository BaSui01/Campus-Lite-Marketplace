/**
 * 校园活动页面
 * @author BaSui 😎
 * @date 2025-11-11
 */

import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Button, Card, Skeleton, Modal } from '@campus/shared/components';
import { eventService, type Event } from '@campus/shared/services';
import { useNotificationStore } from '../../store';
import './Events.css';

const Events: React.FC = () => {
  const toast = useNotificationStore();
  const [selectedEvent, setSelectedEvent] = useState<Event | null>(null);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [statusFilter, setStatusFilter] = useState<string>('');

  // 获取活动列表
  const { data, isLoading, refetch } = useQuery({
    queryKey: ['events', statusFilter],
    queryFn: () => eventService.list({ page: 0, size: 20, status: statusFilter || undefined }),
  });

  // 打开详情弹窗
  const handleOpenDetail = (event: Event) => {
    setSelectedEvent(event);
    setShowDetailModal(true);
  };

  // 报名活动
  const handleRegister = async (eventId: number) => {
    try {
      await eventService.register(eventId);
      toast.success('报名成功！🎉');
      refetch();
      setShowDetailModal(false);
    } catch (error: any) {
      toast.error(error.response?.data?.message || '报名失败！');
    }
  };

  // 取消报名
  const handleCancelRegistration = async (eventId: number) => {
    try {
      await eventService.cancelRegistration(eventId);
      toast.success('已取消报名');
      refetch();
      setShowDetailModal(false);
    } catch (error: any) {
      toast.error(error.response?.data?.message || '取消报名失败！');
    }
  };

  // 格式化时间
  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    return date.toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  // 获取状态标签
  const getStatusLabel = (status: string) => {
    const statusMap: Record<string, { label: string; className: string }> = {
      UPCOMING: { label: '即将开始', className: 'status-upcoming' },
      ONGOING: { label: '进行中', className: 'status-ongoing' },
      ENDED: { label: '已结束', className: 'status-ended' },
      CANCELLED: { label: '已取消', className: 'status-cancelled' },
    };
    return statusMap[status] || { label: status, className: '' };
  };

  return (
    <div className="events-page">
      <div className="events-header">
        <h1>🎉 校园活动</h1>
        <p>发现精彩校园生活</p>
      </div>

      {/* 筛选器 */}
      <div className="events-filters">
        <button
          className={`filter-btn ${statusFilter === '' ? 'active' : ''}`}
          onClick={() => setStatusFilter('')}
        >
          全部
        </button>
        <button
          className={`filter-btn ${statusFilter === 'UPCOMING' ? 'active' : ''}`}
          onClick={() => setStatusFilter('UPCOMING')}
        >
          即将开始
        </button>
        <button
          className={`filter-btn ${statusFilter === 'ONGOING' ? 'active' : ''}`}
          onClick={() => setStatusFilter('ONGOING')}
        >
          进行中
        </button>
      </div>

      {/* 活动列表 */}
      <div className="events-list">
        {isLoading ? (
          <Skeleton type="card" count={6} />
        ) : data?.content && data.content.length > 0 ? (
          data.content.map((event) => (
            <Card key={event.id} className="event-card" onClick={() => handleOpenDetail(event)}>
              {event.coverImage && (
                <img src={event.coverImage} alt={event.title} className="event-cover" />
              )}
              <div className="event-content">
                <div className="event-header">
                  <h3>{event.title}</h3>
                  <span className={`event-status ${getStatusLabel(event.status).className}`}>
                    {getStatusLabel(event.status).label}
                  </span>
                </div>
                <p className="event-desc">{event.description?.substring(0, 100)}...</p>
                <div className="event-info">
                  <span>📍 {event.location || '待定'}</span>
                  <span>🕐 {formatDate(event.startTime)}</span>
                  <span>
                    👥 {event.currentParticipants}
                    {event.maxParticipants > 0 && `/${event.maxParticipants}`}
                  </span>
                </div>
              </div>
            </Card>
          ))
        ) : (
          <div className="empty-state">
            <p>📭 暂无活动</p>
          </div>
        )}
      </div>

      {/* 详情弹窗 */}
      {showDetailModal && selectedEvent && (
        <Modal onClose={() => setShowDetailModal(false)} title="活动详情">
          <div className="event-detail">
            {selectedEvent.coverImage && (
              <img src={selectedEvent.coverImage} alt={selectedEvent.title} className="detail-cover" />
            )}
            <h2>{selectedEvent.title}</h2>
            <div className="detail-info">
              <p>📍 地点：{selectedEvent.location || '待定'}</p>
              <p>🕐 开始时间：{formatDate(selectedEvent.startTime)}</p>
              <p>🕐 结束时间：{formatDate(selectedEvent.endTime)}</p>
              <p>
                👥 报名人数：{selectedEvent.currentParticipants}
                {selectedEvent.maxParticipants > 0 && `/${selectedEvent.maxParticipants}`}
              </p>
            </div>
            <div className="detail-desc">
              <h3>活动简介</h3>
              <p>{selectedEvent.description}</p>
            </div>
            <div className="detail-actions">
              <Button onClick={() => setShowDetailModal(false)}>关闭</Button>
              {selectedEvent.status === 'UPCOMING' && (
                <Button type="primary" onClick={() => handleRegister(selectedEvent.id)}>
                  立即报名
                </Button>
              )}
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};

export default Events;
