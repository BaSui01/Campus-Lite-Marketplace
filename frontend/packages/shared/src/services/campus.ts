/**
 * ✅ 校园管理 API 服务 - 完全重构版
 * @author BaSui 😎
 * @description 基于 OpenAPI 生成的 DefaultApi，零手写路径！
 *
 * 功能：
 * - 校园列表查询（分页）
 * - 校园详情查看
 * - 校园创建/更新/删除
 * - 校园统计数据
 * - 批量删除校园
 */

import { getApi } from '../utils/apiClient';
import type {
  Campus,
  CampusCreateRequest,
  CampusUpdateRequest,
  CampusStatisticsResponse,
} from '../api';

// ==================== 类型重导出 ====================
export type { Campus, CampusCreateRequest, CampusUpdateRequest, CampusStatisticsResponse } from '../api';

export type CampusRequest = CampusCreateRequest;
export type CampusStatistics = CampusStatisticsResponse;

/**
 * 校园状态枚举
 */
export enum CampusStatus {
  ENABLED = 'ENABLED',
  DISABLED = 'DISABLED'
}

/**
 * 校园列表查询参数
 */
export interface CampusListParams {
  keyword?: string;
  status?: CampusStatus;
  page?: number;
  size?: number;
}

/**
 * 校园 API 服务类
 */
export class CampusService {
  /**
   * 获取校园列表
   * @param params 查询参数
   * @returns 校园列表
   */
  async list(params?: CampusListParams): Promise<Campus[]> {
    const api = getApi();
    const response = await api.listCampuses();
    let campuses = response.data.data as Campus[];

    // 前端筛选（如果后端不支持）
    if (params?.keyword) {
      campuses = campuses.filter(c =>
        c.name?.toLowerCase().includes(params.keyword!.toLowerCase()) ||
        c.code?.toLowerCase().includes(params.keyword!.toLowerCase())
      );
    }

    if (params?.status) {
      campuses = campuses.filter(c => c.status === params.status);
    }

    return campuses;
  }

  /**
   * 获取校园详情
   * @param id 校园ID
   * @returns 校园详情
   */
  async getDetail(id: number): Promise<Campus> {
    const api = getApi();
    const response = await api.getCampusById({ id });
    return response.data.data as Campus;
  }

  /**
   * 创建校园
   * @param data 校园信息
   * @returns 创建的校园ID
   */
  async create(data: CampusCreateRequest): Promise<number> {
    const api = getApi();
    const response = await api.createCampus({ campusCreateRequest: data });
    return response.data.data as number;
  }

  /**
   * 更新校园信息
   * @param id 校园ID
   * @param data 校园信息
   * @returns 更新后的校园信息
   */
  async update(id: number, data: CampusUpdateRequest): Promise<Campus> {
    const api = getApi();
    const response = await api.updateCampus({ id, campusUpdateRequest: data });
    return response.data.data as Campus;
  }

  /**
   * 删除校园
   * @param id 校园ID
   */
  async delete(id: number): Promise<void> {
    const api = getApi();
    await api.deleteCampus({ id });
  }

  /**
   * 启用/禁用校园
   * @param id 校园ID
   * @param status 状态
   * @returns 更新后的校园信息
   */
  async updateStatus(id: number, status: CampusStatus): Promise<Campus> {
    // 先获取当前校园信息
    const campus = await this.getDetail(id);
    // 更新状态
    return this.update(id, {
      name: campus.name || '', // 处理可能的 undefined
      status: status as any, // 类型转换：CampusStatus -> CampusUpdateRequestStatusEnum
    });
  }

  /**
   * 获取校园统计数据
   * @param id 校园ID
   * @returns 校园统计数据
   */
  async statistics(id: number): Promise<CampusStatisticsResponse> {
    const api = getApi();
    const response = await api.getCampusStatistics({ id });
    return response.data.data as CampusStatisticsResponse;
  }

  /**
   * 批量删除校园
   * @param ids 校园ID列表
   */
  async batchDelete(ids: number[]): Promise<void> {
    const api = getApi();
    await api.batchDeleteCampuses({ requestBody: ids });
  }
}

/**
 * 校园服务实例
 */
export const campusService = new CampusService();

/**
 * 导出单例
 */
export default campusService;
