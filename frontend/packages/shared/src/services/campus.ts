/**
 * 校园管理 API 服务
 * @author BaSui 😎
 * @description 校园列表、添加、编辑、删除、统计等接口
 */

import { http } from '../utils/http';
import type { BaseResponse } from '@campus/shared/api';

/**
 * 校园状态枚举
 */
export enum CampusStatus {
  ENABLED = 'ENABLED',
  DISABLED = 'DISABLED'
}

/**
 * 校园信息
 */
export interface Campus {
  id: number;
  name: string;
  code: string;
  address?: string;
  phone?: string;
  status: CampusStatus;
  createdAt: string;
  updatedAt?: string;
}

/**
 * 校园列表查询参数
 */
export interface CampusListParams {
  keyword?: string;        // 搜索关键词（名称或代码）
  status?: CampusStatus;   // 状态筛选
  page?: number;           // 页码（从0开始）
  size?: number;           // 每页大小
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
 * 校园统计数据
 */
export interface CampusStatistics {
  campusId: number;
  campusName: string;
  userCount: number;        // 用户数
  goodsCount: number;       // 商品数
  orderCount: number;       // 订单数
  activeUserCount: number;  // 活跃用户数（近30天）
}

/**
 * 添加/编辑校园请求
 */
export interface CampusRequest {
  name: string;
  code: string;
  address?: string;
  phone?: string;
  status: CampusStatus;
}

/**
 * 校园 API 服务类
 */
export class CampusService {
  /**
   * 获取校园列表（分页）
   * @param params 查询参数
   * @returns 校园列表（分页）
   */
  async list(params?: CampusListParams): Promise<PageResponse<Campus>> {
    const response = await apiClient.get<PageResponse<Campus>>('/api/admin/campuses', {
      params: {
        keyword: params?.keyword,
        status: params?.status,
        page: params?.page ?? 0,
        size: params?.size ?? 20
      }
    });
    return response.data;
  }

  /**
   * 获取校园详情
   * @param id 校园ID
   * @returns 校园详情
   */
  async getDetail(id: number): Promise<Campus> {
    const response = await http.get<BaseResponse<Campus>>(`/api/admin/campuses/${id}`);
    return response.data.data;
  }

  /**
   * 添加校园
   * @param data 校园信息
   * @returns 创建的校园ID
   */
  async create(data: CampusRequest): Promise<number> {
    const response = await http.post<BaseResponse<number>>('/api/admin/campuses', data);
    return response.data.data;
  }

  /**
   * 更新校园信息
   * @param id 校园ID
   * @param data 校园信息
   * @returns 更新后的校园信息
   */
  async update(id: number, data: Partial<CampusRequest>): Promise<Campus> {
    const response = await http.put<BaseResponse<Campus>>(`/api/admin/campuses/${id}`, data);
    return response.data.data;
  }

  /**
   * 删除校园
   * @param id 校园ID
   */
  async delete(id: number): Promise<void> {
    await http.delete(`/api/admin/campuses/${id}`);
  }

  /**
   * 启用/禁用校园
   * @param id 校园ID
   * @param status 状态
   * @returns 更新后的校园信息
   */
  async updateStatus(id: number, status: CampusStatus): Promise<Campus> {
    return this.update(id, { status });
  }

  /**
   * 获取校园统计数据
   * @param id 校园ID
   * @returns 校园统计数据
   */
  async statistics(id: number): Promise<CampusStatistics> {
    const response = await http.get<BaseResponse<CampusStatistics>>(
      `/api/admin/campuses/${id}/statistics`
    );
    return response.data.data;
  }

  /**
   * 批量删除校园
   * @param ids 校园ID列表
   */
  async batchDelete(ids: number[]): Promise<void> {
    await http.post('/api/admin/campuses/batch/delete', { ids });
  }
}

/**
 * 校园服务实例
 */
export const campusService = new CampusService();

/**
 * 导出类型
 */
export type {
  Campus as CampusType,
  CampusListParams as CampusListParamsType,
  CampusStatistics as CampusStatisticsType,
  CampusRequest as CampusRequestType
};
