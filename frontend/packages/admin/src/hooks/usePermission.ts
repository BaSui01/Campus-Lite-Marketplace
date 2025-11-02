/**
 * 权限检查 Hook
 * @author BaSui 😎
 * @date 2025-11-02
 */

import { useMemo } from 'react';
import { useAuthStore } from '@/stores/auth';
import { PERMISSION_CODES } from '@campus/shared';

export const usePermission = () => {
  const user = useAuthStore((state) => state.user);

  const hasPermission = useMemo(() => {
    return (permission: string): boolean => {
      return user?.permissions?.includes(permission) ?? false;
    };
  }, [user]);

  const hasAnyPermission = useMemo(() => {
    return (permissions: string[]): boolean => {
      if (!permissions || permissions.length === 0) return true;
      return permissions.some((permission) => user?.permissions?.includes(permission) ?? false);
    };
  }, [user]);

  const hasAllPermissions = useMemo(() => {
    return (permissions: string[]): boolean => {
      if (!permissions || permissions.length === 0) return true;
      return permissions.every((permission) => user?.permissions?.includes(permission) ?? false);
    };
  }, [user]);

  const hasRole = useMemo(() => {
    return (role: string): boolean => {
      return user?.roles?.includes(role) ?? false;
    };
  }, [user]);

  const hasAnyRole = useMemo(() => {
    return (roles: string[]): boolean => {
      if (!roles || roles.length === 0) return true;
      return roles.some((role) => user?.roles?.includes(role) ?? false);
    };
  }, [user]);

  return {
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    hasRole,
    hasAnyRole,
    PERMISSION_CODES,
  };
};
