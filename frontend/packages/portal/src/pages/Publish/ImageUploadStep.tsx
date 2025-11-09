/**
 * 图片上传步骤 📷
 * @author BaSui 😎
 * @description 上传商品图片（最多9张）- 集成裁剪功能 ✂️
 */

import React from 'react';
import { ImageUploadWithCrop } from '@campus/shared';
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
  // 处理图片变化
  const handleImagesChange = (urls: string[]) => {
    onUpdate({ images: urls });
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
        <p>• ✨ 支持图片裁剪、粘贴板上传（Ctrl+V）</p>
      </div>

      {/* 使用新的带裁剪功能的上传组件 */}
      <ImageUploadWithCrop
        value={formData.images}
        onChange={handleImagesChange}
        maxCount={MAX_IMAGES}
        multiple
        enableCrop={true}
        cropAspect={1}  // 1:1 正方形裁剪
        category="goods"
        uploadText="上传商品图片"
        tip={`已上传 ${formData.images.length}/${MAX_IMAGES} 张图片`}
      />

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
