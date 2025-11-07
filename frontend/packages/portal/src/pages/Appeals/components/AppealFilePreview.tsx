/**
 * 申诉材料预览组件 - 文件查看大师！👁️
 *
 * @author BaSui 😎
 * @description 申诉材料的预览、下载、管理功能
 * @date 2025-11-07
 */

import React, { useState } from 'react';
import type { AppealMaterial } from '@campus/shared/api/models';

/**
 * 申诉材料预览组件属性
 */
export interface AppealFilePreviewProps {
  /** 申诉材料列表 */
  materials: AppealMaterial[];
  /** 是否显示操作按钮 */
  showActions?: boolean;
  /** 是否可编辑 */
  editable?: boolean;
  /** 材料删除回调 */
  onMaterialDelete?: (materialId: number) => void;
  /** 材料编辑回调 */
  onMaterialEdit?: (material: AppealMaterial) => void;
  /** 自定义样式类名 */
  className?: string;
}

/**
 * 获取文件图标
 */
const getFileIcon = (mimeType?: string, isImage?: boolean): JSX.Element => {
  if (isImage) {
    return (
      <svg className="h-8 w-8 text-green-500" fill="currentColor" viewBox="0 0 20 20">
        <path fillRule="evenodd" d="M4 3a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V5a2 2 0 00-2-2H4zm12 12H4l4-8 3 6 2-4 3 6z" clipRule="evenodd" />
      </svg>
    );
  }

  switch (mimeType) {
    case 'application/pdf':
      return (
        <svg className="h-8 w-8 text-red-500" fill="currentColor" viewBox="0 0 20 20">
          <path fillRule="evenodd" d="M4 4a2 2 0 00-2 2v8a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-5L9 2H4z" clipRule="evenodd" />
        </svg>
      );
    case 'application/msword':
    case 'application/vnd.openxmlformats-officedocument.wordprocessingml.document':
      return (
        <svg className="h-8 w-8 text-blue-500" fill="currentColor" viewBox="0 0 20 20">
          <path fillRule="evenodd" d="M4 4a2 2 0 00-2 2v8a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-5L9 2H4z" clipRule="evenodd" />
        </svg>
      );
    default:
      return (
        <svg className="h-8 w-8 text-gray-500" fill="currentColor" viewBox="0 0 20 20">
          <path fillRule="evenodd" d="M4 4a2 2 0 00-2 2v8a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-5L9 2H4z" clipRule="evenodd" />
        </svg>
      );
  }
};

/**
 * 获取状态标签样式
 */
const getStatusBadgeStyle = (status?: string): string => {
  switch (status) {
    case 'UPLOADED':
      return 'bg-blue-100 text-blue-800';
    case 'REVIEWING':
      return 'bg-yellow-100 text-yellow-800';
    case 'APPROVED':
      return 'bg-green-100 text-green-800';
    case 'REJECTED':
      return 'bg-red-100 text-red-800';
    case 'WITHDRAWN':
      return 'bg-gray-100 text-gray-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
};

/**
 * 获取状态标签文本
 */
const getStatusLabelText = (status?: string): string => {
  switch (status) {
    case 'UPLOADED':
      return '已上传';
    case 'REVIEWING':
      return '审核中';
    case 'APPROVED':
      return '已通过';
    case 'REJECTED':
      return '已驳回';
    case 'WITHDRAWN':
      return '已撤回';
    default:
      return '未知';
  }
};

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
 * 格式化日期
 */
const formatDate = (dateString?: string): string => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN');
};

/**
 * 申诉材料预览组件
 */
