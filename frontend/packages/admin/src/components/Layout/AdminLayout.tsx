/**
 * 管理端布局组件 - 响应式设计
 * @author BaSui 😎
 * @date 2025-11-02
 * @updated 2025-11-06 - 添加响应式支持（手机/平板/桌面）
 */

import React from 'react';
import { Layout, Menu, Button, Dropdown, Space, Avatar, Typography, Drawer, Modal } from 'antd';
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined,
  LogoutOutlined,
  ExclamationCircleOutlined,
  DashboardOutlined,
  FileTextOutlined,
  SafetyOutlined,
  SettingOutlined,
  FileSearchOutlined,
  ShoppingOutlined,
  ShoppingCartOutlined,
  FileProtectOutlined,
  SafetyCertificateOutlined,
  StarOutlined,
  ThunderboltOutlined,
  TeamOutlined,
  BarChartOutlined,
  MessageOutlined,
  PayCircleOutlined,
  CarOutlined,
  ExportOutlined,
  FundOutlined,
  RocketOutlined,
  SearchOutlined,
  BellOutlined,
  BulbOutlined,
  BulbFilled,
} from '@ant-design/icons';
import type { MenuProps } from 'antd';
import { useAuth, usePermission, useBreakpoint, useTheme } from '@/hooks';
import { UserAvatar, Badge } from '@campus/shared';
import { MENU_ITEMS } from '@/config/menu';
import { useNavigate, useLocation, Outlet } from 'react-router-dom';

const { Header, Sider, Content } = Layout;
const { Title } = Typography;

const getIcon = (iconName: string) => {
  const icons: Record<string, React.ReactNode> = {
    DashboardOutlined: <DashboardOutlined />,
    UserOutlined: <UserOutlined />,
    FileTextOutlined: <FileTextOutlined />,
    SafetyOutlined: <SafetyOutlined />,
    SettingOutlined: <SettingOutlined />,
    FileSearchOutlined: <FileSearchOutlined />,
    ShoppingOutlined: <ShoppingOutlined />,
    ShoppingCartOutlined: <ShoppingCartOutlined />,
    FileProtectOutlined: <FileProtectOutlined />,
    SafetyCertificateOutlined: <SafetyCertificateOutlined />,
    StarOutlined: <StarOutlined />,
    ThunderboltOutlined: <ThunderboltOutlined />,
    TeamOutlined: <TeamOutlined />,
    BarChartOutlined: <BarChartOutlined />,
    MessageOutlined: <MessageOutlined />,
    PayCircleOutlined: <PayCircleOutlined />,
    CarOutlined: <CarOutlined />,
    ExportOutlined: <ExportOutlined />,
    FundOutlined: <FundOutlined />,
    RocketOutlined: <RocketOutlined />,
    SearchOutlined: <SearchOutlined />,
    BellOutlined: <BellOutlined />,
  };
  return icons[iconName];
};

