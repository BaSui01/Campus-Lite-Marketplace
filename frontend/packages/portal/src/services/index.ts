/**
 * Portal 端服务层统一导出
 * ✅ 所有服务基于 OpenAPI 生成的 DefaultApi
 *
 * @author BaSui 😎
 * @description Portal 用户端专属服务
 * @date 2025-11-07
 */

// ==================== Portal 专属服务 ====================
export * from './appeal';
export * from './dispute';
export * from './blacklist';
export * from './credit';
export * from './favorite';
export * from './follow';
// export * from './marketing'; // ❌ 已删除重复文件，使用 shared 的 marketing
export * from './recommend';
export * from './sellerStatistics';
export * from './subscription';

// ==================== 从 Shared 导入通用服务 ====================
export {
  marketingService,
  CampaignType,
  CampaignStatus,
  DiscountType,
  CAMPAIGN_TYPE_CONFIG,
  CAMPAIGN_STATUS_CONFIG,
} from '@campus/shared/services/marketing';
export type {
  MarketingCampaign,
  CreateCampaignRequest,
  CampaignListParams,
  CampaignStatistics,
  DiscountConfig,
} from '@campus/shared/services/marketing';

// ==================== 明确导出常用类型（避免模块解析问题）====================
export type {
  TodayOverview,
  SalesTrend,
  GoodsRanking,
  GoodsRankingItem,
  VisitorAnalysis,
  VisitorSource,
  SalesTrendPoint,
  ReportType,
  DataReport,
  ReportSection,
} from './sellerStatistics';

// ==================== 确保运行时导出（Vite HMR 兼容）====================
export { sellerStatisticsService } from './sellerStatistics';
