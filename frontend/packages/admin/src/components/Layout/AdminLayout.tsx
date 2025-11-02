/**
 * 管理端布局组件
 * @author BaSui 😎
 * @date 2025-11-02
 */

import React from 'react';
import { Layout, Menu, Button, Dropdown, Space, Avatar, Typography } from 'antd';
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined,
  LogoutOutlined,
} from '@ant-design/icons';
import type { MenuProps } from 'antd';
import { useAuth, usePermission } from '@/hooks';
import { UserAvatar, Badge } from '@campus/shared';
import { MENU_ITEMS } from '@/config/menu';
import { useNavigate, useLocation } from 'react-router-dom';
import { DashboardOutlined, FileTextOutlined, SafetyOutlined, SettingOutlined, FileSearchOutlined } from '@ant-design/icons';

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
  };
  return icons[iconName];
};

interface AdminLayoutProps {
  children: React.ReactNode;
}

export const AdminLayout: React.FC<AdminLayoutProps> = ({ children }) => {
  const { user, logout } = useAuth();
  const { hasPermission } = usePermission();
  const [collapsed, setCollapsed] = React.useState(false);
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = async () => {
    try {
      await logout();
    } catch (error) {
      console.error('登出失败:', error);
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
      onClick: handleLogout,
    },
  ];

  return (
    <Layout style={{ minHeight: '100vh' }}>
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
        }}
      >
        <div style={{ padding: '16px', textAlign: 'center' }}>
          <Title level={4} style={{ color: 'white', margin: 0 }}>
            {collapsed ? '管理' : '校园集市管理系统'}
          </Title>
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={getSelectedKeys()}
          items={filterMenuByPermission(MENU_ITEMS)}
          onClick={handleMenuClick}
        />
      </Sider>
      
      <Layout style={{ marginLeft: collapsed ? 80 : 200 }}>
        <Header
          style={{
            padding: '0 16px',
            background: '#fff',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
          }}
        >
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
            style={{
              fontSize: '16px',
              width: 64,
              height: 64,
            }}
          />
          
          <Space>
            <Badge dot>
              <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
                <Space style={{ cursor: 'pointer' }}>
                  <UserAvatar 
                    src={user?.avatar}
                    alt={user?.nickname || user?.username}
                    size="small"
                  />
                  <span>{user?.nickname || user?.username}</span>
                </Space>
              </Dropdown>
            </Badge>
          </Space>
        </Header>
        
        <Content
          style={{
            margin: '16px',
            padding: '16px',
            minHeight: 280,
            background: '#fff',
          }}
        >
          {children}
        </Content>
      </Layout>
    </Layout>
  );
};
