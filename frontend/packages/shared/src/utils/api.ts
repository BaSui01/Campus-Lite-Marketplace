/**
 * 🎯 BaSui 的 API 实例工厂
 * @description 统一创建和管理 OpenAPI 生成的 API 实例
 */

import { DefaultApi } from '../api';
import { axiosInstance, createApiConfig } from './http';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8200/api';

/**
 * 🚀 全局 API 实例（单例模式）
 *
 * 包含所有后端 API 接口，自动注入 JWT Token 和拦截器
 */
export const api = new DefaultApi(createApiConfig(), API_BASE_URL, axiosInstance);

/**
 * 🔄 更新 API 实例的 Token
 * @description 登录成功后调用此方法更新 Token
 */
export const updateApiToken = (token: string): void => {
  // Token 会自动通过 Axios 拦截器注入，无需手动更新
  console.log('[API] ✅ Token 已更新');
};

/**
 * 🎯 使用示例：
 *
 * ```typescript
 * import { api } from '@campus/shared';
 *
 * // 获取当前用户信息
 * const profile = await api.getCurrentUserProfile();
 *
 * // 获取物品列表
 * const goods = await api.listGoods({ page: 0, size: 10 });
 *
 * // 创建订单
 * const order = await api.createOrder({ createOrderRequest: { goodsId: 123 } });
 * ```
 */

export default api;
