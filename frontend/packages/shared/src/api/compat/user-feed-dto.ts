/**
 * 兼容层：UserFeedDTO
 * 说明：
 * - 在未执行 OpenAPI 生成前，提供临时的类型定义，保障编译通过
 * - 执行 `pnpm run api:generate` 后，建议改为从 '../models/user-feed-dto' 导入
 */

export type UserFeedDTOFeedType = 'POST' | 'REVIEW' | 'COLLECT' | 'LIKE';
export type UserFeedDTOTargetType = 'POST' | 'GOODS';

export interface UserFeedDTO {
  id?: number;
  actorId?: number;
  displayName?: string;
  avatarUrl?: string;
  feedType?: UserFeedDTOFeedType;
  targetType?: UserFeedDTOTargetType;
  targetId?: number;
  createdAt?: string;
}

// 导出命名保持与生成物料一致（尽量）
export default {} as any;

