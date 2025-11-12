#!/usr/bin/env node
// ╔═══════════════════════════════════════════════════════════════════════╗
// ║  后端 API 自动生成监听器（BaSui 智能化方案）🔍 (Node.js 版本)            ║
// ║  作者: BaSui 😎 | 状态: 开发中 🚧                                       ║
// ║  用途: 监听后端 Controller 变更，自动重新生成前端 API 客户端            ║
// ║  启动: pnpm api:watch 或 node scripts/watch-api.js                      ║
// ╚═══════════════════════════════════════════════════════════════════════╝

const path = require('path');
const { execSync, spawn } = require('child_process');
const fs = require('fs');

// ==================== 配置区域 ====================
// 🎯 BaSui提示：无论从哪里启动,都能正确找到项目根目录！
const projectRoot = path.resolve(__dirname, '..');
const config = {
    backendDir: path.join(projectRoot, 'backend'),
    frontendDir: path.join(projectRoot, 'frontend'),  // ✅ 修正：frontend 是实际的前端根目录
    apiPackage: path.join(projectRoot, 'frontend/packages/shared/src/api'),
    watchPattern: '*Controller*.java',
    debounceDelay: 2000, // 毫秒
    apiGenerateCmd: 'pnpm api:generate'
};

// ==================== 颜色输出 ====================
const colors = {
    reset: '\x1b[0m',
    bright: '\x1b[1m',
    red: '\x1b[31m',
    green: '\x1b[32m',
    yellow: '\x1b[33m',
    blue: '\x1b[34m',
    magenta: '\x1b[35m',
    cyan: '\x1b[36m',
    gray: '\x1b[90m'
};

const log = {
    info: (msg) => console.log(`${colors.blue}ℹ️  [INFO]${colors.reset} ${msg}`),
    success: (msg) => console.log(`${colors.green}✅ ${colors.reset} ${msg}`),
    warning: (msg) => console.log(`${colors.yellow}⚠️  [WARN]${colors.reset} ${msg}`),
    error: (msg) => console.log(`${colors.red}❌ [ERROR]${colors.reset} ${msg}`),
    debug: (msg) => console.log(`${colors.gray}🐛 [DEBUG]${colors.reset} ${msg}`)
};

// ==================== 工具函数 ====================
const printHeader = () => {
    console.log(`\n${colors.magenta}╔════════════════════════════════════════════════════════════════╗${colors.reset}`);
    console.log(`${colors.magenta}║${colors.bright}  🚀 BaSui 的 API 自动监听器启动.${colors.reset}${colors.magenta}                    ║${colors.reset}`);
    console.log(`${colors.magenta}║${colors.bright}  监听后端变更 → 自动生成前端 API.${colors.reset}${colors.magenta}                     ║${colors.reset}`);
    console.log(`${colors.magenta}╚════════════════════════════════════════════════════════════════╝${colors.reset}\n`);
};

// 检查环境
const checkEnvironment = () => {
    log.info('🔍 检查运行环境...');
    
    if (!fs.existsSync(config.backendDir)) {
        log.error(`❌ 后端目录不存在: ${config.backendDir}`);
        process.exit(1);
    }
    
    if (!fs.existsSync(path.join(config.frontendDir, 'package.json'))) {
        log.error(`❌ 前端根目录不存在或缺少 package.json: ${config.frontendDir}`);
        process.exit(1);
    }
    
    try {
        execSync('pnpm --version', { stdio: 'pipe' });
    } catch (error) {
        log.error('❌ pnpm 命令未找到，请先安装 pnpm');
        process.exit(1);
    }
    
    log.success('✅ 环境检查通过！');
};

// 检查并安装 chokidar
const ensureChokidar = () => {
    try {
        require.resolve('chokidar');
        log.success('✅ chokidar 已安装');
    } catch (error) {
        log.info('📦 安装 chokidar 文件监听库...');
        try {
            execSync('npm install chokidar', { stdio: 'inherit' });
            log.success('✅ chokidar 安装成功');
        } catch (installError) {
            log.error('❌ chokidar 安装失败，请手动安装: npm install chokidar');
            process.exit(1);
        }
    }
};

