/**
 * 积分中心页 - 查看积分、签到、积分流水！💰
 * @author BaSui 😎
 * @description 完整的积分系统功能页面
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Skeleton, Pagination } from '@campus/shared/components';
import { userService } from '@campus/shared/services/user';
import { useAuthStore, useNotificationStore } from '../../store';
import type { User } from '@campus/shared/types';
import './Points.css';

/**
 * 积分流水类型
 */
interface PointsLog {
  id: number;
  type: string;
  points: number;
  balance: number;
  description: string;
  createdAt: string;
}

/**
 * 积分中心页组件
 */
const Points: React.FC = () => {
  const navigate = useNavigate();
  const { user: currentUser } = useAuthStore();
  const toast = useNotificationStore();

  // ==================== 状态管理 ====================

  const [loading, setLoading] = useState(true);
  const [signingIn, setSigningIn] = useState(false);
  const [currentPoints, setCurrentPoints] = useState(0);
  const [pointsLogs, setPointsLogs] = useState<PointsLog[]>([]);
  const [page, setPage] = useState(1);
  const [pageSize] = useState(20);
  const [total, setTotal] = useState(0);
  const [hasSignedToday, setHasSignedToday] = useState(false);
  const [consecutiveDays, setConsecutiveDays] = useState(0);

  // ==================== 数据加载 ====================

  /**
   * 加载积分信息
   */
  const loadPoints = async () => {
    setLoading(true);

    try {
      // 🚀 调用真实后端 API 获取当前用户资料（包含积分）
      const userData: User = await userService.getCurrentUser();
      setCurrentPoints(userData.points || 0);

      // 加载积分流水
      await loadPointsLogs();
    } catch (err: any) {
      console.error('加载积分信息失败：', err);
      toast.error(err.response?.data?.message || '加载积分信息失败！😭');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 加载积分流水
   */
  const loadPointsLogs = async () => {
    try {
      // 🚀 调用真实后端 API 获取积分流水
      const response = await userService.getPointsLogs({
        page: page - 1, // 后端从 0 开始
        pageSize,
      });

      setPointsLogs(response.content || []);
      setTotal(response.totalElements || 0);
    } catch (err: any) {
      console.error('加载积分流水失败：', err);
      toast.error(err.response?.data?.message || '加载积分流水失败！😭');
    }
  };

  /**
   * 签到
   */
  const handleSignIn = async () => {
    if (hasSignedToday) {
      toast.warning('今天已经签到过了哦！明天再来吧！😊');
      return;
    }

    setSigningIn(true);

    try {
      // 🚀 调用真实后端 API 签到
      const result = await userService.signIn();

      toast.success(`签到成功！获得 ${result.points} 积分 🎉`);
      setHasSignedToday(true);
      setConsecutiveDays((prev) => prev + 1);

      // 刷新积分信息
      loadPoints();
    } catch (err: any) {
      console.error('签到失败：', err);
      toast.error(err.response?.data?.message || '签到失败！😭');
    } finally {
      setSigningIn(false);
    }
  };

  /**
   * 翻页
   */
  const handlePageChange = (newPage: number) => {
    setPage(newPage);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  /**
   * 获取积分类型显示文本
   */
  const getPointsTypeText = (type: string): string => {
    const typeMap: Record<string, string> = {
      REGISTER: '注册奖励',
      LOGIN: '登录奖励',
      SIGN_IN: '签到奖励',
      PUBLISH: '发布商品',
      TRADE: '交易完成',
      CONSUME: '积分消费',
      ADMIN_ADJUST: '管理员调整',
    };
    return typeMap[type] || type;
  };

  /**
   * 获取积分类型图标
   */
  const getPointsTypeIcon = (type: string): string => {
    const iconMap: Record<string, string> = {
      REGISTER: '🎁',
      LOGIN: '🔑',
      SIGN_IN: '✅',
      PUBLISH: '📦',
      TRADE: '💰',
      CONSUME: '💸',
      ADMIN_ADJUST: '⚙️',
    };
    return iconMap[type] || '💰';
  };

  // ==================== 生命周期 ====================

  useEffect(() => {
    loadPoints();
  }, []);

  useEffect(() => {
    if (page > 1) {
      loadPointsLogs();
    }
  }, [page]);

  // ==================== 渲染 ====================

  // 加载中
  if (loading) {
    return (
      <div className="points-page">
        <div className="points-container">
          <Skeleton type="card" count={1} animation="wave" />
          <Skeleton type="list" count={10} animation="wave" />
        </div>
      </div>
    );
  }

  return (
    <div className="points-page">
      <div className="points-container">
        <h1 className="points-title">💰 积分中心</h1>

        {/* ==================== 积分概览卡片 ==================== */}
        <div className="points-overview">
          {/* 当前积分 */}
          <div className="points-card">
            <div className="points-card__icon">💎</div>
            <div className="points-card__content">
              <div className="points-card__label">当前积分</div>
              <div className="points-card__value">{currentPoints}</div>
            </div>
          </div>

          {/* 签到卡片 */}
          <div className="points-card points-card--sign-in">
            <div className="sign-in-content">
              <div className="sign-in-info">
                <div className="sign-in-title">每日签到</div>
                <div className="sign-in-desc">
                  {hasSignedToday ? (
                    <>✅ 今日已签到</>
                  ) : (
                    <>签到可获得积分奖励</>
                  )}
                </div>
                {consecutiveDays > 0 && (
                  <div className="sign-in-days">
                    🔥 连续签到 {consecutiveDays} 天
                  </div>
                )}
              </div>
              <Button
                type={hasSignedToday ? 'default' : 'primary'}
                size="large"
                onClick={handleSignIn}
                loading={signingIn}
                disabled={hasSignedToday}
              >
                {hasSignedToday ? '已签到' : '立即签到'}
              </Button>
            </div>
          </div>
        </div>

        {/* ==================== 积分规则说明 ==================== */}
        <div className="points-rules">
          <h2 className="points-rules__title">📋 积分规则</h2>
          <div className="points-rules__content">
            <div className="rule-item">
              <span className="rule-icon">🎁</span>
              <span className="rule-text">注册账号：+100 积分</span>
            </div>
            <div className="rule-item">
              <span className="rule-icon">✅</span>
              <span className="rule-text">每日签到：+5 积分</span>
            </div>
            <div className="rule-item">
              <span className="rule-icon">📦</span>
              <span className="rule-text">发布商品：+10 积分</span>
            </div>
            <div className="rule-item">
              <span className="rule-icon">💰</span>
              <span className="rule-text">交易完成：+20 积分</span>
            </div>
          </div>
        </div>

        {/* ==================== 积分流水列表 ==================== */}
        <div className="points-logs">
          <h2 className="points-logs__title">📊 积分流水</h2>

          {pointsLogs.length === 0 ? (
            <div className="points-logs__empty">
              <div className="empty-icon">📭</div>
              <p className="empty-text">暂无积分流水记录</p>
              <p className="empty-tip">快去签到或发布商品获得积分吧！</p>
            </div>
          ) : (
            <>
              <div className="points-logs__list">
                {pointsLogs.map((log) => (
                  <div key={log.id} className="log-item">
                    <div className="log-item__icon">
                      {getPointsTypeIcon(log.type)}
                    </div>
                    <div className="log-item__content">
                      <div className="log-item__title">
                        {getPointsTypeText(log.type)}
                      </div>
                      <div className="log-item__desc">{log.description}</div>
                      <div className="log-item__time">
                        {new Date(log.createdAt).toLocaleString('zh-CN')}
                      </div>
                    </div>
                    <div
                      className={`log-item__points ${
                        log.points > 0 ? 'log-item__points--positive' : 'log-item__points--negative'
                      }`}
                    >
                      {log.points > 0 ? '+' : ''}
                      {log.points}
                    </div>
                    <div className="log-item__balance">
                      余额：{log.balance}
                    </div>
                  </div>
                ))}
              </div>

              {/* 分页 */}
              {total > pageSize && (
                <div className="points-logs__pagination">
                  <Pagination
                    current={page}
                    pageSize={pageSize}
                    total={total}
                    onChange={handlePageChange}
                    showSizeChanger={false}
                    showQuickJumper={false}
                    showTotal={true}
                  />
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default Points;
