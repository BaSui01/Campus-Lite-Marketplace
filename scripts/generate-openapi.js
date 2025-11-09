#!/usr/bin/env node
/**
 * OpenAPI 客户端生成脚本
 * - 运行 backend mvn clean
 * - 启动 spring-boot:run 导出 JSON（允许 OpenApiExporter 主动退出）
 * - 若 JSON 存在，则继续执行 openapi-generator
 */

const { spawnSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const repoRoot = path.resolve(__dirname, '..');
const backendDir = path.join(repoRoot, 'backend');
const openapiFile = path.join(backendDir, 'target', 'openapi-frontend.json');
const mvnCmd = process.platform === 'win32' ? 'mvn.cmd' : 'mvn';

const SPRING_BOOT_ARGS =
  '--openapi.export.enabled=true,--openapi.export.path=target/openapi-frontend.json';

function runMaven(stepName, args, { ignoreFailure = false } = {}) {
  console.log(`\n[api:generate] 开始执行 ${stepName}: mvn ${args.join(' ')}`);
  const result = spawnSync(mvnCmd, args, {
    cwd: backendDir,
    stdio: 'inherit',
    shell: true,
    windowsHide: true,
  });

  if (result.error) {
    console.error(`[api:generate] ${stepName} 执行异常: ${result.error.message}`);
    process.exit(1);
  }

  if (result.status !== 0 && !ignoreFailure) {
    console.error(`[api:generate] ${stepName} 失败，退出码: ${result.status}`);
    process.exit(result.status ?? 1);
  }

  return result.status ?? 0;
}

runMaven('clean', ['clean']);

const runStatus = runMaven(
  'spring-boot:run',
  [`-Dspring-boot.run.arguments=${SPRING_BOOT_ARGS}`, 'spring-boot:run'],
  { ignoreFailure: true }
);

if (runStatus !== 0) {
  if (!fs.existsSync(openapiFile)) {
    console.error(
      `[api:generate] spring-boot:run 退出码 ${runStatus}，且未生成 ${openapiFile}，终止。`
    );
    process.exit(runStatus);
  }

  console.warn(
    `[api:generate] spring-boot:run 返回 ${runStatus}，但检测到 OpenAPI JSON 已生成，继续执行后续步骤。`
  );
} else if (!fs.existsSync(openapiFile)) {
  console.error(
    `[api:generate] spring-boot:run 成功执行但未找到 ${openapiFile}，终止。`
  );
  process.exit(1);
}

runMaven('openapi-generator:generate', ['-P', 'openapi', 'openapi-generator:generate']);

console.log('\n[api:generate] ✅ OpenAPI 客户端更新完成');
