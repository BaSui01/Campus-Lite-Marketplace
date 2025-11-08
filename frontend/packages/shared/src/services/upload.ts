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
 * 文件上传 API 服务
 * @author BaSui 😎
 * @description 图片、文件上传等接口
 */

import { apiClient } from '../utils/apiClient';
import type { ApiResponse, UploadResponse } from '../types';
import { IMAGE_UPLOAD_URL, FILE_UPLOAD_URL } from '../constants';

/**
 * 文件上传 API 服务类
 */
export class UploadService {
  /**
   * 上传图片
   * @param file 图片文件或FormData
   * @param onProgress 上传进度回调
   * @returns 上传结果（包含图片URL）
   */
  async uploadImage(
    file: File | FormData,
    onProgress?: (percent: number) => void
  ): Promise<ApiResponse<UploadResponse>> {
    const formData = file instanceof FormData ? file : new FormData();
    if (file instanceof File) {
      formData.append('file', file);
    }

    const response = await apiClient.post(IMAGE_UPLOAD_URL, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          onProgress(percent);
        }
      },
    });
    return response.data;
  }

  /**
   * 批量上传图片
   * @param files 图片文件数组
   * @param onProgress 上传进度回调
   * @returns 上传结果数组
   */
  async uploadImages(
    files: File[],
    onProgress?: (percent: number) => void
  ): Promise<ApiResponse<UploadResponse[]>> {
    const formData = new FormData();
    files.forEach((file) => {
      formData.append('files', file);
    });

    const response = await apiClient.post(`${IMAGE_UPLOAD_URL}/batch`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          onProgress(percent);
        }
      },
    });
    return response.data;
  }

  /**
   * 上传文件
   * @param file 文件
   * @param onProgress 上传进度回调
   * @returns 上传结果（包含文件URL）
   */
  async uploadFile(
    file: File,
    onProgress?: (percent: number) => void
  ): Promise<ApiResponse<UploadResponse>> {
    const formData = new FormData();
    formData.append('file', file);

    const response = await apiClient.post(FILE_UPLOAD_URL, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        if (onProgress && progressEvent.total) {
          const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          onProgress(percent);
        }
      },
    });
    return response.data;
  }

  /**
   * 上传Base64图片
   * @param base64 Base64编码的图片数据
   * @returns 上传结果（包含图片URL）
   */
  async uploadBase64Image(base64: string): Promise<ApiResponse<UploadResponse>> {
    const response = await apiClient.post(`${IMAGE_UPLOAD_URL}/base64`, { base64 });
    return response.data;
  }

  /**
   * 删除文件
   * @param url 文件URL
   * @returns 删除结果
   */
  async deleteFile(url: string): Promise<ApiResponse<void>> {
    const response = await apiClient.delete('/upload/delete', { data: { url } });
    return response.data;
  }
}

// 导出单例
export const uploadService = new UploadService();
export default uploadService;
