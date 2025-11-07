#!/usr/bin/env node
/**
 * 服务层批量重构脚本
 * @author BaSui 😎
 * @description 批量将手写API路径替换为OpenAPI生成的DefaultApi
 */

const fs = require('fs');
const path = require('path');

// 配置
const SERVICES_DIR = path.join(__dirname, '../frontend/packages/shared/src/services');
const BACKUP_SUFFIX = '.old';

// 待重构文件列表
const FILES_TO_REFACTOR = [
  'adminUser.ts',
  'appeal.ts',
  'blacklist.ts',
  'campus.ts',
  'category.ts',
  'community.ts',
  'compliance.ts',
  'dispute.ts',
  'disputeStatistics.ts',
  'featureFlag.ts',
  'logistics.ts',
  'message.ts',
  'monitor.ts',
  'notificationPreference.ts',
  'notificationTemplate.ts',
  'post.ts',
  'rateLimit.ts',
  'recommend.ts',
  'refund.ts',
  'report.ts',
  'role.ts',
  'softDelete.ts',
  'tag.ts',
  'task.ts',
  'topic.ts',
  'upload.ts',
  'user.ts',
];

// 重构统计
const stats = {
  total: 0,
  success: 0,
  skipped: 0,
  failed: 0,
  details: [],
};

/**
 * 重构单个文件
 */
