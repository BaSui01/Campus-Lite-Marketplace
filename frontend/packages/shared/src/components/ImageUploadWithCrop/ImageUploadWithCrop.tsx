/**
 * ImageUploadWithCrop 组件 - 带裁剪功能的图片上传！📸✂️
 * @author BaSui 😎
 * @description 集成图片上传和裁剪功能，支持裁剪后上传、粘贴板上传
 */

import React, { useState, useRef } from 'react';
import { ImageCropper } from '../ImageCropper';
import { uploadService } from '../../services/upload';
import { Loading } from '../Loading';
import './ImageUploadWithCrop.css';

/**
 * ImageUploadWithCrop 组件属性
 */
export interface ImageUploadWithCropProps {
  /** 已上传的图片 URL 列表 */
  value?: string[];
  /** 值变化回调 */
  onChange?: (urls: string[]) => void;
  /** 最大上传数量，默认 1 */
  maxCount?: number;
  /** 是否支持多选，默认 false */
  multiple?: boolean;
  /** 上传按钮文本 */
  uploadText?: string;
  /** 是否禁用，默认 false */
  disabled?: boolean;
  /** 图片最大尺寸（MB），默认 5MB */
  maxSize?: number;
  /** 是否启用裁剪功能，默认 true */
  enableCrop?: boolean;
  /** 裁剪框宽高比（默认自由裁剪） */
  cropAspect?: number;
  /** 业务场景分类 */
  category?: 'avatar' | 'goods' | 'post' | 'message' | 'general';
  /** 是否启用粘贴板上传，默认 true */
  enablePaste?: boolean;
  /** 提示文本 */
  tip?: string;
}

/**
 * ImageUploadWithCrop 组件
 *
 * @example
 * ```tsx
 * // 基础用法（带裁剪）
 * <ImageUploadWithCrop
 *   value={imageUrls}
 *   onChange={setImageUrls}
 * />
 *
 * // 头像上传（1:1 裁剪）
 * <ImageUploadWithCrop
 *   value={[avatarUrl]}
 *   onChange={(urls) => setAvatarUrl(urls[0])}
 *   maxCount={1}
 *   cropAspect={1}
 *   category="avatar"
 * />
 *
 * // 商品图片上传（多图、16:9 裁剪）
 * <ImageUploadWithCrop
 *   value={goodsImages}
 *   onChange={setGoodsImages}
 *   maxCount={5}
 *   multiple
 *   cropAspect={16 / 9}
 *   category="goods"
 * />
 * ```
 */
