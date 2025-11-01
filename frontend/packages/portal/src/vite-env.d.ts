/// <reference types="vite/client" />

/**
 * 🎯 BaSui 类型声明：Vite 环境变量
 *
 * 让 TypeScript 能识别 import.meta.env 的自定义变量
 */
interface ImportMetaEnv {
  /** API 基础 URL */
  readonly VITE_API_BASE_URL: string;
  /** WebSocket URL */
  readonly VITE_WS_URL: string;
  /** 静态资源基础 URL */
  readonly VITE_STATIC_BASE_URL: string;
  /** 应用标题（Portal） */
  readonly VITE_APP_TITLE_PORTAL: string;
  /** 应用标题（Admin） */
  readonly VITE_APP_TITLE_ADMIN: string;
  /** 应用描述 */
  readonly VITE_APP_DESCRIPTION: string;
  /** Portal 端口 */
  readonly VITE_PORTAL_PORT: string;
  /** Admin 端口 */
  readonly VITE_ADMIN_PORT: string;
  /** 当前运行模式 */
  readonly MODE: string;
  /** 是否开发环境 */
  readonly DEV: boolean;
  /** 是否生产环境 */
  readonly PROD: boolean;
  /** 是否 SSR */
  readonly SSR: boolean;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}

/**
 * 🎯 BaSui 类型声明：全局变量
 */
declare const __APP_VERSION__: string;
