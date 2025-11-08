/**
 * LogisticsCard - 物流卡片组件
 * @author BaSui 😎
 * @description 展示订单物流信息和轨迹，支持展开/收起、复制快递单号
 */

import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Timeline, Skeleton, Empty, toast } from '@campus/shared/components';
import { logisticsService } from '@campus/shared/services';;
import type { TimelineItem } from '@campus/shared/components';
import './LogisticsCard.css';

interface LogisticsCardProps {
  orderId: number;
}

export const LogisticsCard: React.FC<LogisticsCardProps> = ({ orderId }) => {
  const [expanded, setExpanded] = useState(false);

  // 获取物流信息
  const { data: logistics, isLoading, error } = useQuery({
    queryKey: ['logistics', orderId],
    queryFn: () => logisticsService.getOrderLogistics(orderId),
    staleTime: 5 * 60 * 1000, // 5分钟缓存
  });

  // 复制快递单号
  const handleCopy = async () => {
    if (logistics?.trackingNumber) {
      try {
        await navigator.clipboard.writeText(logistics.trackingNumber);
        toast.success('快递单号已复制');
      } catch (error) {
        toast.error('复制失败');
      }
    }
  };

  // 转换为 Timeline 数据
  const timelineItems: TimelineItem[] = logistics?.tracks.map((track) => ({
    time: track.time,
    title: track.description,
    description: track.location,
    status: track.status === logistics.status ? 'success' : 'default',
  })) || [];

  // 加载状态
  if (isLoading) {
    return (
      <div className="logistics-card">
        <Skeleton type="paragraph" count={3} />
      </div>
    );
  }

  // 错误状态
  if (error || !logistics) {
    return (
      <div className="logistics-card">
        <Empty icon="📦" title="暂无物流信息" description="物流信息还未更新" />
      </div>
    );
  }

  return (
    <div className="logistics-card">
      <div className="logistics-card__header">
        <div className="logistics-card__info">
          <h3 className="logistics-card__title">
            {logistics.expressName}
          </h3>
          <p className="logistics-card__tracking">
            {logistics.trackingNumber}
            <button className="logistics-card__copy-btn" onClick={handleCopy}>
              复制
            </button>
          </p>
        </div>
        <span className="logistics-card__status">{logistics.status}</span>
      </div>

      {logistics.tracks.length > 0 && (
        <>
          <div className="logistics-card__latest">
            <p>{logistics.tracks[0].description}</p>
            <span>{logistics.tracks[0].time}</span>
          </div>

          <button
            className="logistics-card__toggle"
            onClick={() => setExpanded(!expanded)}
          >
            {expanded ? '收起' : '查看全部'}
          </button>

          {expanded && (
            <div className="logistics-card__timeline">
              <Timeline items={timelineItems} activeIndex={0} />
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default LogisticsCard;
