/**
 * 黑名单管理页面 🚫
 * @author BaSui 😎
 * @description 管理黑名单用户，支持搜索、解除拉黑、批量操作
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Input, Button, Spin, Checkbox, Empty, Pagination, Modal, message as antMessage } from 'antd';
import { SearchOutlined, ArrowLeftOutlined, UserDeleteOutlined, ExclamationCircleOutlined } from '@ant-design/icons';
import { blacklistService } from '@campus/shared/services';;
import type { BlacklistItem, BlacklistListParams } from '@campus/shared/services';
import './BlacklistSettings.css';

const { confirm } = Modal;

export const BlacklistSettings: React.FC = () => {
  const navigate = useNavigate();

  // ==================== 状态管理 ====================
  const [loading, setLoading] = useState(true);
  const [unblocking, setUnblocking] = useState(false);
  const [blacklist, setBlacklist] = useState<BlacklistItem[]>([]);
  const [total, setTotal] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [selectAll, setSelectAll] = useState(false);

  // ==================== 数据加载 ====================
  useEffect(() => {
    loadBlacklist();
  }, [currentPage, pageSize]);

  const loadBlacklist = async () => {
    setLoading(true);
    try {
      const params: BlacklistListParams = {
        page: currentPage,
        size: pageSize,
        keyword: searchKeyword.trim() || undefined,
      };

      const response = await blacklistService.getBlacklist(params);
      setBlacklist(response.items);
      setTotal(response.total);

      console.log('[BlacklistSettings] ✅ 加载黑名单成功', response);
    } catch (error) {
      console.error('[BlacklistSettings] ❌ 加载失败:', error);
      antMessage.error('加载黑名单失败，请刷新重试');
    } finally {
      setLoading(false);
    }
  };

  // ==================== 搜索处理 ====================
  const handleSearch = () => {
    setCurrentPage(1); // 重置到第一页
    loadBlacklist();
  };

  const handleSearchClear = () => {
    setSearchKeyword('');
    setCurrentPage(1);
    setTimeout(() => loadBlacklist(), 0);
  };

  // ==================== 分页处理 ====================
  const handlePageChange = (page: number, size?: number) => {
    setCurrentPage(page);
    if (size && size !== pageSize) {
      setPageSize(size);
    }
  };

  // ==================== 选择处理 ====================
  const handleSelectItem = (id: number, checked: boolean) => {
    if (checked) {
      setSelectedIds([...selectedIds, id]);
    } else {
      setSelectedIds(selectedIds.filter((selectedId) => selectedId !== id));
    }
  };

  const handleSelectAll = (checked: boolean) => {
    setSelectAll(checked);
    if (checked) {
      const allIds = blacklist.map((item) => item.blockedUserId);
      setSelectedIds(allIds);
    } else {
      setSelectedIds([]);
    }
  };

  // ==================== 解除拉黑 ====================
  const handleUnblock = (item: BlacklistItem) => {
    confirm({
      title: '确认解除拉黑',
      icon: <ExclamationCircleOutlined />,
      content: `确定要将 "${item.blockedUserName}" 从黑名单中移除吗？`,
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        setUnblocking(true);
        try {
          await blacklistService.unblockUser(item.blockedUserId);
          antMessage.success(`已解除拉黑 ${item.blockedUserName}`);

          // 重新加载列表
          await loadBlacklist();

          // 清空选择
          setSelectedIds([]);
          setSelectAll(false);

          console.log('[BlacklistSettings] ✅ 解除拉黑成功', item.blockedUserId);
        } catch (error) {
          console.error('[BlacklistSettings] ❌ 解除拉黑失败:', error);
          antMessage.error('解除拉黑失败，请重试');
        } finally {
          setUnblocking(false);
        }
      },
    });
  };

  // ==================== 批量解除拉黑 ====================
  const handleBatchUnblock = () => {
    if (selectedIds.length === 0) {
      antMessage.warning('请先选择要解除拉黑的用户！');
      return;
    }

    confirm({
      title: '批量解除拉黑',
      icon: <ExclamationCircleOutlined />,
      content: `确定要将 ${selectedIds.length} 个用户从黑名单中移除吗？`,
      okText: '确认',
      cancelText: '取消',
      onOk: async () => {
        setUnblocking(true);
        try {
          await blacklistService.batchUnblock(selectedIds);
          antMessage.success(`已批量解除 ${selectedIds.length} 个用户的拉黑`);

          // 重新加载列表
          await loadBlacklist();

          // 清空选择
          setSelectedIds([]);
          setSelectAll(false);

          console.log('[BlacklistSettings] ✅ 批量解除拉黑成功', selectedIds);
        } catch (error) {
          console.error('[BlacklistSettings] ❌ 批量解除拉黑失败:', error);
          antMessage.error('批量解除拉黑失败，请重试');
        } finally {
          setUnblocking(false);
        }
      },
    });
  };

  // ==================== 返回上一页 ====================
  const handleGoBack = () => {
    navigate('/settings');
  };

  // ==================== 渲染 ====================

  return (
    <div className="blacklist-settings">
      {/* 页面头部 */}
      <div className="blacklist-header">
        <Button
          type="text"
          icon={<ArrowLeftOutlined />}
          onClick={handleGoBack}
          className="back-button"
        >
          返回
        </Button>
        <h1 className="blacklist-title">🚫 黑名单管理</h1>
        <p className="blacklist-subtitle">
          管理被拉黑的用户，拉黑后将屏蔽其消息和内容
        </p>
      </div>

      {/* 工具栏 */}
      <div className="blacklist-toolbar">
        <div className="search-box">
          <Input
            placeholder="搜索用户名..."
            prefix={<SearchOutlined />}
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            onPressEnter={handleSearch}
            allowClear
            onClear={handleSearchClear}
          />
          <Button type="primary" onClick={handleSearch}>
            搜索
          </Button>
        </div>

        {blacklist.length > 0 && (
          <div className="batch-actions">
            <Checkbox checked={selectAll} onChange={(e) => handleSelectAll(e.target.checked)}>
              全选
            </Checkbox>

            {selectedIds.length > 0 && (
              <>
                <span className="selected-count">已选择 {selectedIds.length} 项</span>
                <Button danger onClick={handleBatchUnblock} loading={unblocking}>
                  批量解除拉黑
                </Button>
              </>
            )}
          </div>
        )}
      </div>

      {/* 统计信息 */}
      {!loading && (
        <div className="blacklist-stats">
          <div className="stat-card">
            <UserDeleteOutlined className="stat-icon" />
            <div className="stat-info">
              <span className="stat-label">黑名单总数</span>
              <span className="stat-value">{total}</span>
            </div>
          </div>
        </div>
      )}

      {/* 黑名单列表 */}
      <div className="blacklist-content">
        {loading ? (
          <div className="loading-container">
            <Spin size="large" tip="加载中..." />
          </div>
        ) : blacklist.length === 0 ? (
          <Empty
            image={Empty.PRESENTED_IMAGE_SIMPLE}
            description={
              searchKeyword ? '没有找到匹配的用户' : '黑名单为空，暂无被拉黑的用户'
            }
          >
            {!searchKeyword && (
              <p className="empty-hint">💡 在用户主页可以将骚扰用户加入黑名单</p>
            )}
          </Empty>
        ) : (
          <div className="blacklist-list">
            {blacklist.map((item) => {
              const isSelected = selectedIds.includes(item.blockedUserId);

              return (
                <div
                  key={item.id}
                  className={`blacklist-item ${isSelected ? 'selected' : ''}`}
                >
                  {/* 选择框 */}
                  <Checkbox
                    checked={isSelected}
                    onChange={(e) => handleSelectItem(item.blockedUserId, e.target.checked)}
                    className="item-checkbox"
                  />

                  {/* 用户头像 */}
                  <div className="user-avatar">
                    {item.blockedUserAvatar ? (
                      <img src={item.blockedUserAvatar} alt={item.blockedUserName} />
                    ) : (
                      <div className="avatar-placeholder">
                        {item.blockedUserName.charAt(0).toUpperCase()}
                      </div>
                    )}
                  </div>

                  {/* 用户信息 */}
                  <div className="user-info">
                    <h3 className="user-name">{item.blockedUserName}</h3>
                    <div className="user-meta">
                      <span className="blocked-time">
                        拉黑时间：{new Date(item.createdAt).toLocaleString('zh-CN')}
                      </span>
                      {item.reason && (
                        <span className="blocked-reason">原因：{item.reason}</span>
                      )}
                    </div>
                  </div>

                  {/* 操作按钮 */}
                  <Button
                    type="default"
                    danger
                    onClick={() => handleUnblock(item)}
                    loading={unblocking}
                  >
                    解除拉黑
                  </Button>
                </div>
              );
            })}
          </div>
        )}

        {/* 分页 */}
        {!loading && blacklist.length > 0 && (
          <div className="blacklist-pagination">
            <Pagination
              current={currentPage}
              pageSize={pageSize}
              total={total}
              onChange={handlePageChange}
              showSizeChanger
              showTotal={(total) => `共 ${total} 条`}
              pageSizeOptions={['10', '20', '50', '100']}
            />
          </div>
        )}
      </div>

      {/* 底部提示 */}
      <div className="blacklist-footer">
        <p className="footer-hint">
          💡 提示：拉黑用户后，您将无法收到其消息和查看其发布的内容
        </p>
      </div>
    </div>
  );
};

export default BlacklistSettings;
