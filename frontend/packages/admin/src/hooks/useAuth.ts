/**
 * 认证 Hook
 *
 * 使用示例：
 * const { user, isAuthenticated, login, logout } = useAuth();
 *
 * @author BaSui 😎
 * @date 2025-11-01
 */

import { useAuthStore } from '@/stores/auth';

export const useAuth = () => {
  const { token, user, isAuthenticated, login, logout } = useAuthStore();

  return {
    token,
    user,
    isAuthenticated,
    login,
    logout,
  };
};
