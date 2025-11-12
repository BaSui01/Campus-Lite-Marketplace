/**
 * 权限守卫组件
 * @author BaSui 😎
 * @date 2025-11-02
 */

import React from 'react';
import { Result, Button } from 'antd';
import { useNavigate } from 'react-router-dom';
import { usePermission } from '@/hooks';

interface PermissionGuardProps {
  permission?: string;
  permissions?: string[];
  requireAll?: boolean; // 需要同时拥有所有权限
  children: React.ReactNode;
  fallback?: React.ReactNode;
}

export const PermissionGuard: React.FC<PermissionGuardProps> = ({
  permission,
  permissions,
  requireAll = false,
  children,
  fallback,
}) => {
  const navigate = useNavigate();
  const { hasPermission, hasAnyPermission, hasAllPermissions } = usePermission();

  let hasAccess = true;

  if (permission) {
    hasAccess = hasPermission(permission);
  } else if (permissions && permissions.length > 0) {
    hasAccess = requireAll
      ? hasAllPermissions(permissions)
      : hasAnyPermission(permissions);
  }

  // 如果有权限，直接渲染子组件
  if (hasAccess) {
    return <>{children}</>;
  }

  // 如果提供了自定义 fallback，使用它
  if (fallback) {
    return <>{fallback}</>;
  }

  // 默认显示无权限提示页面
  return (
    <div style={{ padding: '48px' }}>
      <Result
        status="403"
        title="无权限访问"
        subTitle="抱歉，您没有权限访问此页面。请联系管理员获取相应权限。"
        extra={
          <Button type="primary" onClick={() => navigate('/admin/dashboard')}>
            返回首页
          </Button>
        }
      />
    </div>
  );
};
