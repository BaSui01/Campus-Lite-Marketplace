/**
 * 营销活动服务 - 促销引流，提升销量！🎁
 * @author BaSui 😎
 * @description 限时折扣、满减优惠、秒杀活动管理
 */

import { apiClient } from '../utils/request';

// ==================== 类型定义 ====================

/**
 * 活动类型枚举
 */
export enum CampaignType {
  DISCOUNT = 'DISCOUNT',           // 限时折扣
  FULL_REDUCTION = 'FULL_REDUCTION', // 满减优惠
  FLASH_SALE = 'FLASH_SALE',       // 秒杀活动
}

/**
 * 活动状态枚举
 */
export enum CampaignStatus {
  PENDING = 'PENDING',     // 待审核
  APPROVED = 'APPROVED',   // 已通过
  RUNNING = 'RUNNING',     // 进行中
  PAUSED = 'PAUSED',       // 已暂停
  ENDED = 'ENDED',         // 已结束
  REJECTED = 'REJECTED',   // 已拒绝
}

/**
 * 折扣类型
 */
export enum DiscountType {
  PERCENTAGE = 'PERCENTAGE',  // 百分比折扣（如8折）
  FIXED_AMOUNT = 'FIXED_AMOUNT', // 固定金额减免（如减10元）
}

/**
 * 折扣配置
 */
export interface DiscountConfig {
  discountType: DiscountType;   // 折扣类型
  discountValue: number;         // 折扣值（0.8表示8折，10表示减10元）
  threshold?: number;            // 门槛金额（满减活动的"满"金额）
  maxDiscount?: number;          // 最大优惠金额
}

/**
 * 营销活动
 */
export interface MarketingCampaign {
  id?: number;
  merchantId?: number;
  campaignName: string;          // 活动名称
  campaignType: CampaignType;    // 活动类型
  startTime: string;             // 开始时间
  endTime: string;               // 结束时间
  status?: CampaignStatus;       // 活动状态
  discountConfig: DiscountConfig; // 折扣配置
  goodsIds: number[];            // 参与商品ID列表
  stockLimit?: number;           // 库存限制（秒杀活动）
  stockRemaining?: number;       // 剩余库存
  participationCount?: number;   // 参与人数
  salesAmount?: number;          // 活动销售额
  createdAt?: string;
  updatedAt?: string;
}

/**
 * 创建活动请求
 */
export interface CreateCampaignRequest {
  campaignName: string;
  campaignType: CampaignType;
  startTime: string;
  endTime: string;
  discountConfig: DiscountConfig;
  goodsIds: number[];
  stockLimit?: number;
}

/**
 * 活动列表查询参数
 */
export interface CampaignListParams {
  page?: number;
  size?: number;
  status?: CampaignStatus;
  campaignType?: CampaignType;
  merchantId?: number;
}

/**
 * 活动统计
 */
export interface CampaignStatistics {
  totalCampaigns: number;        // 总活动数
  runningCampaigns: number;      // 进行中活动数
  totalSalesAmount: number;      // 总销售额
  totalParticipation: number;    // 总参与人数
  avgConversionRate: number;     // 平均转化率
}

// ==================== 活动类型配置 ====================

export const CAMPAIGN_TYPE_CONFIG = {
  [CampaignType.DISCOUNT]: {
    name: '限时折扣',
    icon: '🏷️',
    color: '#1890ff',
    description: '商品打折优惠，吸引用户购买',
  },
  [CampaignType.FULL_REDUCTION]: {
    name: '满减优惠',
    icon: '💰',
    color: '#52c41a',
    description: '满足金额门槛即可享受优惠',
  },
  [CampaignType.FLASH_SALE]: {
    name: '秒杀活动',
    icon: '⚡',
    color: '#f5222d',
    description: '限时限量抢购，营造紧迫感',
  },
};

export const CAMPAIGN_STATUS_CONFIG = {
  [CampaignStatus.PENDING]: {
    name: '待审核',
    color: '#faad14',
  },
  [CampaignStatus.APPROVED]: {
    name: '已通过',
    color: '#52c41a',
  },
  [CampaignStatus.RUNNING]: {
    name: '进行中',
    color: '#1890ff',
  },
  [CampaignStatus.PAUSED]: {
    name: '已暂停',
    color: '#8c8c8c',
  },
  [CampaignStatus.ENDED]: {
    name: '已结束',
    color: '#d9d9d9',
  },
  [CampaignStatus.REJECTED]: {
    name: '已拒绝',
    color: '#f5222d',
  },
};

// ==================== Service 类 ====================

/**
 * 营销活动服务类
 */
class MarketingService {
  /**
   * 创建营销活动
   */
  async createCampaign(request: CreateCampaignRequest): Promise<MarketingCampaign> {
    const response = await apiClient.post('/marketing/campaigns', request);
    return response.data;
  }

  /**
   * 获取活动列表
   */
  async getCampaigns(params: CampaignListParams = {}): Promise<{
    content: MarketingCampaign[];
    totalElements: number;
    totalPages: number;
  }> {
    const response = await apiClient.get('/marketing/campaigns', { params });
    return response.data;
  }

  /**
   * 获取商家的活动列表
   */
  async getMerchantCampaigns(merchantId?: number): Promise<MarketingCampaign[]> {
    const url = merchantId
      ? `/marketing/campaigns/merchant/${merchantId}`
      : '/marketing/campaigns/my';
    
    const response = await apiClient.get(url);
    return response.data;
  }

  /**
   * 获取进行中的活动
   */
  async getRunningCampaigns(): Promise<MarketingCampaign[]> {
    const response = await apiClient.get('/marketing/campaigns/running');
    return response.data;
  }

