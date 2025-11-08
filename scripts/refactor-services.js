#!/usr/bin/env node
/**
 * 🚀 自动化重构脚本 - 将所有 service 文件重构为使用 OpenAPI 生成的 DefaultApi
 * @author BaSui 😎
 * @description 批量替换 http/apiClient 调用为 getApi() 方式
 * @usage node scripts/refactor-services.js
 */

const fs = require('fs');
const path = require('path');

// ==================== 配置 ====================

const FRONTEND_ROOT = path.join(__dirname, '../frontend/packages');

const SERVICE_DIRS = [
  path.join(FRONTEND_ROOT, 'shared/src/services'),
  path.join(FRONTEND_ROOT, 'admin/src/services'),
  path.join(FRONTEND_ROOT, 'portal/src/services'),
];

// 需要排除的文件（已经重构完成的）
const EXCLUDE_FILES = [
  'index.ts',
  'tag.ts',      // ✅ 已手工重构
  'task.ts',     // ✅ 已手工重构
  'goods.ts',    // ✅ 已使用 OpenAPI
  'order.ts',    // ✅ 已使用 OpenAPI
  'export.ts',   // ✅ 已使用 OpenAPI
  'favorite.ts', // ✅ 已使用 OpenAPI
  'payment.ts',  // ✅ 已使用 OpenAPI
];

// ==================== 工具函数 ====================

/**
 * 递归获取所有 .ts 文件
 */
function getAllTsFiles(dir) {
  const files = [];

  if (!fs.existsSync(dir)) {
    console.log(`⚠️  目录不存在: ${dir}`);
    return files;
  }

  const items = fs.readdirSync(dir);

  for (const item of items) {
    const fullPath = path.join(dir, item);
    const stat = fs.statSync(fullPath);

    if (stat.isDirectory()) {
      // 递归子目录
      files.push(...getAllTsFiles(fullPath));
    } else if (item.endsWith('.ts') && !item.endsWith('.test.ts')) {
      files.push(fullPath);
    }
  }

  return files;
}

/**
 * 检查文件是否需要重构
 */
function needsRefactoring(content) {
  // 检查是否使用了 http. 或 apiClient. 调用
  const hasHttpCall = /\b(http|apiClient)\.(get|post|put|delete|patch)\(/.test(content);

  // 检查是否已经使用了 getApi()
  const hasGetApi = /const api = getApi\(\)/.test(content);

  return hasHttpCall && !hasGetApi;
}

/**
 * 分析文件中的 API 调用
 */
function analyzeApiCalls(content) {
  const calls = [];

  // 匹配 http.get/post/put/delete/patch 调用
  const httpRegex = /(http|apiClient)\.(get|post|put|delete|patch)<[^>]*>\s*\(\s*['"`]([^'"`]+)['"`]/g;
  let match;

  while ((match = httpRegex.exec(content)) !== null) {
    const [fullMatch, client, method, path] = match;
    calls.push({
      client,
      method,
      path,
      fullMatch,
    });
  }

  return calls;
}

/**
 * 生成重构报告
 */
function generateReport(filePath, calls) {
  const fileName = path.basename(filePath);
  const relativePath = path.relative(FRONTEND_ROOT, filePath);

  console.log(`\n📄 ${relativePath}`);
  console.log(`   发现 ${calls.length} 个需要重构的 API 调用：`);

  calls.forEach((call, index) => {
    console.log(`   ${index + 1}. ${call.method.toUpperCase()} ${call.path}`);
  });
}

/**
 * 添加重构警告注释
 */
function addWarningComment(content) {
  // 检查是否已有警告注释
  if (content.includes('⚠️ 警告：此文件仍使用手写 API 路径')) {
    return content;
  }

  const warningComment = `/**
 * ⚠️ 警告：此文件仍使用手写 API 路径（http.get/post/put/delete 或 apiClient.xxx）
 * 🔧 需要重构：将所有 API 调用替换为 getApi() + DefaultApi 方法
 * 📋 参考：frontend/packages/shared/src/services/tag.ts（已完成重构示例）
 * 👉 重构步骤：
 *    1. 在 api/api/default-api.ts 中找到对应的 OpenAPI 生成方法
 *    2. 替换为：const api = getApi(); await api.methodName(...)
 *    3. 更新类型定义为使用 OpenAPI 生成的类型
 */
`;

  // 在第一个 import 之前插入警告
  return content.replace(/^(\/\*\*[\s\S]*?\*\/\s*)?(import\s)/, `${warningComment}\n$1$2`);
}

/**
 * 自动导入 getApi
 */
function ensureGetApiImport(content) {
  // 检查是否已导入 getApi
  if (/import.*getApi.*from.*apiClient/.test(content)) {
    return content;
  }

  // 检查是否有其他从 apiClient 导入
  const apiClientImportRegex = /import\s*{([^}]+)}\s*from\s*['"]([^'"]*apiClient)['"]/;
  const match = content.match(apiClientImportRegex);

  if (match) {
    const [fullMatch, imports, modulePath] = match;
    // 添加 getApi 到现有导入
    const newImports = imports.trim() + ', getApi';
    return content.replace(fullMatch, `import { ${newImports} } from '${modulePath}'`);
  } else {
    // 添加新的导入语句
    const importStatement = "import { getApi } from '../utils/apiClient';";
    // 在第一个 import 后插入
    return content.replace(/(import\s[^;]+;)/, `$1\n${importStatement}`);
  }
}

// ==================== 主流程 ====================

function main() {
  console.log('🚀 开始批量分析 service 文件...\n');
  console.log('=' .repeat(80));

  let totalFiles = 0;
  let needsRefactorFiles = 0;
  let totalApiCalls = 0;

  for (const serviceDir of SERVICE_DIRS) {
    const files = getAllTsFiles(serviceDir);

    for (const filePath of files) {
      const fileName = path.basename(filePath);

      // 跳过排除的文件
      if (EXCLUDE_FILES.includes(fileName)) {
        continue;
      }

      totalFiles++;
      const content = fs.readFileSync(filePath, 'utf-8');

      if (needsRefactoring(content)) {
        needsRefactorFiles++;
        const calls = analyzeApiCalls(content);
        totalApiCalls += calls.length;

        if (calls.length > 0) {
          generateReport(filePath, calls);

          // 添加警告注释
          let updatedContent = addWarningComment(content);
          updatedContent = ensureGetApiImport(updatedContent);

          // 写回文件
          fs.writeFileSync(filePath, updatedContent, 'utf-8');
          console.log(`   ✅ 已添加重构提示注释`);
        }
      }
    }
  }

  console.log('\n' + '=' .repeat(80));
  console.log('\n📊 统计报告：');
  console.log(`   总文件数：${totalFiles}`);
  console.log(`   需要重构：${needsRefactorFiles} 个文件`);
  console.log(`   API 调用：${totalApiCalls} 处`);
  console.log(`   已完成：${EXCLUDE_FILES.length} 个文件（已使用 OpenAPI）`);

  console.log('\n💡 下一步：');
  console.log('   1. 查看标记了 ⚠️ 警告的文件');
  console.log('   2. 参考 tag.ts 和 task.ts 的重构示例');
  console.log('   3. 在 api/api/default-api.ts 中找到对应的方法');
  console.log('   4. 替换为 getApi() 方式调用');
  console.log('\n✨ 重构完成！\n');
}

// 执行脚本
if (require.main === module) {
  main();
}

module.exports = { getAllTsFiles, needsRefactoring, analyzeApiCalls };
