/**
 * useAuthGuard Hook - 权限守卫大师！🛡️
 * @author BaSui 😎
 * @description 检查登录状态，未登录时友好提示并引导用户登录
 */

import { useCallback } from 'react';
import { useNavigate } from 'react-router-dom';

/**
 * useAuthGuard 返回值
 */
export interface UseAuthGuardResult {
  /**
   * 检查是否已登录（未登录时提示）
   * @param message 提示信息（可选）
   * @returns 是否已登录
   */
  checkAuth: (message?: string) => boolean;

  /**
   * 要求登录（未登录时跳转登录页）
   * @param returnUrl 登录后返回的 URL（可选）
   * @param message 提示信息（可选）
   * @returns 是否已登录
   */
  requireAuth: (returnUrl?: string, message?: string) => boolean;

  /**
   * 弹出登录确认框（用户手动确认）
   * @param action 操作名称（如：发布商品、下单等）
   * @param returnUrl 登录后返回的 URL（可选）
   * @returns Promise<boolean> 用户是否确认登录
   */
  confirmLogin: (action: string, returnUrl?: string) => Promise<boolean>;
}

/**
 * useAuthGuard Hook
 *
 * @description
 * 权限守卫 Hook，用于检查登录状态并友好提示用户登录。
 * 支持三种模式：
 * 1. checkAuth - 静默检查（返回布尔值）
 * 2. requireAuth - 强制登录（自动跳转）
 * 3. confirmLogin - 确认登录（用户手动确认）
 *
 * @returns 权限守卫方法
 *
 * @example
 * ```tsx
 * // 示例1：发布商品前检查登录
 * function PublishButton() {
 *   const { confirmLogin } = useAuthGuard();
 *
 *   const handlePublish = async () => {
 *     // 弹出登录确认框
 *     const confirmed = await confirmLogin('发布商品', '/publish');
 *     if (!confirmed) return;
 *
 *     // 执行发布逻辑...
 *   };
 *
 *   return <Button onClick={handlePublish}>发布商品</Button>;
 * }
 * ```
 *
 * @example
 * ```tsx
 * // 示例2：需要权限的页面（自动跳转）
 * function ProfilePage() {
 *   const { requireAuth } = useAuthGuard();
 *
 *   useEffect(() => {
 *     // 页面加载时检查登录
 *     requireAuth('/profile', '请先登录后查看个人中心');
 *   }, [requireAuth]);
 *
 *   return <div>个人中心内容</div>;
 * }
 * ```
 *
 * @example
 * ```tsx
 * // 示例3：收藏商品前检查登录
 * function FavoriteButton({ goodsId }: { goodsId: number }) {
 *   const { checkAuth } = useAuthGuard();
 *   const toast = useNotificationStore();
 *
 *   const handleFavorite = async () => {
 *     // 静默检查登录状态
 *     if (!checkAuth('收藏商品需要先登录')) {
 *       return;
 *     }
 *
 *     // 执行收藏逻辑...
 *   };
 *
 *   return <Button onClick={handleFavorite}>收藏</Button>;
 * }
 * ```
 */
export const useAuthGuard = (): UseAuthGuardResult => {
  const navigate = useNavigate();

  /**
   * 获取当前登录状态
   */
  const isAuthenticated = useCallback((): boolean => {
    // 从 localStorage 检查 Token
    const token = localStorage.getItem('auth_token');
    return !!token;
  }, []);

  /**
   * 静默检查登录状态
   */
  const checkAuth = useCallback(
    (message?: string): boolean => {
      const isAuth = isAuthenticated();

      if (!isAuth && message) {
        // 打印提示信息到控制台
        console.warn(`[Auth Guard] ⚠️ ${message}`);
      }

      return isAuth;
    },
    [isAuthenticated]
  );

  /**
   * 要求登录（自动跳转）
   */
  const requireAuth = useCallback(
    (returnUrl?: string, message?: string): boolean => {
      const isAuth = isAuthenticated();

      if (!isAuth) {
        // 打印提示信息
        console.warn(`[Auth Guard] 🚨 ${message || '请先登录'}`);

        // 保存返回 URL
        if (returnUrl) {
          sessionStorage.setItem('auth_return_url', returnUrl);
        }

        // 跳转登录页
        navigate('/login');
      }

      return isAuth;
    },
    [isAuthenticated, navigate]
  );

  /**
   * 弹出登录确认框（用户手动确认）
   */
  const confirmLogin = useCallback(
    async (action: string, returnUrl?: string): Promise<boolean> => {
      const isAuth = isAuthenticated();

      if (!isAuth) {
        // 使用浏览器原生确认框（暂时方案，后续可替换为更好的 Modal）
        const confirmed = window.confirm(
          `${action}需要先登录。\n\n是否前往登录页面？`
        );

        if (confirmed) {
          // 保存返回 URL
          if (returnUrl) {
            sessionStorage.setItem('auth_return_url', returnUrl);
          }

          // 跳转登录页
          navigate('/login');
        }

        return false;
      }

      return true;
    },
    [isAuthenticated, navigate]
  );

  return {
    checkAuth,
    requireAuth,
    confirmLogin,
  };
};

export default useAuthGuard;
