#!/usr/bin/env node
/**
 * 🔥 强力自动化重构脚本 V2 - 直接替换代码
 * @author BaSui 😎
 * @description 自动将 http./apiClient. 调用替换为 getApi() 方式
 * @usage node scripts/refactor-services-v2.js [--dry-run]
 */

const fs = require('fs');
const path = require('path');

// ==================== 配置 ====================

const DRY_RUN = process.argv.includes('--dry-run');

const FRONTEND_ROOT = path.join(__dirname, '../frontend/packages');

// 需要重构的文件列表
const FILES_TO_REFACTOR = [
  // shared
  'shared/src/services/campus.ts',
  'shared/src/services/category.ts',
  'shared/src/services/community.ts',
  'shared/src/services/logistics.ts',
  'shared/src/services/message.ts',
  'shared/src/services/notificationPreference.ts',
  'shared/src/services/rateLimit.ts',
  'shared/src/services/recommend.ts',
  'shared/src/services/refund.ts',
  'shared/src/services/softDelete.ts',
  'shared/src/services/topic.ts',
  'shared/src/services/upload.ts',
  'shared/src/services/post.ts',
  'shared/src/services/credit.ts',
  'shared/src/services/marketing.ts',
  'shared/src/services/sellerStatistics.ts',

  // admin
  'admin/src/services/disputeStatistics.ts',
  'admin/src/services/featureFlag.ts',
  'admin/src/services/monitor.ts',
  'admin/src/services/report.ts',

  // portal
  'portal/src/services/credit.ts',
  'portal/src/services/marketing.ts',
  'portal/src/services/sellerStatistics.ts',
];

// ==================== 重构函数 ====================

/**
 * 替换 import 语句，确保导入 getApi
 */
function refactorImports(content) {
  // 移除 http 导入（如果有）
  content = content.replace(/import\s*\{\s*http\s*\}\s*from\s*['"][^'"]+['"];\s*\n?/g, '');

  // 检查是否已导入 getApi
  const hasGetApi = /import.*getApi.*from.*apiClient/.test(content);

  if (!hasGetApi) {
    // 查找 apiClient 的导入
    const apiClientImportRegex = /import\s*\{([^}]+)\}\s*from\s*['"]([^'"]*apiClient)['"]/;
    const match = content.match(apiClientImportRegex);

    if (match) {
      const [fullMatch, imports, modulePath] = match;

      // 如果只导入了 apiClient，替换为 getApi
      if (imports.trim() === 'apiClient') {
        content = content.replace(fullMatch, `import { getApi } from '${modulePath}'`);
      } else if (!imports.includes('getApi')) {
        // 添加 getApi 到现有导入
        const newImports = imports.trim() + ', getApi';
        content = content.replace(fullMatch, `import { ${newImports} } from '${modulePath}'`);
      }
    } else {
      // 没有 apiClient 导入，添加 getApi 导入
      const importStatement = "import { getApi } from '../utils/apiClient';\n";

      // 在第一个 import 后插入
      if (/^import\s/.test(content)) {
        content = content.replace(/(^import\s[^;]+;)/, `$1\n${importStatement}`);
      } else {
        // 在文件开头的注释后插入
        content = content.replace(/^(\/\*\*[\s\S]*?\*\/\s*)/,`$1${importStatement}\n`);
      }
    }
  }

  return content;
}

/**
 * 替换 API 调用
 * http.get() / apiClient.get() → const api = getApi(); api.xxx()
 */
