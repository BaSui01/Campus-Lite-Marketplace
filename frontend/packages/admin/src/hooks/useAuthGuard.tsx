/**
 * 路由权限守卫 Hook
 * @author BaSui 😎
 * @date 2025-11-02
 */

import { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from './useAuth';
import { PERMISSION_CODES } from '@campus/shared';

interface PermissionRequirement {
  permission?: string;
  permissions?: string[];
  requireAll?: boolean;
}

export const useAuthGuard = (requirements?: PermissionRequirement | PermissionRequirement[]) => {
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    // 检查是否已登录
    if (!isAuthenticated) {
      // ⚠️ 防止无限重定向：如果已经在登录页，不再跳转
      if (location.pathname !== '/admin/login') {
        navigate('/admin/login', { state: { from: location.pathname } });
      }
      return;
    }

    // 如果没有权限要求直接通过
    if (!requirements) return;

    // 检查权限
    const checkPermission = (req: PermissionRequirement) => {
      if (!req.permission && !req.permissions) return true;
      
      const userPermissions = user?.permissions || [];
      
      if (req.permission) {
        return userPermissions.includes(req.permission);
      }
      
      if (req.permissions) {
        if (req.requireAll) {
          return req.permissions.every(p => userPermissions.includes(p));
        } else {
          return req.permissions.some(p => userPermissions.includes(p));
        }
      }
      
      return true;
    };

    const hasPermission = Array.isArray(requirements) 
      ? requirements.some(checkPermission)
      : checkPermission(requirements);

    if (!hasPermission) {
      // 跳转到无权限页面或仪表盘
      navigate('/admin/dashboard');
    }
  }, [isAuthenticated, user, navigate, location, requirements]);
};
