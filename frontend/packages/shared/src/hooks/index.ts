/**
 * 自定义 Hooks 入口
 * @author BaSui 😎
 * @description 导出所有 React Hooks
 */

// ==================== P0 核心 Hooks（已完成）====================

/**
 * useAuth - 认证状态管理 Hook
 * @description 管理用户登录状态、Token 刷新、权限检查
 */
export {
  useAuth,
  type User,
  type LoginParams,
  type UseAuthResult,
} from './useAuth';

/**
 * useRequest - API 请求封装 Hook
 * @description 封装 API 请求，提供加载状态、错误处理、自动重试等功能
 */
export {
  useRequest,
  type UseRequestOptions,
  type UseRequestResult,
} from './useRequest';

/**
 * usePagination - 分页状态管理 Hook
 * @description 管理分页状态、页码切换、每页条数变更等功能
 */
export {
  usePagination,
  type PaginationParams,
  type PaginationResponse,
  type UsePaginationOptions,
  type UsePaginationResult,
} from './usePagination';

/**
 * useForm - 表单状态管理 Hook
 * @description 管理表单状态、验证、提交等功能
 */
export {
  useForm,
  type FormValues,
  type FormErrors,
  type FormTouched,
  type ValidatorFn,
  type ValidationRule,
  type FormConfig,
  type UseFormOptions,
  type UseFormResult,
} from './useForm';

// ==================== P1 高级 Hooks（已完成）====================

/**
 * useDebounce - 防抖 Hook
 * @description 延迟更新值直到停止变化一段时间，常用于搜索输入框
 */
export { useDebounce } from './useDebounce';

/**
 * useThrottle - 节流 Hook
 * @description 限制值更新的频率，常用于滚动事件、鼠标移动等高频场景
 */
export { useThrottle } from './useThrottle';

/**
 * useLocalStorage - 本地存储 Hook
 * @description 封装 LocalStorage，支持自动序列化、跨标签页同步
 */
export { useLocalStorage } from './useLocalStorage';

/**
 * useWebSocket - WebSocket 连接 Hook
 * @description 封装 WebSocket，支持自动重连、心跳检测
 */
export { useWebSocket, WebSocketReadyState, type UseWebSocketOptions, type UseWebSocketResult } from './useWebSocket';

/**
 * useUpload - 文件上传 Hook
 * @description 封装文件上传，支持进度跟踪、多文件上传、错误处理
 */
export { useUpload, type UseUploadOptions, type UseUploadResult, type UploadFile } from './useUpload';

// ==================== P2 业务 Hooks（新增）====================

/**
 * useWebSocketService - WebSocket 服务管理 Hook
 * @description 封装 websocketService，管理 WebSocket 连接生命周期
 */
export {
  useWebSocketService,
  type UseWebSocketServiceOptions,
  type UseWebSocketServiceResult,
} from './useWebSocketService';

/**
 * useChatMessage - 聊天消息订阅 Hook
 * @description 订阅 WebSocket 聊天消息，提供消息列表和发送功能
 */
export {
  useChatMessage,
  type UseChatMessageOptions,
  type UseChatMessageResult,
} from './useChatMessage';

/**
 * useNotification - 系统通知订阅 Hook
 * @description 订阅 WebSocket 系统通知，提供通知列表和管理功能
 */
export {
  useNotification,
  type UseNotificationOptions,
  type UseNotificationResult,
} from './useNotification';

/**
 * useOrderUpdate - 订单状态更新订阅 Hook
 * @description 订阅 WebSocket 订单状态更新,实时跟踪订单变化
 */
export {
  useOrderUpdate,
  type UseOrderUpdateOptions,
  type UseOrderUpdateResult,
  type OrderUpdateRecord,
} from './useOrderUpdate';

/**
 * useAuthGuard - 权限守卫 Hook
 * @description 检查登录状态，未登录时友好提示并引导用户登录
 */
export {
  useAuthGuard,
  type UseAuthGuardResult,
} from './useAuthGuard';
