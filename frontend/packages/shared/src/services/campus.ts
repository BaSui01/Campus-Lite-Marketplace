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
   * @returns 校园列表
   */
  async list(): Promise<Campus[]> {
    const api = getApi();
    const response = await api.listCampuses();
    return response.data.data as Campus[];
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
    return this.update(id, { status } as CampusUpdateRequest);
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
