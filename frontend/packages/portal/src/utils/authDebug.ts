/**
 * 认证调试工具
 * @author BaSui 😎
 * @description 帮助调试认证状态和 Token 问题
 */

import { useAuthStore } from '../store';
import { getAccessToken, isTokenValid } from '@campus/shared/utils';

/**
 * 打印认证状态调试信息
 */
export function debugAuthState() {
  console.group('🔍 [Auth Debug] 认证状态检查');

  try {
    // 1. 检查 Zustand Store 状态
    const store = useAuthStore.getState();
    console.log('📦 Zustand Store 状态:');
    console.log('  - isAuthenticated:', store.isAuthenticated);
    console.log('  - user:', store.user);
    console.log('  - accessToken:', store.accessToken ? '存在 ✅' : '不存在 ❌');
    console.log('  - refreshToken:', store.refreshToken ? '存在 ✅' : '不存在 ❌');

    // 2. 检查 localStorage
    const authStorage = localStorage.getItem('auth-storage');
    console.log('💾 localStorage [auth-storage]:', authStorage ? '存在 ✅' : '不存在 ❌');
    if (authStorage) {
      try {
        const parsedAuth = JSON.parse(authStorage);
        console.log('  - state.isAuthenticated:', parsedAuth?.state?.isAuthenticated);
        console.log('  - state.accessToken:', parsedAuth?.state?.accessToken ? '存在 ✅' : '不存在 ❌');
        console.log('  - state.user:', parsedAuth?.state?.user?.username || '未知');
      } catch (e) {
        console.error('  ❌ localStorage 解析失败:', e);
      }
    }

    // 3. 检查 Token 有效性
    const token = getAccessToken();
    console.log('🔑 Access Token:');
    console.log('  - 存在:', token ? '是 ✅' : '否 ❌');
    if (token) {
      const isValid = isTokenValid(token);
      console.log('  - 有效:', isValid ? '是 ✅' : '否 ❌');
    }

    // 4. 总结
    console.log('📊 总结:');
    const shouldBeAuthenticated = store.isAuthenticated && store.accessToken && isTokenValid(store.accessToken);
    console.log('  - 应该允许访问受保护页面:', shouldBeAuthenticated ? '是 ✅' : '否 ❌');

  } catch (error) {
    console.error('❌ 调试过程出错:', error);
  } finally {
    console.groupEnd();
  }
}

// 开发环境自动在控制台暴露调试函数
if (import.meta.env.DEV) {
  (window as any).debugAuth = debugAuthState;
  console.log('💡 提示：在控制台输入 debugAuth() 可查看认证状态详情');
}

export default debugAuthState;
