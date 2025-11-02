/**
 * 权限守卫组件
 * @author BaSui 😎
 * @date 2025-11-02
 */

import React from 'react';
import { Navigate } from 'react-router-dom';
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
  fallback = <Navigate to="/admin/dashboard" replace />,
}) => {
  const { hasPermission, hasAnyPermission, hasAllPermissions } = usePermission();

  let hasAccess = true;

  if (permission) {
    hasAccess = hasPermission(permission);
  } else if (permissions && permissions.length > 0) {
    hasAccess = requireAll 
      ? hasAllPermissions(permissions)
      : hasAnyPermission(permissions);
  }

  return <>{hasAccess ? children : fallback}</>;
};