// 执行 API 生成
const runApiGenerate = async () => {
    log.info('🔧 开始生成前端 API...');
    
    const startTime = Date.now();
    
    try {
        // 切换到前端目录并执行命令
        const result = execSync(config.apiGenerateCmd, {
            cwd: config.frontendDir,
            encoding: 'utf8',
            stdio: 'pipe'
        });
        
        const duration = Math.round((Date.now() - startTime) / 1000);
        log.success(`🎉 API 生成完成！耗时 ${duration} 秒`);
        
        // 统计生成的文件数量
        if (fs.existsSync(config.apiPackage)) {
            const files = require('fs').readdirSync(config.apiPackage).filter(file => file.endsWith('.ts'));
            log.info(`📊 共生成 ${files.length} 个 TypeScript 文件`);
        }
        
        // 系统通知
        if (process.platform === 'darwin') {
            try {
                execSync(`osascript -e 'display notification "前端 API 已自动更新" with title "API 生成成功"'`);
                log.debug('📢 已发送 macOS 通知');
            } catch (e) {
                log.debug('📢 macOS 通知发送失败，但不影响功能');
            }
        } else if (process.platform === 'linux') {
            try {
                execSync('notify-send "API 生成成功" "前端 API 已自动更新" --icon=dialog-information');
                log.debug('📢 已发送 Linux 通知');
            } catch (e) {
                log.debug('📢 Linux 通知发送失败，但不影响功能');
            }
        }
        
    } catch (error) {
        log.error('❌ API 生成失败，请检查后端是否能正常启动');
        log.info('💡 可以手动运行: pnpm api:generate');
        
        // 错误通知
        if (process.platform === 'darwin') {
            try {
                execSync(`osascript -e 'display notification "请检查错误日志" with title "API 生成失败"'`);
            } catch (e) {}
        } else if (process.platform === 'linux') {
            try {
                execSync('notify-send "API 生成失败" "请检查错误日志" --icon=dialog-error');
            } catch (e) {}
        }
        
        log.debug('生成错误详情:', error.message);
    }
    
    console.log(`${colors.cyan}🔄 继续监听后端变更...${colors.reset}\n`);
};

// 防抖处理
const debounce = (fn, delay) => {
    let timer = null;
    return function(...args) {
        if (timer) clearTimeout(timer);
        timer = setTimeout(() => fn.apply(this, args), delay);
    };
};

// 启动文件监听
const startWatcher = async () => {
    log.info('🔍 启动文件监听器...');
    log.info(`📁 监听目录: ${config.backendDir}/src`);
    log.info(`🎯 文件模式: ${config.watchPattern}`);
    log.info(`⏱️  防抖延迟: ${config.debounceDelay/1000}秒`);
    log.info(`🔧 生成命令: ${config.apiGenerateCmd}`);
    console.log(`${colors.cyan}按 Ctrl+C 停止监听${colors.reset}\n`);

    try {
        // 尝试加载 chokidar
        const chokidar = require('chokidar');
        
        const debouncedGenerate = debounce(runApiGenerate, config.debounceDelay);
        
        log.success('✅ 使用 chokidar (推荐模式)');
        
        // 监 Controller 文件
        const watcher = chokidar.watch(
            path.join(config.backendDir, 'src/**/*Controller*.java'),
            {
                ignored: /node_modules/,
                persistent: true,
                ignoreInitial: true
            }
        );
        
        watcher.on('change', (filePath) => {
            log.info(`📝 检测到 Controller 变更: ${path.relative(process.cwd(), filePath)}`);
            log.debug('⏳ 等待其他可能的变更...');
            debouncedGenerate();
        });
        
        watcher.on('add', (filePath) => {
            log.info(`📝 检测到新 Controller: ${path.relative(process.cwd(), filePath)}`);
            log.debug('⏳ 等待其他可能的变更...');
            debouncedGenerate();
        });
        
        watcher.on('error', (error) => {
            log.error(`❌ 文件监听错误: ${error.message}`);
        });
        
    } catch (chokidarError) {
        log.warning('⚠️ chokidar 不可用，使用回退方案');
        
        // 简单轮询方案
        const pollWatcher = () => {
            log.info('🔄 启动轮询监听模式...');
            log.warning('⚠️ 轮询模式会消耗更多 CPU，建议安装 chokidar');
            
            let lastModified = 0;
            const checkInterval = 2000;
            const controllerFiles = path.join(config.backendDir, 'src');
            
            const check = () => {
                try {
                    const stats = fs.statSync(controllerFiles);
                    if (stats.mtimeMs > lastModified) {
                        lastModified = stats.mtimeMs;
                        // 这里可以进一步检查是否真的有 Controller 文件变更
                        log.info('📝 检测到目录变更，检查 Controller 文件...');
                        // 简化处理：直接触发生成（因为精确检测需要遍历所有文件）
                        runApiGenerate();
                    }
                } catch (error) {
                    log.debug(`检查变更失败: ${error.message}`);
                }
            };
            
            setInterval(check, checkInterval);
        };
        
        pollWatcher();
    }
};

// 主程序
const main = async () => {
    printHeader();
    
    // 检查环境
    checkEnvironment();
    
    // 确保 chokidar 可用
    ensureChokidar();
    
    // 启动监听
    await startWatcher();
};

// 捕获 Ctrl+C
process.on('SIGINT', () => {
    console.log(`\n${colors.yellow}👋 停止监听，再见！${colors.reset}`);
    process.exit(0);
});

// 处理未捕获的异常
process.on('uncaughtException', (error) => {
    log.error(`❌ 未捕获异常: ${error.message}`);
    process.exit(1);
});

process.on('unhandledRejection', (reason) => {
    log.error(`❌ 未处理的 Promise 拒绝: ${reason}`);
    process.exit(1);
});

// 启动主程序
main().catch(error => {
    log.error(`❌ 启动失败: ${error.message}`);
    process.exit(1);
});