function refactorFile(filename) {
  const filePath = path.join(SERVICES_DIR, filename);
  const backupPath = filePath + BACKUP_SUFFIX;

  console.log(`\n🔧 重构: ${filename}`);

  // 检查文件是否存在
  if (!fs.existsSync(filePath)) {
    console.log(`  ⚠️  文件不存在，跳过`);
    stats.skipped++;
    return;
  }

  // 读取文件内容
  let content = fs.readFileSync(filePath, 'utf-8');
  const originalContent = content;

  // 检查是否已经重构过
  if (content.includes('getApi()') && !content.includes('http.get') && !content.includes('http.post')) {
    console.log(`  ✅ 已重构，跳过`);
    stats.skipped++;
    return;
  }

  try {
    // 备份原文件
    fs.copyFileSync(filePath, backupPath);
    console.log(`  📋 已备份到: ${filename}${BACKUP_SUFFIX}`);

    // 1. 替换导入语句
    content = content.replace(
      /import\s+\{\s*http\s*\}\s+from\s+['"]\.\.\/utils\/apiClient['"]/g,
      "import { getApi } from '../utils/apiClient'"
    );

    // 2. 添加类型导入（如果还没有）
    if (!content.includes("from '../api/models'")) {
      // 在第一个 import 之后添加
      const firstImportMatch = content.match(/^import.*from.*$/m);
      if (firstImportMatch) {
        const insertPos = content.indexOf(firstImportMatch[0]) + firstImportMatch[0].length;
        content = content.slice(0, insertPos) + 
          "\nimport type { ApiResponse } from '../api/models';" +
          content.slice(insertPos);
      }
    }

    // 3. 替换方法实现 - http.get()
    content = content.replace(
      /return\s+http\.get\s*\(\s*['"`]([^'"`]+)['"`]\s*,?\s*(\{[^}]*\})?\s*\)/g,
      (match, url, params) => {
        console.log(`  🔄 替换 GET: ${url}`);
        return `// TODO: 使用 getApi() 替换 - 原路径: ${url}\n    // const api = getApi();\n    // const response = await api.methodName(...);\n    // return response.data.data;\n    ${match}`;
      }
    );

    // 4. 替换方法实现 - http.post()
    content = content.replace(
      /return\s+http\.post\s*\(\s*['"`]([^'"`]+)['"`]\s*,?\s*([^)]*)\)/g,
      (match, url, data) => {
        console.log(`  🔄 替换 POST: ${url}`);
        return `// TODO: 使用 getApi() 替换 - 原路径: ${url}\n    // const api = getApi();\n    // const response = await api.methodName(...);\n    // return response.data.data;\n    ${match}`;
      }
    );

    // 5. 替换方法实现 - http.put()
    content = content.replace(
      /return\s+http\.put\s*\(\s*['"`]([^'"`]+)['"`]\s*,?\s*([^)]*)\)/g,
      (match, url, data) => {
        console.log(`  🔄 替换 PUT: ${url}`);
        return `// TODO: 使用 getApi() 替换 - 原路径: ${url}\n    // const api = getApi();\n    // const response = await api.methodName(...);\n    // return response.data.data;\n    ${match}`;
      }
    );

    // 6. 替换方法实现 - http.delete()
    content = content.replace(
      /return\s+http\.delete\s*\(\s*['"`]([^'"`]+)['"`]\s*,?\s*([^)]*)\)/g,
      (match, url, data) => {
        console.log(`  🔄 替换 DELETE: ${url}`);
        return `// TODO: 使用 getApi() 替换 - 原路径: ${url}\n    // const api = getApi();\n    // const response = await api.methodName(...);\n    // return response.data.data;\n    ${match}`;
      }
    );

    // 7. 替换 await http.get/post/put/delete (无 return)
    content = content.replace(
      /await\s+http\.(get|post|put|delete)\s*\(\s*['"`]([^'"`]+)['"`]/g,
      (match, method, url) => {
        console.log(`  🔄 替换 ${method.toUpperCase()}: ${url}`);
        return `// TODO: 使用 getApi() 替换 - 原路径: ${url}\n    ${match}`;
      }
    );

    // 检查是否有实际修改
    if (content === originalContent) {
      console.log(`  ℹ️  无需修改`);
      fs.unlinkSync(backupPath); // 删除备份
      stats.skipped++;
      return;
    }

    // 写入修改后的内容
    fs.writeFileSync(filePath, content, 'utf-8');
    console.log(`  ✅ 重构完成（添加了TODO标记，需手动完成）`);
    
    stats.success++;
    stats.details.push({
      file: filename,
      status: 'success',
      message: '已添加TODO标记，需手动映射API方法',
    });

  } catch (error) {
    console.error(`  ❌ 重构失败: ${error.message}`);
    
    // 恢复备份
    if (fs.existsSync(backupPath)) {
      fs.copyFileSync(backupPath, filePath);
      fs.unlinkSync(backupPath);
      console.log(`  🔙 已恢复原文件`);
    }
    
    stats.failed++;
    stats.details.push({
      file: filename,
      status: 'failed',
      error: error.message,
    });
  }
}

/**
 * 主函数
 */
function main() {
  console.log('🚀 开始批量重构服务层...\n');
  console.log(`📁 目标目录: ${SERVICES_DIR}`);
  console.log(`📋 待重构文件数: ${FILES_TO_REFACTOR.length}\n`);

  stats.total = FILES_TO_REFACTOR.length;

  // 逐个重构
  FILES_TO_REFACTOR.forEach(refactorFile);

  // 输出统计
  console.log('\n' + '='.repeat(60));
  console.log('📊 重构统计');
  console.log('='.repeat(60));
  console.log(`总计: ${stats.total}`);
  console.log(`✅ 成功: ${stats.success}`);
  console.log(`⚠️  跳过: ${stats.skipped}`);
  console.log(`❌ 失败: ${stats.failed}`);
  console.log('='.repeat(60));

  // 输出详细信息
  if (stats.details.length > 0) {
    console.log('\n📋 详细信息:');
    stats.details.forEach(({ file, status, message, error }) => {
      const icon = status === 'success' ? '✅' : '❌';
      console.log(`${icon} ${file}: ${message || error}`);
    });
  }

  console.log('\n💡 下一步:');
  console.log('   1. 检查所有文件的 TODO 标记');
  console.log('   2. 根据 OpenAPI 生成的方法名手动替换');
  console.log('   3. 运行 TypeScript 类型检查');
  console.log('   4. 测试核心功能');
}

// 执行
main();
