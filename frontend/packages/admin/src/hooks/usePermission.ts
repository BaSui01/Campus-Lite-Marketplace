/**
 * 权限校验 Hook
 *
 * 使用示例：
 * const { hasPermission, hasAnyPermission } = usePermission();
 *
 * if (hasPermission(PERMISSIONS.SYSTEM_USER_BAN)) {
 *   // 显示封禁按钮
 * }
 *
 * @author BaSui 😎
 * @date 2025-11-01
 */

import { useAuthStore } from '@/stores/auth';

export const usePermission = () => {
  const { hasPermission, hasAnyPermission, hasRole } = useAuthStore();

  return {
    hasPermission,
    hasAnyPermission,
    hasRole,
  };
};
