/**
 * 退款申请页面 - 维护你的权益！💰
 * @author BaSui 😎
 * @description 申请退款、填写理由、上传凭证
 */

import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Button, Input, ImageUpload } from '@campus/shared/components';
import { useNotificationStore } from '../../store';
import { getApi } from '@campus/shared/utils';
import type { Order } from '@campus/shared/api/models';
import './RefundApply.css';

// ==================== 类型定义 ====================

/**
 * 退款申请页面组件
 */
const RefundApply: React.FC = () => {
  const navigate = useNavigate();
  const { orderNo } = useParams<{ orderNo: string }>();
  const toast = useNotificationStore();

  // ==================== 状态管理 ====================

  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  // 退款表单
  const [reason, setReason] = useState('');
  const [note, setNote] = useState('');
  const [images, setImages] = useState<string[]>([]);

  // 常见退款理由
  const commonReasons = [
    '商品与描述不符',
    '商品质量问题',
    '商品破损',
    '卖家发错货',
    '未按时发货',
    '协商一致退款',
    '其他原因',
  ];

  // API 实例
  const api = getApi();

  // ==================== 数据加载 ====================

  /**
   * 加载订单详情
   */
  const loadOrder = async () => {
    if (!orderNo) {
      toast.error('订单号不能为空！😭');
      navigate('/orders');
      return;
    }

    setLoading(true);

    try {
      // 🚀 调用真实后端 API 获取订单详情
      const response = await api.getOrderDetail({ orderNo });

      if (response.data.success && response.data.data) {
        setOrder(response.data.data);
      } else {
        toast.error('订单不存在！😭');
        navigate('/orders');
      }
    } catch (err: any) {
      console.error('加载订单失败:', err);
      toast.error(err.response?.data?.message || '加载订单失败！😭');
      navigate('/orders');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadOrder();
  }, [orderNo]);

  // ==================== 事件处理 ====================

  /**
   * 提交退款申请
   */
  const handleSubmit = async () => {
    if (!reason.trim()) {
      toast.warning('请选择或填写退款理由！😰');
      return;
    }

    if (!orderNo) {
      toast.error('订单号不存在！😭');
      return;
    }

    setSubmitting(true);

    try {
      // 准备凭证数据
      const evidence: Record<string, any> = {};
      if (images.length > 0) {
        evidence.images = images;
      }
      if (note.trim()) {
        evidence.note = note.trim();
      }

      // 🚀 调用真实后端 API 申请退款
      const response = await api.applyRefund({
        orderNo,
        reason: reason.trim(),
        body: Object.keys(evidence).length > 0 ? evidence : undefined,
      });

      if (response.data.success) {
        toast.success('退款申请提交成功！我们会尽快处理！🚀');
        navigate('/orders');
      }
    } catch (err: any) {
      console.error('提交退款申请失败:', err);
      toast.error(err.response?.data?.message || '提交退款申请失败！😭');
    } finally {
      setSubmitting(false);
    }
  };

  /**
   * 选择常见理由
   */
  const handleSelectReason = (selectedReason: string) => {
    setReason(selectedReason);
  };

  /**
   * 图片上传成功
   */
  const handleImageUpload = (urls: string[]) => {
    setImages(urls);
  };

  /**
   * 格式化价格
   */
  const formatPrice = (price?: number) => {
    if (!price) return '¥0.00';
    // ✅ 后端金额单位为“元”（BigDecimal），无需再除以 100
    return `¥${price.toFixed(2)}`;
  };

  // ==================== 渲染 ====================

  if (loading) {
    return (
      <div className="refund-apply-page">
        <div className="refund-apply-container">
          <div className="loading-text">加载中...</div>
        </div>
      </div>
    );
  }

  if (!order) {
    return null;
  }

  return (
    <div className="refund-apply-page">
      <div className="refund-apply-container">
        {/* ==================== 头部 ==================== */}
        <div className="refund-apply-header">
          <h1 className="refund-apply-header__title">💰 申请退款</h1>
          <p className="refund-apply-header__subtitle">填写退款信息，维护你的权益！</p>
        </div>

        {/* ==================== 订单信息 ==================== */}
        <div className="refund-order-info">
          <h3 className="refund-section-title">📦 订单信息</h3>
          <div className="refund-order-card">
            <div className="refund-order-item">
              <span className="label">订单号：</span>
              <span className="value">{order.orderNo}</span>
            </div>
            <div className="refund-order-item">
              <span className="label">商品名称：</span>
              <span className="value">{order.goodsTitle || '未知商品'}</span>
            </div>
            <div className="refund-order-item">
              <span className="label">订单金额：</span>
              <span className="value price">{formatPrice(order.totalAmount)}</span>
            </div>
          </div>
        </div>

        {/* ==================== 退款表单 ==================== */}
        <div className="refund-form">
          {/* 退款理由 */}
          <div className="form-group">
            <label className="form-label">
              退款理由<span className="required">*</span>
            </label>
            <div className="reason-tags">
              {commonReasons.map((item) => (
                <button
                  key={item}
                  className={`reason-tag ${reason === item ? 'active' : ''}`}
                  onClick={() => handleSelectReason(item)}
                >
                  {item}
                </button>
              ))}
            </div>
            <Input
              type="text"
              placeholder="或输入自定义理由..."
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              maxLength={100}
            />
            <div className="char-count">{reason.length}/100</div>
          </div>

          {/* 详细说明 */}
          <div className="form-group">
            <label className="form-label">详细说明（可选）</label>
            <textarea
              className="form-textarea"
              placeholder="请详细描述退款原因，有助于我们更快处理..."
              value={note}
              onChange={(e) => setNote(e.target.value)}
              rows={5}
              maxLength={500}
            />
            <div className="char-count">{note.length}/500</div>
          </div>

          {/* 上传凭证 */}
          <div className="form-group">
            <label className="form-label">上传凭证（可选）</label>
            <p className="form-tip">上传照片、聊天记录等凭证，有助于我们更快审核</p>
            <ImageUpload
              maxCount={5}
              onUploadSuccess={handleImageUpload}
              accept="image/*"
            />
          </div>

          {/* 操作按钮 */}
          <div className="form-actions">
            <Button type="default" size="large" onClick={() => navigate('/orders')}>
              取消
            </Button>
            <Button type="primary" size="large" onClick={handleSubmit} loading={submitting}>
              提交申请
            </Button>
          </div>
        </div>

        {/* ==================== 温馨提示 ==================== */}
        <div className="refund-tips">
          <h3 className="refund-section-title">💡 温馨提示</h3>
          <ul className="tips-list">
            <li>请确保填写的退款理由真实有效，虚假申请可能导致账号受限</li>
            <li>提交申请后，我们会在 1-3 个工作日内审核处理</li>
            <li>审核通过后，退款将原路退回到你的支付账户</li>
            <li>如有疑问，可联系平台客服或卖家协商</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default RefundApply;