export const ImageUploadWithCrop: React.FC<ImageUploadWithCropProps> = ({
  value = [],
  onChange,
  maxCount = 1,
  multiple = false,
  uploadText = '上传图片',
  disabled = false,
  maxSize = 5,
  enableCrop = true,
  cropAspect,
  category = 'general',
  enablePaste = true,
  tip,
}) => {
  const [uploading, setUploading] = useState(false);
  const [cropImage, setCropImage] = useState<File | null>(null);
  const [showCropper, setShowCropper] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  /**
   * 处理文件选择
   */
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (files && files.length > 0) {
      handleFiles(Array.from(files));
    }
    // 清空 input 值，允许重复选择同一文件
    if (inputRef.current) {
      inputRef.current.value = '';
    }
  };

  /**
   * 处理文件列表
   */
  const handleFiles = async (files: File[]) => {
    // 检查文件数量
    if (value.length + files.length > maxCount) {
      alert(`最多只能上传 ${maxCount} 张图片`);
      return;
    }

    // 检查文件大小
    const maxSizeBytes = maxSize * 1024 * 1024;
    const oversizedFiles = files.filter((file) => file.size > maxSizeBytes);
    if (oversizedFiles.length > 0) {
      alert(`文件大小不能超过 ${maxSize}MB`);
      return;
    }

    // 检查文件类型
    const invalidFiles = files.filter((file) => !file.type.startsWith('image/'));
    if (invalidFiles.length > 0) {
      alert('只能上传图片文件');
      return;
    }

    // 如果启用裁剪，显示裁剪器
    if (enableCrop && files.length === 1) {
      setCropImage(files[0]);
      setShowCropper(true);
    } else {
      // 直接上传
      await uploadFiles(files);
    }
  };

  /**
   * 上传文件
   */
  const uploadFiles = async (files: File[]) => {
    setUploading(true);
    try {
      const uploadPromises = files.map((file) =>
        uploadService.uploadImage(file, { category })
      );
      const results = await Promise.all(uploadPromises);
      const newUrls = results.map((result) => result.url);
      onChange?.([...value, ...newUrls]);
    } catch (error: any) {
      alert(`上传失败：${error.message || '未知错误'}`);
    } finally {
      setUploading(false);
    }
  };

  /**
   * 处理裁剪完成
   */
  const handleCropComplete = async (croppedImageBase64: string) => {
    setShowCropper(false);
    setCropImage(null);
    setUploading(true);

    try {
      // 上传 Base64 图片
      const result = await uploadService.uploadBase64Image(croppedImageBase64, { category });
      onChange?.([...value, result.url]);
    } catch (error: any) {
      alert(`上传失败：${error.message || '未知错误'}`);
    } finally {
      setUploading(false);
    }
  };

  /**
   * 处理裁剪取消
   */
  const handleCropCancel = () => {
    setShowCropper(false);
    setCropImage(null);
  };

  /**
   * 处理粘贴事件
   */
  const handlePaste = async (e: React.ClipboardEvent) => {
    if (!enablePaste || disabled) return;

    const items = e.clipboardData?.items;
    if (!items) return;

    const imageItems = Array.from(items).filter((item) => item.type.startsWith('image/'));
    if (imageItems.length === 0) return;

    e.preventDefault();

    const files = imageItems
      .map((item) => item.getAsFile())
      .filter((file): file is File => file !== null);

    if (files.length > 0) {
      await handleFiles(files);
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
    const newUrls = value.filter((_, i) => i !== index);
    onChange?.(newUrls);
  };

  /**
   * 处理拖拽
   */
  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    if (disabled) return;

    const files = Array.from(e.dataTransfer.files);
    if (files.length > 0) {
      handleFiles(files);
    }
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
  };

  // 是否达到最大数量
  const isMaxCount = value.length >= maxCount;

  return (
    <div
      ref={containerRef}
      className="campus-image-upload-with-crop"
      onPaste={handlePaste}
      tabIndex={0}
    >
      {/* 图片列表 */}
      <div className="campus-image-upload-with-crop__list">
        {value.map((url, index) => (
          <div key={index} className="campus-image-upload-with-crop__item">
            <img src={url} alt={`image-${index}`} className="campus-image-upload-with-crop__image" />
            {!disabled && (
              <div className="campus-image-upload-with-crop__mask">
                <button
                  className="campus-image-upload-with-crop__remove-btn"
                  onClick={() => handleRemove(index)}
                >
                  ×
                </button>
              </div>
            )}
          </div>
        ))}

        {/* 上传按钮 */}
        {!isMaxCount && (
          <div
            className="campus-image-upload-with-crop__upload-btn"
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
              className="campus-image-upload-with-crop__input"
              disabled={disabled}
            />
            {uploading ? (
              <Loading size="small" />
            ) : (
              <>
                <svg
                  className="campus-image-upload-with-crop__icon"
                  viewBox="0 0 24 24"
                  fill="currentColor"
                >
                  <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z" />
                </svg>
                <span className="campus-image-upload-with-crop__text">{uploadText}</span>
              </>
            )}
          </div>
        )}
      </div>

      {/* 提示文本 */}
      {tip && <div className="campus-image-upload-with-crop__tip">{tip}</div>}
      {enablePaste && (
        <div className="campus-image-upload-with-crop__tip">
          💡 提示：可以直接粘贴剪贴板中的图片（Ctrl+V）
        </div>
      )}

      {/* 裁剪器弹窗 */}
      {showCropper && cropImage && (
        <div className="campus-image-upload-with-crop__modal">
          <div className="campus-image-upload-with-crop__modal-content">
            <ImageCropper
              image={cropImage}
              aspect={cropAspect}
              onCropComplete={handleCropComplete}
              onCancel={handleCropCancel}
            />
          </div>
        </div>
      )}
    </div>
  );
};

export default ImageUploadWithCrop;
