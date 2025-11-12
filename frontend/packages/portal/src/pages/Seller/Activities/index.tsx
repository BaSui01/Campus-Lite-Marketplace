/**
 * 营销活动列表页面 - 管理所有活动！📋
 * @author BaSui 😎
 * @description 活动列表、状态管理、效果统计
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Skeleton } from '@campus/shared/components';
import { marketingService, CampaignStatus, CAMPAIGN_TYPE_CONFIG, CAMPAIGN_STATUS_CONFIG, type MarketingCampaign } from '@/services';
import { useNotificationStore } from '@/store';
import './Activities.css';

/**
 * 活动列表页面组件
 */
const Activities: React.FC = () => {
  const navigate = useNavigate();
  const toast = useNotificationStore();

  // ==================== 状态管理 ====================

  const [campaigns, setCampaigns] = useState<MarketingCampaign[]>([]);
  const [loading, setLoading] = useState(true);
  const [filterStatus, setFilterStatus] = useState<CampaignStatus | 'ALL'>('ALL');

  // ==================== 数据加载 ====================

  /**
   * 加载活动列表
   */
  const loadCampaigns = async () => {
    setLoading(true);

    try {
      const data = await marketingService.getMerchantCampaigns();
      setCampaigns(data);
    } catch (err: any) {
      console.error('加载活动列表失败:', err);
      toast.error(err.response?.data?.message || '加载活动列表失败!😭');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCampaigns();
  }, []);

  // ==================== 事件处理 ====================

  /**
   * 暂停活动
   */
  const handlePause = async (id: number) => {
    try {
      await marketingService.pauseCampaign(id);
      toast.success('活动已暂停！');
      loadCampaigns();
    } catch (err: any) {
      toast.error('操作失败!😭');
    }
  };

  /**
   * 恢复活动
   */
  const handleResume = async (id: number) => {
    try {
      await marketingService.resumeCampaign(id);
      toast.success('活动已恢复！');
      loadCampaigns();
    } catch (err: any) {
      toast.error('操作失败!😭');
    }
  };

  /**
   * 结束活动
   */
  const handleEnd = async (id: number) => {
    if (!window.confirm('确定要结束此活动吗？')) return;

    try {
      await marketingService.endCampaign(id);
      toast.success('活动已结束！');
      loadCampaigns();
    } catch (err: any) {
      toast.error('操作失败!😭');
    }
  };

  // ==================== 过滤数据 ====================

  const filteredCampaigns = filterStatus === 'ALL'
    ? campaigns
    : campaigns.filter(c => c.status === filterStatus);

  // ==================== 渲染 ====================

  return (
    <div className="activities-page">
      <div className="activities-container">
        {/* 头部 */}
        <div className="activities-header">
          <h1 className="activities-header__title">📋 营销活动</h1>
          <Button type="primary" size="large" onClick={() => navigate('/seller/activities/create')}>
            + 创建活动
          </Button>
        </div>

        {/* 过滤器 */}
        <div className="activities-filters">
          {['ALL', ...Object.keys(CampaignStatus)].map(status => (
            <button
              key={status}
              className={`filter-btn ${filterStatus === status ? 'active' : ''}`}
              onClick={() => setFilterStatus(status as any)}
            >
              {status === 'ALL' ? '全部' : CAMPAIGN_STATUS_CONFIG[status as CampaignStatus].name}
            </button>
          ))}
        </div>

        {/* 活动列表 */}
        <div className="activities-content">
          {loading ? (
            <Skeleton type="card" count={3} animation="wave" />
          ) : filteredCampaigns.length === 0 ? (
            <div className="activities-empty">
              <div className="empty-icon">🎁</div>
              <h3 className="empty-text">暂无活动</h3>
              <Button type="primary" size="large" onClick={() => navigate('/seller/activities/create')}>
                创建第一个活动
              </Button>
            </div>
          ) : (
            <div className="campaigns-list">
              {filteredCampaigns.map(campaign => (
                <div key={campaign.id} className="campaign-card">
                  <div className="campaign-header">
                    <div className="campaign-type" style={{ background: CAMPAIGN_TYPE_CONFIG[campaign.campaignType].color }}>
                      {CAMPAIGN_TYPE_CONFIG[campaign.campaignType].icon} {CAMPAIGN_TYPE_CONFIG[campaign.campaignType].name}
                    </div>
                    <div className="campaign-status" style={{ color: CAMPAIGN_STATUS_CONFIG[campaign.status!].color }}>
                      {CAMPAIGN_STATUS_CONFIG[campaign.status!].name}
                    </div>
                  </div>

                  <h3 className="campaign-name">{campaign.campaignName}</h3>

                  <div className="campaign-details">
                    <div className="detail-item">
                      <span className="detail-label">优惠</span>
                      <span className="detail-value">{marketingService.formatDiscount(campaign.discountConfig)}</span>
                    </div>
                    <div className="detail-item">
                      <span className="detail-label">商品数</span>
                      <span className="detail-value">{campaign.goodsIds.length}个</span>
                    </div>
                    <div className="detail-item">
                      <span className="detail-label">参与人数</span>
                      <span className="detail-value">{campaign.participationCount || 0}人</span>
                    </div>
                  </div>

                  <div className="campaign-time">
                    <div>开始：{new Date(campaign.startTime).toLocaleString('zh-CN')}</div>
                    <div>结束：{new Date(campaign.endTime).toLocaleString('zh-CN')}</div>
                  </div>

                  {campaign.status === CampaignStatus.RUNNING && (
                    <div className="campaign-countdown">
                      {marketingService.getRemainingTimeText(campaign)}
                    </div>
                  )}

                  <div className="campaign-actions">
                    {campaign.status === CampaignStatus.RUNNING && (
                      <>
                        <Button size="small" onClick={() => handlePause(campaign.id!)}>暂停</Button>
                        <Button size="small" onClick={() => handleEnd(campaign.id!)}>结束</Button>
                      </>
                    )}
                    {campaign.status === CampaignStatus.PAUSED && (
                      <Button type="primary" size="small" onClick={() => handleResume(campaign.id!)}>恢复</Button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Activities;
