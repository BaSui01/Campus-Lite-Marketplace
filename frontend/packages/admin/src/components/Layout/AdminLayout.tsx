/**
 * 主布局组件
 *
 * 包含：侧边栏、顶栏、面包屑、内容区域
 *
 * @author BaSui 😎
 * @date 2025-11-01
 */

import React from 'react';
import { Layout, Menu, Breadcrumb, Avatar, Dropdown, Space, Button, Typography } from 'antd';
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined,
  LogoutOutlined,
  DashboardOutlined,
  FileTextOutlined,
  SafetyOutlined,
  SettingOutlined,
  FileSearchOutlined,
} from '@ant-design/icons';
import type { MenuProps } from 'antd';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAppStore } from '@/stores/app';
import { useAuth } from '@/hooks/useAuth';
import { usePermission } from '@/hooks/usePermission';
import { PERMISSIONS } from '@/utils/constants';
import './AdminLayout.css';

const { Header, Sider, Content } = Layout;
const { Text } = Typography;

const AdminLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { menuCollapsed, toggleMenu } = useAppStore();
  const { user, logout } = useAuth();
  const { hasPermission } = usePermission();

  // ===== 菜单配置 =====
  const menuItems: MenuProps['items'] = [
    {
      key: '/admin/dashboard',
      icon: <DashboardOutlined />,
      label: '仪表盘',
    },
    {
      key: '/admin/users',
      icon: <UserOutlined />,
      label: '用户管理',
      children: [
        { key: '/admin/users/list', label: '用户列表' },
        { key: '/admin/users/banned', label: '封禁记录' },
      ],
    },
    {
      key: '/admin/content',
      icon: <FileTextOutlined />,
      label: '内容管理',
      children: [
        { key: '/admin/content/goods', label: '商品审核' },
        { key: '/admin/content/posts', label: '帖子审核' },
        { key: '/admin/content/reports', label: '举报处理' },
      ],
    },
    {
      key: '/admin/roles',
      icon: <SafetyOutlined />,
      label: '角色权限',
    },
    {
      key: '/admin/system',
      icon: <SettingOutlined />,
      label: '系统管理',
      children: [
        { key: '/admin/system/rate-limit', label: '限流管理' },
        { key: '/admin/system/notifications', label: '通知模板' },
        { key: '/admin/system/compliance', label: '合规管理' },
        { key: '/admin/system/recycle-bin', label: '回收站' },
      ],
    },
    {
      key: '/admin/logs',
      icon: <FileSearchOutlined />,
      label: '日志管理',
      children: [
        { key: '/admin/logs/audit', label: '审计日志' },
        { key: '/admin/logs/operation', label: '操作日志' },
      ],
    },
  ];

  // ===== 用户菜单 =====
  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人信息',
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      danger: true,
      onClick: () => {
        logout();
        navigate('/admin/login');
      },
    },
  ];

  // ===== 菜单点击 =====
  const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
    navigate(key);
  };

  return (
    <Layout className="admin-layout">
      {/* 侧边栏 */}
      <Sider
        trigger={null}
        collapsible
        collapsed={menuCollapsed}
        width={240}
        className="admin-sider"
      >
        <div className="logo">
          {menuCollapsed ? 'CM' : '校园轻享集市'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={handleMenuClick}
        />
      </Sider>

      <Layout>
        {/* 顶部栏 */}
        <Header className="admin-header">
          <Button
            type="text"
            icon={menuCollapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={toggleMenu}
            className="trigger"
          />

          <Space className="user-info">
            <Text>欢迎，{user?.nickname}</Text>
            <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
              <Avatar src={user?.avatar} icon={<UserOutlined />} />
            </Dropdown>
          </Space>
        </Header>

        {/* 面包屑 */}
        <div className="breadcrumb-wrapper">
          <Breadcrumb
            items={[
              { title: '首页' },
              { title: '仪表盘' },
            ]}
          />
        </div>

        {/* 内容区域 */}
        <Content className="admin-content">
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default AdminLayout;
