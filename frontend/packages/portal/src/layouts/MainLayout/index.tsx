/**
 * 主布局组件
 * @author BaSui 😎
 * @description 包含顶部导航栏、侧边栏、底部栏的主布局
 */

import { Outlet, Link, useNavigate } from 'react-router-dom';
import { useAuthStore, useNotificationStore } from '../../store';
import { Badge, Dropdown, UserAvatar } from '@campus/shared';
import { useNotification } from '@campus/shared';
import './MainLayout.css';

/**
 * 主布局组件
 */
const MainLayout = () => {
  const navigate = useNavigate();
  const { user, isAuthenticated, logout } = useAuthStore();
  const toast = useNotificationStore();

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
   * 用户菜单项
   */
  const userMenuItems = isAuthenticated
    ? [
        {
          key: 'profile',
          label: '个人中心',
          onClick: () => navigate('/profile'),
        },
        {
          key: 'orders',
          label: '我的订单',
          onClick: () => navigate('/orders'),
        },
        {
          key: 'divider1',
          label: <div className="h-px bg-gray-200 my-2" />,
        },
        {
          key: 'logout',
          label: '退出登录',
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
          {/* Logo */}
          <Link to="/" className="main-layout__logo">
            <span className="main-layout__logo-icon">🎓</span>
            <span className="main-layout__logo-text">校园轻享集市</span>
          </Link>

          {/* 导航菜单 */}
          <nav className="main-layout__nav">
            <Link to="/" className="main-layout__nav-item">
              首页
            </Link>
            <Link to="/community" className="main-layout__nav-item">
              社区
            </Link>
            {isAuthenticated && (
              <>
                <Link to="/publish" className="main-layout__nav-item">
                  发布
                </Link>
                <Link to="/chat" className="main-layout__nav-item">
                  聊天
                </Link>
              </>
            )}
          </nav>

          {/* 右侧操作区 */}
          <div className="main-layout__actions">
            {/* 通知 */}
            {isAuthenticated && (
              <Badge count={unreadCount} dot={unreadCount > 0}>
                <button
                  className="main-layout__action-btn"
                  onClick={() => navigate('/notifications')}
                  title="通知"
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
