/**
 * 管理端认证 Hook
 * @author BaSui 😎
 * @date 2025-11-02
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
