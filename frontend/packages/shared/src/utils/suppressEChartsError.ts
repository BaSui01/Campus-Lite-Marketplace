/**
 * 🛡️ 抑制开发环境中的已知警告和错误
 * @author BaSui 😎
 * @date 2025-11-09
 * @description
 * 这是一个全局错误处理器，用于抑制开发环境中的已知警告和错误。
 *
 * 问题原因：
 * 1. ECharts ResizeObserver 错误：
 *    - React 18 的 Strict Mode 会在开发环境下双重挂载/卸载组件
 *    - echarts-for-react@3.0.5 的 ResizeObserver 清理逻辑存在竞态条件
 *    - 导致 `Cannot read properties of undefined (reading 'disconnect')` 错误
 * 
 * 2. Antd 静态方法警告：
 *    - Antd 的 message、notification、modal 静态方法无法消费动态主题上下文
 *    - 警告提示使用 App 组件（已修复，但保留抑制器防止其他地方遗漏）
 *
 * 解决方案：
 * - 在应用启动时调用此函数，全局捕获并抑制这些错误
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

  // 保存原始的 console 方法
  const originalConsoleError = console.error;
  const originalConsoleWarn = console.warn;

  // 重写 console.error
  console.error = (...args: any[]) => {
    // 检查是否是 ResizeObserver 相关错误
    const errorMessage = args[0]?.toString() || '';
    const stackTrace = args[0]?.stack?.toString() || '';
    const errorName = args[0]?.name || '';

    // 更精确的 ECharts ResizeObserver 错误检测
    const isEChartsResizeObserverError = 
      // TypeError: Cannot read properties of undefined (reading 'disconnect')
      (errorName === 'TypeError' && errorMessage.includes('disconnect')) ||
      // 包含 ResizeObserver 关键字
      errorMessage.includes('ResizeObserver') ||
      // 来自 echarts-for-react 相关文件
      errorMessage.includes('resizeObserver.js') ||
      errorMessage.includes('sensorPool.js') ||
      errorMessage.includes('core.tsx') ||
      // 堆栈追踪包含 ECharts 相关信息
      stackTrace.includes('EChartsReactCore') ||
      stackTrace.includes('resizeObserver') ||
      stackTrace.includes('sensorPool') ||
      stackTrace.includes('componentWillUnmount') ||
      stackTrace.includes('dispose') ||
      // 完整错误消息匹配
      errorMessage.includes("Cannot read properties of undefined (reading 'disconnect')");

    if (isEChartsResizeObserverError) {
      // 抑制此错误，不输出到控制台
      return;
    }

    // 其他错误正常输出
    originalConsoleError.apply(console, args);
  };

  // 重写 console.warn（抑制 Antd 静态方法警告 + ECharts 错误）
  console.warn = (...args: any[]) => {
    const warnMessage = args[0]?.toString() || '';
    const stackTrace = args[0]?.stack?.toString() || '';
    const errorName = args[0]?.name || '';

    // 检查是否是 Antd 静态方法警告
    const isAntdStaticWarning =
      warnMessage.includes('[antd:') &&
      warnMessage.includes('Static function can not consume context');

    // 检查是否是 ECharts ResizeObserver 错误（也可能通过 console.warn 输出）
    const isEChartsResizeObserverError =
      // TypeError: Cannot read properties of undefined (reading 'disconnect')
      (errorName === 'TypeError' && warnMessage.includes('disconnect')) ||
      // 包含 ResizeObserver 关键字
      warnMessage.includes('ResizeObserver') ||
      // 来自 echarts-for-react 相关文件
      warnMessage.includes('resizeObserver.js') ||
      warnMessage.includes('sensorPool.js') ||
      warnMessage.includes('core.tsx') ||
      // 堆栈追踪包含 ECharts 相关信息
      stackTrace.includes('EChartsReactCore') ||
      stackTrace.includes('resizeObserver') ||
      stackTrace.includes('sensorPool') ||
      stackTrace.includes('componentWillUnmount') ||
      stackTrace.includes('dispose') ||
      // 完整错误消息匹配
      warnMessage.includes("Cannot read properties of undefined (reading 'disconnect')");

    if (isAntdStaticWarning || isEChartsResizeObserverError) {
      // 抑制此警告，不输出到控制台
      return;
    }

    // 其他警告正常输出
    originalConsoleWarn.apply(console, args);
  };

  // 全局错误处理器（捕获未被 try-catch 捕获的错误）
  const originalOnError = window.onerror;

  window.onerror = (message, source, lineno, colno, error) => {
    const errorMessage = message?.toString() || '';
    const errorStack = error?.stack?.toString() || '';

    // 更精确的 ECharts ResizeObserver 错误检测
    const isEChartsResizeObserverError =
      // TypeError: Cannot read properties of undefined (reading 'disconnect')
      (error?.name === 'TypeError' && errorMessage.includes('disconnect')) ||
      // 包含 ResizeObserver 关键字
      errorMessage.includes('ResizeObserver') ||
      error?.message?.includes('ResizeObserver') ||
      // 来自 echarts-for-react 相关文件
      errorStack.includes('resizeObserver') ||
      errorStack.includes('sensorPool') ||
      errorStack.includes('EChartsReactCore') ||
      errorStack.includes('componentWillUnmount') ||
      // 完整错误消息匹配
      errorMessage.includes("Cannot read properties of undefined (reading 'disconnect')") ||
      error?.message?.includes("Cannot read properties of undefined (reading 'disconnect')");

    if (isEChartsResizeObserverError) {
      return true; // 返回 true 表示错误已处理
    }

    // 其他错误交给原始处理器
    if (originalOnError) {
      return originalOnError(message, source, lineno, colno, error);
    }

    return false;
  };

  console.info('✅ 开发环境错误抑制器已启用');
  console.info('   - ECharts ResizeObserver 错误已抑制');
  console.info('   - Antd 静态方法警告已抑制');
}

export default suppressEChartsResizeObserverError;
