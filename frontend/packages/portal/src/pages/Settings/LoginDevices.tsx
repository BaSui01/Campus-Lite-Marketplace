/**
 * 登录设备管理组件 - 查看和管理登录设备！🖥️
 * @author BaSui 😎
 * @description 显示所有登录设备列表，标记当前设备
 */

import React, { useState, useEffect } from 'react';
import { Skeleton } from '@campus/shared/components';
import { userService } from '@campus/shared/services/user';
import { useAuthStore, useNotificationStore } from '../../store';
import './LoginDevices.css';

/**
 * 登录设备类型
 */
interface LoginDevice {
  id: number;
  deviceType: string;
  deviceName: string;
  ipAddress: string;
  location: string;
  browser: string;
  os: string;
  loginTime: string;
  lastActiveTime: string;
  isCurrent: boolean;
}

/**
 * 登录设备管理组件
 */
const LoginDevices: React.FC = () => {
  const { user: currentUser } = useAuthStore();
  const toast = useNotificationStore();

  // ==================== 状态管理 ====================

  const [loading, setLoading] = useState(true);
  const [devices, setDevices] = useState<LoginDevice[]>([]);

  // ==================== 数据加载 ====================

  /**
   * 加载登录设备列表
   */
  const loadDevices = async () => {
    if (!currentUser?.id) {
      toast.error('用户信息不存在！😭');
      return;
    }

    setLoading(true);

    try {
      // 🚀 调用真实后端 API 获取登录设备列表
      const deviceList = await userService.getLoginDevices(currentUser.id);
      setDevices(deviceList || []);
    } catch (err: any) {
      console.error('加载登录设备失败：', err);
      toast.error(err.response?.data?.message || '加载登录设备失败！😭');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 踢出登录设备（新增 - BaSui 2025-11-09）
   */
  const handleRemoveDevice = async (deviceId: number) => {
    if (!currentUser?.id) {
      toast.error('用户信息不存在！😭');
      return;
    }

    // 确认操作
    if (!window.confirm('确定要踢出该设备吗？该设备将需要重新登录。')) {
      return;
    }

    try {
      // 🚀 调用真实后端 API 踢出设备
      await userService.kickDevice(currentUser.id, deviceId);
      toast.success('设备已踢出！🎉');

      // 重新加载设备列表
      loadDevices();
    } catch (err: any) {
      console.error('踢出设备失败：', err);
      toast.error(err.response?.data?.message || '踢出设备失败！😭');
    }
  };

  /**
   * 获取设备类型图标
   */
  const getDeviceIcon = (deviceType: string): string => {
    const iconMap: Record<string, string> = {
      DESKTOP: '🖥️',
      LAPTOP: '💻',
      MOBILE: '📱',
      TABLET: '📱',
      UNKNOWN: '❓',
    };
    return iconMap[deviceType] || '🖥️';
  };

  /**
   * 获取浏览器图标
   */
  const getBrowserIcon = (browser: string): string => {
    if (browser.includes('Chrome')) return '🌐';
    if (browser.includes('Firefox')) return '🦊';
    if (browser.includes('Safari')) return '🧭';
    if (browser.includes('Edge')) return '🌊';
    return '🌐';
  };

  // ==================== 生命周期 ====================

  useEffect(() => {
    loadDevices();
  }, []);

  // ==================== 渲染 ====================

  // 加载中
  if (loading) {
    return (
      <div className="login-devices">
        <Skeleton type="list" count={5} animation="wave" />
      </div>
    );
  }

  return (
    <div className="login-devices">
      <div className="login-devices__header">
        <h2 className="login-devices__title">🖥️ 登录设备管理</h2>
        <p className="login-devices__desc">
          查看所有登录过的设备，保护账户安全
        </p>
      </div>

      {devices.length === 0 ? (
        <div className="login-devices__empty">
          <div className="empty-icon">🖥️</div>
          <p className="empty-text">暂无登录设备记录</p>
        </div>
      ) : (
        <div className="login-devices__list">
          {devices.map((device) => (
            <div
              key={device.id}
              className={`device-item ${device.isCurrent ? 'device-item--current' : ''}`}
            >
              {/* 设备图标 */}
              <div className="device-item__icon">
                {getDeviceIcon(device.deviceType)}
              </div>

              {/* 设备信息 */}
              <div className="device-item__content">
                <div className="device-item__header">
                  <div className="device-item__name">
                    {device.deviceName || '未知设备'}
                    {device.isCurrent && (
                      <span className="device-item__badge">当前设备</span>
                    )}
                  </div>
                  <div className="device-item__type">
                    {device.deviceType}
                  </div>
                </div>

                <div className="device-item__details">
                  <div className="device-detail">
                    <span className="device-detail__icon">{getBrowserIcon(device.browser)}</span>
                    <span className="device-detail__text">{device.browser}</span>
                  </div>
                  <div className="device-detail">
                    <span className="device-detail__icon">💻</span>
                    <span className="device-detail__text">{device.os}</span>
                  </div>
                  <div className="device-detail">
                    <span className="device-detail__icon">📍</span>
                    <span className="device-detail__text">
                      {device.location || device.ipAddress}
                    </span>
                  </div>
                </div>

                <div className="device-item__time">
                  <div className="device-time">
                    <span className="device-time__label">登录时间：</span>
                    <span className="device-time__value">
                      {new Date(device.loginTime).toLocaleString('zh-CN')}
                    </span>
                  </div>
                  <div className="device-time">
                    <span className="device-time__label">最后活跃：</span>
                    <span className="device-time__value">
                      {new Date(device.lastActiveTime).toLocaleString('zh-CN')}
                    </span>
                  </div>
                </div>
              </div>

              {/* 操作按钮（新增 - BaSui 2025-11-09） */}
              {!device.isCurrent && (
                <div className="device-item__actions">
                  <button className="btn-remove" onClick={() => handleRemoveDevice(device.id)}>
                    踢出设备
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* 安全提示 */}
      <div className="login-devices__tips">
        <h3 className="tips-title">🛡️ 安全提示</h3>
        <ul className="tips-list">
          <li>如果发现陌生设备登录，请立即修改密码</li>
          <li>建议定期检查登录设备列表</li>
          <li>不要在公共设备上保存登录状态</li>
        </ul>
      </div>
    </div>
  );
};

export default LoginDevices;
