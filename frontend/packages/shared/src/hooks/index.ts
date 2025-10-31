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

// ==================== P1 高级 Hooks（待开发）====================
// export { useDebounce } from './useDebounce';
// export { useThrottle } from './useThrottle';
// export { useLocalStorage } from './useLocalStorage';
// export { useWebSocket } from './useWebSocket';
// export { useUpload } from './useUpload';
