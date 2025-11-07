#!/usr/bin/env node
/**
 * 智能批量重构脚本 - 直接替换为 getApi()
 * @author BaSui 😎
 * @description 分析并替换所有 http.get/post/put/delete 为 getApi() 调用
 */

const fs = require('fs');
const path = require('path');

const SERVICES_DIR = path.join(__dirname, '../frontend/packages/shared/src/services');

const FILES_TO_REFACTOR = [
  'user.ts',        // P0 - 高频核心
  'message.ts',     // P0
  'post.ts',        // P0
  'refund.ts',      // P0
  'adminUser.ts',   // P1 - 管理员
  'appeal.ts',
  'campus.ts',
  'category.ts',
  'compliance.ts',
  'dispute.ts',
  'disputeStatistics.ts',
  'featureFlag.ts',
  'role.ts',
  'report.ts',
  'blacklist.ts',   // P2 - 辅助
  'community.ts',
  'logistics.ts',
  'monitor.ts',
  'notificationPreference.ts',
  'notificationTemplate.ts',
  'rateLimit.ts',
  'recommend.ts',
  'softDelete.ts',
  'tag.ts',
  'task.ts',
  'topic.ts',
  'upload.ts',
];

const stats = { total: 0, success: 0, skipped: 0, failed: 0 };

function smartRefactor(filename) {
  const filePath = path.join(SERVICES_DIR, filename);
  const backupPath = filePath + '.old';

  console.log(`\n🔧 处理: ${filename}`);

  if (!fs.existsSync(filePath)) {
    console.log(`  ⚠️  文件不存在`);
    stats.skipped++;
    return;
  }

  let content = fs.readFileSync(filePath, 'utf-8');
  const originalContent = content;

  // 检查是否已重构
  if (content.includes('getApi()') && !content.includes('http.get') && !content.includes('http.post')) {
    console.log(`  ✅ 已重构，跳过`);
    stats.skipped++;
    return;
  }

  try {
    // 备份
    fs.copyFileSync(filePath, backupPath);

    // 1. 替换导入
    content = content.replace(
      /import\s+\{\s*http\s*\}\s+from\s+['"]\.\.\/utils\/apiClient['"]/g,
      "import { getApi } from '../utils/apiClient'"
    );

    // 2. 统一替换模式：http.METHOD(...) → getApi().METHOD(...)
    // 注意：这是通用替换，后续可能需要手动调整参数
    
    // 替换 return http.get(...)
    content = content.replace(
      /return\s+http\.(get|post|put|delete)\s*</g,
      'const api = getApi();\n    // TODO: 调整 API 方法名和参数\n    return api.$1<'
    );

    // 替换 await http.get(...) (无 return)
    content = content.replace(
      /await\s+http\.(get|post|put|delete)\s*</g,
      'const api = getApi();\n    // TODO: 调整 API 方法名和参数\n    await api.$1<'
    );

    // 替换普通调用
    content = content.replace(
      /\bhtt p\.(get|post|put|delete)\(/g,
      'const api = getApi();\n    // TODO: 调整 API 方法名和参数\n    api.$1('
    );

    // 写入
    if (content !== originalContent) {
      fs.writeFileSync(filePath, content, 'utf-8');
      console.log(`  ✅ 重构完成（需手动调整方法名）`);
      stats.success++;
    } else {
      console.log(`  ℹ️  无需修改`);
      fs.unlinkSync(backupPath);
      stats.skipped++;
    }

  } catch (error) {
    console.error(`  ❌ 失败: ${error.message}`);
    if (fs.existsSync(backupPath)) {
      fs.copyFileSync(backupPath, filePath);
      fs.unlinkSync(backupPath);
    }
    stats.failed++;
  }
}

console.log('🚀 智能批量重构开始...\n');
stats.total = FILES_TO_REFACTOR.length;
FILES_TO_REFACTOR.forEach(smartRefactor);

console.log('\n' + '='.repeat(60));
console.log(`📊 统计: 总计 ${stats.total} | 成功 ${stats.success} | 跳过 ${stats.skipped} | 失败 ${stats.failed}`);
console.log('='.repeat(60));
