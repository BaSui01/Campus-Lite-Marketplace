/**
 * 通知类型管理页面 📋
 * @author BaSui 😎
 * @description 管理订阅的通知类型，支持批量操作
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Switch, Button, Input, Spin, Checkbox, message as antMessage } from 'antd';
import { SearchOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { notificationPreferenceService, NotificationChannel, NotificationType,  } from '@campus/shared/services';;
import type { NotificationPreference, NotificationTypeInfo } from '@campus/shared/services';
import './NotificationTypes.css';

export const NotificationTypes: React.FC = () => {
  const navigate = useNavigate();
  
  // ==================== 状态管理 ====================
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [preferences, setPreferences] = useState<NotificationPreference | null>(null);
  const [searchText, setSearchText] = useState('');
  const [selectedTypes, setSelectedTypes] = useState<NotificationType[]>([]);
  const [selectAll, setSelectAll] = useState(false);

  // 所有通知类型
  const allTypes = notificationPreferenceService.getNotificationTypes();

  // ==================== 数据加载 ====================
  useEffect(() => {
    loadPreferences();
  }, []);

  const loadPreferences = async () => {
    setLoading(true);
    try {
      const data = await notificationPreferenceService.getStatus();
      setPreferences(data);
      console.log('[NotificationTypes] ✅ 加载偏好成功', data);
    } catch (error) {
      console.error('[NotificationTypes] ❌ 加载失败:', error);
      antMessage.error('加载失败，请刷新重试');
    } finally {
      setLoading(false);
    }
  };

  // ==================== 过滤逻辑 ====================
  const filteredTypes = allTypes.filter((type) => {
    if (!searchText.trim()) return true;
    const keyword = searchText.toLowerCase();
    return (
      type.name.toLowerCase().includes(keyword) ||
      type.description.toLowerCase().includes(keyword)
    );
  });

  // ==================== 订阅状态检查 ====================
  const isSubscribed = (type: NotificationType): boolean => {
    if (!preferences) return true;
    return !preferences.unsubscribedTypes.includes(type);
  };

  // ==================== 事件处理 ====================

  /**
   * 切换单个通知类型
   */
  const handleToggleType = async (typeInfo: NotificationTypeInfo) => {
    if (!typeInfo.canUnsubscribe) {
      antMessage.warning(`${typeInfo.name}不可退订，这是重要通知！`);
      return;
    }

    const currentlySubscribed = isSubscribed(typeInfo.type);
    const newSubscribed = !currentlySubscribed;

    try {
      if (newSubscribed) {
        // 重新订阅
        await notificationPreferenceService.resubscribe(
          NotificationChannel.IN_APP,
          typeInfo.type
        );
        antMessage.success(`已开启 ${typeInfo.name}`);
      } else {
        // 退订
        await notificationPreferenceService.unsubscribe(
          NotificationChannel.IN_APP,
          typeInfo.type
        );
        antMessage.success(`已关闭 ${typeInfo.name}`);
      }

      // 重新加载偏好
      await loadPreferences();
      
      console.log(`[NotificationTypes] ✅ ${typeInfo.name} 已${newSubscribed ? '订阅' : '退订'}`);
    } catch (error) {
      console.error('[NotificationTypes] ❌ 切换失败:', error);
      antMessage.error('操作失败，请重试');
    }
  };

  /**
   * 选中/取消选中通知类型
   */
  const handleSelectType = (type: NotificationType, checked: boolean) => {
    if (checked) {
      setSelectedTypes([...selectedTypes, type]);
    } else {
      setSelectedTypes(selectedTypes.filter((t) => t !== type));
    }
  };

  /**
   * 全选/全不选
   */
  const handleSelectAll = (checked: boolean) => {
    setSelectAll(checked);
    if (checked) {
      // 只选择可退订的类型
      const subscribableTypes = filteredTypes
        .filter((t) => t.canUnsubscribe)
        .map((t) => t.type);
      setSelectedTypes(subscribableTypes);
    } else {
      setSelectedTypes([]);
    }
  };

  /**
   * 批量订阅
   */
  const handleBatchSubscribe = async () => {
    if (selectedTypes.length === 0) {
      antMessage.warning('请先选择要订阅的类型！');
      return;
    }

    setSaving(true);
    try {
      await notificationPreferenceService.batchResubscribe(
        NotificationChannel.IN_APP,
        selectedTypes
      );

      antMessage.success(`已批量订阅 ${selectedTypes.length} 个通知类型！`);
      
      // 重新加载
      await loadPreferences();
      
      // 清空选择
      setSelectedTypes([]);
      setSelectAll(false);
      
      console.log('[NotificationTypes] ✅ 批量订阅成功', selectedTypes);
    } catch (error) {
      console.error('[NotificationTypes] ❌ 批量订阅失败:', error);
      antMessage.error('批量订阅失败，请重试');
    } finally {
      setSaving(false);
    }
  };

  /**
   * 批量退订
   */
  const handleBatchUnsubscribe = async () => {
    if (selectedTypes.length === 0) {
      antMessage.warning('请先选择要退订的类型！');
      return;
    }

    setSaving(true);
    try {
      await notificationPreferenceService.batchUnsubscribe(
        NotificationChannel.IN_APP,
        selectedTypes
      );

      antMessage.success(`已批量退订 ${selectedTypes.length} 个通知类型！`);
      
      // 重新加载
      await loadPreferences();
      
      // 清空选择
      setSelectedTypes([]);
      setSelectAll(false);
      
      console.log('[NotificationTypes] ✅ 批量退订成功', selectedTypes);
    } catch (error) {
      console.error('[NotificationTypes] ❌ 批量退订失败:', error);
      antMessage.error('批量退订失败，请重试');
    } finally {
      setSaving(false);
    }
  };

  /**
   * 返回上一页
   */
  const handleGoBack = () => {
    navigate('/settings/notifications');
  };

  // ==================== 渲染 ====================

  if (loading) {
    return (
      <div className="notification-types loading-container">
        <Spin size="large" tip="加载中..." />
      </div>
    );
  }

  return (
    <div className="notification-types">
      {/* 页面头部 */}
      <div className="types-header">
        <Button
          type="text"
          icon={<ArrowLeftOutlined />}
          onClick={handleGoBack}
          className="back-button"
        >
          返回
        </Button>
        <h1 className="types-title">📋 通知类型管理</h1>
        <p className="types-subtitle">选择你想接收的通知类型</p>
      </div>

      {/* 搜索和批量操作 */}
      <div className="types-toolbar">
        <div className="search-box">
          <Input
            placeholder="搜索通知类型..."
            prefix={<SearchOutlined />}
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            allowClear
          />
        </div>

        <div className="batch-actions">
          <Checkbox
            checked={selectAll}
            onChange={(e) => handleSelectAll(e.target.checked)}
          >
            全选
          </Checkbox>
          
          {selectedTypes.length > 0 && (
            <>
              <span className="selected-count">
                已选择 {selectedTypes.length} 项
              </span>
              <Button
                type="primary"
                onClick={handleBatchSubscribe}
                loading={saving}
                size="small"
              >
                批量订阅
              </Button>
              <Button
                danger
                onClick={handleBatchUnsubscribe}
                loading={saving}
                size="small"
              >
                批量退订
              </Button>
            </>
          )}
        </div>
      </div>

      {/* 通知类型列表 */}
      <div className="types-list">
        {filteredTypes.length === 0 ? (
          <div className="empty-state">
            <p>😕 没有找到匹配的通知类型</p>
          </div>
        ) : (
          filteredTypes.map((typeInfo) => {
            const subscribed = isSubscribed(typeInfo.type);
            const isSelected = selectedTypes.includes(typeInfo.type);

            return (
              <div
                key={typeInfo.type}
                className={`type-item ${!typeInfo.canUnsubscribe ? 'required' : ''} ${
                  isSelected ? 'selected' : ''
                }`}
              >
                {/* 选择框（仅可退订的显示） */}
                {typeInfo.canUnsubscribe && (
                  <Checkbox
                    checked={isSelected}
                    onChange={(e) => handleSelectType(typeInfo.type, e.target.checked)}
                    className="type-checkbox"
                  />
                )}

                {/* 图标 */}
                <div className="type-icon">{typeInfo.icon}</div>

                {/* 信息 */}
                <div className="type-info">
                  <div className="type-header">
                    <h3 className="type-name">{typeInfo.name}</h3>
                    {!typeInfo.canUnsubscribe && (
                      <span className="required-badge">必需</span>
                    )}
                  </div>
                  <p className="type-description">{typeInfo.description}</p>
                </div>

                {/* 开关 */}
                <Switch
                  checked={subscribed}
                  onChange={() => handleToggleType(typeInfo)}
                  disabled={!typeInfo.canUnsubscribe}
                />
              </div>
            );
          })
        )}
      </div>

      {/* 底部提示 */}
      <div className="types-footer">
        <p className="footer-hint">
          💡 提示：标记为"必需"的通知类型无法关闭，以保障重要信息的及时送达
        </p>
      </div>
    </div>
  );
};

export default NotificationTypes;
