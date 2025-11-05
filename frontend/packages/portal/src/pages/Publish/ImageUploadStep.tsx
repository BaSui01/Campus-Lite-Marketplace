/**
 * 图片上传步骤 📷
 * @author BaSui 😎
 * @description 上传商品图片（最多9张）
 */

import React, { useState } from 'react';
import type { GoodsFormData } from './index';
import './ImageUploadStep.css';

interface ImageUploadStepProps {
  formData: GoodsFormData;
  onUpdate: (data: Partial<GoodsFormData>) => void;
  onNext: () => void;
  onPrev: () => void;
}

const MAX_IMAGES = 9;

export const ImageUploadStep: React.FC<ImageUploadStepProps> = ({
  formData,
  onUpdate,
  onNext,
  onPrev,
}) => {
  const [uploading, setUploading] = useState(false);
  const [dragOver, setDragOver] = useState(false);

  // 处理文件选择
  const handleFileSelect = async (files: FileList | null) => {
    if (!files || files.length === 0) return;

    const remainingSlots = MAX_IMAGES - formData.images.length;
    if (remainingSlots <= 0) {
      alert(`最多只能上传${MAX_IMAGES}张图片`);
      return;
    }

    const filesToUpload = Array.from(files).slice(0, remainingSlots);
    
    // 验证文件类型和大小
    const invalidFiles = filesToUpload.filter(
      (file) => !file.type.startsWith('image/') || file.size > 5 * 1024 * 1024
    );
    
    if (invalidFiles.length > 0) {
      alert('请上传有效的图片文件（JPG、PNG、GIF等），单个文件不超过5MB');
      return;
    }

    setUploading(true);

    try {
      // 模拟上传（实际项目中应该调用uploadService）
      const uploadedUrls = await Promise.all(
        filesToUpload.map(async (file) => {
          // TODO: 替换为真实的上传API调用
          // const url = await uploadService.uploadImage(file);
          
          // 暂时使用本地预览URL
          return URL.createObjectURL(file);
        })
      );

      onUpdate({ images: [...formData.images, ...uploadedUrls] });
    } catch (error) {
      alert('上传失败，请重试');
      console.error('Upload error:', error);
    } finally {
      setUploading(false);
    }
  };

  // 处理文件输入
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    handleFileSelect(e.target.files);
    e.target.value = ''; // 重置input，允许重复选择同一文件
  };

  // 处理拖放
  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(false);
    handleFileSelect(e.dataTransfer.files);
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(true);
  };

  const handleDragLeave = () => {
    setDragOver(false);
  };

  // 删除图片
  const handleRemoveImage = (index: number) => {
    const newImages = formData.images.filter((_, i) => i !== index);
    onUpdate({ images: newImages });
  };

  // 设置封面（移动到第一位）
  const handleSetCover = (index: number) => {
    const newImages = [...formData.images];
    const [image] = newImages.splice(index, 1);
    newImages.unshift(image);
    onUpdate({ images: newImages });
  };

  // 验证并进入下一步
  const handleNext = () => {
    if (formData.images.length === 0) {
      alert('请至少上传一张商品图片');
      return;
    }
    
    onNext();
  };

  return (
    <div className="image-upload-step">
      <h2 className="image-upload-step__title">📷 上传商品图片</h2>
      
      <div className="image-upload-step__hint">
        <p>• 最多上传{MAX_IMAGES}张图片，第一张为封面</p>
        <p>• 支持JPG、PNG、GIF格式，单张不超过5MB</p>
        <p>• 建议尺寸：800x800px，清晰展示商品细节</p>
      </div>

      {/* 图片网格 */}
      <div className="image-upload-grid">
        {/* 已上传的图片 */}
        {formData.images.map((image, index) => (
          <div key={index} className="image-upload-item">
            <img src={image} alt={`商品图片${index + 1}`} className="image-upload-item__img" />
            
            {/* 封面标签 */}
            {index === 0 && (
              <div className="image-upload-item__cover-badge">封面</div>
            )}
            
            {/* 操作按钮 */}
            <div className="image-upload-item__actions">
              {index !== 0 && (
                <button
                  type="button"
                  className="image-upload-item__btn"
                  onClick={() => handleSetCover(index)}
                  title="设为封面"
                >
                  📌
                </button>
              )}
              <button
                type="button"
                className="image-upload-item__btn image-upload-item__btn--delete"
                onClick={() => handleRemoveImage(index)}
                title="删除"
              >
                🗑️
              </button>
            </div>
          </div>
        ))}

        {/* 上传按钮 */}
        {formData.images.length < MAX_IMAGES && (
          <div
            className={`image-upload-placeholder ${dragOver ? 'drag-over' : ''}`}
            onDrop={handleDrop}
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
          >
            <input
              type="file"
              accept="image/*"
              multiple
              onChange={handleInputChange}
              className="image-upload-placeholder__input"
              id="image-upload-input"
              disabled={uploading}
            />
            <label htmlFor="image-upload-input" className="image-upload-placeholder__label">
              {uploading ? (
                <>
                  <div className="image-upload-placeholder__icon">⏳</div>
                  <div className="image-upload-placeholder__text">上传中...</div>
                </>
              ) : (
                <>
                  <div className="image-upload-placeholder__icon">📸</div>
                  <div className="image-upload-placeholder__text">
                    点击或拖拽上传
                  </div>
                  <div className="image-upload-placeholder__subtext">
                    {formData.images.length}/{MAX_IMAGES}
                  </div>
                </>
              )}
            </label>
          </div>
        )}
      </div>

      {/* 操作按钮 */}
      <div className="image-upload-step__actions">
        <button
          type="button"
          className="image-upload-step__btn image-upload-step__btn--prev"
          onClick={onPrev}
        >
          ← 上一步
        </button>
        <button
          type="button"
          className="image-upload-step__btn image-upload-step__btn--next"
          onClick={handleNext}
        >
          下一步：确认发布 →
        </button>
      </div>
    </div>
  );
};

export default ImageUploadStep;
