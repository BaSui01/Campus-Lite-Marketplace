/**
 * ✅ 文件上传 API 服务 - 已重构为 OpenAPI
 * @author BaSui 😎
 * @description 基于 OpenAPI 生成的 DefaultApi，零手写路径！
 *
 * 功能：
 * - 图片上传（单张/批量）
 * - 文件上传（支持进度回调）
 * - 文件删除
 *
 * ⚠️ 注意：
 * - uploadBase64Image() 方法暂未实现（后端接口缺失）
 * - 文件上传支持进度回调（onProgress）
 *
 * 📋 API 路径：/api/files/*
 */

import { getApi } from '../utils/apiClient';

/**
 * 上传选项
 */
export interface UploadOptions {
  category?: 'avatar' | 'goods' | 'post' | 'message' | 'general';
  onProgress?: (percent: number) => void;
}

/**
 * 上传响应
 */
export interface UploadResponse {
  url: string;
  thumbnailUrl?: string;
  fileName?: string;
  fileSize?: number;
  mimeType?: string;
}

/**
 * 文件上传 API 服务类
 */
export class UploadService {
  /**
   * 上传图片
   * POST /api/files/upload
   * @param file 图片文件
   * @param options 上传选项（category、onProgress）
   * @returns 上传结果（包含图片URL）
   */
  async uploadImage(
    file: File,
    options?: UploadOptions
  ): Promise<UploadResponse> {
    const api = getApi();
    const response = await api.uploadFile(
      { file },
      {
        onUploadProgress: (progressEvent) => {
          if (options?.onProgress && progressEvent.total) {
            const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total);
            options.onProgress(percent);
          }
        },
      }
    );

    const data = response.data.data as Record<string, string>;
    return {
      url: data.url || '',
      thumbnailUrl: data.thumbnailUrl,
      fileName: data.fileName,
      fileSize: data.fileSize ? parseInt(data.fileSize) : undefined,
      mimeType: data.mimeType,
    };
  }

  /**
   * 批量上传图片
   * @param files 图片文件数组
   * @param options 上传选项（category、onProgress）
   * @returns 上传结果数组
   */
  async uploadImages(
    files: File[],
    options?: UploadOptions
  ): Promise<UploadResponse[]> {
    const results = await Promise.all(
      files.map((file) => this.uploadImage(file, options))
    );
    return results;
  }

  /**
   * 上传文件（带缩略图）
   * POST /api/files/upload-with-thumbnail
   * @param file 文件
   * @param options 上传选项（category、onProgress）
   * @returns 上传结果（包含文件URL和缩略图URL）
   */
  async uploadFile(
    file: File,
    options?: UploadOptions
  ): Promise<UploadResponse> {
    const api = getApi();
    const response = await api.uploadFileWithThumbnail(
      { file },
      {
        onUploadProgress: (progressEvent) => {
          if (options?.onProgress && progressEvent.total) {
            const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total);
            options.onProgress(percent);
          }
        },
      }
    );

    const data = response.data.data as Record<string, string>;
    return {
      url: data.url || '',
      thumbnailUrl: data.thumbnailUrl,
      fileName: data.fileName,
      fileSize: data.fileSize ? parseInt(data.fileSize) : undefined,
      mimeType: data.mimeType,
    };
  }

  /**
   * ✅ 上传 Base64 图片
   * POST /api/files/upload-base64
   * @param base64Data Base64 编码的图片数据（支持 data:image/png;base64,xxx 格式）
   * @param options 上传选项（category）
   * @returns 上传结果（包含图片URL）
   */
  async uploadBase64Image(
    base64Data: string,
    options?: UploadOptions
  ): Promise<UploadResponse> {
    // ✅ 使用 OpenAPI 生成的方法（uploadBase64Image）
    const api = getApi();
    const response = await api.uploadBase64Image({
      requestBody: {
        base64Data,
        category: options?.category || 'general',
      },
    });

    const data = response.data.data as Record<string, string>;
    return {
      url: data.url || '',
      fileName: data.fileName,
      fileSize: data.fileSize ? parseInt(data.fileSize) : undefined,
      mimeType: data.mimeType,
    };
  }

  /**
   * 删除文件
   * DELETE /api/files/delete
   * @param url 文件URL
   * @returns 删除结果
   */
  async deleteFile(url: string): Promise<boolean> {
    const api = getApi();
    const response = await api.deleteFile({ url });
    return response.data.data as boolean;
  }
}

// 导出单例
export const uploadService = new UploadService();
export default uploadService;