export const AppealFilePreview: React.FC<AppealFilePreviewProps> = ({
  materials,
  showActions = true,
  editable = false,
  onMaterialDelete,
  onMaterialEdit,
  className = '',
}) => {
  const [previewMaterial, setPreviewMaterial] = useState<AppealMaterial | null>(null);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState<number | null>(null);

  /**
   * 处理文件预览
   */
  const handlePreview = (material: AppealMaterial) => {
    if (material.imageFile) {
      // 图片直接预览
      setPreviewMaterial(material);
    } else {
      // 文档在新窗口打开
      if (material.filePath) {
        window.open(material.filePath, '_blank');
      }
    }
  };

  /**
   * 处理文件下载
   */
  const handleDownload = (material: AppealMaterial) => {
    if (material.filePath) {
      const link = document.createElement('a');
      link.href = material.filePath;
      link.download = material.fileName || 'document';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    }
  };

  /**
   * 处理材料删除
   */
  const handleDelete = (materialId: number) => {
    onMaterialDelete?.(materialId);
    setShowDeleteConfirm(null);
  };

  /**
   * 处理材料编辑
   */
  const handleEdit = (material: AppealMaterial) => {
    onMaterialEdit?.(material);
  };

  if (materials.length === 0) {
    return (
      <div className={`text-center py-8 text-gray-500 ${className}`}>
        <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 13h6m-3-3v6m-9 1V7a2 2 0 012-2h6l2 2h6a2 2 0 012 2v8a2 2 0 01-2 2H5a2 2 0 01-2-2z" />
        </svg>
        <p className="mt-2 text-sm">暂无申诉材料</p>
      </div>
    );
  }

  return (
    <div className={`appeal-file-preview ${className}`}>
      {/* 材料网格 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {materials.map((material) => (
          <div
            key={material.id}
            className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow"
          >
            {/* 文件头部 */}
            <div className="flex items-start justify-between mb-3">
              {/* 文件图标 */}
              <div className="flex-shrink-0">
                {getFileIcon(material.mimeType, material.imageFile)}
              </div>

              {/* 状态标签 */}
              <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusBadgeStyle(material.status)}`}>
                {getStatusLabelText(material.status)}
              </span>
            </div>

            {/* 文件信息 */}
            <div className="mb-3">
              <h4 className="text-sm font-medium text-gray-900 truncate" title={material.fileName}>
                {material.fileName}
              </h4>
              <div className="text-xs text-gray-500 space-y-1">
                <div>类型: {material.fileType}</div>
                <div>大小: {material.formattedFileSize || formatFileSize(material.fileSize)}</div>
                <div>上传者: {material.uploadedByName}</div>
                <div>时间: {formatDate(material.uploadedAt)}</div>
              </div>
            </div>

            {/* 缩略图预览（仅图片） */}
            {material.imageFile && material.thumbnailPath && (
              <div className="mb-3">
                <img
                  src={material.thumbnailPath}
                  alt={material.fileName}
                  className="w-full h-32 object-cover rounded cursor-pointer hover:opacity-90 transition-opacity"
                  onClick={() => handlePreview(material)}
                />
              </div>
            )}

            {/* 文件描述 */}
            {material.description && (
              <div className="mb-3">
                <p className="text-xs text-gray-600 line-clamp-2">{material.description}</p>
              </div>
            )}

            {/* 操作按钮 */}
            {showActions && (
              <div className="flex items-center justify-between space-x-2">
                <div className="flex space-x-2">
                  {/* 预览按钮 */}
                  <button
                    type="button"
                    className="text-blue-600 hover:text-blue-800 text-sm font-medium"
                    onClick={() => handlePreview(material)}
                  >
                    预览
                  </button>

                  {/* 下载按钮 */}
                  <button
                    type="button"
                    className="text-green-600 hover:text-green-800 text-sm font-medium"
                    onClick={() => handleDownload(material)}
                  >
                    下载
                  </button>
                </div>

                {/* 编辑和删除按钮 */}
                {editable && (
                  <div className="flex space-x-2">
                    {/* 编辑按钮 */}
                    <button
                      type="button"
                      className="text-gray-600 hover:text-gray-800 text-sm"
                      onClick={() => handleEdit(material)}
                    >
                      <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                        <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
                      </svg>
                    </button>

                    {/* 删除按钮 */}
                    <button
                      type="button"
                      className="text-red-600 hover:text-red-800 text-sm"
                      onClick={() => setShowDeleteConfirm(material.id!)}
                    >
                      <svg className="h-4 w-4" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z" clipRule="evenodd" />
                      </svg>
                    </button>
                  </div>
                )}
              </div>
            )}
          </div>
        ))}
      </div>

      {/* 图片预览模态框 */}
      {previewMaterial && previewMaterial.imageFile && (
        <div
          className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
          onClick={() => setPreviewMaterial(null)}
        >
          <div className="max-w-4xl max-h-screen p-4">
            <img
              src={previewMaterial.filePath || previewMaterial.thumbnailPath}
              alt={previewMaterial.fileName}
              className="max-w-full max-h-full object-contain"
            />
            <div className="text-center mt-4">
              <p className="text-white text-lg">{previewMaterial.fileName}</p>
              <button
                type="button"
                className="mt-2 px-4 py-2 bg-white text-gray-800 rounded hover:bg-gray-100"
                onClick={() => setPreviewMaterial(null)}
              >
                关闭
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 删除确认模态框 */}
      {showDeleteConfirm && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 max-w-sm w-full mx-4">
            <h3 className="text-lg font-medium text-gray-900 mb-4">确认删除</h3>
            <p className="text-sm text-gray-600 mb-6">
              确定要删除这个申诉材料吗？此操作不可撤销。
            </p>
            <div className="flex justify-end space-x-3">
              <button
                type="button"
                className="px-4 py-2 text-gray-700 bg-gray-100 rounded hover:bg-gray-200"
                onClick={() => setShowDeleteConfirm(null)}
              >
                取消
              </button>
              <button
                type="button"
                className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
                onClick={() => handleDelete(showDeleteConfirm)}
              >
                删除
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AppealFilePreview;