export const AdminLayout: React.FC = () => {
  const { user, logout } = useAuth();
  const { hasPermission } = usePermission();
  const { isMobile, isTablet, isDesktop } = useBreakpoint();
  const { actualTheme, toggleTheme } = useTheme();
  const navigate = useNavigate();
  const location = useLocation();

  // 响应式collapsed状态：桌面端默认展开，平板端默认收起，手机端用Drawer
  const [collapsed, setCollapsed] = React.useState(() => {
    return !isDesktop; // 非桌面端默认收起
  });

  // 手机端用Drawer的显示状态
  const [drawerVisible, setDrawerVisible] = React.useState(false);

  // 响应式切换时更新collapsed状态
  React.useEffect(() => {
    if (isMobile) {
      setCollapsed(true); // 手机端强制收起（实际用Drawer）
      setDrawerVisible(false); // 关闭Drawer
    } else if (isTablet) {
      setCollapsed(true); // 平板端默认收起
    } else {
      setCollapsed(false); // 桌面端默认展开
    }
  }, [isMobile, isTablet, isDesktop]);

  const handleLogout = async () => {
    Modal.confirm({
      title: '确认退出登录',
      icon: <ExclamationCircleOutlined />,
      content: (
        <div>
          <p>确定要退出登录吗？</p>
          <p style={{ color: '#999', fontSize: 12, marginTop: 8 }}>
            退出后需要重新登录才能访问系统
          </p>
        </div>
      ),
      okText: '确认退出',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        try {
          await logout();
          navigate('/admin/login', { replace: true });
        } catch (error) {
          console.error('登出失败:', error);
        }
      },
    });
  };

  // ===== 用户菜单点击处理 =====
  const handleUserMenuClick: MenuProps['onClick'] = ({ key }) => {
    if (key === 'profile') {
      // 跳转到个人信息页面（可选：使用Modal显示）
      navigate('/admin/profile');
    } else if (key === 'logout') {
      handleLogout();
    }
  };

  // ===== 菜单处理函数 =====
  const filterMenuByPermission = (items: typeof MENU_ITEMS) => {
    return items
      .filter(item => {
        // 如果没有权限要求，显示菜单
        if (!item.permission) return true;
        // 检查用户是否有权限
        return hasPermission(item.permission);
      })
      .map(item => ({
        key: item.key,
        label: item.label,
        icon: item.icon ? getIcon(item.icon) : undefined,
        children: item.children 
          ? filterMenuByPermission(item.children)
          : undefined,
      }));
  };

  const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
    const findMenuItem = (items: typeof MENU_ITEMS, targetKey: string) => {
      for (const item of items) {
        if (item.key === targetKey && item.path) {
          return item.path;
        }
        if (item.children) {
          const found = findMenuItem(item.children, targetKey);
          if (found) return found;
        }
      }
      return null;
    };

    const path = findMenuItem(MENU_ITEMS, key);
    if (path) {
      navigate(path);
      // 手机端点击菜单后关闭Drawer
      if (isMobile) {
        setDrawerVisible(false);
      }
    }
  };

  // 切换侧边栏/Drawer显示状态
  const toggleMenu = () => {
    if (isMobile) {
      setDrawerVisible(!drawerVisible);
    } else {
      setCollapsed(!collapsed);
    }
  };

  // 获取当前选中的菜单项
  const getSelectedKeys = (): string[] => {
    const currentPath = location.pathname;
    
    const findMatchingKey = (items: typeof MENU_ITEMS): string | null => {
      for (const item of items) {
        if (item.path === currentPath) {
          return item.key;
        }
        if (item.children) {
          const found = findMatchingKey(item.children);
          if (found) return found;
        }
      }
      return null;
    };

    const selectedKey = findMatchingKey(MENU_ITEMS);
    return selectedKey ? [selectedKey] : ['dashboard'];
  };

  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      label: '个人信息',
      icon: <UserOutlined />,
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      label: '退出登录',
      icon: <LogoutOutlined />,
      danger: true, // 红色样式表示危险操作
    },
  ];

  // 菜单内容组件（Sider和Drawer共用）
  const MenuContent = () => (
    <div
      style={{
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        overflow: 'hidden',
      }}
    >
      {/* 🎨 优化后的Logo区域 - 现代化设计 */}
      <div
        style={{
          padding: collapsed && !isMobile ? '20px 12px' : '20px 16px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          gap: '12px',
          background: 'linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%)',
          borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
          transition: 'all 0.3s ease',
        }}
      >
        {/* Logo Icon */}
        <span
          style={{
            fontSize: collapsed && !isMobile ? '28px' : '36px',
            filter: 'drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2))',
            transition: 'all 0.3s ease',
          }}
        >
          🎓
        </span>

        {/* Logo Text */}
        {(!collapsed || isMobile) && (
          <div
            style={{
              display: 'flex',
              flexDirection: 'column',
              gap: '2px',
              transition: 'all 0.3s ease',
            }}
          >
            <span
              style={{
                fontSize: '18px',
                fontWeight: 700,
                color: '#ffffff',
                letterSpacing: '0.5px',
                lineHeight: '1.2',
              }}
            >
              校园轻享集市
            </span>
            <span
              style={{
                fontSize: '12px',
                fontWeight: 500,
                color: 'rgba(255, 255, 255, 0.65)',
                letterSpacing: '1px',
              }}
            >
              管理后台
            </span>
          </div>
        )}
      </div>

      <div
        style={{
          flex: 1,
          minHeight: 0,
          overflowY: 'auto',
          paddingBottom: 16, // 预留底部空间，避免最后一项被挡
        }}
      >
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={getSelectedKeys()}
          items={filterMenuByPermission(MENU_ITEMS)}
          onClick={handleMenuClick}
          style={{
            borderInlineEnd: 0,
          }}
        />
      </div>
    </div>
  );

  return (
    <Layout style={{ minHeight: '100vh' }}>
      {/* 手机端：使用Drawer */}
      {isMobile ? (
        <Drawer
          placement="left"
          closable={false}
          onClose={() => setDrawerVisible(false)}
          open={drawerVisible}
          styles={{
            body: {
              padding: 0,
              background: '#001529',
              display: 'flex',
              flexDirection: 'column',
              height: '100%',
            },
          }}
          width={250}
        >
          <MenuContent />
        </Drawer>
      ) : (
        /* 平板/桌面端：使用Sider */
        <Sider
          trigger={null}
          collapsible
          collapsed={collapsed}
          style={{
            position: 'fixed',
            height: '100vh',
            left: 0,
            top: 0,
            bottom: 0,
            zIndex: 999,
            display: 'flex',
            flexDirection: 'column',
          }}
        >
          <MenuContent />
        </Sider>
      )}
      
      <Layout style={{ marginLeft: isMobile ? 0 : (collapsed ? 80 : 200), height: '100vh', display: 'flex', flexDirection: 'column' }}>
        <Header
          style={{
            padding: '0 16px',
            background: '#fff',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            position: 'sticky',
            top: 0,
            zIndex: 998,
            borderBottom: '1px solid #f0f0f0',
          }}
        >
          <Button
            type="text"
            icon={isMobile ? <MenuFoldOutlined /> : (collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />)}
            onClick={toggleMenu}
            style={{
              fontSize: '16px',
              width: 64,
              height: 64,
            }}
          />
          
          <Space size="middle">
            {/* 🌙 主题切换按钮 */}
            <Button
              type="text"
              icon={actualTheme === 'dark' ? <BulbFilled style={{ color: '#faad14' }} /> : <BulbOutlined />}
              onClick={toggleTheme}
              title={actualTheme === 'dark' ? '切换到亮色模式' : '切换到暗色模式'}
              style={{
                fontSize: '18px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            />
            
            <Badge dot>
              <Dropdown menu={{ items: userMenuItems, onClick: handleUserMenuClick }} placement="bottomRight">
                <Space style={{ cursor: 'pointer' }}>
                  <UserAvatar
                    src={user?.avatar}
                    alt={user?.nickname || user?.username}
                    size="small"
                  />
                  {!isMobile && <span>{user?.nickname || user?.username}</span>}
                </Space>
              </Dropdown>
            </Badge>
          </Space>
        </Header>
        
        <Content
          style={{
            margin: '16px',
            padding: '16px',
            background: '#fff',
            overflow: 'auto',
            flex: 1,
          }}
        >
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default AdminLayout;
