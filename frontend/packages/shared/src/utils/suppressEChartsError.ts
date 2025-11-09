/**
 * 🛡️ 抑制 ECharts ResizeObserver 错误
 * @author BaSui 😎
 * @date 2025-11-09
 * @description
 * 这是一个全局错误处理器，用于抑制 ECharts 在 React 18 Strict Mode 下的 ResizeObserver 错误。
 *
 * 问题原因：
 * - React 18 的 Strict Mode 会在开发环境下双重挂载/卸载组件
 * - echarts-for-react@3.0.5 的 ResizeObserver 清理逻辑存在竞态条件
 * - 导致 `Cannot read properties of undefined (reading 'disconnect')` 错误
 *
 * 解决方案：
 * - 在应用启动时调用此函数，全局捕获并抑制此错误
 * - 不影响其他错误的正常抛出
 * - 仅在开发环境生效（生产环境不会出现此问题）
 */

/**
 * 初始化全局错误抑制器
 * @description 在应用入口（main.tsx）调用此函数
 */
export function suppressEChartsResizeObserverError(): void {
  // 只在开发环境启用
  if (import.meta.env.MODE !== 'development') {
    return;
  }

  // 保存原始的 console.error
  const originalConsoleError = console.error;

  // 重写 console.error
  console.error = (...args: any[]) => {
    // 检查是否是 ResizeObserver 相关错误
    const errorMessage = args[0]?.toString() || '';

    if (
      errorMessage.includes('ResizeObserver') ||
      errorMessage.includes('disconnect') ||
      errorMessage.includes('resizeObserver.js') ||
      errorMessage.includes('sensorPool.js')
    ) {
      // 抑制此错误，不输出到控制台
      return;
    }

    // 其他错误正常输出
    originalConsoleError.apply(console, args);
  };

  // 全局错误处理器（捕获未被 try-catch 捕获的错误）
  const originalOnError = window.onerror;

  window.onerror = (message, source, lineno, colno, error) => {
    const errorMessage = message?.toString() || '';

    // 抑制 ResizeObserver 错误
    if (
      errorMessage.includes('ResizeObserver') ||
      errorMessage.includes('disconnect') ||
      error?.message?.includes('ResizeObserver') ||
      error?.message?.includes('disconnect')
    ) {
      return true; // 返回 true 表示错误已处理
    }

    // 其他错误交给原始处理器
    if (originalOnError) {
      return originalOnError(message, source, lineno, colno, error);
    }

    return false;
  };

  console.info('✅ ECharts ResizeObserver 错误抑制器已启用（开发环境）');
}

export default suppressEChartsResizeObserverError;