  /**
   * 获取活动详情
   */
  async getCampaignDetail(campaignId: number): Promise<MarketingCampaign> {
    const response = await apiClient.get(`/marketing/campaigns/${campaignId}`);
    return response.data;
  }

  /**
   * 更新活动
   */
  async updateCampaign(campaignId: number, data: Partial<CreateCampaignRequest>): Promise<MarketingCampaign> {
    const response = await apiClient.put(`/marketing/campaigns/${campaignId}`, data);
    return response.data;
  }

  /**
   * 暂停活动
   */
  async pauseCampaign(campaignId: number): Promise<void> {
    await apiClient.post(`/marketing/campaigns/${campaignId}/pause`);
  }

  /**
   * 恢复活动
   */
  async resumeCampaign(campaignId: number): Promise<void> {
    await apiClient.post(`/marketing/campaigns/${campaignId}/resume`);
  }

  /**
   * 结束活动
   */
  async endCampaign(campaignId: number): Promise<void> {
    await apiClient.post(`/marketing/campaigns/${campaignId}/end`);
  }

  /**
   * 删除活动
   */
  async deleteCampaign(campaignId: number): Promise<void> {
    await apiClient.delete(`/marketing/campaigns/${campaignId}`);
  }

  /**
   * 获取活动统计
   */
  async getCampaignStatistics(): Promise<CampaignStatistics> {
    const response = await apiClient.get('/marketing/campaigns/statistics');
    return response.data;
  }

  /**
   * 扣减活动库存（秒杀）
   */
  async deductStock(campaignId: number, quantity: number): Promise<boolean> {
    const response = await apiClient.post(`/marketing/campaigns/${campaignId}/deduct-stock`, {
      quantity,
    });
    return response.data;
  }

  /**
   * 检查商品是否参与活动
   */
  async checkGoodsInCampaign(goodsId: number): Promise<MarketingCampaign | null> {
    try {
      const response = await apiClient.get(`/marketing/campaigns/goods/${goodsId}`);
      return response.data;
    } catch (err) {
      return null;
    }
  }

  // ==================== 工具方法 ====================

  /**
   * 格式化折扣显示
   */
  formatDiscount(config: DiscountConfig): string {
    if (config.discountType === DiscountType.PERCENTAGE) {
      return `${(config.discountValue * 10).toFixed(1)}折`;
    }
    return `减¥${config.discountValue.toFixed(0)}`;
  }

  /**
   * 计算折扣后价格
   */
  calculateDiscountedPrice(originalPrice: number, config: DiscountConfig): number {
    if (config.discountType === DiscountType.PERCENTAGE) {
      return originalPrice * config.discountValue;
    }
    return Math.max(0, originalPrice - config.discountValue);
  }

  /**
   * 检查是否满足活动门槛
   */
  checkThreshold(amount: number, config: DiscountConfig): boolean {
    if (!config.threshold) return true;
    return amount >= config.threshold;
  }

  /**
   * 计算活动倒计时
   */
  calculateCountdown(endTime: string): {
    days: number;
    hours: number;
    minutes: number;
    seconds: number;
    total: number;
  } {
    const end = new Date(endTime).getTime();
    const now = new Date().getTime();
    const total = Math.max(0, end - now);

    const seconds = Math.floor((total / 1000) % 60);
    const minutes = Math.floor((total / 1000 / 60) % 60);
    const hours = Math.floor((total / (1000 * 60 * 60)) % 24);
    const days = Math.floor(total / (1000 * 60 * 60 * 24));

    return { days, hours, minutes, seconds, total };
  }

  /**
   * 格式化倒计时显示
   */
  formatCountdown(endTime: string): string {
    const { days, hours, minutes, seconds } = this.calculateCountdown(endTime);
    
    if (days > 0) {
      return `${days}天${hours}小时`;
    }
    if (hours > 0) {
      return `${hours}小时${minutes}分钟`;
    }
    if (minutes > 0) {
      return `${minutes}分钟${seconds}秒`;
    }
    return `${seconds}秒`;
  }

  /**
   * 检查活动是否进行中
   */
  isRunning(campaign: MarketingCampaign): boolean {
    if (campaign.status !== CampaignStatus.RUNNING) return false;
    
    const now = new Date().getTime();
    const start = new Date(campaign.startTime).getTime();
    const end = new Date(campaign.endTime).getTime();
    
    return now >= start && now <= end;
  }

  /**
   * 获取活动剩余时间文本
   */
  getRemainingTimeText(campaign: MarketingCampaign): string {
    const now = new Date().getTime();
    const start = new Date(campaign.startTime).getTime();
    const end = new Date(campaign.endTime).getTime();

    if (now < start) {
      return `距离开始 ${this.formatCountdown(campaign.startTime)}`;
    }
    if (now > end) {
      return '已结束';
    }
    return `剩余 ${this.formatCountdown(campaign.endTime)}`;
  }

  /**
   * 获取活动进度百分比
   */
  getProgressPercentage(campaign: MarketingCampaign): number {
    const now = new Date().getTime();
    const start = new Date(campaign.startTime).getTime();
    const end = new Date(campaign.endTime).getTime();

    if (now < start) return 0;
    if (now > end) return 100;

    return Math.round(((now - start) / (end - start)) * 100);
  }

  /**
   * 获取活动类型配置
   */
  getCampaignTypeConfig(type: CampaignType) {
    return CAMPAIGN_TYPE_CONFIG[type];
  }

  /**
   * 获取活动状态配置
   */
  getCampaignStatusConfig(status: CampaignStatus) {
    return CAMPAIGN_STATUS_CONFIG[status];
  }
}

// ==================== 导出单例 ====================

export const marketingService = new MarketingService();
