/**
 * 批量更新 import 路径
 * @author BaSui 😎
 */

const fs = require('fs');
const path = require('path');

// 移动到 admin 的服务
const adminServices = [
  'statistics',
  'monitor',
  'featureFlag',
  'disputeStatistics',
  'appeal',
  'blacklist',
  'report',
  'dispute',
  'compliance',
  'role',
  'adminUser',
];

// 移动到 portal 的服务
const portalServices = [
  'favorite',
  'follow',
  'credit',
  'recommend',
  'marketing',
  'sellerStatistics',
  'subscription',
];

function updateFile(filePath, services, targetDir) {
  try {
    let content = fs.readFileSync(filePath, 'utf8');
    let modified = false;

    services.forEach(service => {
      const oldImport = `@campus/shared/services/${service}`;

      // 计算相对路径
      const fileDir = path.dirname(filePath);
      const servicesDir = path.join(path.dirname(path.dirname(fileDir)), 'services');
      const relativePath = path.relative(fileDir, servicesDir).replace(/\\/g, '/');
      const newImport = `${relativePath}/${service}`;

      // 替换所有匹配的导入
      if (content.includes(oldImport)) {
        content = content.replace(new RegExp(oldImport, 'g'), newImport);
        modified = true;
      }
    });

    if (modified) {
      fs.writeFileSync(filePath, content, 'utf8');
      console.log(`✅ ${path.relative(process.cwd(), filePath)}`);
      return 1;
    }
    return 0;
  } catch (err) {
    console.error(`❌ ${filePath}: ${err.message}`);
    return 0;
  }
}

function walkDir(dir, services, callback) {
  const files = fs.readdirSync(dir);

  files.forEach(file => {
    const filePath = path.join(dir, file);
    const stat = fs.statSync(filePath);

    if (stat.isDirectory()) {
      if (file !== 'node_modules' && file !== 'services') {
        walkDir(filePath, services, callback);
      }
    } else if (file.endsWith('.ts') || file.endsWith('.tsx')) {
      callback(filePath, services);
    }
  });
}

// 处理 Admin 包
console.log('\n🔍 更新 Admin 包...\n');
const adminSrc = path.join(__dirname, '../frontend/packages/admin/src');
let adminCount = 0;
walkDir(adminSrc, adminServices, (filePath) => {
  adminCount += updateFile(filePath, adminServices, adminSrc);
});
console.log(`\n📊 Admin 包更新了 ${adminCount} 个文件\n`);

// 处理 Portal 包
console.log('\n🔍 更新 Portal 包...\n');
const portalSrc = path.join(__dirname, '../frontend/packages/portal/src');
let portalCount = 0;
walkDir(portalSrc, portalServices, (filePath) => {
  portalCount += updateFile(filePath, portalServices, portalSrc);
});
console.log(`\n📊 Portal 包更新了 ${portalCount} 个文件\n`);

console.log('\n🎉 所有 import 更新完成！\n');
