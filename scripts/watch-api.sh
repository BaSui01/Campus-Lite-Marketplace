#!/bin/bash
# ╔═══════════════════════════════════════════════════════════════════════╗
# ║  后端 API 自动生成监听器（BaSui 智能化方案）🔍                          ║
# ║  作者: BaSui 😎 | 状态: 开发中 🚧                                       ║
# ║  用途: 监听后端 Controller 变更，自动重新生成前端 API 客户端            ║
# ║  启动: pnpm api:watch                                                 ║
# ╚═══════════════════════════════════════════════════════════════════════╝

set -euo pipefail  # 🛡️ 安全模式：遇到错误立即退出

# ==================== 配置区域 ====================
BACKEND_DIR="../backend"                           # 后端源码目录
FRONTEND_DIR="."                                  # 前端根目录
API_PACKAGE="frontend/packages/shared/src/api"     # API 生成目标路径
WATCH_PATTERN="*Controller*.java"                  # 监听文件模式
DEBOUNCE_DELAY=2                                  # 防抖延迟（秒）
API_GENERATE_CMD="pnpm api:generate"              # 生成命令

# ==================== 颜色输出 ====================
RED="\033[0;31m"
GREEN="\033[0;32m"
YELLOW="\033[1;33m"
BLUE="\033[0;34m"
PURPLE="\033[0;35m"
CYAN="\033[0;36m"
WHITE="\033[1;37m"
GRAY="\033[0;90m"
NC="\033[0m"  # No Color

# ==================== 工具函数 ====================
log_info() {
    echo -e "${BLUE}ℹ️  [INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}✅ ${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}⚠️  [WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}❌ [ERROR]${NC} $1"
}

log_debug() {
    echo -e "${GRAY}🐛 [DEBUG]${NC} $1"
}

print_header() {
    echo -e "\n${PURPLE}╔════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${PURPLE}║${WHITE}  🚀 BaSui 的 API 自动监听器启动.${NC}${PURPLE}                       ║${NC}"
    echo -e "${PURPLE}║${WHITE}  监听后端变更 → 自动生成前端 API.${NC}${PURPLE}                      ║${NC}"
    echo -e "${PURPLE}╚════════════════════════════════════════════════════════════════╝${NC}\n"
}

# ==================== 环境检查 ====================
check_environment() {
    log_info "🔍 检查运行环境..."
    
    # 检查后端目录是否存在
    if [[ ! -d "$BACKEND_DIR" ]]; then
        log_error "❌ 后端目录不存在: ${BACKEND_DIR}"
        exit 1
    fi
    
    # 检查前端目录
    if [[ ! -d "$FRONTEND_DIR/package.json" ]]; then
        log_error "❌ 前端根目录不存在或缺少 package.json: ${FRONTEND_DIR}"
        exit 1
    fi
    
    # 检查 pnpm 是否可用
    if ! command -v pnpm &> /dev/null; then
        log_error "❌ pnpm 命令未找到，请先安装 pnpm"
        exit 1
    fi
    
    log_success "✅ 环境检查通过！"
}

# ==================== 依赖监听器跨平台实现 ====================
start_watcher() {
    log_info "🔍 启动文件监听器..."
    log_info "📁 监听目录: ${BACKEND_DIR}/src"
    log_info "🎯 文件模式: ${WATCH_PATTERN}"
    log_info "⏱️  防抖延迟: ${DEBOUNCE_DELAY}秒"
    log_info "🔧 生成命令: ${API_GENERATE_CMD}"
    echo -e "${CYAN}按 Ctrl+C 停止监听${NC}\n"

    # 使用 inotifywait (Linux/Mac WSL/Windows with WSL)
    if command -v inotifywait &> /dev/null; then
        log_success "✅ 使用 inotifyway (Linux/WSL)"
        watch_with_inotify
    # 使用 fswatch (Mac)
    elif command -v fswatch &> /dev/null; then
        log_success "✅ 使用 fswatch (Mac)"
        watch_with_fswatch
    # 使用 entr (Linux/Max)
    elif command -v entr &> /dev/null; then
        log_success "✅ 使用 entr (通用)"
        watch_with_entr
    else
        log_warning "⚠️ 未找到文件监听工具，使用轮询模式"
        watch_with_polling
    fi
}

