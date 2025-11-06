/**
 * 信用服务 - 用户信用评级管理！⭐
 * @author BaSui 😎
 * @description 信用等级查询、信用分展示、信用历史记录
 */

import { apiClient } from '../utils/request';

// ==================== 类型定义 ====================

/**
 * 信用等级枚举
 */
export enum CreditLevel {
  NEWBIE = 'NEWBIE',     // 新手 (0-10单)
  BRONZE = 'BRONZE',     // 铜牌 (11-50单)
  SILVER = 'SILVER',     // 银牌 (51-100单)
  GOLD = 'GOLD',         // 金牌 (101-200单)
  DIAMOND = 'DIAMOND',   // 钻石 (201+单)
}

/**
 * 信用等级信息
 */
export interface CreditLevelInfo {
  level: CreditLevel;
  levelName: string;        // 等级名称（新手、铜牌、银牌、金牌、钻石）
  description: string;      // 等级描述
  minOrders: number;        // 最小订单数
  maxOrders: number;        // 最大订单数
  color: string;            // 等级颜色
  icon: string;             // 等级图标
}

/**
 * 用户信用信息
 */
export interface UserCreditInfo {
  userId: number;
  username: string;
  avatar?: string;
  
  // 信用分
  creditScore: number;      // 信用分（0-200）
  creditLevel: CreditLevel; // 信用等级
  
  // 信用评分明细
  orderCount: number;       // 完成订单数
  positiveRate: number;     // 好评率（0-1）
  avgResponseTime: number;  // 平均响应时间（分钟）
  
  // 信用等级进度
  currentLevelInfo: CreditLevelInfo;
  nextLevelInfo?: CreditLevelInfo;
  progressToNextLevel: number; // 到下一等级的进度（0-1）
  
  // 信用历史
  creditHistory?: CreditHistoryItem[];
}

/**
 * 信用历史记录项
 */
export interface CreditHistoryItem {
  id: number;
  changeType: string;       // 变动类型（交易完成、好评、差评、违规等）
  changeValue: number;      // 变动值（+/-）
  reason: string;           // 变动原因
  createdAt: string;        // 发生时间
}

/**
 * 信用统计信息
 */
export interface CreditStatistics {
  totalUsers: number;           // 总用户数
  avgCreditScore: number;       // 平均信用分
  levelDistribution: {          // 等级分布
    [key in CreditLevel]: number;
  };
}

// ==================== 信用等级配置 ====================

/**
 * 信用等级详细信息配置
 */
export const CREDIT_LEVEL_CONFIG: Record<CreditLevel, CreditLevelInfo> = {
  [CreditLevel.NEWBIE]: {
    level: CreditLevel.NEWBIE,
    levelName: '新手',
    description: '完成0-10单交易',
    minOrders: 0,
    maxOrders: 10,
    color: '#95a5a6',
    icon: '🌱',
  },
  [CreditLevel.BRONZE]: {
    level: CreditLevel.BRONZE,
    levelName: '铜牌',
    description: '完成11-50单交易',
    minOrders: 11,
    maxOrders: 50,
    color: '#cd7f32',
    icon: '🥉',
  },
  [CreditLevel.SILVER]: {
    level: CreditLevel.SILVER,
    levelName: '银牌',
    description: '完成51-100单交易',
    minOrders: 51,
    maxOrders: 100,
    color: '#c0c0c0',
    icon: '🥈',
  },
  [CreditLevel.GOLD]: {
    level: CreditLevel.GOLD,
    levelName: '金牌',
    description: '完成101-200单交易',
    minOrders: 101,
    maxOrders: 200,
    color: '#ffd700',
    icon: '🥇',
  },
  [CreditLevel.DIAMOND]: {
    level: CreditLevel.DIAMOND,
    levelName: '钻石',
    description: '完成201+单交易',
    minOrders: 201,
    maxOrders: Number.MAX_SAFE_INTEGER,
    color: '#b9f2ff',
    icon: '💎',
  },
};

// ==================== Service 类 ====================

/**
 * 信用服务类
 */
class CreditService {
  /**
   * 获取当前用户的信用信息
   */
  async getMyCredit(): Promise<UserCreditInfo> {
    const response = await apiClient.get('/credit/my');
    return this.enrichCreditInfo(response.data);
  }

  /**
   * 获取指定用户的信用信息
   */
  async getUserCredit(userId: number): Promise<UserCreditInfo> {
    const response = await apiClient.get(`/credit/user/${userId}`);
    return this.enrichCreditInfo(response.data);
  }

  /**
   * 获取信用历史记录
   */
  async getCreditHistory(page: number = 0, size: number = 20): Promise<{
    content: CreditHistoryItem[];
    totalElements: number;
    totalPages: number;
  }> {
    const response = await apiClient.get('/credit/history', {
      params: { page, size },
    });
    return response.data;
  }

