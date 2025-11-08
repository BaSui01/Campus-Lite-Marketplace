/**
 * ✅ 限流管理服务 - 已重构为 OpenAPI
 * @author BaSui 😎
 * @description 基于 OpenAPI 生成的 DefaultApi，零手写路径！
 *
 * 功能：
 * - 限流规则查询
 * - 限流开关管理
 * - 用户白名单管理
 * - IP 白名单管理
 * - IP 黑名单管理
 *
 * ⚠️ 注意：所有接口需要管理员权限（ADMIN角色）
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
   * 获取限流规则
   * GET /api/admin/rate-limit/rules
   * @returns 限流规则（开关状态、白名单、黑名单）
   */
  async getRules(): Promise<RateLimitRules> {
    const api = getApi();
    const response = await api.getRules();
    const data = response.data.data as Record<string, any>;

    return {
      enabled: Boolean(data.enabled),
      userWhitelist: (data.userWhitelist || []) as number[],
      ipWhitelist: (data.ipWhitelist || []) as string[],
      ipBlacklist: (data.ipBlacklist || []) as string[],
    };
  }

  /**
   * 设置限流开关
   * POST /api/admin/rate-limit/enabled/{enabled}
   * @param enabled 是否启用限流
   */
  async setEnabled(enabled: boolean): Promise<void> {
    const api = getApi();
    await api.setEnabled({ enabled });
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