# ==================== 监听器实现 ====================
watch_with_inotify() {
    cd "$BACKEND_DIR/src"
    
    # 无限循环，监听文件修改事件
    while true; do
        inotifywait -r -e modify --include='.*Controller.*\.java$' . \
            --format '%w%f' | while read file; do
            
            log_info "📝 检测到 Controller 变更: $file"
            
            # 防抖处理：等待文件保存完成
            log_debug "⏳ 等待 ${DEBOUNCE_DELAY} 秒..."
            sleep "$DEBOUNCE_DELAY"
            
            # 执行 API 生成
            run_api_generate
        done
    done
}

watch_with_fswatch() {
    cd "$BACKEND_DIR/src"
    
    fswatch -r -1 . --include='.*Controller.*\.java$' | while read file; do
        log_info "📝 检测到 Controller 变更: $file"
        log_debug "⏳ 等待 ${DEBOUNCE_DELAY} 秒..."
        sleep "$DEBOUNCE_DELAY"
        run_api_generate
    done
    
    # fswatch -1 监听一次后退出，重新启动以持续监听
    start_watcher
}

watch_with_entr() {
    cd "$BACKEND_DIR/src"
    
    find . -name "*Controller*.java" | entr -d run_api_generate
    
    # entr 在文件变更后退出，需要重新循环
    start_watcher
}

watch_with_polling() {
    log_warning "⚠️ 轮询模式会消耗更多 CPU，建议安装 inotify-tools 或 fswatch"
    
    # 记录最后修改时间
    local last_mtime=0
    local check_interval=2
    
    cd "$BACKEND_DIR/src"
    
    while true; do
        # 查找最新的 Controller 文件
        local newest_file=$(find . -name "*Controller*.java" -type f -printf '%T@ %p\n' | sort -n | tail -1 | cut -d' ' -f2-)
        local current_mtime=$(stat -c %Y "$newest_file" 2>/dev/null || echo 0)
        
        if [[ $current_mtime -gt $last_mtime ]]; then
            log_info "📝 检测到 Controller 变更: $newest_file"
            
            # 更新最后修改时间
            last_mtime=$current_mtime
            
            # 防抖处理
            log_debug "⏳ 等待 ${DEBOUNCE_DELAY} 秒..."
            sleep "$DEBOUNCE_DELAY"
            
            # 执行 API 生成
            run_api_generate
        fi
        
        sleep "$check_interval"
    done
}

# ==================== API 生成执行 ====================
run_api_generate() {
    log_info "🔧 开始生成前端 API..."
    
    cd "$FRONTEND_DIR"
    
    # 记录开始时间
    local start_time=$(date +%s)
    
    # 执行生成命令
    if $API_GENERATE_CMD; then
        local end_time=$(date +%s)
        local duration=$((end_time - start_time))
        
        log_success "🎉 API 生成完成！耗时 ${duration} 秒"
        
        # 统计生成的文件数量
        if [[ -d "$API_PACKAGE" ]]; then
            local file_count=$(find "$API_PACKAGE" -name "*.ts" | wc -l)
            log_info "📊 共生成 ${file_count} 个 TypeScript 文件"
        fi
        
        # 发送通知（如果系统支持）
        if command -v notify-send &> /dev/null; then
            notify-send "API 生成成功" "前端 API 已自动更新" --icon=dialog-information
        elif command -v osascript &> /dev/null; then
            osascript -e 'display notification "前端 API 已自动更新" with title "API 生成成功"'
        fi
        
    else
        log_error "❌ API 生成失败，请检查后端是否能正常启动"
        log_info "💡 可以手动运行: pnpm api:generate"
        
        # 错误通知
        if command -v notify-send &> /dev/null; then
            notify-send "API 生成失败" "请检查错误日志" --icon=dialog-error
        elif command -v osascript &> /dev/null; then
            osascript -e 'display notification "请检查错误日志" with title "API 生成失败"'
        fi
    fi
    
    echo -e "${CYAN}🔄 继续监听后端变更...\n${NC}"
}

# ==================== 主程序 ====================
main() {
    print_header
    
    # 环境检查
    check_environment
    
    # 启动监听
    start_watcher
}

# 捕获 Ctrl+C 信号
trap 'echo -e "\n${YELLOW}👋 停止监听，再见！${NC}"; exit 0' INT

# 启动主程序
main "$@"
