/**
 * 发布商品页 - 卖家发布商品！📝
 * @author BaSui 😎
 * @description 商品标题、描述、价格、分类、图片上传、标签选择
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { goodsService } from '@campus/shared/services/goods';
import { uploadService } from '@campus/shared/services/upload';
import { useNotificationStore } from '../../store';
import type { CreateGoodsRequest } from '@campus/shared/api/models';
import './Publish.css';

/**
 * 表单数据接口
 */
interface FormData {
  title: string;
  description: string;
  price: string;
  categoryId: number | null;
  images: string[];
  tagIds: number[];
}

/**
 * 分类接口
 */
interface Category {
  id: number;
  name: string;
}

/**
 * 标签接口
 */
interface Tag {
  id: number;
  name: string;
}

/**
 * 发布商品页组件
 */
const Publish: React.FC = () => {
  const navigate = useNavigate();
  const toast = useNotificationStore();

  const [formData, setFormData] = useState<FormData>({
    title: '',
    description: '',
    price: '',
    categoryId: null,
    images: [],
    tagIds: [],
  });

  const [categories, setCategories] = useState<Category[]>([]);
  const [tags, setTags] = useState<Tag[]>([]);
  const [loading, setLoading] = useState(false);
  const [loadingData, setLoadingData] = useState(true);
  const [uploadingImages, setUploadingImages] = useState<boolean[]>([]);
  const [errors, setErrors] = useState<Partial<Record<keyof FormData, string>>>({});

  const loadData = async () => {
    setLoadingData(true);

    try {
      const [categoryTree, hotTags] = await Promise.all([
        goodsService.getCategoryTree(),
        goodsService.getHotTags(20),
      ]);

      setCategories(categoryTree || []);
      setTags(hotTags || []);
    } catch (err: any) {
      console.error('加载数据失败：', err);
      toast.error('加载分类和标签失败，请刷新页面重试！😭');
    } finally {
      setLoadingData(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleInputChange = (field: keyof FormData, value: any) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    setErrors((prev) => ({ ...prev, [field]: undefined }));
  };

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (!files || files.length === 0) return;

    const fileArray = Array.from(files);
    const maxImages = 9 - formData.images.length;

    if (fileArray.length > maxImages) {
      toast.warning(`最多只能上传${maxImages}张图片！😰`);
      return;
    }

    const uploadStatuses = new Array(fileArray.length).fill(true);
    setUploadingImages(uploadStatuses);

    try {
      const uploadPromises = fileArray.map(async (file, index) => {
        try {
          if (file.size > 5 * 1024 * 1024) {
            throw new Error('图片大小不能超过5MB');
          }

          if (!file.type.startsWith('image/')) {
            throw new Error('只能上传图片文件');
          }

          const response = await uploadService.uploadImage(file);
          const url = response.data?.url;

          if (!url) {
            throw new Error('上传失败，未返回图片URL');
          }

          uploadStatuses[index] = false;
          setUploadingImages([...uploadStatuses]);

          return url;
        } catch (err: any) {
          console.error(`图片${index + 1}上传失败：`, err);
          uploadStatuses[index] = false;
          setUploadingImages([...uploadStatuses]);
          throw err;
        }
      });

      const uploadedUrls = await Promise.all(uploadPromises);
      setFormData((prev) => ({
        ...prev,
        images: [...prev.images, ...uploadedUrls],
      }));
      setErrors((prev) => ({ ...prev, images: undefined }));
    } catch (err: any) {
      toast.error(err.message || '图片上传失败，请重试！😭');
    } finally {
      setUploadingImages([]);
    }
  };

  const handleRemoveImage = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      images: prev.images.filter((_, i) => i !== index),
    }));
  };

  const handleToggleTag = (tagId: number) => {
    setFormData((prev) => {
      const tagIds = prev.tagIds.includes(tagId)
        ? prev.tagIds.filter((id) => id !== tagId)
        : [...prev.tagIds, tagId];

      return { ...prev, tagIds };
    });
  };

  const validateForm = (): boolean => {
    const newErrors: Partial<Record<keyof FormData, string>> = {};

    if (!formData.title.trim()) {
      newErrors.title = '请输入商品标题';
    } else if (formData.title.length > 100) {
      newErrors.title = '标题不能超过100个字符';
    }

    if (!formData.description.trim()) {
      newErrors.description = '请输入商品描述';
    } else if (formData.description.length > 1000) {
      newErrors.description = '描述不能超过1000个字符';
    }

    if (!formData.price.trim()) {
      newErrors.price = '请输入商品价格';
    } else {
      const price = parseFloat(formData.price);
      if (isNaN(price) || price <= 0) {
        newErrors.price = '请输入有效的价格';
      } else if (price > 999999) {
        newErrors.price = '价格不能超过999999元';
      }
    }

    if (!formData.categoryId) {
      newErrors.categoryId = '请选择商品分类';
    }

    if (formData.images.length === 0) {
      newErrors.images = '请至少上传一张商品图片';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async () => {
    if (!validateForm()) {
      return;
    }

    setLoading(true);

    try {
      const requestData: CreateGoodsRequest = {
        title: formData.title.trim(),
        description: formData.description.trim(),
        price: Math.round(parseFloat(formData.price) * 100),
        categoryId: formData.categoryId!,
        images: formData.images,
        tagIds: formData.tagIds.length > 0 ? formData.tagIds : undefined,
      };

      const goodsId = await goodsService.createGoods(requestData);

      toast.success('商品发布成功！🎉');
      navigate(`/goods/${goodsId}`);
    } catch (err: any) {
      console.error('发布商品失败：', err);
      toast.error(err.response?.data?.message || '发布失败，请稍后重试！😭');
    } finally {
      setLoading(false);
    }
  };

  if (loadingData) {
    return (
      <div className="publish-page">
        <div className="publish-loading">
          <div className="loading-spinner">⏳</div>
          <p>正在加载...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="publish-page">
      <div className="publish-container">
        <h1 className="publish-title">发布商品</h1>

        <div className="publish-form">
          <div className="form-group">
            <label className="form-label">
              商品标题 <span className="required">*</span>
            </label>
            <input
              type="text"
              className={`form-input ${errors.title ? 'error' : ''}`}
              placeholder="请输入商品标题（最多100字）"
              value={formData.title}
              onChange={(e) => handleInputChange('title', e.target.value)}
              maxLength={100}
            />
            {errors.title && <div className="form-error">{errors.title}</div>}
            <div className="form-tip">清晰的标题能让商品更快被发现！</div>
          </div>

          <div className="form-group">
            <label className="form-label">
              商品描述 <span className="required">*</span>
            </label>
            <textarea
              className={`form-textarea ${errors.description ? 'error' : ''}`}
              placeholder="请详细描述商品的外观、成色、购买时间等信息（最多1000字）"
              value={formData.description}
              onChange={(e) => handleInputChange('description', e.target.value)}
              maxLength={1000}
              rows={6}
            />
            {errors.description && <div className="form-error">{errors.description}</div>}
            <div className="form-tip">
              已输入 {formData.description.length}/1000 字
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">
              商品价格 <span className="required">*</span>
            </label>
            <div className="price-input-wrapper">
              <span className="price-prefix">¥</span>
              <input
                type="number"
                className={`form-input price-input ${errors.price ? 'error' : ''}`}
                placeholder="0.00"
                value={formData.price}
                onChange={(e) => handleInputChange('price', e.target.value)}
                min="0"
                step="0.01"
              />
            </div>
            {errors.price && <div className="form-error">{errors.price}</div>}
            <div className="form-tip">建议设置合理的价格，更容易成交！</div>
          </div>

          <div className="form-group">
            <label className="form-label">
              商品分类 <span className="required">*</span>
            </label>
            <select
              className={`form-select ${errors.categoryId ? 'error' : ''}`}
              value={formData.categoryId || ''}
              onChange={(e) => handleInputChange('categoryId', parseInt(e.target.value, 10))}
            >
              <option value="">请选择分类</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
            {errors.categoryId && <div className="form-error">{errors.categoryId}</div>}
          </div>

          <div className="form-group">
            <label className="form-label">
              商品图片 <span className="required">*</span>
            </label>
            <div className="image-upload-wrapper">
              <div className="image-list">
                {formData.images.map((url, index) => (
                  <div key={index} className="image-item">
                    <img src={url} alt={`商品图片${index + 1}`} />
                    <button
                      className="image-remove-btn"
                      onClick={() => handleRemoveImage(index)}
                    >
                      ×
                    </button>
                  </div>
                ))}

                {uploadingImages.map((_, index) => (
                  <div key={`uploading-${index}`} className="image-item uploading">
                    <div className="uploading-spinner">⏳</div>
                    <div className="uploading-text">上传中...</div>
                  </div>
                ))}

                {formData.images.length + uploadingImages.length < 9 && (
                  <label className="image-upload-btn">
                    <input
                      type="file"
                      accept="image/*"
                      multiple
                      onChange={handleImageUpload}
                      style={{ display: 'none' }}
                    />
                    <div className="upload-icon">+</div>
                    <div className="upload-text">上传图片</div>
                  </label>
                )}
              </div>
              {errors.images && <div className="form-error">{errors.images}</div>}
              <div className="form-tip">
                最多上传9张图片，每张不超过5MB，支持 JPG、PNG 格式
              </div>
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">商品标签（可选）</label>
            <div className="tag-list">
              {tags.map((tag) => (
                <button
                  key={tag.id}
                  className={`tag-item ${formData.tagIds.includes(tag.id) ? 'active' : ''}`}
                  onClick={() => handleToggleTag(tag.id)}
                >
                  {tag.name}
                </button>
              ))}
            </div>
            <div className="form-tip">选择合适的标签，让商品更容易被搜索到！</div>
          </div>

          <div className="form-actions">
            <button
              className="btn-cancel"
              onClick={() => navigate('/')}
              disabled={loading}
            >
              取消
            </button>
            <button
              className="btn-submit"
              onClick={handleSubmit}
              disabled={loading}
            >
              {loading ? '⏳ 发布中...' : '🚀 立即发布'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Publish;
