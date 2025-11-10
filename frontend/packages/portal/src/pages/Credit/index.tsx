/**
 * 信用展示页面 - 展示你的信用成就！⭐
 * @author BaSui 😎
 * @description 用户信用分、等级、历史记录展示
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Skeleton } from '@campus/shared/components';
import { creditService, CREDIT_LEVEL_CONFIG } from '../../services';
import type { UserCreditInfo, CreditHistoryItem } from '../../services/credit';
import { useNotificationStore } from '../../store';
import './Credit.css';

/**
 * 信用页面组件
 */
const Credit: React.FC = () => {
  const navigate = useNavigate();
  const toast = useNotificationStore();

  // ==================== 状态管理 ====================

  const [creditInfo, setCreditInfo] = useState<UserCreditInfo | null>(null);
  const [history, setHistory] = useState<CreditHistoryItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [historyLoading, setHistoryLoading] = useState(false);

  // ==================== 数据加载 ====================

  /**
   * 加载信用信息
   */
  const loadCreditInfo = async () => {
    setLoading(true);

    try {
      const data = await creditService.getMyCredit();
      setCreditInfo(data);
    } catch (err: any) {
      console.error('加载信用信息失败:', err);
      toast.error(err.response?.data?.message || '加载信用信息失败!😭');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 加载信用历史
   */
  const loadHistory = async () => {
    setHistoryLoading(true);

    try {
      const response = await creditService.getCreditHistory(0, 20);
      setHistory(response.content);
    } catch (err: any) {
      console.error('加载信用历史失败:', err);
      // 不显示错误提示（可能后端未实现）
    } finally {
      setHistoryLoading(false);
    }
  };

  useEffect(() => {
    loadCreditInfo();
    loadHistory();
  }, []);

  // ==================== 工具函数 ====================

  /**
   * 格式化时间
   */
  const formatTime = (time: string) => {
    const date = new Date(time);
    return date.toLocaleString('zh-CN');
  };

  /**
   * 获取信用变动图标
   */
  const getChangeIcon = (changeValue: number) => {
    if (changeValue > 0) return '⬆️';
    if (changeValue < 0) return '⬇️';
    return '➡️';
  };

  /**
   * 获取信用变动颜色
   */
  const getChangeColor = (changeValue: number) => {
    if (changeValue > 0) return '#52c41a';
    if (changeValue < 0) return '#f5222d';
    return '#8c8c8c';
  };

  // ==================== 渲染 ====================

  if (loading) {
    return (
      <div className="credit-page">
        <div className="credit-container">
          <Skeleton type="card" count={3} animation="wave" />
        </div>
      </div>
    );
  }

  if (!creditInfo) {
    return (
      <div className="credit-page">
        <div className="credit-container">
          <div className="credit-error">
            <div className="error-icon">⚠️</div>
            <h3 className="error-text">加载失败</h3>
            <Button type="primary" size="large" onClick={loadCreditInfo}>
              重试
            </Button>
          </div>
        </div>
      </div>
    );
  }

  const { currentLevelInfo, nextLevelInfo, progressToNextLevel } = creditInfo;

  return (
    <div className="credit-page">
      <div className="credit-container">
        {/* ==================== 头部 ==================== */}
        <div className="credit-header">
          <h1 className="credit-header__title">⭐ 我的信用</h1>
          <p className="credit-header__subtitle">
            信用是你在平台的宝贵资产
          </p>
        </div>

        {/* ==================== 信用概览 ==================== */}
        <div className="credit-overview">
          {/* 信用分卡片 */}
          <div className="credit-score-card">
            <div className="score-circle">
              <svg className="score-progress" viewBox="0 0 200 200">
                <circle
                  className="score-progress-bg"
                  cx="100"
                  cy="100"
                  r="85"
                  fill="none"
                  stroke="#e0e0e0"
                  strokeWidth="10"
                />
                <circle
                  className="score-progress-fill"
                  cx="100"
                  cy="100"
                  r="85"
                  fill="none"
                  stroke={creditService.getCreditScoreColor(creditInfo.creditScore)}
                  strokeWidth="10"
                  strokeDasharray={`${(creditInfo.creditScore / 200) * 534} 534`}
                  transform="rotate(-90 100 100)"
                />
              </svg>
              <div className="score-content">
                <div className="score-value">{creditInfo.creditScore}</div>
                <div className="score-max">/200</div>
              </div>
            </div>
            <div className="score-info">
              <h3 className="score-grade">
                {creditService.getCreditScoreGrade(creditInfo.creditScore)}
              </h3>
              <p className="score-tip">信用评级</p>
            </div>
          </div>

          {/* 信用等级卡片 */}
          <div className="credit-level-card">
            <div className="level-badge" style={{ backgroundColor: currentLevelInfo.color }}>
              <span className="level-icon">{currentLevelInfo.icon}</span>
              <span className="level-name">{currentLevelInfo.levelName}</span>
            </div>
            <div className="level-description">{currentLevelInfo.description}</div>
            
            {nextLevelInfo && (
              <div className="level-progress">
                <div className="progress-header">
                  <span>距离 {nextLevelInfo.icon} {nextLevelInfo.levelName}</span>
                  <span>{Math.round(progressToNextLevel * 100)}%</span>
                </div>
                <div className="progress-bar">
                  <div 
                    className="progress-fill" 
                    style={{ 
                      width: `${progressToNextLevel * 100}%`,
                      background: `linear-gradient(90deg, ${currentLevelInfo.color}, ${nextLevelInfo.color})`
                    }}
                  />
                </div>
                <div className="progress-tip">
                  还需完成 {nextLevelInfo.minOrders - creditInfo.orderCount} 单交易
                </div>
              </div>
            )}
          </div>
        </div>

        {/* ==================== 信用明细 ==================== */}
        <div className="credit-details">
          <h2 className="section-title">📊 信用明细</h2>
          <div className="details-grid">
            <div className="detail-item">
              <div className="detail-icon">📦</div>
              <div className="detail-content">
                <div className="detail-label">完成订单</div>
                <div className="detail-value">{creditInfo.orderCount} 单</div>
              </div>
            </div>
            <div className="detail-item">
              <div className="detail-icon">⭐</div>
              <div className="detail-content">
                <div className="detail-label">好评率</div>
                <div className="detail-value">{(creditInfo.positiveRate * 100).toFixed(1)}%</div>
              </div>
            </div>
            <div className="detail-item">
              <div className="detail-icon">⚡</div>
              <div className="detail-content">
                <div className="detail-label">平均响应</div>
                <div className="detail-value">
                  {creditInfo.avgResponseTime 
                    ? creditService.formatResponseTime(creditInfo.avgResponseTime)
                    : '暂无数据'}
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* ==================== 信用历史 ==================== */}
        <div className="credit-history">
          <h2 className="section-title">📜 信用历史</h2>
          {historyLoading ? (
            <Skeleton type="list" count={5} animation="wave" />
          ) : history.length === 0 ? (
            <div className="history-empty">
              <div className="empty-icon">📭</div>
              <p className="empty-text">暂无信用历史记录</p>
            </div>
          ) : (
            <div className="history-list">
              {history.map((item) => (
                <div key={item.id} className="history-item">
                  <div className="history-icon">
                    {getChangeIcon(item.changeValue)}
                  </div>
                  <div className="history-content">
                    <div className="history-type">{item.changeType}</div>
                    <div className="history-reason">{item.reason}</div>
                    <div className="history-time">{formatTime(item.createdAt)}</div>
                  </div>
                  <div 
                    className="history-value" 
                    style={{ color: getChangeColor(item.changeValue) }}
                  >
                    {item.changeValue > 0 ? '+' : ''}{item.changeValue}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* ==================== 提升信用建议 ==================== */}
        <div className="credit-tips">
          <h2 className="section-title">💡 提升信用建议</h2>
          <div className="tips-list">
            <div className="tip-item">
              <div className="tip-icon">✅</div>
              <div className="tip-content">
                <h4>按时交易</h4>
                <p>及时发货、按时收货，维护良好交易记录</p>
              </div>
            </div>
            <div className="tip-item">
              <div className="tip-icon">⭐</div>
              <div className="tip-content">
                <h4>获得好评</h4>
                <p>提供优质商品和服务，获得买家好评</p>
              </div>
            </div>
            <div className="tip-item">
              <div className="tip-icon">💬</div>
              <div className="tip-content">
                <h4>快速响应</h4>
                <p>及时回复买家消息，提供良好沟通体验</p>
              </div>
            </div>
            <div className="tip-item">
              <div className="tip-icon">🚫</div>
              <div className="tip-content">
                <h4>避免违规</h4>
                <p>遵守平台规则，避免违规行为</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Credit;
