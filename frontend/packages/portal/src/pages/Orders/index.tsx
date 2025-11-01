/**
 * 订单列表页 - 我的买入/卖出订单管理！📋
 * @author BaSui 😎
 * @description 分页展示订单列表、状态筛选、Tab切换
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Skeleton } from '@campus/shared/components';
import { orderService } from '@campus/shared/services/order';
import type { Order, OrderStatus, PageInfo } from '@campus/shared/types';
import './Orders.css';

/**
 * Tab 类型
 */
type OrderTab = 'buyer' | 'seller';

/**
 * 订单列表页组件
 */
const Orders: React.FC = () => {
  const navigate = useNavigate();

  // ==================== 状态管理 ====================

  const [activeTab, setActiveTab] = useState<OrderTab>('buyer'); // 当前 Tab（买入/卖出）
  const [orderList, setOrderList] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 分页状态
  const [page, setPage] = useState(0);
  const [pageSize] = useState(10);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [hasMore, setHasMore] = useState(false);

  // 筛选状态
  const [filterStatus, setFilterStatus] = useState<OrderStatus | 'ALL'>('ALL');
  const [keyword, setKeyword] = useState('');

  // ==================== 数据加载 ====================

  /**
   * 加载订单列表（使用真实后端 API！）
   */
  const loadOrderList = async (isLoadMore = false) => {
    if (isLoadMore) {
      setLoadingMore(true);
    } else {
      setLoading(true);
    }
    setError(null);

    try {
      const currentPage = isLoadMore ? page + 1 : 0;

      // 构建查询参数
      const params: any = {
        page: currentPage,
        pageSize,
      };

      if (filterStatus !== 'ALL') {
        params.status = filterStatus;
      }

      if (keyword.trim()) {
        params.keyword = keyword.trim();
      }

      // 🚀 调用真实后端 API 获取订单列表
      let response: any;
      if (activeTab === 'buyer') {
        response = await orderService.getBuyerOrders(params);
      } else {
        response = await orderService.getSellerOrders(params);
      }

      const pageData: PageInfo<Order> = response.data;
      const newOrders = pageData.content || [];

      if (isLoadMore) {
        setOrderList((prev) => [...prev, ...newOrders]);
        setPage(currentPage);
      } else {
        setOrderList(newOrders);
        setPage(0);
      }

      setTotalPages(pageData.totalPages || 0);
      setTotalElements(pageData.totalElements || 0);
      setHasMore(!pageData.last);
    } catch (err: any) {
      console.error('加载订单列表失败：', err);
      setError(err.response?.data?.message || '加载订单列表失败，请稍后重试！😭');
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  };

  // 初始加载
  useEffect(() => {
    loadOrderList();
  }, [activeTab, filterStatus]);

  // ==================== 事件处理 ====================

  /**
   * 切换 Tab
   */
  const handleTabChange = (tab: OrderTab) => {
    if (tab === activeTab) return;
    setActiveTab(tab);
    setOrderList([]);
    setPage(0);
    setFilterStatus('ALL');
    setKeyword('');
  };

  /**
   * 切换筛选状态
   */
  const handleFilterChange = (status: OrderStatus | 'ALL') => {
    setFilterStatus(status);
    setOrderList([]);
    setPage(0);
  };

  /**
   * 搜索关键词
   */
  const handleSearch = () => {
    setOrderList([]);
    setPage(0);
    loadOrderList();
  };

  /**
   * 加载更多
   */
  const handleLoadMore = () => {
    if (loadingMore || !hasMore) return;
    loadOrderList(true);
  };

  /**
   * 查看订单详情
   */
  const handleViewOrder = (orderNo: string) => {
    navigate(`/orders/${orderNo}`);
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

    const date = new Date(time);
    const now = new Date();
    const diff = now.getTime() - date.getTime();

    const minutes = Math.floor(diff / 1000 / 60);
    const hours = Math.floor(diff / 1000 / 60 / 60);
    const days = Math.floor(diff / 1000 / 60 / 60 / 24);

    if (minutes < 1) return '刚刚';
    if (minutes < 60) return `${minutes}分钟前`;
    if (hours < 24) return `${hours}小时前`;
    if (days < 30) return `${days}天前`;

    return date.toLocaleDateString('zh-CN');
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

  // ==================== 渲染 ====================

  // 加载中状态
  if (loading && orderList.length === 0) {
    return (
      <div className="orders-page">
        <div className="orders-container">
          <h1 className="orders-page-title">我的订单</h1>
          {/* 使用列表骨架屏 */}
          <Skeleton type="list" count={5} animation="wave" />
        </div>
      </div>
    );
  }

  return (
    <div className="orders-page">
      <div className="orders-container">
        {/* ==================== 页面标题 ==================== */}
        <h1 className="orders-page-title">我的订单</h1>

        {/* ==================== Tab 切换 ==================== */}
        <div className="orders-tabs">
          <div
            className={`tab-item ${activeTab === 'buyer' ? 'active' : ''}`}
            onClick={() => handleTabChange('buyer')}
          >
            🛒 我买到的
          </div>
          <div
            className={`tab-item ${activeTab === 'seller' ? 'active' : ''}`}
            onClick={() => handleTabChange('seller')}
          >
            💰 我卖出的
          </div>
        </div>

        {/* ==================== 筛选栏 ==================== */}
        <div className="orders-filter">
          <div className="filter-status">
            <button
              className={`status-btn ${filterStatus === 'ALL' ? 'active' : ''}`}
              onClick={() => handleFilterChange('ALL')}
            >
              全部
            </button>
            <button
              className={`status-btn ${filterStatus === 'PENDING_PAYMENT' ? 'active' : ''}`}
              onClick={() => handleFilterChange('PENDING_PAYMENT')}
            >
              待支付
            </button>
            <button
              className={`status-btn ${filterStatus === 'PAID' ? 'active' : ''}`}
              onClick={() => handleFilterChange('PAID')}
            >
              已支付
            </button>
            <button
              className={`status-btn ${filterStatus === 'PENDING_RECEIPT' ? 'active' : ''}`}
              onClick={() => handleFilterChange('PENDING_RECEIPT')}
            >
              待收货
            </button>
            <button
              className={`status-btn ${filterStatus === 'COMPLETED' ? 'active' : ''}`}
              onClick={() => handleFilterChange('COMPLETED')}
            >
              已完成
            </button>
            <button
              className={`status-btn ${filterStatus === 'CANCELLED' ? 'active' : ''}`}
              onClick={() => handleFilterChange('CANCELLED')}
            >
              已取消
            </button>
          </div>

          <div className="filter-search">
            <input
              type="text"
              className="search-input"
              placeholder="搜索订单号或商品名称..."
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
            />
            <button className="search-btn" onClick={handleSearch}>
              🔍 搜索
            </button>
          </div>
        </div>

        {/* ==================== 订单列表 ==================== */}
        {error ? (
          <div className="orders-error">
            <div className="error-icon">😭</div>
            <p>{error}</p>
            <button className="btn-retry" onClick={() => loadOrderList()}>
              重试
            </button>
          </div>
        ) : orderList.length === 0 ? (
          <div className="orders-empty">
            <div className="empty-icon">📦</div>
            <p className="empty-text">暂无订单</p>
            <p className="empty-tip">快去逛逛，淘点好货吧！</p>
            <button className="btn-go-home" onClick={() => navigate('/')}>
              去首页逛逛
            </button>
          </div>
        ) : (
          <>
            <div className="orders-list">
              {orderList.map((order) => (
                <div
                  key={order.id}
                  className="order-card"
                  onClick={() => handleViewOrder(order.orderNo)}
                >
                  {/* 订单头部 */}
                  <div className="order-header">
                    <div className="order-no">订单号：{order.orderNo}</div>
                    <div className={`order-status ${getStatusClass(order.status)}`}>
                      {getStatusText(order.status)}
                    </div>
                  </div>

                  {/* 商品信息 */}
                  <div className="order-body">
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
                      <div className="order-meta">
                        <span className="order-time">{formatTime(order.createdAt)}</span>
                        {activeTab === 'buyer' && order.seller?.username && (
                          <span className="order-user">卖家：{order.seller.username}</span>
                        )}
                        {activeTab === 'seller' && order.buyer?.username && (
                          <span className="order-user">买家：{order.buyer.username}</span>
                        )}
                      </div>
                    </div>
                    <div className="order-price">{formatPrice(order.amount)}</div>
                  </div>

                  {/* 操作按钮 */}
                  <div className="order-footer">
                    <button
                      className="btn-view-detail"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleViewOrder(order.orderNo);
                      }}
                    >
                      查看详情
                    </button>
                  </div>
                </div>
              ))}
            </div>

            {/* 加载更多 */}
            {hasMore && (
              <div className="orders-load-more">
                <button
                  className="btn-load-more"
                  onClick={handleLoadMore}
                  disabled={loadingMore}
                >
                  {loadingMore ? '⏳ 加载中...' : '加载更多'}
                </button>
              </div>
            )}

            {/* 分页信息 */}
            <div className="orders-pagination-info">
              已加载 <span className="count">{orderList.length}</span> / 共{' '}
              <span className="count">{totalElements}</span> 条订单
              {!hasMore && orderList.length > 0 && <span className="all-loaded"> · 已全部加载</span>}
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default Orders;
