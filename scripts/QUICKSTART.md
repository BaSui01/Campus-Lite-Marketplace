# 🚀 快速启动指南 - BaSui 的 API 监听器

> **3分钟上手！** 让后端 API 变更自动同步到前端！🎉

---

## ✅ 已完成配置

✨ **scripts 文件夹已独立配置完成！**
- ✅ `package.json` 创建完成
- ✅ `chokidar` 依赖已安装
- ✅ 路径配置已修正（支持任意位置启动）
- ✅ `watch-api.js` 脚本已优化

---

## 🎮 启动方式（三选一）

### 方式1：在 scripts 文件夹内启动 ⭐ 推荐

```bash
# 进入 scripts 文件夹
cd d:/code/campus-lite-marketplace/scripts

# 启动监听器
npm run watch

# 或者直接运行
node watch-api.js
```

**优点**：
- 🎯 独立依赖管理
- 📦 不影响项目根目录
- 🚀 启动速度快

---

### 方式2：从项目根目录启动

```bash
# 在项目根目录
cd d:/code/campus-lite-marketplace

# 直接运行脚本
node scripts/watch-api.js
```

**优点**：
- 🎯 路径自动识别
- 📦 无需切换目录

---

### 方式3：使用 pnpm 启动（前端目录）

```bash
# 在 frontend 文件夹
cd d:/code/campus-lite-marketplace/frontend

# 使用 pnpm 脚本
pnpm api:watch
```

**优点**：
- 🎯 与前端工作流集成
- 📦 统一使用 pnpm 管理

---

## 📊 运行效果

启动成功后会看到：

```
╔════════════════════════════════════════════════════════════════╗
║  🚀 BaSui 的 API 自动监听器启动.                              ║
║  监听后端变更 → 自动生成前端 API.                             ║
╚════════════════════════════════════════════════════════════════╝

ℹ️  [INFO] 🔍 检查运行环境...
✅  ✅ 环境检查通过！
✅  ✅ chokidar 已安装
ℹ️  [INFO] 🔍 启动文件监听器...
ℹ️  [INFO] 📁 监听目录: D:\code\campus-lite-marketplace\backend/src
ℹ️  [INFO] 🎯 文件模式: *Controller*.java
ℹ️  [INFO] ⏱️  防抖延迟: 2秒
ℹ️  [INFO] 🔧 生成命令: pnpm api:generate
按 Ctrl+C 停止监听

✅  ✅ 使用 chokidar (推荐模式)
```

当你修改 Controller 文件时：

```
📝 检测到 Controller 变更: UserController.java
⏳ 等待其他可能的变更...
🔧 开始生成前端 API...
🎉 API 生成完成！耗时 15 秒
📊 共生成 142 个 TypeScript 文件
🔄 继续监听后端变更...
```

---

## 🔧 配置说明

### 核心配置（scripts/watch-api.js）

```javascript
const config = {
    backendDir: 'D:/code/campus-lite-marketplace/backend',
    frontendDir: 'D:/code/campus-lite-marketplace/frontend',
    apiPackage: 'D:/code/campus-lite-marketplace/frontend/packages/shared/src/api',
    watchPattern: '*Controller*.java',  // 监听的文件模式
    debounceDelay: 2000,                // 防抖延迟（毫秒）
    apiGenerateCmd: 'pnpm api:generate' // API 生成命令
};
```

### 依赖配置（scripts/package.json）

```json
{
  "name": "campus-marketplace-scripts",
  "version": "1.0.0",
  "description": "BaSui 的工具脚本集合 🚀",
  "scripts": {
    "watch": "node watch-api.js"
  },
  "dependencies": {
    "chokidar": "^4.0.1"
  }
}
```

---

## 🐛 常见问题

### Q1: 启动失败，提示找不到模块

```bash
# 解决方案：重新安装依赖
cd d:/code/campus-lite-marketplace/scripts
npm install
```

### Q2: 监听器不生成 API

```bash
# 检查后端是否正常
cd d:/code/campus-lite-marketplace/backend
mvn spring-boot:run

# 手动测试 API 生成
cd d:/code/campus-lite-marketplace/frontend
pnpm api:generate
```

### Q3: 路径报错

确认路径配置正确：
- ✅ 后端目录：`d:/code/campus-lite-marketplace/backend`
- ✅ 前端目录：`d:/code/campus-lite-marketplace/frontend`
- ✅ API 包目录：`d:/code/campus-lite-marketplace/frontend/packages/shared/src/api`

---

## 🎯 开发建议

### 推荐工作流

1. **启动监听器**
   ```bash
   cd scripts && npm run watch
   ```

2. **修改后端 Controller**
   - 自动触发 API 生成
   - 无需手动操作

3. **前端开发**
   - 使用最新的类型定义
   - 享受 TypeScript 智能提示

---

## 📁 文件结构

```
scripts/
├── watch-api.js          # 监听器主程序 ✨
├── watch-api.sh           # Bash 版本（Linux/Mac）
├── package.json           # 依赖配置 ✨ NEW!
├── package-lock.json      # npm 锁定文件
├── node_modules/          # 依赖包（chokidar）
├── README.md              # 完整说明文档
└── QUICKSTART.md          # 本快速指南 ⭐ YOU ARE HERE!
```

---

## 💝 BaSui 的最后叮嘱

**记住三点：**
1. 🎯 **独立启动** - scripts 文件夹独立配置，想在哪里启动都行！
2. 🔄 **自动同步** - 修改 Controller 后自动生成 API，省心省力！
3. 🚀 **快速开发** - 专注业务逻辑，API 同步交给我！

**座右铭**：
> 代码要写得漂亮，工具要用得顺手！
> 自动化是生产力，快乐编程才是王道！💪✨

---

**作者：** BaSui 😎
**更新：** 2025-11-07
**状态：** ✅ 可用

🎉 **开始使用吧！祝你编程愉快！** 🚀
