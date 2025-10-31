/**
 * useUpload Hook - 文件上传大师！📤
 * @author BaSui 😎
 * @description 文件上传封装 Hook，支持进度跟踪、多文件上传、错误处理
 */

import { useState, useCallback } from 'react';

/**
 * 上传文件接口
 */
export interface UploadFile {
  /**
   * 文件唯一 ID
   */
  uid: string;

  /**
   * 文件名
   */
  name: string;

  /**
   * 文件大小（字节）
   */
  size: number;

  /**
   * 文件类型
   */
  type: string;

  /**
   * 文件对象
   */
  file: File;

  /**
   * 上传状态
   */
  status: 'pending' | 'uploading' | 'success' | 'error';

  /**
   * 上传进度（0-100）
   */
  progress: number;

  /**
   * 上传后的 URL
   */
  url?: string;

  /**
   * 错误信息
   */
  error?: string;
}

/**
 * useUpload 配置选项
 */
export interface UseUploadOptions {
  /**
   * 上传 API 地址
   */
  action: string;

  /**
   * 允许的文件类型（MIME type，如 'image/*'）
   */
  accept?: string;

  /**
   * 文件大小限制（字节）
   * @default 10485760 (10MB)
   */
  maxSize?: number;

  /**
   * 最大文件数量
   * @default 1
   */
  maxCount?: number;

  /**
   * 是否支持多文件上传
   * @default false
   */
  multiple?: boolean;

  /**
   * 上传请求的额外参数
   */
  data?: Record<string, any>;

  /**
   * 上传请求的额外 Headers
   */
  headers?: Record<string, string>;

  /**
   * 文件上传前的钩子
   */
  beforeUpload?: (file: File) => boolean | Promise<boolean>;

  /**
   * 文件上传成功回调
   */
  onSuccess?: (file: UploadFile, response: any) => void;

  /**
   * 文件上传失败回调
   */
  onError?: (file: UploadFile, error: string) => void;

  /**
   * 文件上传进度回调
   */
  onProgress?: (file: UploadFile, progress: number) => void;
}

/**
 * useUpload 返回值
 */
export interface UseUploadResult {
  /**
   * 文件列表
   */
  fileList: UploadFile[];

  /**
   * 是否正在上传
   */
  uploading: boolean;

  /**
   * 上传文件
   */
  upload: (files: FileList | File[]) => Promise<void>;

  /**
   * 删除文件
   */
  remove: (uid: string) => void;

  /**
   * 清空文件列表
   */
  clear: () => void;
}

/**
 * 生成唯一 ID
 */
