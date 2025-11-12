/**
 * TypeScript 类型定义入口
 * @author BaSui 😎
 * @description 导出本地特有类型，避免与 OpenAPI 生成的类型冲突
 *
 * ⚠️ 重要说明：
 * - 所有实体类型（User、Goods等）应从 '@campus/shared/api' 导入
 * - 所有枚举（UserStatus、OrderStatus等）应从 '@campus/shared/api' 导入
 * - 此处只导出本地特有的 UI 相关类型和工具类型
 */

// ==================== 通用类型（仅保留本地特有类型）====================
// 注意：已移除与 API 重复的枚举定义
export * from './common';

// ==================== ❌ 已禁用的导出 ====================
//
// 以下模块包含与 API 重复的类型定义，已停止导出：
//
// export * from './enum';    // ❌ 所有枚举应从 '@campus/shared/api' 导入
// export * from './entity';  // ❌ 所有实体应从 '@campus/shared/api' 导入

// ==================== API 类型 ====================
export * from './api';

// ==================== 撤销系统类型 ====================
export * from './revert';
