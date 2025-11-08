/**
 * 纠纷文件消息组件 - 文件展示专家！📁
 *
 * @author BaSui 😎
 * @description 纠纷协商中文件消息的展示和处理
 * @date 2025-11-07
 */

import React, { useState } from 'react';

/**
 * 文件消息属性
 */
export interface FileMessageProps {
  /** 文件URL */
  fileUrl: string;
  /** 文件名 */
  fileName: string;
  /** 文件大小（字节） */
  fileSize?: number;
  /** 文件类型 */
  fileType?: string;
  /** MIME类型 */
  mimeType?: string;
  /** 缩略图URL */
  thumbnailUrl?: string;
  /** 是否是自己发送的 */
  isOwn?: boolean;
  /** 是否可下载 */
  downloadable?: boolean;
  /** 点击回调 */
  onClick?: () => void;
  /** 自定义样式类名 */
  className?: string;
}

/**
 * 格式化文件大小
 */
const formatFileSize = (bytes?: number): string => {
  if (!bytes || bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

/**
 * 获取文件图标
 */
const getFileIcon = (mimeType?: string): JSX.Element => {
  if (!mimeType) {
    return (
      <svg className="w-6 h-6 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
        <path fillRule="evenodd" d="M4 4a2 2 0 012-2h4.586A2 2 0 0112 2.586L15.414 6A2 2 0 0116 7.414V16a2 2 0 01-2 2H6a2 2 0 01-2-2V4z" clipRule="evenodd" />
      </svg>
    );
  }

  if (mimeType.startsWith('image/')) {
    return (
      <svg className="w-6 h-6 text-green-500" fill="currentColor" viewBox="0 0 20 20">
        <path fillRule="evenodd" d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z" clipRule="evenodd" />
      </svg>
    );
  }

  if (mimeType === 'application/pdf') {
    return (
      <svg className="w-6 h-6 text-red-500" fill="currentColor" viewBox="0 0 20 20">
        <path fillRule="evenodd" d="M4 4a2 2 0 00-2 2v8a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-5L9 2H4z" clipRule="evenodd" />
      </svg>
    );
  }

  if (mimeType.includes('word') || mimeType.includes('document')) {
    return (
      <svg className="w-6 h-6 text-blue-500" fill="currentColor" viewBox="0 0 20 20">
        <path fillRule="evenodd" d="M4 4a2 2 0 00-2 2v8a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-5L9 2H4z" clipRule="evenodd" />
      </svg>
    );
  }

  if (mimeType.includes('excel') || mimeType.includes('spreadsheet')) {
    return (
      <svg className="w-6 h-6 text-green-600" fill="currentColor" viewBox="0 0 20 20">
        <path fillRule="evenodd" d="M5 4a3 3 0 00-3 3v6a3 3 0 003 3h10a3 3 0 003-3V7a3 3 0 00-3-3h-2l-4-4H5z" clipRule="evenodd" />
      </svg>
    );
  }

  if (mimeType.includes('powerpoint') || mimeType.includes('presentation')) {
    return (
      <svg className="w-6 h-6 text-orange-500" fill="currentColor" viewBox="0 0 20 20">
        <path fillRule="evenodd" d="M4 4a2 2 0 00-2 2v8a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-5L9 2H4z" clipRule="evenodd" />
      </svg>
    );
  }

  if (mimeType.includes('zip') || mimeType.includes('rar')) {
    return (
      <svg className="w-6 h-6 text-purple-500" fill="currentColor" viewBox="0 0 20 20">
        <path fillRule="evenodd" d="M5 4a3 3 0 00-3 3v6a3 3 0 003 3h10a3 3 0 003-3V7a3 3 0 00-3-3h-2l-4-4H5z" clipRule="evenodd" />
      </svg>
    );
  }

  // 默认文件图标
  return (
    <svg className="w-6 h-6 text-gray-500" fill="currentColor" viewBox="0 0 20 20">
      <path fillRule="evenodd" d="M4 4a2 2 0 00-2 2v8a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-5L9 2H4z" clipRule="evenodd" />
    </svg>
  );
};

/**
 * 获取文件扩展名
 */
const getFileExtension = (fileName?: string): string => {
  if (!fileName) return '';
  const lastDot = fileName.lastIndexOf('.');
  return lastDot > 0 ? fileName.substring(lastDot + 1).toUpperCase() : '';
};

/**
 * 判断是否为图片文件
 */
const isImageFile = (mimeType?: string): boolean => {
  return mimeType ? mimeType.startsWith('image/') : false;
};

/**
 * 文件消息组件
 */
export const FileMessage: React.FC<FileMessageProps> = ({
  fileUrl,
  fileName,
  fileSize,
  fileType,
  mimeType,
  thumbnailUrl,
  isOwn = false,
  downloadable = true,
  onClick,
  className = '',
}) => {
  const [imageError, setImageError] = useState(false);
  const [isDownloading, setIsDownloading] = useState(false);

  // 判断是否为图片
  const isImage = isImageFile(mimeType);

  // 处理文件点击
  const handleFileClick = () => {
    onClick?.();

    if (isImage) {
      // 图片在新窗口打开
      window.open(fileUrl, '_blank');
    } else {
      // 其他文件执行下载
      handleDownload();
    }
  };

  // 处理文件下载
  const handleDownload = async () => {
    if (!downloadable || isDownloading) return;

    setIsDownloading(true);
    try {
      // 创建临时链接进行下载
      const link = document.createElement('a');
      link.href = fileUrl;
      link.download = fileName || 'file';
      link.target = '_blank';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    } catch (error) {
      console.error('下载失败:', error);
    } finally {
      setIsDownloading(false);
    }
  };

  // 图片加载错误处理
  const handleImageError = () => {
    setImageError(true);
  };

  return (
    <div
      className={`file-message cursor-pointer transition-all hover:opacity-90 ${
        isOwn ? 'file-message-own' : 'file-message-other'
      } ${className}`}
      onClick={handleFileClick}
    >
      {/* 图片文件 */}
      {isImage && !imageError && (
        <div className="relative group">
          {/* 缩略图 */}
          {thumbnailUrl ? (
            <img
              src={thumbnailUrl}
              alt={fileName}
              className="max-w-full h-auto rounded-lg"
              onError={handleImageError}
              loading="lazy"
            />
          ) : (
            <img
              src={fileUrl}
              alt={fileName}
              className="max-w-full h-auto rounded-lg"
              onError={handleImageError}
              loading="lazy"
            />
          )}

          {/* 文件名覆盖层 */}
          {fileName && (
            <div className="absolute bottom-0 left-0 right-0 bg-black bg-opacity-50 text-white p-2 rounded-b-lg">
              <p className="text-sm truncate">{fileName}</p>
            </div>
          )}

          {/* 放大图标 */}
          <div className="absolute top-2 right-2 opacity-0 group-hover:opacity-100 transition-opacity">
            <div className="bg-white rounded-full p-1">
              <svg className="w-4 h-4 text-gray-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0zM10 7v3m0 0v3m0-3h3m-3 0H7" />
              </svg>
            </div>
          </div>
        </div>
      )}

      {/* 非图片文件或图片加载失败 */}
      {(!isImage || imageError) && (
        <div
          className={`flex items-center space-x-3 p-4 rounded-lg border ${
            isOwn
              ? 'bg-blue-50 border-blue-200'
              : 'bg-gray-50 border-gray-200'
          }`}
        >
          {/* 文件图标 */}
          <div className="flex-shrink-0">
            {getFileIcon(mimeType)}
          </div>

          {/* 文件信息 */}
          <div className="flex-1 min-w-0">
            <p className={`text-sm font-medium truncate ${
              isOwn ? 'text-blue-900' : 'text-gray-900'
            }`}>
              {fileName || '未知文件'}
            </p>
            <div className="flex items-center space-x-2 mt-1">
              {fileType && (
                <span className={`text-xs ${isOwn ? 'text-blue-600' : 'text-gray-600'}`}>
                  {fileType}
                </span>
              )}
              {fileSize && (
                <span className={`text-xs ${isOwn ? 'text-blue-600' : 'text-gray-600'}`}>
                  {formatFileSize(fileSize)}
                </span>
              )}
              {getFileExtension(fileName) && (
                <span className={`text-xs px-1 py-0.5 rounded ${
                  isOwn ? 'bg-blue-100 text-blue-700' : 'bg-gray-200 text-gray-700'
                }`}>
                  {getFileExtension(fileName)}
                </span>
              )}
            </div>
          </div>

          {/* 操作按钮 */}
          <div className="flex-shrink-0 flex items-center space-x-2">
            {isImage && (
              <button
                className="p-1 rounded hover:bg-opacity-10"
                onClick={(e) => {
                  e.stopPropagation();
                  window.open(fileUrl, '_blank');
                }}
              >
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
              </button>
            )}

            {downloadable && (
              <button
                className={`p-1 rounded hover:bg-opacity-10 ${
                  isDownloading ? 'animate-spin' : ''
                }`}
                disabled={isDownloading}
                onClick={(e) => {
                  e.stopPropagation();
                  handleDownload();
                }}
              >
                {isDownloading ? (
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                  </svg>
                ) : (
                  <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                )}
              </button>
            )}
          </div>
        </div>
      )}

      {/* 下载进度 */}
      {isDownloading && (
        <div className="mt-2">
          <div className="flex items-center space-x-2">
            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div>
            <span className="text-sm text-gray-600">下载中...</span>
          </div>
        </div>
      )}
    </div>
  );
};

export default FileMessage;