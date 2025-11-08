/**
 * 申诉材料上传组件 - 增强版！📤
 *
 * @author BaSui 😎
 * @description 基于现有useUpload Hook，为申诉场景定制的文件上传组件
 * @date 2025-11-07
 */

import React, { useState, useCallback, useRef } from 'react';
import { useUpload, type UploadFile, type UseUploadOptions } from '@campus/shared/hooks';
import { appealService } from '../../../services';

/**
 * 申诉文件上传配置
 */
const APPEAL_UPLOAD_CONFIG = {
  // 支持的文件类型
  accept: 'image/*,.pdf,.doc,.docx',
  // 最大文件大小：图片10MB，文档20MB
  maxImageSize: 10 * 1024 * 1024, // 10MB
  maxDocSize: 20 * 1024 * 1024,   // 20MB
  // 最大文件数量
  maxCount: 5,
  // 文件类型标签
  fileTypeLabels: {
    'image/jpeg': 'JPG图片',
    'image/png': 'PNG图片',
    'image/gif': 'GIF图片',
    'image/webp': 'WebP图片',
    'application/pdf': 'PDF文档',
    'application/msword': 'Word文档',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'Word文档',
  }
} as const;

/**
 * 申诉文件上传组件属性
 */
export interface AppealFileUploaderProps {
  /** 申诉ID */
  appealId?: number;
  /** 已存在的文件列表 */
  initialFiles?: UploadFile[];
  /** 上传成功回调 */
  onUploadSuccess?: (files: UploadFile[]) => void;
  /** 文件删除回调 */
  onFileRemove?: (fileId: string) => void;
  /** 是否禁用 */
  disabled?: boolean;
  /** 是否显示上传按钮 */
  showUploadButton?: boolean;
  /** 自定义样式类名 */
  className?: string;
}

/**
 * 获取文件类型标签
 */
const getFileTypeLabel = (file: File): string => {
  return APPEAL_UPLOAD_CONFIG.fileTypeLabels[file.type as keyof typeof APPEAL_UPLOAD_CONFIG.fileTypeLabels] || '未知文件';
};

/**
 * 验证申诉文件
 */
const validateAppealFile = (file: File): string | null => {
  // 检查文件类型
  const isImage = file.type.startsWith('image/');
  const isDoc = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'].includes(file.type);

  if (!isImage && !isDoc) {
    return '只支持图片（JPG、PNG、GIF、WebP）和文档（PDF、Word）文件';
  }

  // 检查文件大小
  const maxSize = isImage ? APPEAL_UPLOAD_CONFIG.maxImageSize : APPEAL_UPLOAD_CONFIG.maxDocSize;
  const maxSizeMB = maxSize / (1024 * 1024);

  if (file.size > maxSize) {
    return `${isImage ? '图片' : '文档'}大小不能超过 ${maxSizeMB}MB`;
  }

  // 检查文件名长度
  if (file.name.length > 100) {
    return '文件名过长，请重命名后再上传';
  }

  return null;
};

/**
 * 格式化文件大小
 */
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

/**
 * 申诉文件上传组件
 */
