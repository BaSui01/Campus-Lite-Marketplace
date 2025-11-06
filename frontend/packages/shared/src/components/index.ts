/**
 * 公共组件库入口
 * @author BaSui 😎
 * @description 导出所有公共 React 组件
 */

// ==================== P0 基础组件（已完成）====================

/**
 * Button - 按钮组件
 * @description 支持多种类型（primary、default、danger、link）和尺寸（large、medium、small）
 */
export { Button, type ButtonProps, type ButtonType, type ButtonSize } from './Button';

/**
 * Input - 输入框组件
 * @description 支持文本、密码、数字、搜索等多种类型，带清除按钮和前后缀图标
 */
export { Input, type InputProps, type InputType, type InputSize } from './Input';

/**
 * Select - 选择器组件
 * @description 下拉选择器，支持单选、多选、搜索、清除等功能
 */
export { Select, type SelectProps, type SelectOptionProps } from './Select';

/**
 * Empty - 空状态组件
 * @description 空状态展示组件，支持自定义图片、描述、操作按钮
 */
export { Empty, type EmptyProps } from './Empty';

/**
 * Loading - 加载动画组件
 * @description 支持 Spinner（转圈圈）和 Skeleton（骨架屏）两种模式
 */
export { Loading, type LoadingProps, type LoadingType, type LoadingSize } from './Loading';

/**
 * Skeleton - 骨架屏组件
 * @description 更专业的骨架屏组件，支持多种预设布局（文本、头像、图片、卡片、列表、表单等）
 */
export { Skeleton, type SkeletonProps, type SkeletonType, type AvatarShape, type AnimationType } from './Skeleton';

/**
 * Toast - 消息提示组件
 * @description 轻量级消息提示，支持 success、error、warning、info 四种类型
 * @example
 * import { toast } from '@campus/shared';
 * toast.success('操作成功！');
 * toast.error('操作失败！');
 */
export { toast, type ToastOptions, type ToastType, type ToastPosition } from './Toast';

/**
 * Modal - 模态框组件
 * @description 支持标题、内容、底部按钮，可自定义尺寸和样式
 */
export { Modal, type ModalProps, type ModalSize } from './Modal';

/**
 * Form - 表单组件
 * @description 支持表单验证、错误提示、水平/垂直/行内布局
 */
export { Form, FormItem, type FormProps, type FormItemProps, type FormLayout } from './Form';

// ==================== P1 高级组件（已完成）====================

/**
 * Dropdown - 下拉菜单组件
 * @description 支持多级菜单、禁用项、自定义触发器、多种位置
 */
export { Dropdown, type DropdownProps, type DropdownMenuItem, type DropdownTrigger, type DropdownPlacement } from './Dropdown';

/**
 * Table - 数据表格组件
 * @description 支持排序、选择、分页、自定义渲染、固定列
 */
export { Table, type TableProps, type TableColumn, type TableRowSelection, type SortInfo, type SortDirection } from './Table';

/**
 * Pagination - 分页组件
 * @description 支持页码跳转、每页条数选择、总数显示、快速跳转
 */
export { Pagination, type PaginationProps, type PaginationSize } from './Pagination';

/**
 * Card - 卡片组件
 * @description 支持标题、封面、操作按钮、悬浮效果
 */
export { Card, type CardProps } from './Card';

/**
 * Tabs - 标签页组件
 * @description 支持水平/垂直布局、图标、徽标、禁用状态
 */
export { Tabs, type TabsProps, type TabItem, type TabsLayout, type TabsSize } from './Tabs';

/**
 * Badge - 徽标组件
 * @description 支持数字徽标、小红点、状态点
 */
export { Badge, type BadgeProps, type BadgeStatus } from './Badge';

/**
 * Avatar - 头像组件
 * @description 支持图片、文字、图标、多种尺寸和形状
 */
export { Avatar, type AvatarProps, type AvatarSize, type AvatarShape } from './Avatar';

/**
 * Tag - 标签组件
 * @description 支持多种颜色、尺寸、可关闭、带图标
 */
export { Tag, type TagProps, type TagColor, type TagSize } from './Tag';

// ==================== P2 业务组件（已完成）====================

/**
 * GoodsCard - 商品卡片组件
 * @description 用于展示商品信息，支持价格、标签、卖家信息、状态徽标
 */
export { GoodsCard, type GoodsCardProps, type GoodsData, type GoodsStatus } from './GoodsCard';

/**
 * OrderCard - 订单卡片组件
 * @description 用于展示订单信息，支持订单状态、商品列表、买卖家信息、操作按钮
 */
export { OrderCard, type OrderCardProps, type OrderData, type OrderItem, type OrderStatus } from './OrderCard';

/**
 * UserAvatar - 用户头像组件
 * @description 增强版头像组件，支持在线状态、认证徽标、用户名显示
 */
export { UserAvatar, type UserAvatarProps, type UserOnlineStatus } from './UserAvatar';

/**
 * ImageUpload - 图片上传组件
 * @description 支持单图/多图上传、拖拽上传、预览、删除、进度显示
 */
export { ImageUpload, type ImageUploadProps } from './ImageUpload';

/**
 * RichTextEditor - 富文本编辑器组件
 * @description 基于 contentEditable 实现，支持基础格式化功能（粗体、斜体、对齐、列表等）
 */
export { RichTextEditor, type RichTextEditorProps } from './RichTextEditor';

// ==================== P3 数据撤销组件（新增）====================

/**
 * RevertOperationsList - 可撤销操作列表组件
 * @description 展示用户可以撤销的操作列表（表格形式），支持筛选、分页、预览和申请撤销
 */
export { 
  RevertOperationsList, 
  type RevertOperationsListProps,
  type RevertableOperation,
  type RevertListParams,
  type RevertListResponse,
  type EntityType,
  type ActionType
} from './RevertOperationsList';

/**
 * RevertPreviewModal - 撤销预览弹窗组件
 * @description 展示撤销操作的影响预览和确认，包括验证结果、影响范围、数据对比等
 */
export { 
  RevertPreviewModal, 
  type RevertPreviewModalProps,
  type RevertPreviewData,
  type ValidationResult,
  type ValidationLevel
} from './RevertPreviewModal';

// ==================== Spec #11 新增组件 ====================

/**
 * StarRating - 星级评分组件
 * @description 支持只读/可编辑、半星、多尺寸的星级评分组件
 * @example
 * ```tsx
 * // 只读展示
 * <StarRating value={4.5} readonly showValue />
 * 
 * // 可编辑模式
 * <StarRating 
 *   value={rating} 
 *   onChange={setRating} 
 *   allowHalf 
 *   size="large" 
 * />
 * ```
 */
export { StarRating, type StarRatingProps, type StarSize } from './StarRating';

/**
 * Timeline - 时间轴组件
 * @description 支持垂直/水平布局、多种状态、自定义图标的时间轴组件
 * @example
 * ```tsx
 * // 垂直时间轴（默认）
 * <Timeline items={logisticsTrack} activeIndex={0} />
 * 
 * // 水平时间轴
 * <Timeline 
 *   items={orderSteps} 
 *   direction="horizontal"
 *   showTime={false}
 * />
 * ```
 */
export {
  Timeline,
  type TimelineProps,
  type TimelineItem,
  type TimelineStatus,
  type TimelineDirection,
} from './Timeline';
