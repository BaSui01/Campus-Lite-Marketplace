/**
 * 物流追踪页面 - 实时查看物流信息！📦
 * @author BaSui 😎
 * @description 通过快递公司和单号追踪物流轨迹
 */

import React, { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Button, Input, Skeleton, Timeline } from '@campus/shared/components';
import { logisticsService, type Logistics, type LogisticsTrack } from '@campus/shared/services';;
import { useNotificationStore } from '../../store';
import './Logistics.css';

// ==================== 快递公司配置 ====================

const EXPRESS_COMPANIES = [
  { code: 'SF', name: '顺丰速运', icon: '🚀' },
  { code: 'YTO', name: '圆通速递', icon: '📦' },
  { code: 'ZTO', name: '中通快递', icon: '🚚' },
  { code: 'STO', name: '申通快递', icon: '🚛' },
  { code: 'YD', name: '韵达快递', icon: '📮' },
  { code: 'JTSD', name: '极兔速递', icon: '🐰' },
  { code: 'JD', name: '京东物流', icon: '🛒' },
  { code: 'EMS', name: '邮政EMS', icon: '✉️' },
  { code: 'BEST', name: '百世快递', icon: '🌟' },
  { code: 'DBL', name: '德邦物流', icon: '🏢' },
];

// ==================== 物流状态配置 ====================

const STATUS_CONFIG = {
  PENDING: { text: '待揽件', color: '#faad14', icon: '⏰' },
  IN_TRANSIT: { text: '运输中', color: '#1890ff', icon: '🚚' },
  OUT_FOR_DELIVERY: { text: '派送中', color: '#13c2c2', icon: '🏃' },
  DELIVERED: { text: '已签收', color: '#52c41a', icon: '✅' },
  EXCEPTION: { text: '异常', color: '#f5222d', icon: '⚠️' },
};

/**
 * 物流追踪页面组件
 */
