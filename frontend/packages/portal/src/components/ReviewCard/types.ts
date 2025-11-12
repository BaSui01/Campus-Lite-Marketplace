/**
 * ReviewCard 类型定义
 * @author BaSui 😎
 */

import type { ReviewDetail } from '@campus/shared/services/goods';

/**
 * ReviewCard 组件的 Props
 */
export interface ReviewCardProps {
  /**
   * 评价数据
   */
  review: ReviewDetail;

  /**
   * 是否显示商品信息
   * @default false
   */
  showGoods?: boolean;

  /**
   * 是否显示操作按钮（编辑/删除）
   * @default false
   */
  showActions?: boolean;

  /**
   * 点赞回调
   */
  onLike?: (reviewId: number) => void;

  /**
   * 编辑回调
   */
  onEdit?: (reviewId: number) => void;

  /**
   * 删除回调
   */
  onDelete?: (reviewId: number) => void;

  /**
   * 自定义类名
   */
  className?: string;
}
