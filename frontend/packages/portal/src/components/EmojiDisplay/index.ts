/**
 * EmojiDisplay 组件导出文件
 * @author BaSui 😎
 * @description 统一导出表情展示相关组件
 */

export { EmojiDisplay, EmojiTextRenderer } from './EmojiDisplay';
export type { EmojiDisplayProps, EmojiTextRendererProps } from './EmojiDisplay';

// 重新导出相关类型
export type {
  EmojiItem,
  EmojiPack,
  EmojiMessage,
  EmojiCategory,
  EmojiPackType,
} from '@campus/shared/types/emoji';