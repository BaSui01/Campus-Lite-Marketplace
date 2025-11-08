/**
 * ⚠️ 警告：此文件仍使用手写 API 路径（http.get/post/put/delete）
 * 🔧 需要重构：将所有 http. 调用替换为 getApi() + DefaultApi 方法
 * 📋 参考：frontend/packages/shared/src/services/order.ts（已完成重构）
 * 👉 重构步骤：
 *    1. 找到对应的 OpenAPI 生成的方法名（在 api/api/default-api.ts）
 *    2. 替换为：const api = getApi(); api.methodName(...)
 *    3. 更新返回值类型
 */
/**
 * 任务调度服务
 * @author BaSui 😎
 * @description 定时任务管理、触发、暂停与恢复
 */

import { getApi } from '../utils/apiClient';
import type { BaseResponse } from '@campus/shared/api';

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

  /** 获取任务执行日志（扩展接口，假设后端会添加） */
  getLogs(name: string, limit?: number): Promise<TaskExecutionLog[]>;

  /** 获取任务统计（扩展接口，假设后端会添加） */
  getStatistics(name: string): Promise<TaskStatistics>;
}

// ==================== 服务实现 ====================

class TaskServiceImpl implements TaskService {
  private readonly BASE_PATH = '/api/tasks';

  async list(): Promise<ScheduledTask[]> {
    const response = await http.get<BaseResponse<ScheduledTask[]>>(`${this.BASE_PATH}`);
    return response.data.data;
  }

  async trigger(name: string, params?: string): Promise<number> {
    const response = await http.post<BaseResponse<number>>(
      `${this.BASE_PATH}/${name}/trigger`,
      null,
      { params: params ? { params } : undefined }
    );
    return response.data.data;
  }

  async pause(name: string): Promise<void> {
    await http.post(`${this.BASE_PATH}/${name}/pause`);
  }

  async resume(name: string): Promise<void> {
    await http.post(`${this.BASE_PATH}/${name}/resume`);
  }

  async getLogs(name: string, limit: number = 100): Promise<TaskExecutionLog[]> {
    // 扩展接口，假设后端会添加
    // const response = await http.get<BaseResponse<TaskExecutionLog[]>>(
    //   `${this.BASE_PATH}/${name}/logs`,
    //   { params: { limit } }
    // );
    // return response.data.data;
    return [];
  }

  async getStatistics(name: string): Promise<TaskStatistics> {
    // 扩展接口，假设后端会添加
    // const response = await http.get<BaseResponse<TaskStatistics>>(
    //   `${this.BASE_PATH}/${name}/statistics`
    // );
    // return response.data.data;
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
