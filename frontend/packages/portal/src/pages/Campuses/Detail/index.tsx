/**
 * 校区详情页面 - 深入了解校区！🏫
 * @author BaSui 😎
 * @description 校区信息、统计数据
 */

import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button, Skeleton, Badge } from '@campus/shared/components';
import { campusService, CampusStatus, type Campus, type CampusStatistics } from '@campus/shared/services';
import { useNotificationStore } from '../../../store';
import './CampusDetail.css';

// ==================== 状态配置 ====================

const STATUS_CONFIG = {
  [CampusStatus.ENABLED]: {
    text: '开放中',
    color: '#52c41a',
    icon: '✅',
  },
  [CampusStatus.DISABLED]: {
    text: '已关闭',
    color: '#d9d9d9',
    icon: '🔒',
  },
};

/**
 * 校区详情页面组件
 */
const CampusDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const toast = useNotificationStore();

  // ==================== 状态管理 ====================

  const [campus, setCampus] = useState<Campus | null>(null);
  const [statistics, setStatistics] = useState<CampusStatistics | null>(null);
  const [loading, setLoading] = useState(true);
  const [statsLoading, setStatsLoading] = useState(false);

  // ==================== 数据加载 ====================

  /**
   * 加载校区详情
   */
  const loadCampusDetail = async () => {
    if (!id) return;

    setLoading(true);

    try {
      // ✅ 获取校区详情
      const campusData = await campusService.getDetail(Number(id));
      setCampus(campusData);

      // ✅ 获取校区统计数据
      loadStatistics(Number(id));
    } catch (err: any) {
      console.error('加载校区详情失败:', err);
      toast.error(err.response?.data?.message || '加载校区详情失败！😭');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 加载统计数据
   */
  const loadStatistics = async (campusId: number) => {
    setStatsLoading(true);

    try {
      const stats = await campusService.statistics(campusId);
      setStatistics(stats);
    } catch (err: any) {
      console.error('加载统计数据失败:', err);
      // 不显示错误提示（统计数据可能未实现）
    } finally {
      setStatsLoading(false);
    }
  };

  useEffect(() => {
    loadCampusDetail();
  }, [id]);

  // ==================== 事件处理 ====================

  /**
   * 返回校区列表
   */
  const handleBack = () => {
    navigate('/campuses');
  };

  /**
   * 浏览校区商品
   */
  const handleBrowseGoods = () => {
    // 跳转到商品列表，带上校区筛选参数
    navigate(`/goods?campusId=${id}`);
  };

  /**
   * 格式化时间
   */
  const formatTime = (time: string) => {
    const date = new Date(time);
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  /**
   * 获取状态配置
   */
  const getStatusConfig = (status: CampusStatus) => {
    return STATUS_CONFIG[status] || STATUS_CONFIG[CampusStatus.ENABLED];
  };

  // ==================== 渲染 ====================

  if (loading) {
    return (
      <div className="campus-detail-page">
        <div className="campus-detail-container">
          <Skeleton type="card" count={3} animation="wave" />
        </div>
      </div>
    );
  }

  if (!campus) {
    return (
      <div className="campus-detail-page">
        <div className="campus-detail-container">
          <div className="campus-detail-error">
            <div className="error-icon">⚠️</div>
            <h3 className="error-text">校区不存在</h3>
            <Button type="primary" size="large" onClick={handleBack}>
              返回校区列表 →
            </Button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="campus-detail-page">
      <div className="campus-detail-container">
        {/* ==================== 返回按钮 ==================== */}
        <div className="campus-detail-back">
          <Button type="default" size="small" onClick={handleBack}>
            ← 返回校区列表
          </Button>
        </div>

        {/* ==================== 校区头部 ==================== */}
        <div className="campus-detail-header">
          <div className="campus-detail-header__icon">
            {campus.status === CampusStatus.ENABLED ? '🏫' : '🔒'}
          </div>
          <div className="campus-detail-header__content">
            <div className="campus-detail-header__title-row">
              <h1 className="campus-detail-header__title">{campus.name}</h1>
              <Badge
                text={getStatusConfig(campus.status).text}
                color={getStatusConfig(campus.status).color}
              />
            </div>
            <div className="campus-detail-header__code">
              校区代码：{campus.code}
            </div>
            {campus.address && (
              <div className="campus-detail-header__address">
                📍 {campus.address}
              </div>
            )}
            {campus.phone && (
              <div className="campus-detail-header__phone">
                📞 {campus.phone}
              </div>
            )}
            <div className="campus-detail-header__meta">
              <span className="meta-item">
                🕒 创建于 {formatTime(campus.createdAt)}
              </span>
              {campus.updatedAt && (
                <span className="meta-item">
                  🔄 更新于 {formatTime(campus.updatedAt)}
                </span>
              )}
            </div>
          </div>
          <div className="campus-detail-header__actions">
            {campus.status === CampusStatus.ENABLED && (
              <Button
                type="primary"
                size="large"
                onClick={handleBrowseGoods}
              >
                🛍️ 浏览商品
              </Button>
            )}
          </div>
        </div>

        {/* ==================== 统计数据 ==================== */}
        {statsLoading ? (
          <div className="campus-detail-stats">
            <Skeleton type="card" count={1} animation="wave" />
          </div>
        ) : statistics ? (
          <div className="campus-detail-stats">
            <h2 className="section-title">📊 校区统计</h2>
            <div className="stats-grid">
              <div className="stat-card">
                <div className="stat-card__icon">👥</div>
                <div className="stat-card__content">
                  <div className="stat-card__value">{statistics.userCount}</div>
                  <div className="stat-card__label">注册用户</div>
                </div>
              </div>
              <div className="stat-card">
                <div className="stat-card__icon">📦</div>
                <div className="stat-card__content">
                  <div className="stat-card__value">{statistics.goodsCount}</div>
                  <div className="stat-card__label">在售商品</div>
                </div>
              </div>
              <div className="stat-card">
                <div className="stat-card__icon">📝</div>
                <div className="stat-card__content">
                  <div className="stat-card__value">{statistics.orderCount}</div>
                  <div className="stat-card__label">交易订单</div>
                </div>
              </div>
              <div className="stat-card">
                <div className="stat-card__icon">🔥</div>
                <div className="stat-card__content">
                  <div className="stat-card__value">{statistics.activeUserCount}</div>
                  <div className="stat-card__label">活跃用户（30天）</div>
                </div>
              </div>
            </div>
          </div>
        ) : (
          <div className="campus-detail-stats">
            <div className="stats-empty">
              <div className="empty-icon">📊</div>
              <p className="empty-text">暂无统计数据</p>
            </div>
          </div>
        )}

        {/* ==================== 校区介绍 ==================== */}
        <div className="campus-detail-intro">
          <h2 className="section-title">📖 校区介绍</h2>
          <div className="intro-content">
            <p className="intro-text">
              {campus.name}是我们平台的重要组成部分，为校区内的师生提供便捷的二手交易服务。
              {campus.status === CampusStatus.ENABLED
                ? '目前校区开放中，欢迎发布和购买商品！'
                : '目前校区暂时关闭，暂时无法进行交易活动。'}
            </p>
            {campus.address && (
              <div className="intro-item">
                <strong>校区地址：</strong>
                <span>{campus.address}</span>
              </div>
            )}
            {campus.phone && (
              <div className="intro-item">
                <strong>联系电话：</strong>
                <span>{campus.phone}</span>
              </div>
            )}
          </div>
        </div>

        {/* ==================== 温馨提示 ==================== */}
        <div className="campus-detail-tips">
          <h4 className="campus-detail-tips__title">💡 温馨提示</h4>
          <ul className="campus-detail-tips__list">
            <li>在该校区发布的商品仅对本校区用户可见</li>
            <li>建议优先选择同校区的交易，方便线下面交</li>
            <li>如需跨校区交易，请提前与对方协商好配送方式</li>
            <li>遇到问题可以联系平台客服或校区管理员</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default CampusDetail;
