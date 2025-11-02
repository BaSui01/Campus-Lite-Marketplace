/**
 * 订单详情页 - 查看订单详情、支付、取消、确认收货！💳
 * @author BaSui 😎
 * @description 完整的订单流程管理页面
 */

import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Skeleton } from '@campus/shared/components';
import { orderService } from '@campus/shared/services/order';
import { websocketService } from '@campus/shared/utils';
import { useNotificationStore } from '../../store';
import type { Order, PaymentMethod, OrderStatus } from '@campus/shared/types';
import './OrderDetail.css';

/**
 * 订单详情页组件
 */
const OrderDetail: React.FC = () => {
  const { orderNo } = useParams<{ orderNo: string }>();
  const navigate = useNavigate();
  const toast = useNotificationStore();

  // ==================== 状态管理 ====================

  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [paying, setPaying] = useState(false);
  const [cancelling, setCancelling] = useState(false);
  const [confirming, setConfirming] = useState(false);
  const [refunding, setRefunding] = useState(false);

  // 支付弹窗相关
  const [showPayModal, setShowPayModal] = useState(false);
  const [selectedPaymentMethod, setSelectedPaymentMethod] = useState<PaymentMethod | null>(null);

  // 取消订单弹窗
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [cancelReason, setCancelReason] = useState('');

  // 退款弹窗
  const [showRefundModal, setShowRefundModal] = useState(false);
  const [refundReason, setRefundReason] = useState('');

  // ==================== 数据加载 ====================

  /**
   * 加载订单详情（使用真实后端 API！）
   */
  const loadOrderDetail = async () => {
    if (!orderNo) {
      setError('订单号不能为空！😰');
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);

    try {
      // 🚀 调用真实后端 API 获取订单详情
      const response = await orderService.getOrderByNo(orderNo);
      const orderData = response.data;
      setOrder(orderData);
    } catch (err: any) {
      console.error('加载订单详情失败：', err);
      setError(err.response?.data?.message || '加载订单详情失败，请稍后重试！😭');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadOrderDetail();
  }, [orderNo]);

  // ==================== 📦 实时订单状态更新（WebSocket）====================

  /**
   * 📦 监听 WebSocket 订单状态更新
   */
  useEffect(() => {
    if (!order) return; // 订单未加载完成时不订阅

    console.log('[OrderDetail] 📦 开始监听实时订单状态更新...');

    // 定义订单更新处理器
    const handleOrderUpdate = (data: any) => {
      console.log('[OrderDetail] 📦 收到订单状态更新:', data);

      const { orderId, orderNo: updatedOrderNo, status, message } = data;

      // 只处理当前订单的更新
      if (order.orderNo !== updatedOrderNo && order.orderId !== orderId) {
        return;
      }

      console.log(`[OrderDetail] ✅ 更新订单 ${updatedOrderNo} 状态: ${order.status} → ${status}`);

      // 🚀 乐观更新 UI（更新当前订单状态）
      setOrder((prev) => {
        if (!prev) return prev;
        return {
          ...prev,
          status,
          updateTime: new Date().toISOString(),
        };
      });

      // 💬 显示 Toast 提示
      if (message) {
        toast.success(message);
      } else {
        toast.success(`订单状态已更新为：${getStatusText(status)} ✅`);
      }
    };

    // 📡 订阅订单更新推送
    websocketService.onOrderUpdate(handleOrderUpdate);

    // 🔌 确保 WebSocket 已连接
    if (!websocketService.isConnected()) {
      console.log('[OrderDetail] 🔌 WebSocket 未连接，尝试连接...');
      websocketService.connect();
    }

    // 🧹 清理函数（组件卸载时取消订阅）
    return () => {
      console.log('[OrderDetail] 🧹 取消订阅实时订单状态更新');
      websocketService.offOrderUpdate(handleOrderUpdate);
    };
  }, [order]); // 依赖 order，订单加载后才订阅

  // ==================== 事件处理 ====================

  /**
   * 打开支付弹窗
   */
  const handleOpenPayModal = () => {
    setShowPayModal(true);
    setSelectedPaymentMethod(null);
  };

  /**
   * 关闭支付弹窗
   */
  const handleClosePayModal = () => {
    setShowPayModal(false);
    setSelectedPaymentMethod(null);
  };

  /**
   * 选择支付方式
   */
  const handleSelectPaymentMethod = (method: PaymentMethod) => {
    setSelectedPaymentMethod(method);
  };

  /**
   * 确认支付
   */
  const handleConfirmPay = async () => {
    if (!selectedPaymentMethod || !order) return;

    setPaying(true);

    try {
      // 🚀 调用真实后端 API 支付订单
      const response = await orderService.payOrder({
        orderNo: order.orderNo,
        paymentMethod: selectedPaymentMethod,
      });

      const payData = response.data;

      // 支付成功后的处理
      toast.success('支付请求已提交！请在新窗口中完成支付。💳');

      // 如果有支付跳转URL，打开新窗口
      if (payData?.payUrl) {
        window.open(payData.payUrl, '_blank');
      }

      // 关闭弹窗
      handleClosePayModal();

      // 轮询查询支付状态
      startPollingPaymentStatus();
    } catch (err: any) {
      console.error('支付失败：', err);
      toast.error(err.response?.data?.message || '支付失败，请稍后重试！😭');
    } finally {
      setPaying(false);
    }
  };

  /**
   * 轮询查询支付状态
   */
  const startPollingPaymentStatus = () => {
    if (!order) return;

    const pollInterval = setInterval(async () => {
      try {
        const response = await orderService.getPaymentStatus(order.orderNo);
        const status = response.data?.status;

        if (status === 'PAID') {
          clearInterval(pollInterval);
          toast.success('支付成功！🎉');
          loadOrderDetail(); // 重新加载订单详情
        } else if (status === 'FAILED') {
          clearInterval(pollInterval);
          toast.error('支付失败，请重试！😭');
        }
      } catch (err) {
        console.error('查询支付状态失败：', err);
      }
    }, 3000); // 每3秒查询一次

    // 60秒后停止轮询
    setTimeout(() => {
      clearInterval(pollInterval);
    }, 60000);
  };

  /**
   * 打开取消订单弹窗
   */
  const handleOpenCancelModal = () => {
    setShowCancelModal(true);
    setCancelReason('');
  };

  /**
   * 关闭取消订单弹窗
   */
  const handleCloseCancelModal = () => {
    setShowCancelModal(false);
    setCancelReason('');
  };

  /**
   * 确认取消订单
   */
  const handleConfirmCancel = async () => {
    if (!order || !cancelReason.trim()) {
      toast.warning('请填写取消原因！😰');
      return;
    }

    setCancelling(true);

    try {
      // 🚀 调用真实后端 API 取消订单
      await orderService.cancelOrder({
        orderNo: order.orderNo,
        reason: cancelReason,
      });

      toast.success('订单已取消！🚫');
      handleCloseCancelModal();
      loadOrderDetail(); // 重新加载订单详情
    } catch (err: any) {
      console.error('取消订单失败：', err);
      toast.error(err.response?.data?.message || '取消订单失败，请稍后重试！😭');
    } finally {
      setCancelling(false);
    }
  };

  /**
   * 确认收货
   */
  const handleConfirmReceipt = async () => {
    if (!order) return;

    if (!window.confirm('确认收到商品了吗？确认后将无法退款！🤔')) {
      return;
    }

    setConfirming(true);

    try {
      // 🚀 调用真实后端 API 确认收货
      await orderService.confirmReceipt({
        orderNo: order.orderNo,
      });

      toast.success('确认收货成功！✅');
      loadOrderDetail(); // 重新加载订单详情
    } catch (err: any) {
      console.error('确认收货失败：', err);
      toast.error(err.response?.data?.message || '确认收货失败，请稍后重试！😭');
    } finally {
      setConfirming(false);
    }
  };

  /**
   * 打开退款弹窗
   */
  const handleOpenRefundModal = () => {
    setShowRefundModal(true);
    setRefundReason('');
  };

  /**
   * 关闭退款弹窗
   */
  const handleCloseRefundModal = () => {
    setShowRefundModal(false);
    setRefundReason('');
  };

  /**
   * 确认申请退款
   */
  const handleConfirmRefund = async () => {
    if (!order || !refundReason.trim()) {
      toast.warning('请填写退款原因！😰');
      return;
    }

    setRefunding(true);

    try {
      // 🚀 调用真实后端 API 申请退款
      await orderService.requestRefund({
        orderNo: order.orderNo,
        reason: refundReason,
        amount: order.amount,
      });

      toast.success('退款申请已提交，请等待审核！🔄');
      handleCloseRefundModal();
      loadOrderDetail(); // 重新加载订单详情
    } catch (err: any) {
      console.error('申请退款失败：', err);
      toast.error(err.response?.data?.message || '申请退款失败，请稍后重试！😭');
    } finally {
      setRefunding(false);
    }
  };

  // ==================== 工具函数 ====================

  /**
   * 格式化价格 - ¥X.XX
   */
  const formatPrice = (price?: number) => {
    if (!price) return '¥0.00';
    // 后端价格单位是分，需要除以100
    return `¥${(price / 100).toFixed(2)}`;
  };

  /**
   * 格式化时间
   */
  const formatTime = (time?: string) => {
    if (!time) return '—';
    return new Date(time).toLocaleString('zh-CN');
  };

  /**
   * 获取订单状态文本
   */
  const getStatusText = (status?: OrderStatus) => {
    switch (status) {
      case 'PENDING_PAYMENT':
        return '待支付';
      case 'PAID':
        return '已支付';
      case 'PENDING_DELIVERY':
        return '待发货';
      case 'PENDING_RECEIPT':
        return '待收货';
      case 'COMPLETED':
        return '已完成';
      case 'CANCELLED':
        return '已取消';
      case 'REFUNDING':
        return '退款中';
      case 'REFUNDED':
        return '已退款';
      default:
        return '未知';
    }
  };

  /**
   * 获取订单状态样式类
   */
  const getStatusClass = (status?: OrderStatus) => {
    switch (status) {
      case 'PENDING_PAYMENT':
        return 'status-pending';
      case 'PAID':
      case 'PENDING_DELIVERY':
      case 'PENDING_RECEIPT':
        return 'status-processing';
      case 'COMPLETED':
        return 'status-completed';
      case 'CANCELLED':
      case 'REFUNDED':
        return 'status-cancelled';
      case 'REFUNDING':
        return 'status-refunding';
      default:
        return '';
    }
  };

  /**
   * 获取支付方式文本
   */
  const getPaymentMethodText = (method?: PaymentMethod | string) => {
    switch (method) {
      case 'WECHAT':
        return '微信支付';
      case 'ALIPAY':
        return '支付宝';
      case 'POINTS':
        return '积分支付';
      default:
        return '—';
    }
  };

  // ==================== 渲染 ====================

  // 加载中状态
  if (loading) {
    return (
      <div className="order-detail-page">
        <div className="order-detail-container">
          <h1 className="order-detail-title">订单详情</h1>
          {/* 使用表单骨架屏模拟订单信息 */}
          <Skeleton type="form" count={6} animation="wave" style={{ marginBottom: '24px' }} />
          {/* 使用卡片骨架屏模拟商品信息 */}
          <Skeleton type="card" animation="wave" />
        </div>
      </div>
    );
  }

  // 错误状态
  if (error || !order) {
    return (
      <div className="order-detail-page">
        <div className="order-detail-error">
          <div className="error-icon">😭</div>
          <h2>{error || '订单不存在'}</h2>
          <button onClick={() => navigate('/orders')} className="btn-back">
            返回订单列表
          </button>
        </div>
      </div>
    );
  }

  // 判断按钮可见性
  const canPay = order.status === 'PENDING_PAYMENT';
  const canCancel = order.status === 'PENDING_PAYMENT' || order.status === 'PAID';
  const canConfirmReceipt = order.status === 'PENDING_RECEIPT';
  const canRefund = order.status === 'PAID' || order.status === 'PENDING_DELIVERY';

  return (
    <div className="order-detail-page">
      <div className="order-detail-container">
        {/* ==================== 订单状态 ==================== */}
        <div className="order-status-section">
          <div className={`status-badge ${getStatusClass(order.status)}`}>
            {getStatusText(order.status)}
          </div>
          <h1 className="order-title">订单详情</h1>
          <p className="order-no">订单号：{order.orderNo}</p>
        </div>

        {/* ==================== 商品信息 ==================== */}
        <div className="order-goods-section">
          <h2 className="section-title">商品信息</h2>
          <div className="goods-card" onClick={() => order.goodsId && navigate(`/goods/${order.goodsId}`)}>
            <div className="goods-image">
              {order.goods?.images?.[0] ? (
                <img src={order.goods.images[0]} alt={order.goods.title} />
              ) : (
                <div className="image-placeholder">📦</div>
              )}
            </div>
            <div className="goods-info">
              <h3 className="goods-title">{order.goods?.title || '未知商品'}</h3>
              <p className="goods-desc">{order.goods?.description || '暂无描述'}</p>
              <div className="goods-price">{formatPrice(order.amount)}</div>
            </div>
          </div>
        </div>

        {/* ==================== 订单信息 ==================== */}
        <div className="order-info-section">
          <h2 className="section-title">订单信息</h2>
          <div className="info-list">
            <div className="info-item">
              <span className="info-label">订单金额：</span>
              <span className="info-value price">{formatPrice(order.amount)}</span>
            </div>
            <div className="info-item">
              <span className="info-label">支付方式：</span>
              <span className="info-value">{getPaymentMethodText(order.paymentMethod)}</span>
            </div>
            <div className="info-item">
              <span className="info-label">创建时间：</span>
              <span className="info-value">{formatTime(order.createdAt)}</span>
            </div>
            <div className="info-item">
              <span className="info-label">支付时间：</span>
              <span className="info-value">{formatTime(order.paidAt)}</span>
            </div>
            <div className="info-item">
              <span className="info-label">完成时间：</span>
              <span className="info-value">{formatTime(order.completedAt)}</span>
            </div>
            {order.cancelledAt && (
              <div className="info-item">
                <span className="info-label">取消时间：</span>
                <span className="info-value">{formatTime(order.cancelledAt)}</span>
              </div>
            )}
          </div>
        </div>

        {/* ==================== 买卖双方信息 ==================== */}
        <div className="users-info-section">
          <h2 className="section-title">买卖双方</h2>
          <div className="users-grid">
            {/* 买家 */}
            <div className="user-card">
              <div className="user-label">买家</div>
              <div className="user-info">
                <div className="user-avatar">👤</div>
                <div className="user-details">
                  <div className="user-name">{order.buyer?.username || '未知用户'}</div>
                  {order.buyer?.phone && (
                    <div className="user-contact">📱 {order.buyer.phone}</div>
                  )}
                </div>
              </div>
            </div>

            {/* 卖家 */}
            <div className="user-card">
              <div className="user-label">卖家</div>
              <div className="user-info">
                <div className="user-avatar">👤</div>
                <div className="user-details">
                  <div className="user-name">{order.seller?.username || '未知用户'}</div>
                  {order.seller?.phone && (
                    <div className="user-contact">📱 {order.seller.phone}</div>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* ==================== 操作按钮 ==================== */}
        <div className="order-actions">
          {canPay && (
            <button className="btn-primary" onClick={handleOpenPayModal}>
              💳 立即支付
            </button>
          )}
          {canConfirmReceipt && (
            <button className="btn-success" onClick={handleConfirmReceipt} disabled={confirming}>
              {confirming ? '⏳ 确认中...' : '✅ 确认收货'}
            </button>
          )}
          {canRefund && (
            <button className="btn-warning" onClick={handleOpenRefundModal}>
              🔄 申请退款
            </button>
          )}
          {canCancel && (
            <button className="btn-danger" onClick={handleOpenCancelModal}>
              🚫 取消订单
            </button>
          )}
          <button className="btn-secondary" onClick={() => navigate('/orders')}>
            📋 我的订单
            </button>
        </div>
      </div>

      {/* ==================== 支付弹窗 ==================== */}
      {showPayModal && (
        <div className="modal-overlay" onClick={handleClosePayModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2 className="modal-title">选择支付方式</h2>
            <div className="payment-methods">
              <div
                className={`payment-method ${selectedPaymentMethod === 'WECHAT' ? 'active' : ''}`}
                onClick={() => handleSelectPaymentMethod('WECHAT' as PaymentMethod)}
              >
                <span className="method-icon">💚</span>
                <span className="method-name">微信支付</span>
              </div>
              <div
                className={`payment-method ${selectedPaymentMethod === 'ALIPAY' ? 'active' : ''}`}
                onClick={() => handleSelectPaymentMethod('ALIPAY' as PaymentMethod)}
              >
                <span className="method-icon">💙</span>
                <span className="method-name">支付宝</span>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn-cancel" onClick={handleClosePayModal}>
                取消
              </button>
              <button
                className="btn-confirm"
                onClick={handleConfirmPay}
                disabled={!selectedPaymentMethod || paying}
              >
                {paying ? '⏳ 支付中...' : '确认支付'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ==================== 取消订单弹窗 ==================== */}
      {showCancelModal && (
        <div className="modal-overlay" onClick={handleCloseCancelModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2 className="modal-title">取消订单</h2>
            <textarea
              className="reason-input"
              placeholder="请输入取消原因..."
              value={cancelReason}
              onChange={(e) => setCancelReason(e.target.value)}
              rows={4}
            />
            <div className="modal-footer">
              <button className="btn-cancel" onClick={handleCloseCancelModal}>
                取消
              </button>
              <button
                className="btn-confirm"
                onClick={handleConfirmCancel}
                disabled={!cancelReason.trim() || cancelling}
              >
                {cancelling ? '⏳ 取消中...' : '确认取消'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ==================== 退款弹窗 ==================== */}
      {showRefundModal && (
        <div className="modal-overlay" onClick={handleCloseRefundModal}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2 className="modal-title">申请退款</h2>
            <textarea
              className="reason-input"
              placeholder="请输入退款原因..."
              value={refundReason}
              onChange={(e) => setRefundReason(e.target.value)}
              rows={4}
            />
            <div className="modal-footer">
              <button className="btn-cancel" onClick={handleCloseRefundModal}>
                取消
              </button>
              <button
                className="btn-confirm"
                onClick={handleConfirmRefund}
                disabled={!refundReason.trim() || refunding}
              >
                {refunding ? '⏳ 提交中...' : '确认申请'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default OrderDetail;
