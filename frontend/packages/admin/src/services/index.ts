/**
 * Admin Services 统一导出
 * @author BaSui 😎
 * @description 管理端服务层统一导出入口
 */

// ========== 管理端专属服务 ==========
export * from './statistics';
export * from './adminUser';
export * from './adminGoods';
export * from './adminCategory';
export * from './monitor';
export * from './compliance';
export * from './dispute';
export * from './disputeStatistics';
export * from './appeal';
export * from './blacklist';
export * from './report';
export * from './featureFlag';
export * from './role';
export * from './bannedUser';
export * from './operationLog';
export * from './auditLog';
export * from './recommend';
export * from './behavior';
export * from './searchStatistics';
export * from './recycleBin';
export * from './revertManagement';

// ========== 从共享层导出常用服务（避免重复导入）==========
export { paymentService } from '@campus/shared';
