/**
 * ✅ 重构完成：已使用 OpenAPI 生成的 API 客户端
 * 📋 使用方法：healthCheck, getMetrics, getHealthHistory, getSlowQueries, getEndpointStatistics,
 *             getErrorRequests, getQpsStatistics, getUnresolvedErrors, getErrorsBySeverity,
 *             getErrorStatistics, markErrorAsResolved, generatePerformanceReport, getHealthScore, cleanupAllHistory
 */
/**
 * 系统监控服务
 * @author BaSui 😎
 * @description 系统健康检查、性能监控、错误日志（基于 OpenAPI 生成代码）
 */

import { getApi } from '@campus/shared/utils/apiClient';
import type { PageResponse, BaseResponse } from '@campus/shared/api';

// ==================== 类型定义 ====================

/**
 * 健康检查响应
 */
export interface HealthCheckResponse {
  status: 'UP' | 'DOWN' | 'DEGRADED';
  timestamp: string;
  checks: {
    name: string;
    status: 'UP' | 'DOWN';
    message?: string;
    responseTime?: number;
  }[];
}

/**
 * 系统指标
 */
export interface SystemMetrics {
  cpu: {
    usage: number;
    cores: number;
  };
  memory: {
    used: number;
    max: number;
    usagePercent: number;
  };
  jvm: {
    heapUsed: number;
    heapMax: number;
    heapUsagePercent: number;
    threadCount: number;
  };
  disk: {
    used: number;
    total: number;
    usagePercent: number;
  };
  uptime: number;
}

/**
 * 健康检查历史记录
 */
export interface HealthCheckRecord {
  id: number;
  status: 'UP' | 'DOWN' | 'DEGRADED';
  timestamp: string;
  details: Record<string, unknown>;
}

/**
 * API性能日志
 */
export interface ApiPerformanceLog {
  id: number;
  endpoint: string;
  method: string;
  duration: number;
  statusCode: number;
  timestamp: string;
  errorMessage?: string;
}

/**
 * 端点统计
 */
export interface EndpointStats {
  endpoint: string;
  method: string;
  totalRequests: number;
  avgDuration: number;
  maxDuration: number;
  minDuration: number;
  errorRate: number;
}

/**
 * QPS数据
 */
export interface QpsData {
  timestamp: string;
  qps: number;
  avgResponseTime: number;
}

/**
 * 错误日志
 */
export interface ErrorLog {
  id: number;
  message: string;
  stackTrace: string;
  severity: ErrorSeverity;
  endpoint?: string;
  userId?: number;
  timestamp: string;
  resolved: boolean;
}

/**
 * 错误严重程度
 */
export enum ErrorSeverity {
  LOW = 'LOW',
  MEDIUM = 'MEDIUM',
  HIGH = 'HIGH',
  CRITICAL = 'CRITICAL',
}

/**
 * 性能报表
 */
export interface PerformanceReport {
  period: {
    start: string;
    end: string;
    hours: number;
  };
  summary: {
    totalRequests: number;
    avgResponseTime: number;
    errorRate: number;
    slowQueryCount: number;
  };
  topSlowEndpoints: EndpointStats[];
  topErrorEndpoints: EndpointStats[];
  qpsTrend: QpsData[];
  healthScore: number;
}

// ==================== 服务接口 ====================

export interface MonitorService {
  /** 健康检查 */
  healthCheck(): Promise<HealthCheckResponse>;

  /** 获取系统指标 */
  getMetrics(): Promise<SystemMetrics>;

  /** 获取健康检查历史 */
  getHealthHistory(hours?: number): Promise<HealthCheckRecord[]>;

  /** 获取慢查询日志 */
  getSlowQueries(hours?: number): Promise<ApiPerformanceLog[]>;

  /** 获取端点性能统计 */
  getEndpointStatistics(hours?: number): Promise<EndpointStats[]>;

  /** 获取错误请求日志 */
  getErrorRequests(hours?: number): Promise<ApiPerformanceLog[]>;

  /** 获取QPS统计 */
  getQpsStatistics(hours?: number): Promise<QpsData[]>;

  /** 获取未解决的错误 */
  getUnresolvedErrors(): Promise<ErrorLog[]>;

  /** 获取指定严重程度的错误 */
  getErrorsBySeverity(severity: ErrorSeverity, hours?: number): Promise<ErrorLog[]>;

  /** 获取错误统计 */
  getErrorStatistics(hours?: number): Promise<Record<ErrorSeverity, number>>;

  /** 标记错误为已解决 */
  markErrorAsResolved(errorId: number): Promise<void>;

  /** 生成性能报表 */
  generatePerformanceReport(hours?: number): Promise<PerformanceReport>;

