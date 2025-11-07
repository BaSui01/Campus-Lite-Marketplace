/**
 * 主布局组件
 * @author BaSui 😎
 * @description 包含顶部导航栏、侧边栏、底部栏的主布局
 * @updated 2025-11-06 - 增强导航功能，添加下拉菜单和搜索框
 */

import { Outlet, Link, useNavigate, useLocation } from 'react-router-dom';
import { useState } from 'react';
import { useAuthStore, useNotificationStore } from '../../store';
import { Badge, Dropdown, UserAvatar } from '@campus/shared';
import { useNotification } from '@campus/shared';
import { MAIN_NAV_ITEMS, USER_MENU_ITEMS, MOBILE_TAB_BAR, type NavItem } from '../../config/navigation';
import './MainLayout.css';

/**
 * 主布局组件
 */
const MainLayout = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, isAuthenticated, logout } = useAuthStore();
  const toast = useNotificationStore();
  const [searchQuery, setSearchQuery] = useState('');
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  // 订阅 WebSocket 通知
  const { unreadCount } = useNotification({
    onNewNotification: (notification) => {
      toast.info(notification.content, notification.title);
    },
  });

  /**
   * 处理登出
   */
  const handleLogout = async () => {
    try {
      await logout();
      toast.success('已退出登录');
      navigate('/login');
    } catch (error: any) {
      toast.error(error.message || '退出登录失败');
    }
  };

  /**
   * 处理搜索
   */
  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchQuery.trim())}`);
      setSearchQuery('');
    }
  };

  /**
   * 渲染导航菜单项
   */
  const renderNavItem = (item: NavItem) => {
    // 需要登录但用户未登录，不显示
    if (item.auth && !isAuthenticated) {
      return null;
    }

    // 有子菜单的项
    if (item.children && item.children.length > 0) {
      const filteredChildren = item.children.filter(
        (child) => !child.auth || isAuthenticated
      );

      if (filteredChildren.length === 0) {
        return null;
      }

      const dropdownItems = filteredChildren.map((child) => ({
        key: child.key,
        label: child.label,
        onClick: () => child.path && navigate(child.path),
      }));

      return (
        <Dropdown key={item.key} menu={dropdownItems} trigger="hover">
          <span className="main-layout__nav-item main-layout__nav-item--dropdown">
            {item.icon && <span className="mr-1">{item.icon}</span>}
            {item.label}
            <span className="ml-1">▼</span>
          </span>
        </Dropdown>
      );
    }

    // 普通菜单项
    return (
      <Link
        key={item.key}
        to={item.path || '#'}
        className={`main-layout__nav-item ${
          location.pathname === item.path ? 'main-layout__nav-item--active' : ''
        }`}
      >
        {item.icon && <span className="mr-1">{item.icon}</span>}
        {item.label}
      </Link>
    );
  };

  /**
   * 用户菜单项
   */
  const userMenuItems = isAuthenticated
    ? [
        ...USER_MENU_ITEMS.map((item) => {
          if (item.key.startsWith('divider')) {
            return {
              key: item.key,
              label: <div className="h-px bg-gray-200 my-2" />,
            };
          }
          return {
            key: item.key,
            label: (
              <span>
                {item.icon && <span className="mr-2">{item.icon}</span>}
                {item.label}
              </span>
            ),
            onClick: () => item.path && navigate(item.path),
          };
        }),
        {
          key: 'logout-divider',
          label: <div className="h-px bg-gray-200 my-2" />,
        },
        {
          key: 'logout',
          label: (
            <span className="text-red-600">
              <span className="mr-2">🚪</span>
              退出登录
            </span>
          ),
          onClick: handleLogout,
        },
      ]
    : [
        {
          key: 'login',
          label: '登录',
          onClick: () => navigate('/login'),
        },
        {
          key: 'register',
          label: '注册',
          onClick: () => navigate('/register'),
        },
      ];

  return (
    <div className="main-layout">
      {/* ==================== 顶部导航栏 ==================== */}
      <header className="main-layout__header">
        <div className="main-layout__header-content">
          {/* 移动端菜单按钮 */}
          <button
            className="main-layout__mobile-menu-btn"
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            aria-label="菜单"
          >
            {isMobileMenuOpen ? '✕' : '☰'}
          </button>

          {/* Logo */}
          <Link to="/" className="main-layout__logo">
            <span className="main-layout__logo-icon">🎓</span>
            <span className="main-layout__logo-text">校园轻享集市</span>
          </Link>

          {/* 导航菜单（桌面端） */}
          <nav className={`main-layout__nav ${isMobileMenuOpen ? 'main-layout__nav--mobile-open' : ''}`}>
            {MAIN_NAV_ITEMS.map((item) => renderNavItem(item))}
            
            {/* 聊天独立入口（登录后显示） */}
            {isAuthenticated && (
              <Link to="/chat" className="main-layout__nav-item">
                <span className="mr-1">💬</span>
                聊天
              </Link>
            )}
          </nav>

          {/* 搜索框 */}
          <form className="main-layout__search" onSubmit={handleSearch}>
            <input
              type="text"
              placeholder="搜索商品、用户、社区..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="main-layout__search-input"
            />
            <button type="submit" className="main-layout__search-btn" aria-label="搜索">
              🔍
            </button>
          </form>

          {/* 右侧操作区 */}
          <div className="main-layout__actions">
            {/* 通知 */}
            {isAuthenticated && (
              <Badge count={unreadCount} dot={unreadCount > 0}>
                <button
                  className="main-layout__action-btn"
                  onClick={() => navigate('/notifications')}
                  title="通知"
                  aria-label="通知"
                >
                  🔔
                </button>
              </Badge>
            )}

            {/* 用户菜单 */}
            {isAuthenticated ? (
              <Dropdown menu={userMenuItems} trigger="click">
                <div className="main-layout__user">
                  <UserAvatar
                    userId={user?.id.toString() || ''}
                    username={user?.username || ''}
                    avatarUrl={user?.avatar}
                    size="small"
                  />
                  <span className="main-layout__username">{user?.username}</span>
                </div>
              </Dropdown>
            ) : (
              <div className="main-layout__auth-buttons">
                <button
                  className="main-layout__btn main-layout__btn--outline"
                  onClick={() => navigate('/login')}
                >
                  登录
                </button>
                <button
                  className="main-layout__btn main-layout__btn--primary"
                  onClick={() => navigate('/register')}
                >
                  注册
                </button>
              </div>
            )}
          </div>
        </div>
      </header>

      {/* ==================== 主内容区 ==================== */}
      <main className="main-layout__main">
        <Outlet />
      </main>

      {/* ==================== 移动端底部导航栏 ==================== */}
      <div className="main-layout__mobile-tabbar">
        {MOBILE_TAB_BAR.map((item) => {
          // 需要登录但用户未登录，不显示
          if (item.auth && !isAuthenticated) {
            return null;
          }

          const isActive = location.pathname === item.path;

          return (
            <Link
              key={item.key}
              to={item.path || '#'}
              className={`main-layout__mobile-tab ${
                isActive ? 'main-layout__mobile-tab--active' : ''
              }`}
            >
              <span className="main-layout__mobile-tab-icon">{item.icon}</span>
              <span className="main-layout__mobile-tab-label">{item.label}</span>
            </Link>
          );
        })}
      </div>

      {/* ==================== 底部栏 ==================== */}
      <footer className="main-layout__footer">
        <div className="main-layout__footer-content">
          <p className="main-layout__footer-text">
            © 2025 校园轻享集市 | Made with ❤️ by BaSui 😎
          </p>
          <div className="main-layout__footer-links">
            <a href="/about" className="main-layout__footer-link">
              关于我们
            </a>
            <span className="main-layout__footer-divider">|</span>
            <a href="/privacy" className="main-layout__footer-link">
              隐私政策
            </a>
            <span className="main-layout__footer-divider">|</span>
            <a href="/terms" className="main-layout__footer-link">
              服务条款
            </a>
          </div>
        </div>
      </footer>
    </div>
  );
};

export default MainLayout;
