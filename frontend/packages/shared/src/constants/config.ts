/**
 * 应用配置常量
 * @author BaSui 😎
 * @description 前端应用的配置常量（API、上传、缓存、限制等）
 */

// ==================== API 配置 ====================

/**
 * API 基础URL（从环境变量读取，默认为本地开发地址）
 * ⚠️ 注意：不要加 /api 后缀，因为 OpenAPI 生成的代码已包含路径前缀
 */
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8200';

/**
 * API 请求超时时间（毫秒）
 */
export const API_TIMEOUT = 30000;

/**
 * API 请求重试次数
 */
export const API_RETRY_COUNT = 3;

/**
 * API 请求重试延迟（毫秒）
 */
export const API_RETRY_DELAY = 1000;

/**
 * WebSocket 连接URL（从环境变量读取）
 * 💡 BaSui：后端有两个 WebSocket 端点：
 * - /api/ws/message: 私信消息（默认）
 * - /api/ws/dispute: 纠纷系统
 * ⚠️ 注意：必须包含 /api 前缀（context-path）
 */
export const WEBSOCKET_URL = import.meta.env.VITE_WS_URL || 'ws://localhost:8200/api/ws/message';

/**
 * WebSocket 是否启用（开发环境可设为 false 来禁用自动连接）
 * 💡 在 .env 中设置 VITE_ENABLE_WEBSOCKET=false 可禁用
 */
export const WEBSOCKET_ENABLED = import.meta.env.VITE_ENABLE_WEBSOCKET !== 'false';

/**
 * WebSocket 心跳间隔（毫秒）
 */
export const WEBSOCKET_HEARTBEAT_INTERVAL = 30000;

/**
 * WebSocket 重连间隔（毫秒）
 */
export const WEBSOCKET_RECONNECT_INTERVAL = 3000;

/**
 * WebSocket 最大重连次数（减少到5次，避免无限重连浪费资源）
 */
export const WEBSOCKET_MAX_RECONNECT = 5;

/**
 * WebSocket 重连退避倍数（每次重连间隔翻倍）
 */
export const WEBSOCKET_RECONNECT_BACKOFF = 2;

// ==================== 认证配置 ====================

/**
 * Token 存储键名
 */
export const TOKEN_KEY = 'access_token';

/**
 * Refresh Token 存储键名
 */
export const REFRESH_TOKEN_KEY = 'refresh_token';

/**
 * 用户信息存储键名
 */
export const USER_INFO_KEY = 'user_info';

/**
 * Token 刷新提前时间（毫秒，提前5分钟刷新）
 */
export const TOKEN_REFRESH_ADVANCE_TIME = 5 * 60 * 1000;

/**
 * 记住我的过期时间（天）
 */
export const REMEMBER_ME_DAYS = 7;

// ==================== 文件上传配置 ====================

/**
 * 图片上传地址
 */
export const IMAGE_UPLOAD_URL = `${API_BASE_URL}/file/upload`;

/**
 * 文件上传地址
 */
export const FILE_UPLOAD_URL = `${API_BASE_URL}/file/upload`;

/**
 * 图片上传最大大小（字节，默认 5MB）
 */
export const IMAGE_MAX_SIZE = 5 * 1024 * 1024;

/**
 * 文件上传最大大小（字节，默认 10MB）
 */
export const FILE_MAX_SIZE = 10 * 1024 * 1024;

/**
 * 图片上传最大数量
 */
export const IMAGE_MAX_COUNT = 9;

/**
 * 支持的图片格式
 */
export const IMAGE_ACCEPT = 'image/jpeg,image/png,image/gif,image/webp';

/**
 * 支持的文件格式
 */
export const FILE_ACCEPT = '.pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt';

// ==================== 缓存配置 ====================

/**
 * 默认缓存过期时间（毫秒，30分钟）
 */
export const CACHE_EXPIRE_TIME = 30 * 60 * 1000;

/**
 * 用户信息缓存过期时间（毫秒，1天）
 */
export const USER_CACHE_EXPIRE_TIME = 24 * 60 * 60 * 1000;

/**
 * 分类列表缓存过期时间（毫秒，1小时）
 */
export const CATEGORY_CACHE_EXPIRE_TIME = 60 * 60 * 1000;

/**
 * 本地存储键名前缀
 */
export const STORAGE_KEY_PREFIX = 'campus_marketplace_';

// ==================== 分页配置 ====================

/**
 * 默认每页条数
 */
