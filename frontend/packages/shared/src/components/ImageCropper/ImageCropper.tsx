/**
 * ImageCropper 组件 - 图片裁剪专家！✂️
 * @author BaSui 😎
 * @description 基于 react-image-crop 的图片裁剪组件，支持裁剪、缩放、旋转
 */

import React, { useState, useRef, useCallback } from 'react';
import ReactCrop, { Crop, PixelCrop } from 'react-image-crop';
import 'react-image-crop/dist/ReactCrop.css';
import './ImageCropper.css';

/**
 * ImageCropper 组件属性
 */
export interface ImageCropperProps {
  /** 图片 URL 或 File 对象 */
  image: string | File;
  /** 裁剪完成回调（返回 Base64 数据） */
  onCropComplete: (croppedImageBase64: string) => void;
  /** 取消裁剪回调 */
  onCancel?: () => void;
  /** 裁剪框宽高比（默认自由裁剪） */
  aspect?: number;
  /** 裁剪框最小宽度（像素） */
  minWidth?: number;
  /** 裁剪框最小高度（像素） */
  minHeight?: number;
  /** 是否显示裁剪按钮（默认 true） */
  showButtons?: boolean;
  /** 裁剪按钮文本 */
  cropButtonText?: string;
  /** 取消按钮文本 */
  cancelButtonText?: string;
}

/**
 * ImageCropper 组件
 *
 * @example
 * ```tsx
 * // 基础用法
 * <ImageCropper
 *   image={imageFile}
 *   onCropComplete={(base64) => console.log(base64)}
 * />
 *
 * // 固定宽高比（1:1 正方形）
 * <ImageCropper
 *   image={imageUrl}
 *   aspect={1}
 *   onCropComplete={handleCrop}
 *   onCancel={handleCancel}
 * />
 *
 * // 固定宽高比（16:9 横屏）
 * <ImageCropper
 *   image={imageFile}
 *   aspect={16 / 9}
 *   minWidth={200}
 *   minHeight={112}
 *   onCropComplete={handleCrop}
 * />
 * ```
 */
export const ImageCropper: React.FC<ImageCropperProps> = ({
  image,
  onCropComplete,
  onCancel,
  aspect,
  minWidth = 50,
  minHeight = 50,
  showButtons = true,
  cropButtonText = '确认裁剪',
  cancelButtonText = '取消',
}) => {
  const [crop, setCrop] = useState<Crop>({
    unit: '%',
    width: 80,
    height: 80,
    x: 10,
    y: 10,
  });
  const [completedCrop, setCompletedCrop] = useState<PixelCrop | null>(null);
  const [imageUrl, setImageUrl] = useState<string>('');
  const imgRef = useRef<HTMLImageElement>(null);

  /**
   * 图片加载完成
   */
  const onImageLoad = useCallback((e: React.SyntheticEvent<HTMLImageElement>) => {
    const { width, height } = e.currentTarget;

    // 如果指定了宽高比，自动计算裁剪框
    if (aspect) {
      const cropWidth = Math.min(width, height * aspect);
      const cropHeight = cropWidth / aspect;

      setCrop({
        unit: 'px',
        width: cropWidth,
        height: cropHeight,
        x: (width - cropWidth) / 2,
        y: (height - cropHeight) / 2,
      });
    }
  }, [aspect]);

  /**
   * 处理图片输入（File 或 URL）
   */
  React.useEffect(() => {
    if (typeof image === 'string') {
      setImageUrl(image);
    } else {
      const url = URL.createObjectURL(image);
      setImageUrl(url);
      return () => URL.revokeObjectURL(url);
    }
  }, [image]);

  /**
   * 生成裁剪后的图片（Base64）
   */
  const getCroppedImage = useCallback(async () => {
    if (!completedCrop || !imgRef.current) {
      return null;
    }

    const image = imgRef.current;
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');

    if (!ctx) {
      return null;
    }

    const scaleX = image.naturalWidth / image.width;
    const scaleY = image.naturalHeight / image.height;

    canvas.width = completedCrop.width;
    canvas.height = completedCrop.height;

    ctx.drawImage(
      image,
      completedCrop.x * scaleX,
      completedCrop.y * scaleY,
      completedCrop.width * scaleX,
      completedCrop.height * scaleY,
      0,
      0,
      completedCrop.width,
      completedCrop.height
    );

    return canvas.toDataURL('image/png');
  }, [completedCrop]);

  /**
   * 处理裁剪确认
   */
  const handleCropConfirm = async () => {
    const croppedImageBase64 = await getCroppedImage();
    if (croppedImageBase64) {
      onCropComplete(croppedImageBase64);
    }
  };

  /**
   * 处理取消
   */
  const handleCancel = () => {
    onCancel?.();
  };

  return (
    <div className="campus-image-cropper">
      <div className="campus-image-cropper__container">
        <ReactCrop
          crop={crop}
          onChange={(c) => setCrop(c)}
          onComplete={(c) => setCompletedCrop(c)}
          aspect={aspect}
          minWidth={minWidth}
          minHeight={minHeight}
        >
          <img
            ref={imgRef}
            src={imageUrl}
            alt="Crop preview"
            onLoad={onImageLoad}
            className="campus-image-cropper__image"
          />
        </ReactCrop>
      </div>

      {showButtons && (
        <div className="campus-image-cropper__actions">
          <button
            className="campus-image-cropper__btn campus-image-cropper__btn--primary"
            onClick={handleCropConfirm}
            disabled={!completedCrop}
          >
            {cropButtonText}
          </button>
          <button
            className="campus-image-cropper__btn campus-image-cropper__btn--secondary"
            onClick={handleCancel}
          >
            {cancelButtonText}
          </button>
        </div>
      )}
    </div>
  );
};

export default ImageCropper;
