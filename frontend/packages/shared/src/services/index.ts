/**
 * API 服务导出入口
 * @author BaSui 😎
 * @description 导出所有 API 服务
 */

// ==================== 认证服务 ====================
export { authService } from './auth';
export type { default as AuthService } from './auth';

// ==================== 用户服务 ====================
export { userService } from './user';
export type { default as UserService } from './user';

// ==================== 物品服务 ====================
export { goodsService } from './goods';
export type { default as GoodsService } from './goods';

// ==================== 订单服务 ====================
export { orderService } from './order';
export type { default as OrderService } from './order';

// ==================== 消息服务 ====================
export { messageService } from './message';
export type { default as MessageService } from './message';

// ==================== 通知服务 ====================
export { notificationService } from './notification';
export type { default as NotificationService } from './notification';

// ==================== 帖子服务 ====================
export { postService } from './post';
export type { default as PostService } from './post';

// ==================== 文件上传服务 ====================
export { uploadService } from './upload';
export type { default as UploadService } from './upload';

// ==================== WebSocket 服务 ====================
export { websocketService } from './websocket';
export type { default as WebSocketService } from './websocket';

// ==================== WebSocket 客户端 ====================
export {
  WebSocketClient,
  WebSocketReadyState,
  WebSocketMessageType,
} from './WebSocketClient';
export type {
  WebSocketMessage,
  WebSocketClientOptions,
} from './WebSocketClient';

// ==================== WebSocket 服务类型 ====================
export type {
  ChatMessageData,
  OrderUpdateData,
  UserOnlineStatusData,
} from './websocket';

// ==================== 便捷导出（默认导出对象）====================

/**
 * 所有 API 服务集合
 */
export const services = {
  auth: authService,
  user: userService,
  goods: goodsService,
  order: orderService,
  message: messageService,
  notification: notificationService,
  post: postService,
  upload: uploadService,
  websocket: websocketService,
} as const;

/**
 * 默认导出
 */
export default services;
