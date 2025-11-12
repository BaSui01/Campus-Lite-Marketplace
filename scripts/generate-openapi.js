#!/usr/bin/env node
/**
 * OpenAPI 客户端生成脚本（BaSui 增强版 😎）
 * - 运行 backend mvn clean
 * - 启动 spring-boot:run（后台运行）
 * - 使用 curl 下载 OpenAPI JSON（更可靠！）
 * - 执行 openapi-generator 生成前端客户端
 *
 * @author BaSui 😎
 * @date 2025-11-10
 */

const { spawn, spawnSync } = require('child_process');
const fs = require('fs');
const path = require('path');

const repoRoot = path.resolve(__dirname, '..');
const backendDir = path.join(repoRoot, 'backend');
const openapiFile = path.join(backendDir, 'target', 'openapi-frontend.json');
const mvnCmd = process.platform === 'win32' ? 'mvn.cmd' : 'mvn';
const curlCmd = process.platform === 'win32' ? 'curl.exe' : 'curl';

// 配置
const BACKEND_PORT = 8200;
const BACKEND_CONTEXT_PATH = '/api';
const OPENAPI_GROUP = '前台接口';
const MAX_WAIT_TIME = 60000; // 最大等待时间 60 秒
const CHECK_INTERVAL = 2000; // 每 2 秒检查一次

/**
 * 运行 Maven 命令（同步）
 */
function runMaven(stepName, args, { ignoreFailure = false } = {}) {
  console.log(`\n[api:generate] 🔧 开始执行 ${stepName}: mvn ${args.join(' ')}`);
  const result = spawnSync(mvnCmd, args, {
    cwd: backendDir,
    stdio: 'inherit',
    shell: true,
    windowsHide: true,
  });

  if (result.error) {
    console.error(`[api:generate] ❌ ${stepName} 执行异常: ${result.error.message}`);
    process.exit(1);
  }

  if (result.status !== 0 && !ignoreFailure) {
    console.error(`[api:generate] ❌ ${stepName} 失败，退出码: ${result.status}`);
    process.exit(result.status ?? 1);
  }

  return result.status ?? 0;
}

/**
 * 启动后端服务（后台运行）
 */
function startBackend() {
  console.log('\n[api:generate] 🚀 启动后端服务（后台运行）...');

  const backendProcess = spawn(
    mvnCmd,
    ['spring-boot:run'],
    {
      cwd: backendDir,
      stdio: 'pipe',
      shell: true,
      windowsHide: true,
      detached: false,
    }
  );

  // 监听输出，检测启动成功
  let isStarted = false;
  backendProcess.stdout.on('data', (data) => {
    const output = data.toString();
    if (output.includes('Started MarketplaceApplication')) {
      isStarted = true;
      console.log('[api:generate] ✅ 后端服务启动成功！');
    }
  });

  backendProcess.stderr.on('data', (data) => {
    // 忽略 stderr，避免干扰
  });

  return { process: backendProcess, isStarted: () => isStarted };
}

/**
 * 等待后端服务启动
 */
async function waitForBackend(backendInfo) {
  console.log('[api:generate] ⏳ 等待后端服务启动...');

  const startTime = Date.now();
  while (Date.now() - startTime < MAX_WAIT_TIME) {
    if (backendInfo.isStarted()) {
      // 额外等待 2 秒，确保服务完全就绪
      await new Promise(resolve => setTimeout(resolve, 2000));
      return true;
    }
    await new Promise(resolve => setTimeout(resolve, CHECK_INTERVAL));
  }

  console.error('[api:generate] ❌ 后端服务启动超时！');
  return false;
}

/**
 * 下载 OpenAPI JSON
 */
function downloadOpenApiJson() {
  console.log('\n[api:generate] 📥 下载 OpenAPI JSON...');

  const url = `http://localhost:${BACKEND_PORT}${BACKEND_CONTEXT_PATH}/v3/api-docs?group=${encodeURIComponent(OPENAPI_GROUP)}`;
  console.log(`[api:generate] 📍 URL: ${url}`);

  // 确保 target 目录存在
  const targetDir = path.dirname(openapiFile);
  if (!fs.existsSync(targetDir)) {
    fs.mkdirSync(targetDir, { recursive: true });
  }

  // 使用 curl 下载
  const result = spawnSync(
    curlCmd,
    ['-s', '-o', openapiFile, url],
    {
      stdio: 'inherit',
      shell: true,
    }
  );

  if (result.error) {
    console.error(`[api:generate] ❌ curl 执行失败: ${result.error.message}`);
    return false;
  }

  // 检查文件是否存在且不为空
  if (!fs.existsSync(openapiFile)) {
    console.error(`[api:generate] ❌ OpenAPI JSON 文件未生成: ${openapiFile}`);
    return false;
  }

  const fileSize = fs.statSync(openapiFile).size;
  if (fileSize === 0) {
    console.error(`[api:generate] ❌ OpenAPI JSON 文件为空！`);
    return false;
  }

  console.log(`[api:generate] ✅ OpenAPI JSON 下载成功！文件大小: ${(fileSize / 1024).toFixed(2)} KB`);
  return true;
}

/**
 * 主流程
 */
async function main() {
  console.log('\n╔═══════════════════════════════════════════════════════════╗');
  console.log('║  OpenAPI 客户端生成脚本（BaSui 增强版 😎）                ║');
  console.log('╚═══════════════════════════════════════════════════════════╝\n');

  let backendProcess = null;

  try {
    // 步骤 1：清理编译缓存
    runMaven('clean', ['clean']);

    // 步骤 2：编译项目
    console.log('\n[api:generate] 🔨 编译项目...');
    runMaven('compile', ['compile']);

    // 步骤 3：启动后端服务
    const backendInfo = startBackend();
    backendProcess = backendInfo.process;

    // 步骤 4：等待后端服务启动
    const isReady = await waitForBackend(backendInfo);
    if (!isReady) {
      throw new Error('后端服务启动失败');
    }

    // 步骤 5：下载 OpenAPI JSON
    const downloaded = downloadOpenApiJson();
    if (!downloaded) {
      throw new Error('OpenAPI JSON 下载失败');
    }

    // 步骤 6：生成前端客户端
    runMaven('openapi-generator:generate', ['-P', 'openapi', 'openapi-generator:generate']);

    console.log('\n╔═══════════════════════════════════════════════════════════╗');
    console.log('║  ✅ OpenAPI 客户端更新完成！                              ║');
    console.log('╚═══════════════════════════════════════════════════════════╝\n');

  } catch (error) {
    console.error(`\n[api:generate] ❌ 错误: ${error.message}`);
    process.exit(1);
  } finally {
    // 清理：关闭后端服务
    if (backendProcess) {
      console.log('\n[api:generate] 🛑 关闭后端服务...');
      backendProcess.kill('SIGTERM');

      // 等待进程退出
      await new Promise(resolve => {
        backendProcess.on('exit', () => {
          console.log('[api:generate] ✅ 后端服务已关闭');
          resolve();
        });

        // 如果 2 秒后还没退出，强制杀死
        setTimeout(() => {
          if (!backendProcess.killed) {
            backendProcess.kill('SIGKILL');
          }
          resolve();
        }, 2000);
      });
    }
  }
}

// 运行主流程
main().catch(error => {
  console.error(`\n[api:generate] ❌ 未捕获的错误: ${error.message}`);
  process.exit(1);
});
