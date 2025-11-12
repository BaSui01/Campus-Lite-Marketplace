/**
 * 学习资源 API 服务
 * @author BaSui 😎
 * @date 2025-11-11
 */

// import { getApi } from '../api'; // TODO: 等后端API实现后使用

// ==================== 类型定义 ====================

export interface Resource {
  id: number;
  title: string;
  description: string;
  type: 'DOCUMENT' | 'VIDEO' | 'AUDIO' | 'LINK' | 'CODE' | 'OTHER';
  category?: string;
  fileUrl?: string;
  fileSize?: number;
  uploaderId: number;
  downloadCount: number;
  viewCount: number;
  likeCount: number;
  tags?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ResourceListParams {
  page?: number;
  size?: number;
  type?: string;
  category?: string;
  keyword?: string;
}

// ==================== 服务实现 ====================

export class ResourceService {
  /**
   * 获取资源列表
   * TODO: 待后端 API 完善后实现
   */
  async list(_params?: ResourceListParams): Promise<{ content: Resource[]; totalElements: number; totalPages: number }> {
    /* TODO: 实现后端 API 调用
    const api = getApi();
    const response = await api.get('/resources', { params });
    return response.data.data;
    */
    throw new Error('Not implemented: 待后端 API 完善');
  }

  /**
   * 获取资源详情
   * TODO: 待后端 API 完善后实现
   */
  async getDetail(_id: number): Promise<Resource> {
    /* TODO: 实现后端 API 调用
    const api = getApi();
    const response = await api.get(`/resources/${id}`);
    return response.data.data;
    */
    throw new Error('Not implemented: 待后端 API 完善');
  }

  /**
   * 记录下载
   * TODO: 待后端 API 完善后实现
   */
  async recordDownload(_id: number): Promise<void> {
    /* TODO: 实现后端 API 调用
    const api = getApi();
    await api.post(`/resources/${id}/download`);
    */
    throw new Error('Not implemented: 待后端 API 完善');
  }

  /**
   * 获取热门资源
   * TODO: 待后端 API 完善后实现
   */
  async getHotResources(_page = 0, _size = 10): Promise<{ content: Resource[]; totalElements: number }> {
    /* TODO: 实现后端 API 调用
    const api = getApi();
    const response = await api.get('/resources/hot', { params: { page, size } });
    return response.data.data;
    */
    throw new Error('Not implemented: 待后端 API 完善');
  }

  /**
   * 获取我的资源
   * TODO: 待后端 API 完善后实现
   */
  async getMyResources(_page = 0, _size = 10): Promise<{ content: Resource[]; totalElements: number }> {
    /* TODO: 实现后端 API 调用
    const api = getApi();
    const response = await api.get('/resources/my', { params: { page, size } });
    return response.data.data;
    */
    throw new Error('Not implemented: 待后端 API 完善');
  }
}

export const resourceService = new ResourceService();
