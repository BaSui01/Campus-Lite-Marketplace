/**
 * Token 工具函数
 * @author BaSui 😎
 * @description JWT Token 验证和解析工具
 */

/**
 * JWT Token 载荷接口
 */
export interface JwtPayload {
  userId: number;
  username: string;
  roles: string[];
  permissions: string[];
  exp: number; // 过期时间（Unix 时间戳，秒）
  iat: number; // 签发时间（Unix 时间戳，秒）
  sub: string; // 主题（通常是用户名）
}

/**
 * 解码 JWT Token（不验证签名，仅解析载荷）
 * @param token JWT Token
 * @returns 解码后的载荷，解析失败返回 null
 */
export function decodeJwtToken(token: string): JwtPayload | null {
  try {
    // JWT 格式：header.payload.signature
    const parts = token.split('.');
    if (parts.length !== 3) {
      console.warn('[Token Utils] ⚠️ Token 格式错误，不是标准的 JWT 格式');
      return null;
    }

    // 解码 payload（Base64URL 编码）
    const payload = parts[1];
    if (!payload) {
      console.warn('[Token Utils] ⚠️ Token payload 部分缺失');
      return null;
    }
    const decodedPayload = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
    const parsedPayload = JSON.parse(decodedPayload);

    return parsedPayload as JwtPayload;
  } catch (error) {
    console.error('[Token Utils] ❌ Token 解码失败:', error);
    return null;
  }
}

/**
 * 检查 Token 是否过期
 * @param token JWT Token
 * @returns true 表示已过期，false 表示未过期
 */
export function isTokenExpired(token: string): boolean {
  try {
    const payload = decodeJwtToken(token);
    if (!payload || !payload.exp) {
      console.warn('[Token Utils] ⚠️ Token 载荷无效或缺少过期时间');
      return true; // 无效 Token 视为已过期
    }

    // exp 是 Unix 时间戳（秒），需要转换为毫秒
    const expirationTime = payload.exp * 1000;
    const currentTime = Date.now();

    const isExpired = currentTime >= expirationTime;

    if (isExpired) {
      console.log('[Token Utils] ⏰ Token 已过期:', {
        expirationTime: new Date(expirationTime).toLocaleString(),
        currentTime: new Date(currentTime).toLocaleString(),
      });
    } else {
      const remainingTime = Math.floor((expirationTime - currentTime) / 1000 / 60);
      console.log(`[Token Utils] ✅ Token 有效，剩余 ${remainingTime} 分钟`);
    }

    return isExpired;
  } catch (error) {
    console.error('[Token Utils] ❌ Token 过期检查失败:', error);
    return true; // 检查失败视为已过期
  }
}

/**
 * 从 Token 中获取用户信息
 * @param token JWT Token
 * @returns 用户信息，解析失败返回 null
 */
export function getUserInfoFromToken(token: string): {
  userId: number;
  username: string;
  roles: string[];
  permissions: string[];
} | null {
  try {
    const payload = decodeJwtToken(token);
    if (!payload) {
      return null;
    }

    return {
      userId: payload.userId,
      username: payload.username,
      roles: payload.roles || [],
      permissions: payload.permissions || [],
    };
  } catch (error) {
    console.error('[Token Utils] ❌ 获取用户信息失败:', error);
    return null;
  }
}

/**
 * 检查 Token 是否有效（存在且未过期）
 * @param token JWT Token
 * @returns true 表示有效，false 表示无效
 */
export function isTokenValid(token: string | null | undefined): boolean {
  if (!token) {
    console.log('[Token Utils] ⚠️ Token 不存在');
    return false;
  }

  if (isTokenExpired(token)) {
    console.log('[Token Utils] ⚠️ Token 已过期');
    return false;
  }

  console.log('[Token Utils] ✅ Token 有效');
  return true;
}

/**
 * 从 localStorage 获取 Access Token
 * @returns Access Token，不存在返回 null
 */
export function getAccessToken(): string | null {
  try {
    // 从 Zustand persist 存储中获取
    const authStorage = localStorage.getItem('auth-storage');
    if (!authStorage) {
      return null;
    }

    const authData = JSON.parse(authStorage);
    return authData?.state?.accessToken || null;
  } catch (error) {
    console.error('[Token Utils] ❌ 获取 Access Token 失败:', error);
    return null;
  }
}

/**
 * 检查当前用户是否已登录且 Token 有效
 * @returns true 表示已登录且 Token 有效，false 表示未登录或 Token 无效
 */
export function isUserAuthenticated(): boolean {
  const token = getAccessToken();
  return isTokenValid(token);
}