const Logistics: React.FC = () => {
  const navigate = useNavigate();
  const toast = useNotificationStore();
  const [searchParams] = useSearchParams();

  // ==================== 状态管理 ====================

  // 从 URL 参数获取初始值
  const initialExpressCode = searchParams.get('expressCode') || '';
  const initialTrackingNumber = searchParams.get('trackingNumber') || '';

  const [expressCode, setExpressCode] = useState(initialExpressCode);
  const [trackingNumber, setTrackingNumber] = useState(initialTrackingNumber);
  const [logistics, setLogistics] = useState<Logistics | null>(null);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);

  // ==================== 事件处理 ====================

  /**
   * 追踪物流
   */
  const handleTrack = async () => {
    if (!expressCode) {
      toast.warning('请选择快递公司！😰');
      return;
    }

    if (!trackingNumber.trim()) {
      toast.warning('请输入快递单号！😰');
      return;
    }

    setLoading(true);
    setSearched(true);

    try {
      // ✅ 调用真实 API 追踪物流
      const data = await logisticsService.trackLogistics(
        expressCode,
        trackingNumber.trim()
      );

      setLogistics(data);
      toast.success('查询成功！📦');

      // 更新 URL 参数（方便分享）
      navigate(
        `/logistics/track?expressCode=${expressCode}&trackingNumber=${trackingNumber.trim()}`,
        { replace: true }
      );
    } catch (err: any) {
      console.error('追踪物流失败:', err);
      toast.error(err.response?.data?.message || '查询失败，请检查快递单号！😭');
      setLogistics(null);
    } finally {
      setLoading(false);
    }
  };

  /**
   * 复制快递单号
   */
  const handleCopyTracking = async () => {
    if (!trackingNumber) return;

    try {
      await navigator.clipboard.writeText(trackingNumber);
      toast.success('快递单号已复制！📋');
    } catch (err) {
      console.error('复制失败:', err);
      toast.error('复制失败！😭');
    }
  };

  /**
   * 重置查询
   */
  const handleReset = () => {
    setExpressCode('');
    setTrackingNumber('');
    setLogistics(null);
    setSearched(false);
    navigate('/logistics/track', { replace: true });
  };

  /**
   * 格式化时间
   */
  const formatTime = (time: string) => {
    const date = new Date(time);
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  /**
   * 获取状态配置
   */
  const getStatusConfig = (status: string) => {
    return STATUS_CONFIG[status as keyof typeof STATUS_CONFIG] || STATUS_CONFIG.PENDING;
  };

  // 自动查询（如果 URL 有参数）
  React.useEffect(() => {
    if (initialExpressCode && initialTrackingNumber && !searched) {
      handleTrack();
    }
  }, []);

  // ==================== 渲染 ====================

  return (
    <div className="logistics-page">
      <div className="logistics-container">
        {/* ==================== 头部 ==================== */}
        <div className="logistics-header">
          <h1 className="logistics-header__title">📦 物流追踪</h1>
          <p className="logistics-header__subtitle">
            输入快递公司和单号，实时查看物流信息
          </p>
        </div>

        {/* ==================== 查询表单 ==================== */}
        <div className="logistics-search">
          <div className="logistics-search__form">
            {/* 快递公司选择 */}
            <div className="form-group">
              <label className="form-label">
                快递公司<span className="required">*</span>
              </label>
              <div className="express-grid">
                {EXPRESS_COMPANIES.map((company) => (
                  <button
                    key={company.code}
                    className={`express-item ${
                      expressCode === company.code ? 'active' : ''
                    }`}
                    onClick={() => setExpressCode(company.code)}
                  >
                    <span className="express-item__icon">{company.icon}</span>
                    <span className="express-item__name">{company.name}</span>
                  </button>
                ))}
              </div>
            </div>

            {/* 快递单号输入 */}
            <div className="form-group">
              <label className="form-label">
                快递单号<span className="required">*</span>
              </label>
              <div className="tracking-input">
                <Input
                  type="text"
                  placeholder="请输入快递单号"
                  value={trackingNumber}
                  onChange={(e) => setTrackingNumber(e.target.value)}
                  onKeyPress={(e) => {
                    if (e.key === 'Enter') handleTrack();
                  }}
                />
                {trackingNumber && (
                  <button
                    className="tracking-input__copy"
                    onClick={handleCopyTracking}
                  >
                    📋 复制
                  </button>
                )}
              </div>
            </div>

            {/* 操作按钮 */}
            <div className="form-actions">
              <Button
                type="primary"
                size="large"
                onClick={handleTrack}
                loading={loading}
              >
                🔍 查询物流
              </Button>
              <Button type="default" size="large" onClick={handleReset}>
                重置
              </Button>
            </div>
          </div>
        </div>

        {/* ==================== 物流信息展示 ==================== */}
        {loading && (
          <div className="logistics-result">
            <Skeleton type="card" count={3} animation="wave" />
          </div>
        )}

        {!loading && searched && !logistics && (
          <div className="logistics-result">
            <div className="logistics-empty">
              <div className="empty-icon">🔍</div>
              <h3 className="empty-text">未查询到物流信息</h3>
              <p className="empty-tip">请检查快递公司和单号是否正确</p>
            </div>
          </div>
        )}

        {!loading && logistics && (
          <div className="logistics-result">
            {/* 物流概览 */}
            <div className="logistics-overview">
              <div className="logistics-overview__header">
                <div className="logistics-overview__company">
                  <span className="company-icon">
                    {
                      EXPRESS_COMPANIES.find((c) => c.code === logistics.expressCode)
                        ?.icon || '📦'
                    }
                  </span>
                  <div className="company-info">
                    <h2 className="company-name">{logistics.expressName}</h2>
                    <p className="company-tracking">
                      {logistics.trackingNumber}
                      <button
                        className="copy-btn"
                        onClick={handleCopyTracking}
                      >
                        📋
                      </button>
                    </p>
                  </div>
                </div>
                <div
                  className="logistics-overview__status"
                  style={{
                    backgroundColor: getStatusConfig(logistics.status).color,
                  }}
                >
                  <span className="status-icon">
                    {getStatusConfig(logistics.status).icon}
                  </span>
                  <span className="status-text">
                    {getStatusConfig(logistics.status).text}
                  </span>
                </div>
              </div>

              {/* 时间信息 */}
              <div className="logistics-overview__times">
                {logistics.shippedAt && (
                  <div className="time-item">
                    <span className="time-label">🚚 发货时间：</span>
                    <span className="time-value">
                      {formatTime(logistics.shippedAt)}
                    </span>
                  </div>
                )}
                {logistics.deliveredAt && (
                  <div className="time-item">
                    <span className="time-label">✅ 签收时间：</span>
                    <span className="time-value">
                      {formatTime(logistics.deliveredAt)}
                    </span>
                  </div>
                )}
              </div>
            </div>

            {/* 物流轨迹 */}
            {logistics.tracks && logistics.tracks.length > 0 && (
              <div className="logistics-timeline">
                <h3 className="logistics-timeline__title">🚚 物流轨迹</h3>
                <div className="logistics-timeline__content">
                  <Timeline
                    items={logistics.tracks.map((track, index) => ({
                      time: formatTime(track.time),
                      title: track.description,
                      description: track.location,
                      status: index === 0 ? 'success' : 'default',
                    }))}
                    activeIndex={0}
                  />
                </div>
              </div>
            )}

            {/* 温馨提示 */}
            <div className="logistics-tips">
              <h4 className="logistics-tips__title">💡 温馨提示</h4>
              <ul className="logistics-tips__list">
                <li>物流信息每30分钟自动更新一次</li>
                <li>如遇物流异常，请及时联系快递公司或卖家</li>
                <li>签收后如有问题，可在订单页面申请售后</li>
              </ul>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Logistics;
