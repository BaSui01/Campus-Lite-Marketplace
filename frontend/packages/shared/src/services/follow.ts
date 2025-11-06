/**
 * 关注 API 服务
 * @author BaSui 😎
 * @description 关注卖家、取消关注、查询关注列表等接口
 */

import { getApi } from '../utils/apiClient';
import type { FollowResponse, GoodsResponse } from '../api/models';

/**
 * 关注用户动态项
 */
export interface FollowingActivity {
  sellerId: number;
  sellerName: string;
  sellerAvatar?: string;
  goods: GoodsResponse;
  publishedAt: string;
}

/**
 * 关注 API 服务类
 */
export class FollowService {
  /**
   * 关注卖家
   * @param sellerId 卖家 ID
   */
  async followSeller(sellerId: number): Promise<void> {
    const api = getApi();
    await api.follow(sellerId);
  }

  /**
   * 取消关注卖家
   * @param sellerId 卖家 ID
   */
  async unfollowSeller(sellerId: number): Promise<void> {
    const api = getApi();
    await api.unfollow(sellerId);
  }

  /**
   * 查询关注列表
   * @returns 关注的卖家列表
   */
  async listFollowings(): Promise<FollowResponse[]> {
    const api = getApi();
    const response = await api.listFollowings();
    return (response.data.data as FollowResponse[]) || [];
  }

  /**
   * 检查是否已关注某个卖家（前端判断）
   * @param sellerId 卖家 ID
   * @returns 是否已关注
   */
  async isFollowing(sellerId: number): Promise<boolean> {
    const followings = await this.listFollowings();
    return followings.some((f) => f.sellerId === sellerId);
  }

  /**
   * 获取关注数量
   * @returns 关注的卖家数量
   */
  async getFollowingCount(): Promise<number> {
    const followings = await this.listFollowings();
    return followings.length;
  }

  /**
   * 获取关注用户的最新动态（需要额外接口支持，这里是占位实现）
   * @param page 页码
   * @param size 每页大小
   * @returns 关注用户的商品动态列表
   * 
   * @todo 需要后端提供专门的 API：GET /following/activities
   */
  async getFollowingActivities(page: number = 0, size: number = 20): Promise<FollowingActivity[]> {
    // TODO: 等待后端提供关注动态接口
    // 临时方案：获取关注列表，然后获取每个卖家的最新商品
    console.warn('[FollowService] getFollowingActivities: 需要后端提供专门的关注动态接口');
    
    const followings = await this.listFollowings();
    const activities: FollowingActivity[] = [];
    
    // 这只是示例实现，实际应该由后端聚合返回
    // 因为需要跨多个卖家查询商品，性能和逻辑都不合理
    
    return activities;
  }

  /**
   * 批量关注（如果需要）
   * @param sellerIds 卖家 ID 列表
   */
  async batchFollow(sellerIds: number[]): Promise<void> {
    const promises = sellerIds.map((id) => this.followSeller(id));
    await Promise.all(promises);
  }

  /**
   * 批量取消关注（如果需要）
   * @param sellerIds 卖家 ID 列表
   */
  async batchUnfollow(sellerIds: number[]): Promise<void> {
    const promises = sellerIds.map((id) => this.unfollowSeller(id));
    await Promise.all(promises);
  }
}

/**
 * 导出单例实例
 */
export const followService = new FollowService();
