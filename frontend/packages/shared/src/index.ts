/**
 * 校园轻享集市 - 公共层入口文件
 * @author BaSui 😎
 * @description 导出所有公共模块：API、组件、工具、类型、常量、Services
 */

// ==================== API 层（OpenAPI 自动生成，优先级最高）====================
// 🎯 优先导出 OpenAPI 生成的类型，这些类型与后端保持同步
export * from './api';

// ==================== 本地类型定义（命名空间隔离）====================
// 🎯 本地手写类型统一导出为 Local 命名空间，避免与 API 类型冲突
// 使用方式：import { Local } from '@campus/shared'; const user: Local.User = ...;
export * as Local from './types';

// ==================== 公共组件 ====================
export * from './components';

// ==================== 工具函数 ====================
export * from './utils';

// ==================== 常量 ====================
export * from './constants';

// ==================== 自定义 Hooks ====================
export * from './hooks';

// ==================== API 服务（新增）====================
export * from './services';

// ==================== 工厂方法（Store 等）====================
export * from './factories';
