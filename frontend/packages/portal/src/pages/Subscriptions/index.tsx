/**
 * 订阅管理页面 - 不错过任何心仪商品！🔔
 * @author BaSui 😎
 * @description 管理关键词订阅、新增订阅、取消订阅
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Input, Skeleton, Modal } from '@campus/shared/components';
import { subscriptionService } from '../../services/subscription';;
import { useNotificationStore } from '../../store';
import type { SubscriptionResponse } from '@campus/shared/api/models';
import './Subscriptions.css';

// ==================== 类型定义 ====================

/**
 * 订阅管理页面组件
 */
const Subscriptions: React.FC = () => {
  const navigate = useNavigate();
  const toast = useNotificationStore();

  // ==================== 状态管理 ====================

  const [subscriptions, setSubscriptions] = useState<SubscriptionResponse[]>([]);
  const [loading, setLoading] = useState(true);

  // 新增订阅表单
  const [showAddModal, setShowAddModal] = useState(false);
  const [keyword, setKeyword] = useState('');
  const [campusId, setCampusId] = useState<number | undefined>(undefined);
  const [submitting, setSubmitting] = useState(false);

  // ==================== 数据加载 ====================

  /**
   * 加载订阅列表
   */
  const loadSubscriptions = async () => {
    setLoading(true);

    try {
      // ✅ 使用 subscriptionService 获取订阅列表
      const response = await subscriptionService.listSubscriptions();
      setSubscriptions(response);
    } catch (err: any) {
      console.error('加载订阅列表失败:', err);
      toast.error(err.response?.data?.message || '加载订阅列表失败！😭');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSubscriptions();
  }, []);

  // ==================== 事件处理 ====================

  /**
   * 新增订阅
   */
  const handleAddSubscription = async () => {
    if (!keyword.trim()) {
      toast.warning('请输入关键词！😰');
      return;
    }

    setSubmitting(true);

    try {
      // ✅ 使用 subscriptionService 新增订阅
      await subscriptionService.subscribe({
        keyword: keyword.trim(),
        campusId: campusId,
      });

      toast.success('订阅成功！有新商品会通知你！🎉');
      setShowAddModal(false);
      setKeyword('');
      setCampusId(undefined);
      loadSubscriptions();
    } catch (err: any) {
      console.error('新增订阅失败:', err);
      toast.error(err.response?.data?.message || '新增订阅失败！😭');
    } finally {
      setSubmitting(false);
    }
  };

  /**
   * 取消订阅
   */
  const handleUnsubscribe = async (id: number, keywordText: string) => {
    if (!window.confirm(`确定要取消订阅「${keywordText}」吗？`)) {
      return;
    }

    try {
      // 乐观更新 UI
      setSubscriptions((prev) => prev.filter((s) => s.id !== id));

      // ✅ 使用 subscriptionService 取消订阅
      await subscriptionService.unsubscribe(id);

      toast.success('取消订阅成功！👋');
    } catch (err: any) {
      console.error('取消订阅失败:', err);
      toast.error(err.response?.data?.message || '取消订阅失败！😭');

      // 回滚 UI
      loadSubscriptions();
    }
  };

  /**
   * 格式化时间
   */
  const formatTime = (time?: string) => {
    if (!time) return '未知时间';

    const date = new Date(time);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const days = Math.floor(diff / (1000 * 60 * 60 * 24));

    if (days === 0) {
      return '今天订阅';
    } else if (days === 1) {
      return '昨天订阅';
    } else if (days < 30) {
      return `${days} 天前订阅`;
    } else if (days < 365) {
      return `${Math.floor(days / 30)} 个月前订阅`;
    } else {
      return `${Math.floor(days / 365)} 年前订阅`;
    }
  };

  // ==================== 渲染 ====================

  return (
    <div className="subscriptions-page">
      <div className="subscriptions-container">
        {/* ==================== 头部 ==================== */}
        <div className="subscriptions-header">
          <div className="subscriptions-header__info">
            <h1 className="subscriptions-header__title">🔔 我的订阅</h1>
            <p className="subscriptions-header__subtitle">
              {subscriptions.length > 0
                ? `订阅了 ${subscriptions.length} 个关键词，不错过心仪商品！`
                : '还没有订阅哦，快去添加吧！'}
            </p>
          </div>
          <div className="subscriptions-header__actions">
            <Button type="default" size="large" onClick={() => navigate('/subscriptions/feed')}>
              📰 查看动态流
            </Button>
            <Button type="primary" size="large" onClick={() => setShowAddModal(true)}>
              ➕ 新增订阅
            </Button>
          </div>
        </div>

        {/* ==================== 订阅列表 ==================== */}
        <div className="subscriptions-content">
          {loading ? (
            <div className="subscriptions-loading">
              <Skeleton type="list" count={6} animation="wave" />
            </div>
          ) : subscriptions.length === 0 ? (
            <div className="subscriptions-empty">
              <div className="empty-icon">🔍</div>
              <h3 className="empty-text">还没有订阅哦！</h3>
              <p className="empty-tip">添加关键词订阅，有新商品会及时通知你！</p>
              <Button type="primary" size="large" onClick={() => setShowAddModal(true)}>
                立即订阅 →
              </Button>
            </div>
          ) : (
            <div className="subscriptions-list">
              {subscriptions.map((item) => (
                <div key={item.id} className="subscription-item">
                  <div className="subscription-item__icon">🔔</div>
                  <div className="subscription-item__info">
                    <div className="subscription-item__keyword">{item.keyword}</div>
                    <div className="subscription-item__meta">
                      <span className="meta-item">
                        {item.campusId ? `校区ID: ${item.campusId}` : '全部校区'}
                      </span>
                      <span className="meta-item">{formatTime(item.createdAt)}</span>
                    </div>
                  </div>
                  <div className="subscription-item__actions">
                    <Button
                      type="danger"
                      size="small"
                      onClick={() => handleUnsubscribe(item.id!, item.keyword!)}
                    >
                      取消订阅
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* ==================== 温馨提示 ==================== */}
        <div className="subscriptions-tips">
          <h3 className="subscriptions-section-title">💡 温馨提示</h3>
          <ul className="tips-list">
            <li>订阅关键词后，有匹配的新商品上架会及时通知你</li>
            <li>支持订阅多个关键词，每个关键词独立通知</li>
            <li>可以选择特定校区的商品订阅，也可以订阅全部校区</li>
            <li>不需要的订阅可以随时取消，不影响其他订阅</li>
          </ul>
        </div>
      </div>

      {/* ==================== 新增订阅弹窗 ==================== */}
      {showAddModal && (
        <Modal
          title="➕ 新增订阅"
          visible={showAddModal}
          onClose={() => {
            setShowAddModal(false);
            setKeyword('');
            setCampusId(undefined);
          }}
          footer={
            <>
              <Button type="default" onClick={() => setShowAddModal(false)}>
                取消
              </Button>
              <Button type="primary" onClick={handleAddSubscription} loading={submitting}>
                确定订阅
              </Button>
            </>
          }
        >
          <div className="subscription-form">
            <div className="form-group">
              <label className="form-label">
                订阅关键词<span className="required">*</span>
              </label>
              <Input
                type="text"
                placeholder="例如：自行车、教材、吉他..."
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                maxLength={50}
              />
              <div className="char-count">{keyword.length}/50</div>
            </div>

            <div className="form-group">
              <label className="form-label">校区筛选（可选）</label>
              <Input
                type="number"
                placeholder="留空表示订阅全部校区"
                value={campusId?.toString() || ''}
                onChange={(e) => setCampusId(e.target.value ? Number(e.target.value) : undefined)}
              />
              <p className="form-tip">输入校区 ID，或留空订阅全部校区的商品</p>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};

export default Subscriptions;
