/**
 * StarRating 组件类型定义
 * @author BaSui 😎
 * @description 星级评分组件的 TypeScript 类型
 */

/**
 * 星级评分组件尺寸
 */
export type StarSize = 'small' | 'medium' | 'large';

/**
 * StarRating 组件 Props
 */
export interface StarRatingProps {
  /**
   * 星级值 (0-5)
   * @default 0
   */
  value: number;

  /**
   * 评分变化回调（可编辑模式）
   */
  onChange?: (value: number) => void;

  /**
   * 只读模式
   * @default false
   */
  readonly?: boolean;

  /**
   * 尺寸
   * @default 'medium'
   */
  size?: StarSize;

  /**
   * 星星颜色
   * @default '#fadb14'
   */
  color?: string;

  /**
   * 是否支持半星
   * @default false
   */
  allowHalf?: boolean;

  /**
   * 是否显示数字
   * @default false
   */
  showValue?: boolean;

  /**
   * 自定义类名
   */
  className?: string;

  /**
   * 是否禁用
   * @default false
   */
  disabled?: boolean;
}
