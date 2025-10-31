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

// ==================== P1 高级组件（待开发）====================
// export { Dropdown } from './Dropdown';
// export { Table } from './Table';
// export { Pagination } from './Pagination';
// export { Card } from './Card';
// export { Tabs } from './Tabs';
// export { Badge } from './Badge';
// export { Avatar } from './Avatar';
// export { Tag } from './Tag';

// ==================== P2 业务组件（待开发）====================
// export { GoodsCard } from './GoodsCard';
// export { OrderCard } from './OrderCard';
// export { UserAvatar } from './UserAvatar';
// export { ImageUpload } from './ImageUpload';
// export { RichTextEditor } from './RichTextEditor';