export const DEFAULT_PAGE_SIZE = 20;

/**
 * 每页条数选项
 */
export const PAGE_SIZE_OPTIONS = [10, 20, 30, 50, 100];

/**
 * 最大每页条数
 */
export const MAX_PAGE_SIZE = 100;

// ==================== 输入限制 ====================

/**
 * 用户名最小长度
 */
export const USERNAME_MIN_LENGTH = 3;

/**
 * 用户名最大长度
 */
export const USERNAME_MAX_LENGTH = 20;

/**
 * 密码最小长度
 */
export const PASSWORD_MIN_LENGTH = 6;

/**
 * 密码最大长度
 */
export const PASSWORD_MAX_LENGTH = 20;

/**
 * 昵称最大长度
 */
export const NICKNAME_MAX_LENGTH = 30;

/**
 * 物品标题最小长度
 */
export const GOODS_TITLE_MIN_LENGTH = 5;

/**
 * 物品标题最大长度
 */
export const GOODS_TITLE_MAX_LENGTH = 100;

/**
 * 物品描述最小长度
 */
export const GOODS_DESC_MIN_LENGTH = 10;

/**
 * 物品描述最大长度
 */
export const GOODS_DESC_MAX_LENGTH = 2000;

/**
 * 帖子标题最小长度
 */
export const POST_TITLE_MIN_LENGTH = 5;

/**
 * 帖子标题最大长度
 */
export const POST_TITLE_MAX_LENGTH = 100;

/**
 * 帖子内容最小长度
 */
export const POST_CONTENT_MIN_LENGTH = 10;

/**
 * 帖子内容最大长度
 */
export const POST_CONTENT_MAX_LENGTH = 5000;

/**
 * 评论最小长度
 */
export const COMMENT_MIN_LENGTH = 1;

/**
 * 评论最大长度
 */
export const COMMENT_MAX_LENGTH = 500;

// ==================== 价格限制 ====================

/**
 * 最小价格（单位：分，0.01元）
 */
export const MIN_PRICE = 1;

/**
 * 最大价格（单位：分，99999.99元）
 */
export const MAX_PRICE = 9999999;

/**
 * 价格精度（小数位数）
 */
export const PRICE_PRECISION = 2;

// ==================== 防抖节流配置 ====================

/**
 * 默认防抖延迟（毫秒）
 */
export const DEFAULT_DEBOUNCE_DELAY = 300;

/**
 * 搜索防抖延迟（毫秒）
 */
export const SEARCH_DEBOUNCE_DELAY = 500;

/**
 * 默认节流延迟（毫秒）
 */
export const DEFAULT_THROTTLE_DELAY = 1000;

/**
 * 滚动节流延迟（毫秒）
 */
export const SCROLL_THROTTLE_DELAY = 200;

// ==================== 消息提示配置 ====================

/**
 * Toast 默认显示时长（毫秒）
 */
export const TOAST_DURATION = 3000;

/**
 * Toast 成功提示显示时长（毫秒）
 */
export const TOAST_SUCCESS_DURATION = 2000;

/**
 * Toast 错误提示显示时长（毫秒）
 */
export const TOAST_ERROR_DURATION = 4000;

// ==================== 轮询配置 ====================

/**
 * 消息轮询间隔（毫秒，30秒）
 */
export const MESSAGE_POLL_INTERVAL = 30000;

/**
 * 通知轮询间隔（毫秒，60秒）
 */
export const NOTIFICATION_POLL_INTERVAL = 60000;

/**
 * 订单状态轮询间隔（毫秒，5秒）
 */
export const ORDER_STATUS_POLL_INTERVAL = 5000;

/**
 * 订单状态轮询最大次数
 */
export const ORDER_STATUS_POLL_MAX_COUNT = 60;

// ==================== 日期格式 ====================

/**
 * 日期时间格式
 */
export const DATETIME_FORMAT = 'YYYY-MM-DD HH:mm:ss';

/**
 * 日期格式
 */
export const DATE_FORMAT = 'YYYY-MM-DD';

/**
 * 时间格式
 */
export const TIME_FORMAT = 'HH:mm:ss';

/**
 * 月份格式
 */
export const MONTH_FORMAT = 'YYYY-MM';

/**
 * 年份格式
 */
export const YEAR_FORMAT = 'YYYY';

// ==================== 正则表达式 ====================

/**
 * 用户名正则（3-20位字母数字下划线）
 */
export const USERNAME_REGEX = /^[a-zA-Z0-9_]{3,20}$/;

