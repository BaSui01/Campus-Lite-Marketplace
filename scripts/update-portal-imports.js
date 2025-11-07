/**
 * 批量更新 Portal 包的 import 路径（从 index 统一导入的情况）
 * @author BaSui 😎
 */

const fs = require('fs');
const path = require('path');

// Portal 服务映射
const portalServiceMap = {
  favoriteService: 'favorite',
  followService: 'follow',
  creditService: 'credit',
  recommendService: 'recommend',
  marketingService: 'marketing',
  sellerStatisticsService: 'sellerStatistics',
  subscriptionService: 'subscription',
  // 导出的类型和常量
  CreditLevel: 'credit',
  CREDIT_LEVEL_CONFIG: 'credit',
  ReportType: 'sellerStatistics',
  CampaignType: 'marketing',
  CampaignStatus: 'marketing',
  DiscountType: 'marketing',
  CAMPAIGN_TYPE_CONFIG: 'marketing',
  CAMPAIGN_STATUS_CONFIG: 'marketing',
  SubscriptionType: 'subscription',
};

function updatePortalFile(filePath) {
  try {
    let content = fs.readFileSync(filePath, 'utf8');
    let modified = false;

    // 匹配: import { xxx, yyy } from '@campus/shared/services';
    const importRegex = /import\s+\{([^}]+)\}\s+from\s+['"]@campus\/shared\/services['"]/g;

    const matches = [...content.matchAll(importRegex)];

    matches.forEach(match => {
      const fullImport = match[0];
      const imports = match[1].split(',').map(s => s.trim());

      // 按服务分组
      const serviceGroups = {};
      const remainingImports = [];

      imports.forEach(imp => {
        // 处理类型导入和重命名
        const importName = imp.replace(/^type\s+/, '').split(/\s+as\s+/)[0].trim();
        const serviceName = portalServiceMap[importName];

        if (serviceName) {
          if (!serviceGroups[serviceName]) {
            serviceGroups[serviceName] = [];
          }
          serviceGroups[serviceName].push(imp);
        } else {
          // 不在映射中的保留原样（可能是 shared 服务）
          remainingImports.push(imp);
        }
      });

      // 构建新的 import 语句
      let newImports = [];

      // 添加本地服务的 import
      Object.entries(serviceGroups).forEach(([serviceName, imports]) => {
        // 计算相对路径
        const fileDir = path.dirname(filePath);
        const servicesDir = path.join(path.dirname(path.dirname(fileDir)), 'services');
        const relativePath = path.relative(fileDir, servicesDir).replace(/\\/g, '/');

        newImports.push(`import { ${imports.join(', ')} } from '${relativePath}/${serviceName}';`);
      });

      // 如果还有剩余的 shared 服务，保留原 import
      if (remainingImports.length > 0) {
        newImports.push(`import { ${remainingImports.join(', ')} } from '@campus/shared/services';`);
      }

      // 替换原 import
      if (newImports.length > 0) {
        content = content.replace(fullImport, newImports.join('\n'));
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

function walkDir(dir, callback) {
  const files = fs.readdirSync(dir);

  files.forEach(file => {
    const filePath = path.join(dir, file);
    const stat = fs.statSync(filePath);

    if (stat.isDirectory()) {
      if (file !== 'node_modules' && file !== 'services') {
        walkDir(filePath, callback);
      }
    } else if (file.endsWith('.ts') || file.endsWith('.tsx')) {
      callback(filePath);
    }
  });
}

// 处理 Portal 包
console.log('\n🔍 更新 Portal 包...\n');
const portalSrc = path.join(__dirname, '../frontend/packages/portal/src');
let count = 0;
walkDir(portalSrc, (filePath) => {
  count += updatePortalFile(filePath);
});
console.log(`\n📊 Portal 包更新了 ${count} 个文件\n`);
console.log('\n🎉 Portal import 更新完成！\n');
