/**
 * 校区列表页面 - 发现你的校区！🏫
 * @author BaSui 😎
 * @description 校区浏览、搜索、查看详情
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, Input, Skeleton, Badge } from '@campus/shared/components';
import { campusService, CampusStatus, type Campus } from '@campus/shared/services';
import { useNotificationStore } from '../../store';
import './Campuses.css';

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
 * 校区列表页面组件
 */
const Campuses: React.FC = () => {
  const navigate = useNavigate();
  const toast = useNotificationStore();

  // ==================== 状态管理 ====================

  const [campuses, setCampuses] = useState<Campus[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchKeyword, setSearchKeyword] = useState('');

  // ==================== 数据加载 ====================

  /**
   * 加载校区列表
   */
  const loadCampuses = async () => {
    setLoading(true);

    try {
      // ✅ 调用真实 API 获取校区列表
      const response = await campusService.list({
        page: 0,
        size: 100, // 一次性加载所有校区（校区数量通常不多）
      });

      setCampuses(response.content);
    } catch (err: any) {
      console.error('加载校区列表失败:', err);
      toast.error(err.response?.data?.message || '加载校区列表失败！😭');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCampuses();
  }, []);

  // ==================== 事件处理 ====================

  /**
   * 查看校区详情
   */
  const handleViewCampus = (campusId: number) => {
    navigate(`/campuses/${campusId}`);
  };

  /**
   * 搜索过滤
   */
  const filteredCampuses = campuses.filter((campus) => {
    if (!searchKeyword.trim()) return true;

    const keyword = searchKeyword.toLowerCase();
    return (
      campus.name.toLowerCase().includes(keyword) ||
      campus.code.toLowerCase().includes(keyword) ||
      campus.address?.toLowerCase().includes(keyword)
    );
  });

  /**
   * 按状态分组
   */
  const enabledCampuses = filteredCampuses.filter(
    (c) => c.status === CampusStatus.ENABLED
  );
  const disabledCampuses = filteredCampuses.filter(
    (c) => c.status === CampusStatus.DISABLED
  );

  /**
   * 格式化时间
   */
  const formatTime = (time: string) => {
    const date = new Date(time);
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  /**
   * 获取状态配置
   */
  const getStatusConfig = (status: CampusStatus) => {
    return STATUS_CONFIG[status] || STATUS_CONFIG[CampusStatus.ENABLED];
  };

  // ==================== 渲染 ====================

  return (
    <div className="campuses-page">
      <div className="campuses-container">
        {/* ==================== 头部 ==================== */}
        <div className="campuses-header">
          <div className="campuses-header__info">
            <h1 className="campuses-header__title">🏫 校区列表</h1>
            <p className="campuses-header__subtitle">
              选择你所在的校区，发现身边的好物！
            </p>
          </div>
        </div>

        {/* ==================== 搜索栏 ==================== */}
        <div className="campuses-search">
          <Input
            type="text"
            placeholder="🔍 搜索校区名称、代码或地址..."
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
          />
        </div>

        {/* ==================== 统计信息 ==================== */}
        {!loading && (
          <div className="campuses-stats">
            <div className="stat-item">
              <span className="stat-icon">🏫</span>
              <span className="stat-value">{campuses.length}</span>
              <span className="stat-label">总校区数</span>
            </div>
            <div className="stat-item">
              <span className="stat-icon">✅</span>
              <span className="stat-value">{enabledCampuses.length}</span>
              <span className="stat-label">开放中</span>
            </div>
            <div className="stat-item">
              <span className="stat-icon">🔒</span>
              <span className="stat-value">{disabledCampuses.length}</span>
              <span className="stat-label">已关闭</span>
            </div>
          </div>
        )}

        {/* ==================== 校区列表 ==================== */}
        <div className="campuses-content">
          {loading ? (
            <div className="campuses-loading">
              <Skeleton type="card" count={6} animation="wave" />
            </div>
          ) : filteredCampuses.length === 0 ? (
            <div className="campuses-empty">
              <div className="empty-icon">🔍</div>
              <h3 className="empty-text">
                {searchKeyword ? '没有找到相关校区' : '暂无校区'}
              </h3>
              <p className="empty-tip">
                {searchKeyword
                  ? '试试其他关键词吧~'
                  : '校区信息还未添加'}
              </p>
            </div>
          ) : (
            <>
              {/* 开放中的校区 */}
              {enabledCampuses.length > 0 && (
                <div className="campus-section">
                  <h2 className="campus-section__title">
                    ✅ 开放中的校区 ({enabledCampuses.length})
                  </h2>
                  <div className="campuses-grid">
                    {enabledCampuses.map((campus) => (
                      <div
                        key={campus.id}
                        className="campus-card"
                        onClick={() => handleViewCampus(campus.id)}
                      >
                        {/* 校区图标 */}
                        <div className="campus-card__icon">🏫</div>

                        {/* 校区信息 */}
                        <div className="campus-card__content">
                          <div className="campus-card__header">
                            <h3 className="campus-card__name">
                              {campus.name}
                            </h3>
                            <Badge
                              text={getStatusConfig(campus.status).text}
                              color={getStatusConfig(campus.status).color}
                            />
                          </div>

                          <div className="campus-card__code">
                            代码: {campus.code}
                          </div>

                          {campus.address && (
                            <div className="campus-card__address">
                              📍 {campus.address}
                            </div>
                          )}

                          {campus.phone && (
                            <div className="campus-card__phone">
                              📞 {campus.phone}
                            </div>
                          )}

                          <div className="campus-card__time">
                            创建于 {formatTime(campus.createdAt)}
                          </div>
                        </div>

                        {/* 操作按钮 */}
                        <div className="campus-card__actions">
                          <Button
                            type="primary"
                            size="small"
                            onClick={(e) => {
                              e.stopPropagation();
                              handleViewCampus(campus.id);
                            }}
                          >
                            查看详情 →
                          </Button>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* 已关闭的校区 */}
              {disabledCampuses.length > 0 && (
                <div className="campus-section">
                  <h2 className="campus-section__title">
                    🔒 已关闭的校区 ({disabledCampuses.length})
                  </h2>
                  <div className="campuses-grid disabled">
                    {disabledCampuses.map((campus) => (
                      <div
                        key={campus.id}
                        className="campus-card disabled"
                        onClick={() => handleViewCampus(campus.id)}
                      >
                        <div className="campus-card__icon">🔒</div>
                        <div className="campus-card__content">
                          <div className="campus-card__header">
                            <h3 className="campus-card__name">
                              {campus.name}
                            </h3>
                            <Badge
                              text={getStatusConfig(campus.status).text}
                              color={getStatusConfig(campus.status).color}
                            />
                          </div>
                          <div className="campus-card__code">
                            代码: {campus.code}
                          </div>
                          {campus.address && (
                            <div className="campus-card__address">
                              📍 {campus.address}
                            </div>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </>
          )}
        </div>

        {/* ==================== 温馨提示 ==================== */}
        <div className="campuses-tips">
          <h4 className="campuses-tips__title">💡 温馨提示</h4>
          <ul className="campuses-tips__list">
            <li>选择你所在的校区，可以查看该校区的商品和服务</li>
            <li>每个校区都有独立的商品列表和用户社区</li>
            <li>开放中的校区可以正常发布和交易商品</li>
            <li>已关闭的校区暂时无法进行交易活动</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default Campuses;