const generateUid = (): string => {
  return `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
};

/**
 * useUpload Hook
 *
 * @description
 * 文件上传封装 Hook，提供文件选择、上传进度跟踪、错误处理等功能。
 *
 * @param options 配置选项
 * @returns 上传结果
 *
 * @example
 * ```tsx
 * // 基础用法
 * function ImageUploader() {
 *   const { fileList, uploading, upload, remove } = useUpload({
 *     action: '/api/upload',
 *     accept: 'image/*',
 *     maxSize: 5 * 1024 * 1024, // 5MB
 *     onSuccess: (file, response) => {
 *       toast.success(`${file.name} 上传成功！`);
 *     },
 *   });
 *
 *   const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
 *     if (e.target.files) {
 *       upload(e.target.files);
 *     }
 *   };
 *
 *   return (
 *     <div>
 *       <input type="file" accept="image/*" onChange={handleFileChange} />
 *       {uploading && <Loading />}
 *       {fileList.map((file) => (
 *         <div key={file.uid}>
 *           {file.name} - {file.progress}%
 *           <Button onClick={() => remove(file.uid)}>删除</Button>
 *         </div>
 *       ))}
 *     </div>
 *   );
 * }
 * ```
 *
 * @example
 * ```tsx
 * // 多文件上传
 * function MultiFileUploader() {
 *   const { fileList, uploading, upload } = useUpload({
 *     action: '/api/upload',
 *     multiple: true,
 *     maxCount: 5,
 *     beforeUpload: async (file) => {
 *       // 自定义验证
 *       if (!file.type.startsWith('image/')) {
 *         toast.error('只能上传图片文件！');
 *         return false;
 *       }
 *       return true;
 *     },
 *   });
 *
 *   return (
 *     <div>
 *       <input
 *         type="file"
 *         multiple
 *         onChange={(e) => e.target.files && upload(e.target.files)}
 *       />
 *       <p>已上传: {fileList.filter((f) => f.status === 'success').length} / {fileList.length}</p>
 *     </div>
 *   );
 * }
 * ```
 */
export const useUpload = (options: UseUploadOptions): UseUploadResult => {
  const {
    action,
    accept,
    maxSize = 10 * 1024 * 1024, // 10MB
    maxCount = 1,
    multiple = false,
    data = {},
    headers = {},
    beforeUpload,
    onSuccess,
    onError,
    onProgress,
  } = options;

  // 文件列表
  const [fileList, setFileList] = useState<UploadFile[]>([]);

  // 是否正在上传
  const [uploading, setUploading] = useState(false);

  /**
   * 验证文件
   */
  const validateFile = useCallback(
    (file: File): string | null => {
      // 验证文件类型
      if (accept && !file.type.match(accept)) {
        return `文件类型不支持，只支持 ${accept}`;
      }

      // 验证文件大小
      if (file.size > maxSize) {
        return `文件大小超过限制（${(maxSize / 1024 / 1024).toFixed(2)}MB）`;
      }

      return null;
    },
    [accept, maxSize]
  );

  /**
   * 上传单个文件
   */
  const uploadFile = useCallback(
    async (uploadFile: UploadFile): Promise<void> => {
      // ��新状态为上传中
      setFileList((prev) =>
        prev.map((f) =>
          f.uid === uploadFile.uid ? { ...f, status: 'uploading', progress: 0 } : f
        )
      );

      try {
        // 创建 FormData
        const formData = new FormData();
        formData.append('file', uploadFile.file);

        // 添加额外参数
        Object.keys(data).forEach((key) => {
          formData.append(key, data[key]);
        });

        // 发送上传请求
        const xhr = new XMLHttpRequest();

        // 上传进度
        xhr.upload.onprogress = (e) => {
          if (e.lengthComputable) {
            const progress = Math.round((e.loaded / e.total) * 100);
            setFileList((prev) =>
              prev.map((f) => (f.uid === uploadFile.uid ? { ...f, progress } : f))
            );
            onProgress?.(uploadFile, progress);
          }
        };

        // 上传完成
        xhr.onload = () => {
          if (xhr.status >= 200 && xhr.status < 300) {
            const response = JSON.parse(xhr.responseText);
            const url = response.url || response.data?.url;

            setFileList((prev) =>
              prev.map((f) =>
                f.uid === uploadFile.uid
                  ? { ...f, status: 'success', progress: 100, url }
                  : f
              )
            );

            onSuccess?.({ ...uploadFile, status: 'success', progress: 100, url }, response);
          } else {
            throw new Error(`上传失败: ${xhr.statusText}`);
          }
        };

        // 上传错误
        xhr.onerror = () => {
          const error = '网络错误，上传失败';
          setFileList((prev) =>
            prev.map((f) =>
              f.uid === uploadFile.uid ? { ...f, status: 'error', error } : f
            )
          );
          onError?.({ ...uploadFile, status: 'error', error }, error);
        };

        // 发送请求
        xhr.open('POST', action);

        // 设置请求头
        Object.keys(headers).forEach((key) => {
          xhr.setRequestHeader(key, headers[key]);
        });

        xhr.send(formData);
      } catch (error) {
        const errorMessage = error instanceof Error ? error.message : '上传失败';
        setFileList((prev) =>
          prev.map((f) =>
            f.uid === uploadFile.uid ? { ...f, status: 'error', error: errorMessage } : f
          )
        );
        onError?.({ ...uploadFile, status: 'error', error: errorMessage }, errorMessage);
      }
    },
    [action, data, headers, onSuccess, onError, onProgress]
  );

  /**
   * 上传文件
   */
  const upload = useCallback(
    async (files: FileList | File[]): Promise<void> => {
      const fileArray = Array.from(files);

      // 检查文件数量
      if (!multiple && fileArray.length > 1) {
        console.warn('不支持多文件上传');
        return;
      }

      if (fileList.length + fileArray.length > maxCount) {
        console.warn(`最多只能上传 ${maxCount} 个文件`);
        return;
      }

      // 转换为 UploadFile 对象
      const uploadFiles: UploadFile[] = fileArray.map((file) => ({
        uid: generateUid(),
        name: file.name,
        size: file.size,
        type: file.type,
        file,
        status: 'pending',
        progress: 0,
      }));

      // 验证文件
      const validFiles: UploadFile[] = [];
      for (const uploadFile of uploadFiles) {
        const error = validateFile(uploadFile.file);
        if (error) {
          console.warn(`${uploadFile.name}: ${error}`);
          continue;
        }

        // 调用 beforeUpload 钩子
        if (beforeUpload) {
          const result = await beforeUpload(uploadFile.file);
          if (!result) {
            continue;
          }
        }

        validFiles.push(uploadFile);
      }

      if (validFiles.length === 0) {
        return;
      }

      // 添加到文件列表
      setFileList((prev) => [...prev, ...validFiles]);

      // 开始上传
      setUploading(true);
      try {
        await Promise.all(validFiles.map((file) => uploadFile(file)));
      } finally {
        setUploading(false);
      }
    },
    [fileList, maxCount, multiple, validateFile, beforeUpload, uploadFile]
  );

  /**
   * 删除文件
   */
  const remove = useCallback((uid: string) => {
    setFileList((prev) => prev.filter((f) => f.uid !== uid));
  }, []);

  /**
   * 清空文件列表
   */
  const clear = useCallback(() => {
    setFileList([]);
  }, []);

  return {
    fileList,
    uploading,
    upload,
    remove,
    clear,
  };
};

export default useUpload;
