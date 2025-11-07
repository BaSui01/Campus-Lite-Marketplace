/**
 * 服务层重构 - 自动更新 import 路径脚本
 * @author BaSui 😎
 */

const fs = require('fs');
const path = require('path');
const glob = require('glob');

// 移动到 admin 的服务映射
const adminServiceMap = {
  'statistics': 'statistics',
  'adminUser': 'adminUser',
  'monitor': 'monitor',
  'compliance': 'compliance',
  'dispute': 'dispute',
  'disputeStatistics': 'disputeStatistics',
  'appeal': 'appeal',
  'blacklist': 'blacklist',
  'report': 'report',
  'featureFlag': 'featureFlag',
  'role': 'role',
  // 新增的管理员服务
  'adminGoods': 'adminGoods',
  'adminCategory': 'adminCategory',
};

// 移动到 portal 的服务映射
const portalServiceMap = {
  'favorite': 'favorite',
  'follow': 'follow',
  'credit': 'credit',
  'recommend': 'recommend',
  'marketing': 'marketing',
  'sellerStatistics': 'sellerStatistics',
  'subscription': 'subscription',
};

function updateImportsInFile(filePath, isAdmin) {
  let content = fs.readFileSync(filePath, 'utf8');
  let modified = false;

  const serviceMap = isAdmin ? adminServiceMap : portalServiceMap;
  const targetPath = isAdmin ? '../services' : '../services';

  for (const [oldName, newName] of Object.entries(serviceMap)) {
    // 匹配: import { xxx } from '@campus/shared/services/xxx'
    const regex1 = new RegExp(
      `from\\s+['"]@campus/shared/services/${oldName}['"]`,
      'g'
    );
    if (regex1.test(content)) {
      content = content.replace(regex1, `from '${targetPath}/${newName}'`);
      modified = true;
    }

    // 匹配: import xxx from '@campus/shared/services/xxx'
    const regex2 = new RegExp(
      `from\\s+['"]@campus/shared/services/${oldName}['"]`,
      'g'
    );
    if (regex2.test(content)) {
      content = content.replace(regex2, `from '${targetPath}/${newName}'`);
      modified = true;
    }
  }

  if (modified) {
    fs.writeFileSync(filePath, content, 'utf8');
    console.log(`✅ 更新成功: ${filePath}`);
    return true;
  }

  return false;
}

function processPackage(packagePath, isAdmin) {
  const packageName = isAdmin ? 'admin' : 'portal';
  console.log(`\n🔍 处理 ${packageName} 包...\n`);

  const files = glob.sync(`${packagePath}/src/**/*.{ts,tsx}`, {
    ignore: ['**/node_modules/**', '**/*.test.ts', '**/*.test.tsx'],
  });

  let count = 0;
  files.forEach(file => {
    if (updateImportsInFile(file, isAdmin)) {
      count++;
    }
  });

  console.log(`\n📊 ${packageName} 包共更新 ${count} 个文件\n`);
}

// 主流程
console.log('🚀 开始更新 import 路径...\n');

const rootDir = path.resolve(__dirname, '../frontend/packages');

// 处理 admin 包
processPackage(path.join(rootDir, 'admin'), true);

// 处理 portal 包
processPackage(path.join(rootDir, 'portal'), false);

console.log('\n🎉 所有 import 路径更新完成！\n');
