/**
 * ✅ 限流管理服务 - 已重构为 OpenAPI（部分）
 * @author BaSui 😎
 * @description 基于 OpenAPI 生成的 DefaultApi，零手写路径！
 *
 * 功能：
 * - 用户白名单管理
 * - IP 白名单管理
 * - IP 黑名单管理
 *
 * ⚠️ 注意：
 * - 所有接口需要管理员权限（ADMIN角色）
 * - getRules() 和 setEnabled() 方法暂未实现（后端接口缺失）
 *
 * 📋 API 路径：/api/admin/rate-limit/*
 */

import { getApi } from '../utils/apiClient';

/**
 * 限流规则（暂未实现）
 */
export interface RateLimitRules {
  enabled: boolean;
  userWhitelist: number[];
  ipWhitelist: string[];
  ipBlacklist: string[];
}

/**
 * 限流管理服务类
 */
export class RateLimitService {
  /**
   * ⚠️ 获取限流规则（暂未实现）
   * TODO: 等待后端实现 GET /api/admin/rate-limit/rules
   */
  async getRules(): Promise<RateLimitRules> {
    throw new Error('getRules() 方法暂未实现，等待后端接口');
  }

  /**
   * ⚠️ 设置限流开关（暂未实现）
   * TODO: 等待后端实现 POST /api/admin/rate-limit/enabled/{enabled}
   */
  async setEnabled(enabled: boolean): Promise<void> {
    throw new Error('setEnabled() 方法暂未实现，等待后端接口');
  }

  /**
   * 添加用户到白名单
   * POST /api/admin/rate-limit/whitelist/users/{userId}
   * @param userId 用户ID
   */
  async addUserWhitelist(userId: number): Promise<void> {
    const api = getApi();
    await api.addUserWhitelist({ userId });
  }

  /**
   * 从白名单移除用户
   * DELETE /api/admin/rate-limit/whitelist/users/{userId}
   * @param userId 用户ID
   */
  async removeUserWhitelist(userId: number): Promise<void> {
    const api = getApi();
    await api.removeUserWhitelist({ userId });
  }

  /**
   * 添加 IP 到白名单
   * POST /api/admin/rate-limit/whitelist/ips/{ip}
   * @param ip IP 地址
   */
  async addIpWhitelist(ip: string): Promise<void> {
    const api = getApi();
    await api.addIpWhitelist({ ip });
  }

  /**
   * 从白名单移除 IP
   * DELETE /api/admin/rate-limit/whitelist/ips/{ip}
   * @param ip IP 地址
   */
  async removeIpWhitelist(ip: string): Promise<void> {
    const api = getApi();
    await api.removeIpWhitelist({ ip });
  }

  /**
   * 添加 IP 到黑名单
   * POST /api/admin/rate-limit/blacklist/ips/{ip}
   * @param ip IP 地址
   */
  async addIpBlacklist(ip: string): Promise<void> {
    const api = getApi();
    await api.addIpBlacklist({ ip });
  }

  /**
   * 从黑名单移除 IP
   * DELETE /api/admin/rate-limit/blacklist/ips/{ip}
   * @param ip IP 地址
   */
  async removeIpBlacklist(ip: string): Promise<void> {
    const api = getApi();
    await api.removeIpBlacklist({ ip });
  }
}

export const rateLimitService = new RateLimitService();
export default rateLimitService;
