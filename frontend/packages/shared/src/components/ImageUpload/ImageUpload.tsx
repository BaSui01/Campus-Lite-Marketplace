/**
 * ImageUpload 组件 - 图片上传专家！📸
 * @author BaSui 😎
 * @description 图片上传组件，基于 useUpload Hook 封装，支持预览、删除、拖拽上传
 */

import React, { useRef } from 'react';
import { useUpload, type UploadFile } from '../../hooks/useUpload';
import { Loading } from '../Loading';
import './ImageUpload.css';

/**
 * ImageUpload 组件的 Props 接口
 */
export interface ImageUploadProps {
  /**
   * 上传 API 地址
   */
  action: string;

  /**
   * 已上传的图片列表
   */
  value?: string[];

  /**
   * 最大上传数量
   * @default 1
   */
  maxCount?: number;

  /**
   * 文件大小限制（字节）
   * @default 5242880 (5MB)
   */
  maxSize?: number;

  /**
   * 是否支持多选
   * @default false
   */
  multiple?: boolean;

  /**
   * 是否禁用
   * @default false
   */
  disabled?: boolean;

  /**
   * 上传按钮文本
   * @default '上传图片'
   */
  uploadText?: string;

  /**
   * 上传提示文本
   */
  tip?: string;

  /**
   * 是否显示预览
   * @default true
   */
  showPreview?: boolean;

  /**
   * 上传请求的额外参数
   */
  data?: Record<string, any>;

  /**
   * 上传请求的额外 Headers
   */
  headers?: Record<string, string>;

  /**
   * 值改变回调
   */
  onChange?: (urls: string[]) => void;

  /**
   * 上传成功回调
   */
  onSuccess?: (url: string) => void;

  /**
   * 上传失败回调
   */
  onError?: (error: string) => void;

  /**
   * 自定义类名
   */
  className?: string;

  /**
   * 自定义样式
   */
  style?: React.CSSProperties;
}

/**
 * ImageUpload 组件
 *
 * @example
 * ```tsx
 * // 基础用法
 * <ImageUpload
 *   action="/api/upload"
 *   onChange={(urls) => console.log(urls)}
 * />
 *
 * // 多图上传
 * <ImageUpload
 *   action="/api/upload"
 *   multiple
 *   maxCount={5}
 *   value={imageUrls}
 *   onChange={setImageUrls}
 * />
 *
 * // 自定义配置
 * <ImageUpload
 *   action="/api/upload"
 *   maxSize={10 * 1024 * 1024} // 10MB
 *   uploadText="选择图片"
 *   tip="支持 JPG、PNG 格式，单个文件不超过 10MB"
 *   headers={{ Authorization: `Bearer ${token}` }}
 * />
 * ```
 */
export const ImageUpload: React.FC<ImageUploadProps> = ({
  action,
  value = [],
  maxCount = 1,
  maxSize = 5 * 1024 * 1024, // 5MB
  multiple = false,
  disabled = false,
  uploadText = '上传图片',
  tip,
  showPreview = true,
  data,
  headers,
  onChange,
  onSuccess,
  onError,
  className = '',
  style,
}) => {
  const inputRef = useRef<HTMLInputElement>(null);

  // 使用 useUpload Hook
  const { fileList, uploading, upload, remove } = useUpload({
    action,
    accept: 'image/*',
    maxSize,
    maxCount,
    multiple,
    data,
    headers,
    onSuccess: (file) => {
      if (file.url) {
        const newUrls = [...value, file.url];
        onChange?.(newUrls);
        onSuccess?.(file.url);
      }
    },
    onError: (file, error) => {
      onError?.(error);
    },
  });

  /**
   * 处理文件选择
   */
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (files && files.length > 0) {
      upload(files);
    }
    // 清空 input 值，允许重复选择同一文件
    if (inputRef.current) {
      inputRef.current.value = '';
    }
  };

  /**
   * 处理点击上传按钮
   */
  const handleUploadClick = () => {
    if (!disabled) {
      inputRef.current?.click();
    }
  };

  /**
   * 处理删除图片
   */
  const handleRemove = (index: number) => {
    if (disabled) return;

    // 删除已上传的图片
    if (index < value.length) {
      const newUrls = value.filter((_, i) => i !== index);
      onChange?.(newUrls);
    }
    // 删除正在上传的文件
    else {
      const fileIndex = index - value.length;
      const file = fileList[fileIndex];
      if (file) {
        remove(file.uid);
      }
    }
  };

  /**
   * 处理拖拽上传
   */
  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    if (disabled) return;

    const files = e.dataTransfer.files;
    if (files && files.length > 0) {
      upload(files);
    }
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
  };

  // 是否达到最大数量
  const isMaxCount = value.length + fileList.length >= maxCount;

  // 组装 CSS 类名
  const classNames = [
    'campus-image-upload',
    disabled ? 'campus-image-upload--disabled' : '',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <div className={classNames} style={style}>
      {/* 图片列表 */}
      <div className="campus-image-upload__list">
        {/* 已上传的图片 */}
        {value.map((url, index) => (
          <div key={`uploaded-${index}`} className="campus-image-upload__item">
            {showPreview && (
              <img src={url} alt={`image-${index}`} className="campus-image-upload__image" />
            )}
            {!disabled && (
              <div className="campus-image-upload__mask">
                <button
                  className="campus-image-upload__remove-btn"
                  onClick={() => handleRemove(index)}
                >
                  ×
                </button>
              </div>
            )}
          </div>
        ))}

        {/* 正在上传的文件 */}
        {fileList.map((file, index) => (
          <div key={file.uid} className="campus-image-upload__item">
            {file.file && (
              <img
                src={URL.createObjectURL(file.file)}
                alt={file.name}
                className="campus-image-upload__image"
              />
            )}
            <div className="campus-image-upload__mask">
              {file.status === 'uploading' && (
                <div className="campus-image-upload__progress">
                  <Loading size="small" />
                  <span className="campus-image-upload__progress-text">{file.progress}%</span>
                </div>
              )}
              {file.status === 'error' && (
                <div className="campus-image-upload__error">上传失败</div>
              )}
              {!disabled && file.status !== 'uploading' && (
                <button
                  className="campus-image-upload__remove-btn"
                  onClick={() => handleRemove(value.length + index)}
                >
                  ×
                </button>
              )}
            </div>
          </div>
        ))}

        {/* 上传按钮 */}
        {!isMaxCount && (
          <div
            className="campus-image-upload__upload-btn"
            onClick={handleUploadClick}
            onDrop={handleDrop}
            onDragOver={handleDragOver}
          >
            <input
              ref={inputRef}
              type="file"
              accept="image/*"
              multiple={multiple}
              onChange={handleFileChange}
              className="campus-image-upload__input"
              disabled={disabled}
            />
            {uploading ? (
              <Loading size="small" />
            ) : (
              <>
                <svg
                  className="campus-image-upload__icon"
                  viewBox="0 0 24 24"
                  fill="currentColor"
                >
                  <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z" />
                </svg>
                <span className="campus-image-upload__text">{uploadText}</span>
              </>
            )}
          </div>
        )}
      </div>

      {/* 提示文本 */}
      {tip && <div className="campus-image-upload__tip">{tip}</div>}
    </div>
  );
};

export default ImageUpload;
