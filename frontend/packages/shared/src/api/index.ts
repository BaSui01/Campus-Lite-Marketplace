/**
 * API 层入口文件
 * @author BaSui 😎
 * @description OpenAPI Generator 自动生成的 API 客户端
 *
 * 生成命令（待后端问题修复后再执行）：
 * ```bash
 * pnpm run api:generate
 * ```
 *
 * 或者在后端执行：
 * ```bash
 * mvn -Dspring-boot.run.arguments="--openapi.export.enabled=true,--openapi.export.path=target/openapi-frontend.json" spring-boot:run
 * mvn -P openapi openapi-generator:generate
 * ```
 */

// OpenAPI 自动生成的 API 导出
export * from './api';
export * from './configuration';
export * from './base';
export * from './common';
