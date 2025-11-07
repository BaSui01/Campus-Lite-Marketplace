/**
 * 修复 admin/portal 包中服务文件的相对路径引用
 * @author BaSui 😎
 * @description 将 '../utils/apiClient' 替换为 '@campus/shared/utils/apiClient'
 */

const fs = require('fs');
const path = require('path');

/**
 * 修复单个文件的导入路径
 * @param {string} filePath - 文件路径
 * @returns {boolean} - 是否修改了文件
 */
function fixImportsInFile(filePath) {
  try {
    let content = fs.readFileSync(filePath, 'utf8');
    let modified = false;

    // 1. 修复 apiClient 的相对导入
    const apiClientRegex = /from\s+['"]\.\.\/utils\/apiClient['"]/g;
    if (apiClientRegex.test(content)) {
      content = content.replace(apiClientRegex, "from '@campus/shared/utils/apiClient'");
      modified = true;
    }

    // 2. 修复 API models 的相对导入（两种情况：../ 和 ../../）
    const apiModelsRegex = /from\s+['"]\.\.(?:\/\.\.)?\/api\/models['"]/g;
    if (apiModelsRegex.test(content)) {
      content = content.replace(apiModelsRegex, "from '@campus/shared/api/models'");
      modified = true;
    }

    // 3. 修复 API client 的相对导入（两种情况：../ 和 ../../）
    const apiRegex = /from\s+['"]\.\.(?:\/\.\.)?\/api['"]/g;
    if (apiRegex.test(content)) {
      content = content.replace(apiRegex, "from '@campus/shared/api'");
      modified = true;
    }

    // 4. 修复 types 的相对导入
    const typesRegex = /from\s+['"]\.\.\/types['"]/g;
    if (typesRegex.test(content)) {
      content = content.replace(typesRegex, "from '@campus/shared/types'");
      modified = true;
    }

    // 5. 修复其他可能的 shared 相对导入
    const sharedUtilsRegex = /from\s+['"]\.\.\/\.\.\/utils\/([^'"]+)['"]/g;
    if (sharedUtilsRegex.test(content)) {
      content = content.replace(sharedUtilsRegex, "from '@campus/shared/utils/$1'");
      modified = true;
    }

    if (modified) {
      fs.writeFileSync(filePath, content, 'utf8');
      console.log(`✅ ${path.relative(process.cwd(), filePath)}`);
      return true;
    }

    return false;
  } catch (err) {
    console.error(`❌ ${filePath}: ${err.message}`);
    return false;
  }
}

/**
 * 递归遍历目录并处理所有 TS/TSX 文件
 * @param {string} dir - 目录路径
 * @param {Function} callback - 文件处理回调
 */
function walkDir(dir, callback) {
  if (!fs.existsSync(dir)) {
    console.warn(`⚠️  目录不存在: ${dir}`);
    return;
  }

  const files = fs.readdirSync(dir);

  files.forEach(file => {
    const filePath = path.join(dir, file);
    const stat = fs.statSync(filePath);

    if (stat.isDirectory()) {
      if (file !== 'node_modules' && file !== '.git') {
        walkDir(filePath, callback);
      }
    } else if (file.endsWith('.ts') || file.endsWith('.tsx')) {
      callback(filePath);
    }
  });
}

// ==================== 主流程 ====================

console.log('\n🚀 开始修复服务文件导入路径...\n');

// 1. 修复 Admin 包的 services
console.log('📦 处理 Admin 包...\n');
const adminServicesDir = path.join(__dirname, '../frontend/packages/admin/src/services');
let adminCount = 0;
walkDir(adminServicesDir, (filePath) => {
  if (fixImportsInFile(filePath)) {
    adminCount++;
  }
});
console.log(`\n✅ Admin 包修复了 ${adminCount} 个文件\n`);

// 2. 修复 Portal 包的 services
console.log('📦 处理 Portal 包...\n');
const portalServicesDir = path.join(__dirname, '../frontend/packages/portal/src/services');
let portalCount = 0;
walkDir(portalServicesDir, (filePath) => {
  if (fixImportsInFile(filePath)) {
    portalCount++;
  }
});
console.log(`\n✅ Portal 包修复了 ${portalCount} 个文件\n`);

console.log(`\n🎉 修复完成! 共修复 ${adminCount + portalCount} 个文件!\n`);
