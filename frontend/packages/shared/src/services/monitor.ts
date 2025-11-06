/**
 * 系统监控服务
 * @author BaSui 😎
 * @description 系统健康检查、性能监控、错误日志
 */

import { http } from '@campus/shared/utils/http';
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
  private readonly BASE_PATH = '/api/monitor';

  async healthCheck(): Promise<HealthCheckResponse> {
    const response = await http.get<BaseResponse<HealthCheckResponse>>(`${this.BASE_PATH}/health`);
    return response.data.data;
  }

  async getMetrics(): Promise<SystemMetrics> {
    const response = await http.get<BaseResponse<SystemMetrics>>(`${this.BASE_PATH}/metrics`);
    return response.data.data;
  }

  async getHealthHistory(hours: number = 24): Promise<HealthCheckRecord[]> {
    const response = await http.get<BaseResponse<HealthCheckRecord[]>>(
      `${this.BASE_PATH}/health/history`,
      { params: { hours } }
    );
    return response.data.data;
  }

  async getSlowQueries(hours: number = 24): Promise<ApiPerformanceLog[]> {
    const response = await http.get<BaseResponse<ApiPerformanceLog[]>>(
      `${this.BASE_PATH}/api/slow-queries`,
      { params: { hours } }
    );
    return response.data.data;
  }

  async getEndpointStatistics(hours: number = 24): Promise<EndpointStats[]> {
    const response = await http.get<BaseResponse<EndpointStats[]>>(
      `${this.BASE_PATH}/api/statistics`,
      { params: { hours } }
    );
    return response.data.data;
  }

  async getErrorRequests(hours: number = 24): Promise<ApiPerformanceLog[]> {
    const response = await http.get<BaseResponse<ApiPerformanceLog[]>>(
      `${this.BASE_PATH}/api/errors`,
      { params: { hours } }
    );
    return response.data.data;
  }

  async getQpsStatistics(hours: number = 1): Promise<QpsData[]> {
    const response = await http.get<BaseResponse<QpsData[]>>(
      `${this.BASE_PATH}/api/qps`,
      { params: { hours } }
    );
    return response.data.data;
  }

  async getUnresolvedErrors(): Promise<ErrorLog[]> {
    const response = await http.get<BaseResponse<ErrorLog[]>>(
      `${this.BASE_PATH}/errors/unresolved`
    );
    return response.data.data;
  }

  async getErrorsBySeverity(severity: ErrorSeverity, hours: number = 24): Promise<ErrorLog[]> {
    const response = await http.get<BaseResponse<ErrorLog[]>>(
      `${this.BASE_PATH}/errors/by-severity`,
      { params: { severity, hours } }
    );
    return response.data.data;
  }

  async getErrorStatistics(hours: number = 24): Promise<Record<ErrorSeverity, number>> {
    const response = await http.get<BaseResponse<Record<ErrorSeverity, number>>>(
      `${this.BASE_PATH}/errors/statistics`,
      { params: { hours } }
    );
    return response.data.data;
  }

  async markErrorAsResolved(errorId: number): Promise<void> {
    await http.post(`${this.BASE_PATH}/errors/${errorId}/resolve`);
  }

  async generatePerformanceReport(hours: number = 24): Promise<PerformanceReport> {
    const response = await http.get<BaseResponse<PerformanceReport>>(
      `${this.BASE_PATH}/report`,
      { params: { hours } }
    );
    return response.data.data;
  }

  async getHealthScore(hours: number = 24): Promise<number> {
    const response = await http.get<BaseResponse<number>>(
      `${this.BASE_PATH}/health-score`,
      { params: { hours } }
    );
    return response.data.data;
  }

  async cleanupHistory(daysToKeep: number = 30): Promise<void> {
    await http.delete(`${this.BASE_PATH}/cleanup`, {
      params: { daysToKeep },
    });
  }
}

// ==================== 导出服务实例 ====================

export const monitorService = new MonitorServiceImpl();