  /** 获取系统健康度评分 */
  getHealthScore(hours?: number): Promise<number>;

  /** 清理历史数据 */
  cleanupHistory(daysToKeep?: number): Promise<void>;
}

// ==================== 服务实现 ====================

class MonitorServiceImpl implements MonitorService {
  /**
   * 健康检查
   * @returns 健康检查结果
   */
  async healthCheck(): Promise<HealthCheckResponse> {
    const api = getApi();
    const response = await api.healthCheck();
    return response.data.data as HealthCheckResponse;
  }

  /**
   * 获取系统指标
   * @returns 系统指标
   */
  async getMetrics(): Promise<SystemMetrics> {
    const api = getApi();
    const response = await api.getMetrics();
    return response.data.data as SystemMetrics;
  }

  /**
   * 获取健康检查历史
   * @param hours 时间范围（小时）
   * @returns 健康检查历史记录
   */
  async getHealthHistory(hours: number = 24): Promise<HealthCheckRecord[]> {
    const api = getApi();
    const response = await api.getHealthHistory({ hours });
    return response.data.data as HealthCheckRecord[];
  }

  /**
   * 获取慢查询日志
   * @param hours 时间范围（小时）
   * @returns 慢查询日志
   */
  async getSlowQueries(hours: number = 24): Promise<ApiPerformanceLog[]> {
    const api = getApi();
    const response = await api.getSlowQueries({ hours });
    return response.data.data as ApiPerformanceLog[];
  }

  /**
   * 获取端点性能统计
   * @param hours 时间范围（小时）
   * @returns 端点统计数据
   */
  async getEndpointStatistics(hours: number = 24): Promise<EndpointStats[]> {
    const api = getApi();
    const response = await api.getEndpointStatistics({ hours });
    return response.data.data as EndpointStats[];
  }

  /**
   * 获取错误请求日志
   * @param hours 时间范围（小时）
   * @returns 错误请求日志
   */
  async getErrorRequests(hours: number = 24): Promise<ApiPerformanceLog[]> {
    const api = getApi();
    const response = await api.getErrorRequests({ hours });
    return response.data.data as ApiPerformanceLog[];
  }

  /**
   * 获取QPS统计
   * @param hours 时间范围（小时）
   * @returns QPS统计数据
   */
  async getQpsStatistics(hours: number = 1): Promise<QpsData[]> {
    const api = getApi();
    const response = await api.getQpsStatistics({ hours });
    return response.data.data as QpsData[];
  }

  /**
   * 获取未解决的错误
   * @returns 未解决的错误列表
   */
  async getUnresolvedErrors(): Promise<ErrorLog[]> {
    const api = getApi();
    const response = await api.getUnresolvedErrors();
    return response.data.data as ErrorLog[];
  }

  /**
   * 获取指定严重程度的错误
   * @param severity 错误严重程度
   * @param hours 时间范围（小时）
   * @returns 错误列表
   */
  async getErrorsBySeverity(severity: ErrorSeverity, hours: number = 24): Promise<ErrorLog[]> {
    const api = getApi();
    const response = await api.getErrorsBySeverity({
      severity,
      hours,
    });
    return response.data.data as ErrorLog[];
  }

  /**
   * 获取错误统计
   * @param hours 时间范围（小时）
   * @returns 错误统计数据
   */
  async getErrorStatistics(hours: number = 24): Promise<Record<ErrorSeverity, number>> {
    const api = getApi();
    const response = await api.getErrorStatistics({ hours });
    return response.data.data as Record<ErrorSeverity, number>;
  }

  /**
   * 标记错误为已解决
   * @param errorId 错误ID
   */
  async markErrorAsResolved(errorId: number): Promise<void> {
    const api = getApi();
    await api.markErrorAsResolved({ errorId });
  }

  /**
   * 生成性能报表
   * @param hours 时间范围（小时）
   * @returns 性能报表
   */
  async generatePerformanceReport(hours: number = 24): Promise<PerformanceReport> {
    const api = getApi();
    const response = await api.generatePerformanceReport({ hours });
    return response.data.data as PerformanceReport;
  }

  /**
   * 获取系统健康度评分
   * @param hours 时间范围（小时）
   * @returns 健康度评分（0-100）
   */
  async getHealthScore(hours: number = 24): Promise<number> {
    const api = getApi();
    const response = await api.getHealthScore({ hours });
    return response.data.data as number;
  }

  /**
   * 清理历史数据
   * @param daysToKeep 保留天数
   */
  async cleanupHistory(daysToKeep: number = 30): Promise<void> {
    const api = getApi();
    await api.cleanupAllHistory({ daysToKeep });
  }
}

// ==================== 导出服务实例 ====================

export const monitorService = new MonitorServiceImpl();
