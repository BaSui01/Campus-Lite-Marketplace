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
 * 功能开关管理 API 服务
 * @author BaSui 😎
 * @description 功能开关列表、添加、编辑、删除、灰度策略等接口
 */

import { getApi } from '../utils/apiClient';
import type { BaseResponse } from '@campus/shared/api';

/**
 * 功能开关状态枚举
 */
export enum FeatureFlagStatus {
  ENABLED = 'ENABLED',      // 全量启用
  DISABLED = 'DISABLED',    // 全量禁用
  GRAY = 'GRAY'             // 灰度发布
}

/**
 * 灰度策略类型枚举
 */
export enum GrayStrategy {
  USER = 'USER',              // 按用户ID灰度
  CAMPUS = 'CAMPUS',          // 按校园ID灰度
  PERCENTAGE = 'PERCENTAGE'   // 按百分比灰度
}

/**
 * 环境枚举
 */
export enum Environment {
  DEV = 'DEV',
  TEST = 'TEST',
  PROD = 'PROD'
}

/**
 * 功能开关信息
 */
export interface FeatureFlag {
  id: number;
  name: string;
  key: string;              // 功能Key（唯一标识）
  description: string;
  status: FeatureFlagStatus;
  strategy?: GrayStrategy;   // 灰度策略
  grayRule?: string;        // 灰度规则（JSON字符串）
  environment: Environment;
  createdAt: string;
  updatedAt?: string;
}

/**
 * 功能开关列表查询参数
 */
export interface FeatureFlagListParams {
  keyword?: string;
  status?: FeatureFlagStatus;
  environment?: Environment;
  page?: number;
  size?: number;
}

/**
 * 分页响应
 */
export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

/**
 * 添加/编辑功能开关请求
 */
export interface FeatureFlagRequest {
  name: string;
  key: string;
  description: string;
  status: FeatureFlagStatus;
  strategy?: GrayStrategy;
  grayRule?: GrayRuleConfig;
  environment: Environment;
}

/**
 * 灰度规则配置
 */
export interface GrayRuleConfig {
  userIds?: number[];        // 用户ID列表
  campusIds?: number[];      // 校园ID列表
  percentage?: number;       // 百分比（0-100）
}

/**
 * 功能开关使用日志
 */
export interface FeatureFlagLog {
  id: number;
  featureFlagId: number;
  userId: number;
  userName: string;
  action: string;           // 操作类型
  result: boolean;          // 是否通过
  createdAt: string;
}

/**
 * 功能开关 API 服务类
 */
export class FeatureFlagService {
  /**
   * 获取功能开关列表（分页）
   * @param params 查询参数
   * @returns 功能开关列表（分页）
   */
  async list(params?: FeatureFlagListParams): Promise<PageResponse<FeatureFlag>> {
    const response = await http.get<PageResponse<FeatureFlag>>('/api/admin/feature-flags', {
      params: {
        keyword: params?.keyword,
        status: params?.status,
        environment: params?.environment,
        page: params?.page ?? 0,
        size: params?.size ?? 20
      }
    });
    return response.data;
  }

  /**
   * 获取功能开关详情
   * @param id 功能开关ID
   * @returns 功能开关详情
   */
  async getDetail(id: number): Promise<FeatureFlag> {
    const response = await http.get<FeatureFlag>(`/api/admin/feature-flags/${id}`);
    return response.data;
  }

  /**
   * 添加功能开关
   * @param data 功能开关信息
   * @returns 创建的功能开关ID
   */
  async create(data: FeatureFlagRequest): Promise<number> {
    // 处理灰度规则
    const requestData = {
      ...data,
      grayRule: data.grayRule ? JSON.stringify(data.grayRule) : undefined
    };
    
    const response = await http.post<number>('/api/admin/feature-flags', requestData);
    return response.data;
  }

  /**
   * 更新功能开关信息
   * @param id 功能开关ID
   * @param data 功能开关信息
   * @returns 更新后的功能开关信息
   */
  async update(id: number, data: Partial<FeatureFlagRequest>): Promise<FeatureFlag> {
    // 处理灰度规则
    const requestData = {
      ...data,
      grayRule: data.grayRule ? JSON.stringify(data.grayRule) : undefined
    };
    
    const response = await http.put<FeatureFlag>(`/api/admin/feature-flags/${id}`, requestData);
    return response.data;
  }

  /**
   * 删除功能开关
   * @param id 功能开关ID
   */
  async delete(id: number): Promise<void> {
    await http.delete(`/api/admin/feature-flags/${id}`);
  }

  /**
   * 更新功能开关状态
   * @param id 功能开关ID
   * @param status 状态
   * @returns 更新后的功能开关信息
   */
  async updateStatus(id: number, status: FeatureFlagStatus): Promise<FeatureFlag> {
    return this.update(id, { status });
  }

  /**
   * 获取功能开关使用日志
   * @param id 功能开关ID
   * @param page 页码
   * @param size 每页大小
   * @returns 使用日志列表
   */
  async getLogs(id: number, page: number = 0, size: number = 20): Promise<PageResponse<FeatureFlagLog>> {
    const response = await http.get<PageResponse<FeatureFlagLog>>(
      `/api/admin/feature-flags/${id}/logs`,
      { params: { page, size } }
    );
    return response.data;
  }

  /**
   * 批量删除功能开关
   * @param ids 功能开关ID列表
   */
  async batchDelete(ids: number[]): Promise<void> {
    await http.post('/api/admin/feature-flags/batch/delete', { ids });
  }

  /**
   * 检查功能开关是否启用（客户端调用）
   * @param key 功能Key
   * @returns 是否启用
   */
  async isEnabled(key: string): Promise<boolean> {
    const response = await http.get<boolean>(`/api/admin/feature-flags/${key}/enabled`);
    return response.data;
  }

  /**
   * 解析灰度规则（前端辅助方法）
   * @param grayRule JSON字符串
   * @returns 灰度规则对象
   */
  parseGrayRule(grayRule?: string): GrayRuleConfig | undefined {
    if (!grayRule) return undefined;
    try {
      return JSON.parse(grayRule) as GrayRuleConfig;
    } catch (error) {
      console.error('解析灰度规则失败:', error);
      return undefined;
    }
  }
}

/**
 * 功能开关服务实例
 */
export const featureFlagService = new FeatureFlagService();

/**
 * 导出类型
 */
export type {
  FeatureFlag as FeatureFlagType,
  FeatureFlagListParams as FeatureFlagListParamsType,
  FeatureFlagRequest as FeatureFlagRequestType,
  GrayRuleConfig as GrayRuleConfigType,
  FeatureFlagLog as FeatureFlagLogType
};