  /**
   * 获取信用统计信息（管理员）
   */
  async getCreditStatistics(): Promise<CreditStatistics> {
    const response = await apiClient.get('/credit/statistics');
    return response.data;
  }

  /**
   * 计算信用等级（客户端增强）
   * 基于订单数量和好评率计算信用等级
   */
  calculateCreditLevel(orderCount: number, positiveRate: number): CreditLevel {
    // 先根据订单数量获取基础等级
    let baseLevel = CreditLevel.NEWBIE;
    
    for (const [level, config] of Object.entries(CREDIT_LEVEL_CONFIG)) {
      if (orderCount >= config.minOrders && orderCount <= config.maxOrders) {
        baseLevel = level as CreditLevel;
        break;
      }
    }

    // 好评率 < 80% 降级
    if (positiveRate < 0.8) {
      const levels = Object.keys(CreditLevel) as CreditLevel[];
      const currentIndex = levels.indexOf(baseLevel);
      if (currentIndex > 0) {
        return levels[currentIndex - 1];
      }
    }

    // 好评率 >= 95% 且接近下一级，可以升级
    if (positiveRate >= 0.95) {
      const config = CREDIT_LEVEL_CONFIG[baseLevel];
      if (orderCount >= config.maxOrders * 0.8) {
        const levels = Object.keys(CreditLevel) as CreditLevel[];
        const currentIndex = levels.indexOf(baseLevel);
        if (currentIndex < levels.length - 1) {
          return levels[currentIndex + 1];
        }
      }
    }

    return baseLevel;
  }

  /**
   * 获取信用等级配置信息
   */
  getLevelConfig(level: CreditLevel): CreditLevelInfo {
    return CREDIT_LEVEL_CONFIG[level];
  }

  /**
   * 计算到下一等级的进度
   */
  calculateProgressToNextLevel(orderCount: number, currentLevel: CreditLevel): number {
    const currentConfig = CREDIT_LEVEL_CONFIG[currentLevel];
    const levels = Object.keys(CreditLevel) as CreditLevel[];
    const currentIndex = levels.indexOf(currentLevel);

    // 已经是最高等级
    if (currentIndex >= levels.length - 1) {
      return 1;
    }

    const nextLevel = levels[currentIndex + 1];
    const nextConfig = CREDIT_LEVEL_CONFIG[nextLevel];

    // 计算进度 = (当前订单数 - 当前等级最小值) / (下一等级最小值 - 当前等级最小值)
    const progress = (orderCount - currentConfig.minOrders) / (nextConfig.minOrders - currentConfig.minOrders);
    return Math.max(0, Math.min(1, progress));
  }

  /**
   * 获取下一等级信息
   */
  getNextLevelInfo(currentLevel: CreditLevel): CreditLevelInfo | undefined {
    const levels = Object.keys(CreditLevel) as CreditLevel[];
    const currentIndex = levels.indexOf(currentLevel);

    if (currentIndex >= levels.length - 1) {
      return undefined;
    }

    return CREDIT_LEVEL_CONFIG[levels[currentIndex + 1]];
  }

  /**
   * 增强信用信息（添加前端计算的字段）
   */
  private enrichCreditInfo(data: any): UserCreditInfo {
    const currentLevelInfo = this.getLevelConfig(data.creditLevel);
    const nextLevelInfo = this.getNextLevelInfo(data.creditLevel);
    const progressToNextLevel = this.calculateProgressToNextLevel(data.orderCount, data.creditLevel);

    return {
      ...data,
      currentLevelInfo,
      nextLevelInfo,
      progressToNextLevel,
    };
  }

  /**
   * 获取信用分对应的颜色
   */
  getCreditScoreColor(score: number): string {
    if (score >= 180) return '#52c41a'; // 优秀 - 绿色
    if (score >= 150) return '#1890ff'; // 良好 - 蓝色
    if (score >= 120) return '#faad14'; // 一般 - 橙色
    if (score >= 80) return '#ff7a45';  // 较差 - 深橙
    return '#f5222d';                    // 差 - 红色
  }

  /**
   * 获取信用分对应的评级
   */
  getCreditScoreGrade(score: number): string {
    if (score >= 180) return '优秀';
    if (score >= 150) return '良好';
    if (score >= 120) return '一般';
    if (score >= 80) return '较差';
    return '差';
  }

  /**
   * 格式化响应时间
   */
  formatResponseTime(minutes: number): string {
    if (minutes < 60) {
      return `${minutes}分钟`;
    }
    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;
    return remainingMinutes > 0 ? `${hours}小时${remainingMinutes}分钟` : `${hours}小时`;
  }
}

// ==================== 导出单例 ====================

export const creditService = new CreditService();