/**
 * 邮箱正则
 */
export const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

/**
 * 手机号正则（中国大陆）
 */
export const PHONE_REGEX = /^1[3-9]\d{9}$/;

/**
 * 密码正则（至少包含字母和数字）
 */
export const PASSWORD_REGEX = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{6,20}$/;

/**
 * URL正则
 */
export const URL_REGEX = /^https?:\/\/(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)$/;

/**
 * 身份证号正则
 */
export const ID_CARD_REGEX = /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/;

// ==================== 应用信息 ====================

/**
 * 应用名称
 */
export const APP_NAME = '校园轻享集市';

/**
 * 应用简称
 */
export const APP_SHORT_NAME = '轻享集市';

/**
 * 应用版本
 */
export const APP_VERSION = '1.0.0';

/**
 * 应用描述
 */
export const APP_DESCRIPTION = '校园二手物品交易平台，让闲置物品流动起来';

/**
 * 应用关键词
 */
export const APP_KEYWORDS = ['校园', '二手', '交易', '集市', '闲置'];

/**
 * 应用作者
 */
export const APP_AUTHOR = 'BaSui';

/**
 * 应用版权
 */
export const APP_COPYRIGHT = `© ${new Date().getFullYear()} ${APP_NAME}. All rights reserved.`;

// ==================== 环境判断 ====================

/**
 * 是否为开发环境
 */
export const IS_DEV = import.meta.env.DEV;

/**
 * 是否为生产环境
 */
export const IS_PROD = import.meta.env.PROD;

/**
 * 是否启用调试模式
 */
export const DEBUG = import.meta.env.VITE_DEBUG === 'true' || IS_DEV;

/**
 * 是否启用Mock数据
 */
export const ENABLE_MOCK = import.meta.env.VITE_ENABLE_MOCK === 'true';

// ==================== 第三方服务 ====================

/**
 * 高德地图API Key
 */
export const AMAP_KEY = import.meta.env.VITE_AMAP_KEY || '';

/**
 * 微信AppID
 */
export const WECHAT_APP_ID = import.meta.env.VITE_WECHAT_APP_ID || '';

/**
 * 阿里云OSS Bucket
 */
export const OSS_BUCKET = import.meta.env.VITE_OSS_BUCKET || '';

/**
 * 阿里云OSS Region
 */
export const OSS_REGION = import.meta.env.VITE_OSS_REGION || '';

// ==================== 性能监控 ====================

/**
 * 是否启用性能监控
 */
export const ENABLE_PERFORMANCE = import.meta.env.VITE_ENABLE_PERFORMANCE === 'true';

/**
 * 是否启用错误监控
 */
export const ENABLE_ERROR_TRACKING = import.meta.env.VITE_ENABLE_ERROR_TRACKING === 'true';

/**
 * Sentry DSN
 */
export const SENTRY_DSN = import.meta.env.VITE_SENTRY_DSN || '';

// ==================== 导出所有配置 ====================

/**
 * 应用配置对象
 */
export const APP_CONFIG = {
  name: APP_NAME,
  shortName: APP_SHORT_NAME,
  version: APP_VERSION,
  description: APP_DESCRIPTION,
  keywords: APP_KEYWORDS,
  author: APP_AUTHOR,
  copyright: APP_COPYRIGHT,
} as const;

/**
 * API 配置对象
 */
export const API_CONFIG = {
  baseURL: API_BASE_URL,
  timeout: API_TIMEOUT,
  retryCount: API_RETRY_COUNT,
  retryDelay: API_RETRY_DELAY,
} as const;

/**
 * 上传配置对象
 */
export const UPLOAD_CONFIG = {
  imageUploadURL: IMAGE_UPLOAD_URL,
  fileUploadURL: FILE_UPLOAD_URL,
  imageMaxSize: IMAGE_MAX_SIZE,
  fileMaxSize: FILE_MAX_SIZE,
  imageMaxCount: IMAGE_MAX_COUNT,
  imageAccept: IMAGE_ACCEPT,
  fileAccept: FILE_ACCEPT,
} as const;

/**
 * 缓存配置对象
 */
export const CACHE_CONFIG = {
  expireTime: CACHE_EXPIRE_TIME,
  userCacheExpireTime: USER_CACHE_EXPIRE_TIME,
  categoryCacheExpireTime: CATEGORY_CACHE_EXPIRE_TIME,
  storageKeyPrefix: STORAGE_KEY_PREFIX,
} as const;
