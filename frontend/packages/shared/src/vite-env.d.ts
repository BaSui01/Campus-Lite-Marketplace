/**
 * Vite 环境变量类型定义
 * @description 扩展 ImportMeta 接口，添加 env 属性
 */

/// <reference types="vite/client" />

interface ImportMetaEnv {
  /**
   * 运行模式
   * @description 'development' | 'production' | 'test'
   */
  readonly MODE: string;

  /**
   * 应用基础路径
   */
  readonly BASE_URL: string;

  /**
   * 是否为生产环境
   */
  readonly PROD: boolean;

  /**
   * 是否为开发环境
   */
  readonly DEV: boolean;

  /**
   * 是否为 SSR
   */
  readonly SSR: boolean;

  /**
   * 加密密钥（AES-256）
   * @description 用于前端敏感数据加密（如密码传输）
   */
  readonly VITE_ENCRYPT_KEY?: string;

  /**
   * API 基础 URL
   */
  readonly VITE_API_BASE_URL?: string;

  /**
   * WebSocket URL
   */
  readonly VITE_WS_URL?: string;

  /**
   * 静态资源基础 URL
   */
  readonly VITE_STATIC_BASE_URL?: string;

  /**
   * 微信 App ID
   */
  readonly VITE_WECHAT_APP_ID?: string;

  /**
   * OSS Bucket 名称
   */
  readonly VITE_OSS_BUCKET?: string;

  /**
   * OSS 区域
   */
  readonly VITE_OSS_REGION?: string;

  /**
   * 是否启用性能监控
   */
  readonly VITE_ENABLE_PERFORMANCE?: string;

  /**
   * 是否启用错误追踪
   */
  readonly VITE_ENABLE_ERROR_TRACKING?: string;

  /**
   * Sentry DSN
   */
  readonly VITE_SENTRY_DSN?: string;

  /**
   * 是否启用调试模式
   */
  readonly VITE_DEBUG?: string;

  /**
   * 是否启用 Mock 数据
   */
  readonly VITE_ENABLE_MOCK?: string;

  /**
   * 高德地图 API Key
   */
  readonly VITE_AMAP_KEY?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
