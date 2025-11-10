/**
 * 任务调度服务
 * @author BaSui 😎
 * @description 定时任务管理、触发、暂停与恢复
 * @updated 2025-11-08 - 重构为使用 OpenAPI 生成的 DefaultApi ✅
 */

import { getApi } from '../utils/apiClient';
// import type { ScheduledTask as ApiScheduledTask } from '../api/models'; // TODO: 等待后端实现后使用

// ==================== 类型定义 ====================

/**
 * 定时任务
 */
export interface ScheduledTask {
  name: string;
  description: string;
  cron: string;
  status: TaskStatus;
  lastExecuteTime?: string;
  nextExecuteTime?: string;
  lastExecuteSuccess?: boolean;
  lastExecuteMessage?: string;
  totalExecuteCount?: number;
  successExecuteCount?: number;
  failureExecuteCount?: number;
}

/**
 * 任务状态
 */
export enum TaskStatus {
  RUNNING = 'RUNNING',
  PAUSED = 'PAUSED',
  DISABLED = 'DISABLED',
}

/**
 * 任务执行日志
 */
export interface TaskExecutionLog {
  id: number;
  taskName: string;
  startTime: string;
  endTime?: string;
  duration?: number;
  status: 'SUCCESS' | 'FAILURE' | 'RUNNING';
  message?: string;
  params?: string;
  result?: string;
  errorMessage?: string;
}

/**
 * 任务统计
 */
export interface TaskStatistics {
  taskName: string;
  totalCount: number;
  successCount: number;
  failureCount: number;
  avgDuration: number;
  maxDuration: number;
  minDuration: number;
  successRate: number;
  lastExecuteTime?: string;
}

// ==================== 服务接口 ====================

export interface TaskService {
  /** 获取任务列表 */
  list(): Promise<ScheduledTask[]>;

  /** 触发执行任务 */
  trigger(name: string, params?: string): Promise<number>;

  /** 暂停任务 */
  pause(name: string): Promise<void>;

  /** 恢复任务 */
  resume(name: string): Promise<void>;

  /** 获取任务执行日志 */
  getLogs(name: string, limit?: number): Promise<TaskExecutionLog[]>;

  /** 获取任务统计 */
  getStatistics(name: string): Promise<TaskStatistics>;
}

// ==================== 服务实现 ====================

class TaskServiceImpl implements TaskService {
  /**
   * 获取任务列表
   * ✅ 使用 OpenAPI 生成的 list() 方法
   */
  async list(): Promise<ScheduledTask[]> {
    const api = getApi();
    const response = await api.list();
    return response.data.data as ScheduledTask[];
  }

  /**
   * 触发执行任务
   * ✅ 使用 OpenAPI 生成的 trigger() 方法
   */
  async trigger(name: string, params?: string): Promise<number> {
    const api = getApi();
    const response = await api.trigger({ name, params });
    return response.data.data as number;
  }

  /**
   * 暂停任务
   * ✅ 使用 OpenAPI 生成的 pause() 方法
   */
  async pause(name: string): Promise<void> {
    const api = getApi();
    await api.pause({ name });
  }

  /**
   * 恢复任务
   * ✅ 使用 OpenAPI 生成的 resume() 方法
   */
  async resume(name: string): Promise<void> {
    const api = getApi();
    await api.resume({ name });
  }

  /**
   * 获取任务执行日志
   * ⚠️ 扩展接口，假设后端会添加
   */
  async getLogs(_name: string, _limit: number = 100): Promise<TaskExecutionLog[]> {
    // TODO: 等待后端实现后启用
    return [];
  }

  /**
   * 获取任务统计
   * ⚠️ 扩展接口，假设后端会添加
   */
  async getStatistics(name: string): Promise<TaskStatistics> {
    // TODO: 等待后端实现后启用
    return {
      taskName: name,
      totalCount: 0,
      successCount: 0,
      failureCount: 0,
      avgDuration: 0,
      maxDuration: 0,
      minDuration: 0,
      successRate: 0,
    };
  }
}

// ==================== 导出服务实例 ====================

export const taskService = new TaskServiceImpl();
export default taskService;
