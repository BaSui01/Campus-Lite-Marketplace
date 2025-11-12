/**
 * 图片上传组件
 * 
 * 功能：
 * - 图片上传
 * - 图片预览
 * - 上传进度
 * - 多图上传
 * - 图片删除
 * 
 * @author BaSui 😎
 * @date 2025-11-08
 */

import React, { useState } from 'react';
import { Upload, Modal, App, type UploadFile, type UploadProps } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { uploadService } from '@campus/shared';

/**
 * ImageUpload 组件属性
 */
export interface ImageUploadProps {
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
  /** 接受的文件类型 */
  accept?: string;
}

/**
 * 图片上传组件
 * 
 * @example
 * ```tsx
 * // 单图上传
 * <Form.Item name="avatar" label="头像">
 *   <ImageUpload maxCount={1} uploadText="上传头像" />
 * </Form.Item>
 * 
 * // 多图上传
 * <Form.Item name="images" label="商品图片">
 *   <ImageUpload
 *     maxCount={5}
 *     multiple
 *     uploadText="上传图片"
 *   />
 * </Form.Item>
 * ```
 */
export const ImageUpload: React.FC<ImageUploadProps> = ({
  value = [],
  onChange,
  maxCount = 1,
  multiple = false,
  uploadText = '上传图片',
  disabled = false,
  maxSize = 5,
  accept = 'image/*',
}) => {
  const { message } = App.useApp(); // ✅ 使用 App 提供的 message 实例
  const [previewVisible, setPreviewVisible] = useState(false);
  const [previewImage, setPreviewImage] = useState('');
  const [fileList, setFileList] = useState<UploadFile[]>(() =>
    value.map((url, index) => ({
      uid: `${index}`,
      name: `image-${index}`,
      status: 'done',
      url,
    }))
  );

  /**
   * 上传前校验
   */
  const beforeUpload: UploadProps['beforeUpload'] = (file) => {
    // 检查文件类型
    const isImage = file.type.startsWith('image/');
    if (!isImage) {
      message.error('只能上传图片文件！');
      return false;
    }

    // 检查文件大小
    const isLtMaxSize = file.size / 1024 / 1024 < maxSize;
    if (!isLtMaxSize) {
      message.error(`图片大小不能超过 ${maxSize}MB！`);
      return false;
    }

    return true;
  };

  /**
   * 自定义上传
   */
  const customRequest: UploadProps['customRequest'] = async ({ file, onSuccess, onError, onProgress }) => {
    try {
      const formData = new FormData();
      formData.append('file', file as File);

      // 调用上传服务
      const url = await uploadService.uploadImage(formData, (percent) => {
        onProgress?.({ percent });
      });

      // 上传成功
      onSuccess?.(url);
    } catch (error: any) {
      message.error(error.message || '上传失败');
      onError?.(error);
    }
  };

  /**
   * 文件列表变化
   */
  const handleChange: UploadProps['onChange'] = ({ fileList: newFileList }) => {
    setFileList(newFileList);

    // 获取所有上传成功的图片 URL
    const urls = newFileList
      .filter((file) => file.status === 'done')
      .map((file) => file.response || file.url)
      .filter(Boolean) as string[];

    onChange?.(urls);
  };

  /**
   * 预览图片
   */
  const handlePreview = async (file: UploadFile) => {
    setPreviewImage(file.url || file.response);
    setPreviewVisible(true);
  };

  /**
   * 关闭预览
   */
  const handlePreviewCancel = () => {
    setPreviewVisible(false);
  };

  /**
   * 上传按钮
   */
  const uploadButton = (
    <div>
      <PlusOutlined />
      <div style={{ marginTop: 8 }}>{uploadText}</div>
    </div>
  );

  return (
    <>
      <Upload
        listType="picture-card"
        fileList={fileList}
        beforeUpload={beforeUpload}
        customRequest={customRequest}
        onChange={handleChange}
        onPreview={handlePreview}
        maxCount={maxCount}
        multiple={multiple}
        disabled={disabled}
        accept={accept}
      >
        {fileList.length >= maxCount ? null : uploadButton}
      </Upload>

      {/* 图片预览 */}
      <Modal
        open={previewVisible}
        title="图片预览"
        footer={null}
        onCancel={handlePreviewCancel}
      >
        <img alt="preview" style={{ width: '100%' }} src={previewImage} />
      </Modal>
    </>
  );
};
