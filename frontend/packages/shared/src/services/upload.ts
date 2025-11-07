/**
 * 文件上传 API 服务
 * @author BaSui 😎
 * @description 图片、文件上传等接口
 */

import { http } from '../utils/apiClient';
import type { ApiResponse, UploadResponse } from '../types';
import { IMAGE_UPLOAD_URL, FILE_UPLOAD_URL } from '../constants';

/**
 * 文件上传 API 服务类
 */
export class UploadService {
  /**
   * 上传图片
   * @param file 图片文件
   * @param onProgress 上传进度回调
   * @returns 上传结果（包含图片URL）
   */
  async uploadImage(
    file: File,
    onProgress?: (percent: number) => void
  ): Promise<ApiResponse<UploadResponse>> {
    const formData = new FormData();
    formData.append('file', file);

    return http.post(IMAGE_UPLOAD_URL, formData, {
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

    return http.post(`${IMAGE_UPLOAD_URL}/batch`, formData, {
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

    return http.post(FILE_UPLOAD_URL, formData, {
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
  }

  /**
   * 上传Base64图片
   * @param base64 Base64编码的图片数据
   * @returns 上传结果（包含图片URL）
   */
  async uploadBase64Image(base64: string): Promise<ApiResponse<UploadResponse>> {
    return http.post(`${IMAGE_UPLOAD_URL}/base64`, { base64 });
  }

  /**
   * 删除文件
   * @param url 文件URL
   * @returns 删除结果
   */
  async deleteFile(url: string): Promise<ApiResponse<void>> {
    return http.delete('/upload/delete', { data: { url } });
  }
}

// 导出单例
export const uploadService = new UploadService();
export default uploadService;
