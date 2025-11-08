/**
 * 导出功能 Hook
 * 
 * 功能：
 * - 创建导出任务
 * - 轮询任务状态
 * - 下载文件
 * - 进度展示
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */

import { useState, useCallback, useEffect, useRef } from 'react';
import { message } from 'antd';
import { exportService, ExportType } from '@campus/shared';

/**
 * 导出任务状态
 */
export type ExportStatus = 'IDLE' | 'CREATING' | 'PROCESSING' | 'COMPLETED' | 'FAILED';

/**
 * useExport Hook 参数
 */
export interface UseExportOptions {
  /** 导出类型 */
  type: ExportType;
  /** 导出参数 */
  params?: Record<string, any>;
  /** 轮询间隔（毫秒），默认 3000ms */
  pollingInterval?: number;
  /** 最大轮询次数，默认 100 次 */
  maxPollingCount?: number;
  /** 成功回调 */
  onSuccess?: (downloadUrl: string) => void;
  /** 失败回调 */
  onError?: (error: any) => void;
}

/**
 * useExport Hook 返回值
 */
export interface UseExportResult {
  /** 导出状态 */
  status: ExportStatus;
  /** 导出进度（0-100） */
  progress: number;
  /** 是否正在导出 */
  exporting: boolean;
  /** 下载 URL */
  downloadUrl: string | null;
  /** 错误信息 */
  error: string | null;
  /** 开始导出 */
  startExport: (customParams?: Record<string, any>) => Promise<void>;
  /** 取消导出 */
  cancelExport: () => void;
  /** 下载文件 */
  download: () => void;
  /** 重置状态 */
  reset: () => void;
}

/**
 * 导出功能 Hook
 * 
 * @example
 * ```tsx
 * const { status, progress, exporting, downloadUrl, startExport, download } = useExport({
 *   type: ExportType.ORDERS,
 *   params: { status: 'COMPLETED' },
 *   onSuccess: (url) => {
 *     console.log('导出成功:', url);
 *   },
 * });
 * 
 * <Button
 *   onClick={() => startExport()}
 *   loading={exporting}
 *   icon={<DownloadOutlined />}
 * >
 *   导出
 * </Button>
 * 
 * {status === 'PROCESSING' && (
 *   <Progress percent={progress} />
 * )}
 * 
 * {downloadUrl && (
 *   <Button onClick={download}>
 *     下载文件
 *   </Button>
 * )}
 * ```
 */
export const useExport = (options: UseExportOptions): UseExportResult => {
  const {
    type,
    params,
    pollingInterval = 3000,
    maxPollingCount = 100,
    onSuccess,
    onError,
  } = options;

  const [status, setStatus] = useState<ExportStatus>('IDLE');
  const [progress, setProgress] = useState(0);
  const [downloadUrl, setDownloadUrl] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [taskId, setTaskId] = useState<number | null>(null);

  const pollingTimerRef = useRef<NodeJS.Timeout | null>(null);
  const pollingCountRef = useRef(0);

  /**
   * 清理轮询定时器
   */
  const clearPolling = useCallback(() => {
    if (pollingTimerRef.current) {
      clearTimeout(pollingTimerRef.current);
      pollingTimerRef.current = null;
    }
    pollingCountRef.current = 0;
  }, []);

  /**
   * 轮询任务状态
   */
  const pollTaskStatus = useCallback(async () => {
    if (!taskId) return;

    try {
      const tasks = await exportService.listMyExports();
      const task = tasks.find((t) => t.id === taskId);

      if (!task) {
        throw new Error('任务不存在');
      }

      // 更新状态
      if (task.status === 'COMPLETED') {
        setStatus('COMPLETED');
        setProgress(100);
        
        if (task.downloadToken) {
          const url = exportService.downloadExport(task.downloadToken);
          setDownloadUrl(url);
          message.success('导出成功，可以下载了');
          onSuccess?.(url);
        }
        
        clearPolling();
      } else if (task.status === 'FAILED') {
        setStatus('FAILED');
        setError(task.message || '导出失败');
        message.error('导出失败');
        onError?.(new Error(task.message || '导出失败'));
        clearPolling();
      } else if (task.status === 'PROCESSING') {
        setStatus('PROCESSING');
        // 模拟进度（实际应该从后端获取）
        setProgress((prev) => Math.min(prev + 10, 90));

        // 继续轮询
        pollingCountRef.current += 1;
        if (pollingCountRef.current < maxPollingCount) {
          pollingTimerRef.current = setTimeout(pollTaskStatus, pollingInterval);
        } else {
          setStatus('FAILED');
          setError('导出超时');
          message.error('导出超时，请稍后重试');
          clearPolling();
        }
      } else {
        // PENDING 状态，继续轮询
        pollingCountRef.current += 1;
        if (pollingCountRef.current < maxPollingCount) {
          pollingTimerRef.current = setTimeout(pollTaskStatus, pollingInterval);
        }
      }
    } catch (err: any) {
      setStatus('FAILED');
      setError(err.message || '查询任务状态失败');
      message.error(err.message || '查询任务状态失败');
      onError?.(err);
      clearPolling();
    }
  }, [taskId, pollingInterval, maxPollingCount, onSuccess, onError, clearPolling]);

  /**
   * 开始导出
   */
  const startExport = useCallback(
    async (customParams?: Record<string, any>) => {
      try {
        setStatus('CREATING');
        setProgress(0);
        setError(null);
        setDownloadUrl(null);

        // 创建导出任务
        const finalParams = customParams || params;
        const id = await exportService.requestExport({
          type,
          params: finalParams ? JSON.stringify(finalParams) : undefined,
        });

        setTaskId(id);
        setStatus('PROCESSING');
        setProgress(10);
        message.success('导出任务已创建，正在处理...');

        // 开始轮询
        pollingCountRef.current = 0;
        pollingTimerRef.current = setTimeout(pollTaskStatus, pollingInterval);
      } catch (err: any) {
        setStatus('FAILED');
        setError(err.message || '创建导出任务失败');
        message.error(err.message || '创建导出任务失败');
        onError?.(err);
      }
    },
    [type, params, pollingInterval, pollTaskStatus, onError]
  );

  /**
   * 取消导出
   */
  const cancelExport = useCallback(async () => {
    if (taskId) {
      try {
        await exportService.cancelExport(taskId);
        message.success('已取消导出');
        clearPolling();
        setStatus('IDLE');
        setProgress(0);
        setTaskId(null);
      } catch (err: any) {
        message.error(err.message || '取消失败');
      }
    }
  }, [taskId, clearPolling]);

  /**
   * 下载文件
   */
  const download = useCallback(() => {
    if (downloadUrl) {
      window.open(downloadUrl, '_blank');
    }
  }, [downloadUrl]);

  /**
   * 重置状态
   */
  const reset = useCallback(() => {
    clearPolling();
    setStatus('IDLE');
    setProgress(0);
    setDownloadUrl(null);
    setError(null);
    setTaskId(null);
  }, [clearPolling]);

  /**
   * 组件卸载时清理定时器
   */
  useEffect(() => {
    return () => {
      clearPolling();
    };
  }, [clearPolling]);

  return {
    status,
    progress,
    exporting: status === 'CREATING' || status === 'PROCESSING',
    downloadUrl,
    error,
    startExport,
    cancelExport,
    download,
    reset,
  };
};
