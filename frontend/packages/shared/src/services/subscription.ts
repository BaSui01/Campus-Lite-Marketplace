/**
 * 订阅 API 服务
 * @author BaSui 😎
 * @description 关键词订阅、取消订阅、查询订阅列表等接口
 */

import { getApi } from '../utils/apiClient';
import type { SubscriptionResponse, CreateSubscriptionRequest } from '../api/models';

/**
 * 订阅类型枚举
 */
export enum SubscriptionType {
  KEYWORD = 'KEYWORD',     // 关键词订阅
  CATEGORY = 'CATEGORY',   // 分类订阅
  TOPIC = 'TOPIC',         // 话题订阅
  TAG = 'TAG',             // 标签订阅
}

/**
 * 创建订阅请求参数
 */
export interface CreateSubscriptionParams {
  keyword: string;
  campusId?: number;
  categoryId?: number;
  type?: SubscriptionType;
}

/**
 * 订阅匹配的商品项（动态流用）
 */
export interface SubscriptionMatch {
  subscriptionId: number;
  keyword: string;
  matchedGoods: any; // GoodsResponse
  matchedAt: string;
}

/**
 * 订阅 API 服务类
 */
export class SubscriptionService {
  /**
   * 新增订阅
   * @param params 订阅参数
   * @returns 订阅 ID
   */
  async subscribe(params: CreateSubscriptionParams): Promise<number> {
    const api = getApi();
    
    const request: CreateSubscriptionRequest = {
      keyword: params.keyword,
      campusId: params.campusId,
    };
    
    const response = await api.subscribe({ createSubscriptionRequest: request });
    return (response.data.data as number) || 0;
  }

  /**
   * 取消订阅
   * @param id 订阅 ID
   */
  async unsubscribe(id: number): Promise<void> {
    const api = getApi();
    await api.unsubscribe(id);
  }

  /**
   * 查询订阅列表
   * @returns 订阅列表
   */
  async listSubscriptions(): Promise<SubscriptionResponse[]> {
    const api = getApi();
    const response = await api.listSubscriptions();
    return (response.data.data as SubscriptionResponse[]) || [];
  }

  /**
   * 查询订阅数量
   * @returns 订阅数量
   */
  async getSubscriptionCount(): Promise<number> {
    const subscriptions = await this.listSubscriptions();
    return subscriptions.length;
  }

  /**
   * 检查关键词是否已订阅（前端判断）
   * @param keyword 关键词
   * @returns 是否已订阅
   */
  async isSubscribed(keyword: string): Promise<boolean> {
    const subscriptions = await this.listSubscriptions();
    return subscriptions.some((s) => s.keyword === keyword);
  }

  /**
   * 批量订阅关键词
   * @param keywords 关键词列表
   */
  async batchSubscribe(keywords: string[]): Promise<void> {
    const promises = keywords.map((keyword) => this.subscribe({ keyword }));
    await Promise.all(promises);
  }

  /**
   * 批量取消订阅
   * @param ids 订阅 ID 列表
   */
  async batchUnsubscribe(ids: number[]): Promise<void> {
    const promises = ids.map((id) => this.unsubscribe(id));
    await Promise.all(promises);
  }

  /**
   * 获取订阅匹配的最新商品（动态流）
   * @param page 页码
   * @param size 每页大小
   * @returns 匹配的商品列表
   * 
   * @todo 需要后端提供专门的 API：GET /subscribe/feed
   */
  async getSubscriptionFeed(page: number = 0, size: number = 20): Promise<SubscriptionMatch[]> {
    // TODO: 等待后端提供订阅动态流接口
    console.warn('[SubscriptionService] getSubscriptionFeed: 需要后端提供专门的订阅动态流接口');
    
    // 临时返回空数组
    return [];
  }

  /**
   * 搜索订阅（前端筛选）
   * @param searchText 搜索文本
   * @returns 匹配的订阅列表
   */
  async searchSubscriptions(searchText: string): Promise<SubscriptionResponse[]> {
    const subscriptions = await this.listSubscriptions();
    
    if (!searchText) {
      return subscriptions;
    }
    
    const lowerSearch = searchText.toLowerCase();
    return subscriptions.filter((s) => 
      s.keyword?.toLowerCase().includes(lowerSearch)
    );
  }

  /**
   * 按创建时间排序订阅（前端排序）
   * @param order 排序方向
   * @returns 排序后的订阅列表
   */
  async sortSubscriptions(order: 'asc' | 'desc' = 'desc'): Promise<SubscriptionResponse[]> {
    const subscriptions = await this.listSubscriptions();
    
    return subscriptions.sort((a, b) => {
      const timeA = new Date(a.createdAt || 0).getTime();
      const timeB = new Date(b.createdAt || 0).getTime();
      
      return order === 'asc' ? timeA - timeB : timeB - timeA;
    });
  }
}

/**
 * 导出单例实例
 */
export const subscriptionService = new SubscriptionService();