export const AppealFileUploader: React.FC<AppealFileUploaderProps> = ({
  appealId,
  initialFiles = [],
  onUploadSuccess,
  onFileRemove,
  disabled = false,
  showUploadButton = true,
  className = '',
}) => {
  const [isDragOver, setIsDragOver] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // 使用现有的 useUpload Hook
  const { fileList, uploading, upload, remove } = useUpload({
    action: '/api/upload/appeal-material', // 申诉材料上传接口
    accept: APPEAL_UPLOAD_CONFIG.accept,
    maxSize: Math.max(APPEAL_UPLOAD_CONFIG.maxImageSize, APPEAL_UPLOAD_CONFIG.maxDocSize),
    maxCount: APPEAL_UPLOAD_CONFIG.maxCount,
    multiple: true,
    beforeUpload: async (file) => {
      const error = validateAppealFile(file);
      if (error) {
        setUploadError(error);
        return false;
      }
      setUploadError(null);
      return true;
    },
    onSuccess: (file, response) => {
      setUploadError(null);

      // 注意：文件上传后会通过专门的API接口关联到申诉
      // 这里只是记录成功状态，实际关联在申诉提交时进行

      onUploadSuccess?.(fileList.filter(f => f.status === 'success'));
    },
    onError: (file, error) => {
      setUploadError(error);
    },
  });

  /**
   * 处理文件选择
   */
  const handleFileSelect = useCallback((files: FileList | File[]) => {
    if (disabled) return;

    const fileArray = Array.from(files);

    // 检查文件数量限制
    if (fileList.length + fileArray.length > APPEAL_UPLOAD_CONFIG.maxCount) {
      setUploadError(`最多只能上传 ${APPEAL_UPLOAD_CONFIG.maxCount} 个文件`);
      return;
    }

    setUploadError(null);
    upload(files);
  }, [disabled, fileList.length, upload]);

  /**
   * 处理拖拽进入
   */
  const handleDragEnter = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (!disabled) {
      setIsDragOver(true);
    }
  }, [disabled]);

  /**
   * 处理拖拽经过
   */
  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
  }, []);

  /**
   * 处理拖拽离开
   */
  const handleDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);
  }, []);

  /**
   * 处理文件拖放
   */
  const handleDrop = useCallback((e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragOver(false);

    if (disabled) return;

    const files = e.dataTransfer.files;
    if (files && files.length > 0) {
      handleFileSelect(files);
    }
  }, [disabled, handleFileSelect]);

  /**
   * 处理文件删除
   */
  const handleRemoveFile = useCallback((uid: string) => {
    remove(uid);
    onFileRemove?.(uid);
    setUploadError(null);
  }, [remove, onFileRemove]);

  /**
   * 触发文件选择
   */
  const triggerFileSelect = useCallback(() => {
    if (!disabled && fileInputRef.current) {
      fileInputRef.current.click();
    }
  }, [disabled]);

  return (
    <div className={`appeal-file-uploader ${className}`}>
      {/* 拖拽上传区域 */}
      <div
        className={`
          border-2 border-dashed rounded-lg p-6 text-center transition-colors
          ${isDragOver
            ? 'border-blue-500 bg-blue-50'
            : 'border-gray-300 hover:border-gray-400 bg-gray-50'
          }
          ${disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'}
        `}
        onDragEnter={handleDragEnter}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
        onClick={triggerFileSelect}
      >
        {/* 上传图标 */}
        <div className="mb-4">
          {uploading ? (
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto"></div>
          ) : (
            <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
            </svg>
          )}
        </div>

        {/* 上传文本 */}
        <div className="text-sm text-gray-600 mb-2">
          {uploading ? '正在上传...' : '拖拽文件到此处或点击上传'}
        </div>

        {/* 支持的文件类型 */}
        <div className="text-xs text-gray-500">
          支持：JPG、PNG、GIF、WebP、PDF、Word文档
        </div>

        {/* 文件大小限制 */}
        <div className="text-xs text-gray-500 mt-1">
          图片 ≤ 10MB，文档 ≤ 20MB，最多 {APPEAL_UPLOAD_CONFIG.maxCount} 个文件
        </div>

        {/* 上传按钮 */}
        {showUploadButton && !disabled && (
          <button
            type="button"
            className="mt-4 px-4 py-2 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
            disabled={uploading}
          >
            {uploading ? '上传中...' : '选择文件'}
          </button>
        )}
      </div>

      {/* 隐藏的文件输入 */}
      <input
        ref={fileInputRef}
        type="file"
        multiple
        accept={APPEAL_UPLOAD_CONFIG.accept}
        onChange={(e) => e.target.files && handleFileSelect(e.target.files)}
        className="hidden"
        disabled={disabled}
      />

      {/* 错误提示 */}
      {uploadError && (
        <div className="mt-3 p-3 bg-red-50 border border-red-200 rounded-md">
          <div className="flex">
            <svg className="h-5 w-5 text-red-400" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
            </svg>
            <div className="ml-3">
              <p className="text-sm text-red-800">{uploadError}</p>
            </div>
          </div>
        </div>
      )}

      {/* 文件列表 */}
      {fileList.length > 0 && (
        <div className="mt-4 space-y-2">
          <h4 className="text-sm font-medium text-gray-900">已上传文件 ({fileList.length})</h4>
          {fileList.map((file) => (
            <div key={file.uid} className="flex items-center justify-between p-3 bg-white border border-gray-200 rounded-md">
              <div className="flex items-center space-x-3 flex-1">
                {/* 文件状态图标 */}
                <div className="flex-shrink-0">
                  {file.status === 'uploading' && (
                    <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-blue-500"></div>
                  )}
                  {file.status === 'success' && (
                    <svg className="h-5 w-5 text-green-500" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                    </svg>
                  )}
                  {file.status === 'error' && (
                    <svg className="h-5 w-5 text-red-500" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                    </svg>
                  )}
                  {file.status === 'pending' && (
                    <svg className="h-5 w-5 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                    </svg>
                  )}
                </div>

                {/* 文件信息 */}
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-gray-900 truncate">{file.name}</p>
                  <div className="flex items-center space-x-2 text-xs text-gray-500">
                    <span>{getFileTypeLabel(file.file)}</span>
                    <span>•</span>
                    <span>{formatFileSize(file.size)}</span>
                    {file.status === 'uploading' && (
                      <>
                        <span>•</span>
                        <span>{file.progress}%</span>
                      </>
                    )}
                  </div>

                  {/* 上传进度条 */}
                  {file.status === 'uploading' && (
                    <div className="mt-1">
                      <div className="w-full bg-gray-200 rounded-full h-1">
                        <div
                          className="bg-blue-600 h-1 rounded-full transition-all duration-300"
                          style={{ width: `${file.progress}%` }}
                        ></div>
                      </div>
                    </div>
                  )}

                  {/* 错误信息 */}
                  {file.status === 'error' && file.error && (
                    <p className="text-xs text-red-600 mt-1">{file.error}</p>
                  )}
                </div>
              </div>

              {/* 操作按钮 */}
              <div className="flex items-center space-x-2">
                {file.status === 'success' && file.url && (
                  <button
                    type="button"
                    className="text-blue-600 hover:text-blue-800 text-sm"
                    onClick={() => window.open(file.url, '_blank')}
                  >
                    查看
                  </button>
                )}
                <button
                  type="button"
                  className="text-red-600 hover:text-red-800 text-sm"
                  onClick={() => handleRemoveFile(file.uid)}
                  disabled={disabled}
                >
                  删除
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default AppealFileUploader;