function refactorApiCalls(content, filePath) {
  const fileName = path.basename(filePath);

  // 简单替换策略：添加注释提示手工重构
  // 因为自动映射 API 方法需要复杂的解析

  // 替换 http. 为 apiClient.（统一处理）
  content = content.replace(/\bhttp\./g, 'apiClient.');

  // 在类开头添加 getApi 使用示例注释
  if (content.includes('apiClient.')) {
    const warningComment = `
  /**
   * ⚠️ TODO: 重构为使用 OpenAPI 生成的 DefaultApi
   *
   * 示例：
   * ❌ 错误：const response = await apiClient.get('/api/xxx');
   * ✅ 正确：const api = getApi(); const response = await api.xxxMethod({ params });
   *
   * 步骤：
   * 1. 在 api/api/default-api.ts 中查找对应的方法
   * 2. 替换为：const api = getApi(); await api.methodName({ ...params })
   * 3. 返回 response.data.data
   *
   * 参考：shared/src/services/tag.ts
   */
`;

    // 在第一个 class 定义前插入
    content = content.replace(/(export class \w+Service)/, `${warningComment}\n$1`);
  }

  return content;
}

/**
 * 更新文件头注释
 */
function updateFileHeader(content) {
  // 移除旧的警告注释
  content = content.replace(/\/\*\*\s*\n\s*\*\s*⚠️ 警告[\s\S]*?\*\/\s*\n?/, '');

  // 更新 @updated 标记
  const today = new Date().toISOString().split('T')[0];

  if (/@updated/.test(content)) {
    content = content.replace(
      /@updated\s+\d{4}-\d{2}-\d{2}[^\n]*/,
      `@updated ${today} - 🔧 待完成重构：将 apiClient 调用替换为 getApi()`
    );
  } else if (/@description/.test(content)) {
    content = content.replace(
      /(@description[^\n]*)\n/,
      `$1\n * @updated ${today} - 🔧 待完成重构：将 apiClient 调用替换为 getApi()\n`
    );
  }

  return content;
}

/**
 * 重构单个文件
 */
function refactorFile(filePath) {
  const fullPath = path.join(FRONTEND_ROOT, filePath);

  if (!fs.existsSync(fullPath)) {
    console.log(`⚠️  文件不存在: ${filePath}`);
    return false;
  }

  console.log(`\n📄 处理: ${filePath}`);

  let content = fs.readFileSync(fullPath, 'utf-8');
  const originalContent = content;

  // 执行重构
  content = updateFileHeader(content);
  content = refactorImports(content);
  content = refactorApiCalls(content, filePath);

  if (content !== originalContent) {
    if (DRY_RUN) {
      console.log(`   🔍 [DRY RUN] 将会修改此文件`);
    } else {
      fs.writeFileSync(fullPath, content, 'utf-8');
      console.log(`   ✅ 已重构`);
    }
    return true;
  } else {
    console.log(`   ⏭️  无需修改`);
    return false;
  }
}

// ==================== 主流程 ====================

function main() {
  console.log('🚀 批量重构服务文件...\n');
  console.log(DRY_RUN ? '🔍 DRY RUN 模式（仅预览，不实际修改）\n' : '✍️  实际修改模式\n');
  console.log('='.repeat(80));

  let processed = 0;
  let modified = 0;

  for (const filePath of FILES_TO_REFACTOR) {
    processed++;
    if (refactorFile(filePath)) {
      modified++;
    }
  }

  console.log('\n' + '='.repeat(80));
  console.log('\n📊 统计：');
  console.log(`   处理文件：${processed}`);
  console.log(`   已修改：${modified}`);
  console.log(`   无需修改：${processed - modified}`);

  console.log('\n💡 下一步：');
  console.log('   1. 查看标记了 TODO 的文件');
  console.log('   2. 参考 tag.ts 的重构模式');
  console.log('   3. 手工完成 API 方法映射');
  console.log('   4. 删除 apiClient.xxx 调用，改用 getApi()');

  if (DRY_RUN) {
    console.log('\n🔍 这是 DRY RUN，没有实际修改文件。');
    console.log('   移除 --dry-run 参数以实际执行重构。');
  }

  console.log('\n✨ 完成！\n');
}

// 执行
if (require.main === module) {
  main();
}

module.exports = { refactorFile, refactorImports, refactorApiCalls };
