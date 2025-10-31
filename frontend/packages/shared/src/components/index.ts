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
 * Loading - 加载动画组件
 * @description 支持 Spinner（转圈圈）和 Skeleton（骨架屏）两种模式
 */
export { Loading, type LoadingProps, type LoadingType, type LoadingSize } from './Loading';

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

// ==================== P2 业务组件（待开发）====================
// export { GoodsCard } from './GoodsCard';
// export { OrderCard } from './OrderCard';
// export { UserAvatar } from './UserAvatar';
// export { ImageUpload } from './ImageUpload';
// export { RichTextEditor } from './RichTextEditor';
