/**
 * ReviewCreate - 评价发布页
 * @author BaSui 😎
 * @description 发布订单评价，支持星级评分、文字评价、图片上传
 */

import React, { useState, useMemo } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { StarRating, ImageUpload } from '@campus/shared/components';
import { useReviewStore } from '../../store/useReviewStore';
import type { Order } from '@campus/shared/api/models';
import './Create.css';

/**
 * 评价发布页
 */
const ReviewCreate: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { createReview, loading } = useReviewStore();

  // 从路由状态获取订单信息
  const order = location.state?.order as Order | undefined;

  // 表单状态
  const [rating, setRating] = useState(0);
  const [content, setContent] = useState('');
  const [images, setImages] = useState<string[]>([]);
  const [isAnonymous, setIsAnonymous] = useState(false);

  // 错误提示状态
  const [error, setError] = useState<string | null>(null);

  // 字数统计
  const contentLength = useMemo(() => content.length, [content]);

  // ==================== 验证逻辑 ====================

  /**
   * 验证表单
   */
  const validateForm = (): boolean => {
    // 验证星级
    if (rating === 0) {
      setError('请选择星级评分');
      return false;
    }

    // 验证内容长度
    if (content.trim().length < 10) {
      setError('评价内容至少10个字');
      return false;
    }

    if (content.length > 500) {
      setError('评价内容不能超过500字');
      return false;
    }

    setError(null);
    return true;
  };

  // ==================== 事件处理 ====================

  /**
   * 处理星级变化
   */
  const handleRatingChange = (newRating: number) => {
    setRating(newRating);
    setError(null);
  };

  /**
   * 处理内容变化
   */
  const handleContentChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const value = e.target.value;
    // 限制最大长度
    if (value.length <= 500) {
      setContent(value);
      setError(null);
    }
  };

  /**
   * 处理图片上传
   */
  const handleImagesChange = (newImages: string[]) => {
    setImages(newImages);
  };

  /**
   * 处理提交
   */
  const handleSubmit = async () => {
    // 验证表单
    if (!validateForm()) {
      return;
    }

    if (!order) {
      setError('订单信息不存在');
      return;
    }

    try {
      // 调用接口创建评价
      await createReview({
        orderId: order.id!,
        rating,
        content: content.trim(),
        images,
        isAnonymous,
      });

      // 提交成功，跳转到订单列表
      navigate('/orders', { replace: true });
    } catch (err: any) {
      setError(err.message || '提交失败，请重试');
    }
  };

  /**
   * 处理取消
   */
  const handleCancel = () => {
    navigate(-1);
  };

  // ==================== 渲染逻辑 ====================

  // 订单信息不存在
  if (!order) {
    return (
      <div className="review-create">
        <div className="review-create__error">
          <p>订单信息不存在</p>
          <button onClick={handleCancel} className="review-create__btn">
            返回
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="review-create">
      <div className="review-create__container">
        {/* 页面标题 */}
        <h1 className="review-create__title">发布评价</h1>

        {/* 订单商品信息 */}
        <div className="review-create__order-info">
          <img
            src={order.goods?.imageUrl || '/placeholder.png'}
            alt={order.goods?.title}
            className="review-create__goods-image"
          />
          <div className="review-create__goods-details">
            <h3 className="review-create__goods-title">{order.goods?.title}</h3>
            <p className="review-create__goods-price">¥{order.goods?.price?.toFixed(2)}</p>
            <p className="review-create__order-no">订单号：{order.orderNo}</p>
          </div>
        </div>

        {/* 评分区域 */}
        <div className="review-create__section">
          <label className="review-create__label">
            商品评分 <span className="review-create__required">*</span>
          </label>
          <div className="review-create__rating">
            <StarRating
              value={rating}
              onChange={handleRatingChange}
              size="large"
              showValue
            />
          </div>
        </div>

        {/* 评价内容 */}
        <div className="review-create__section">
          <label className="review-create__label">
            评价内容 <span className="review-create__required">*</span>
          </label>
          <textarea
            className="review-create__textarea"
            placeholder="分享您的购买体验，让更多人了解这个商品吧~（至少10个字）"
            value={content}
            onChange={handleContentChange}
            maxLength={500}
          />
          <div className="review-create__char-count">
            {contentLength} / 500
          </div>
        </div>

        {/* 图片上传 */}
        <div className="review-create__section">
          <label className="review-create__label">上传图片（选填）</label>
          <ImageUpload
            action="/api/upload"
            value={images}
            onChange={handleImagesChange}
            maxCount={9}
            multiple
            maxSize={5 * 1024 * 1024}
            tip="最多9张，单张不超过5MB，支持JPG、PNG格式"
          />
        </div>

        {/* 匿名选项 */}
        <div className="review-create__section">
          <label className="review-create__checkbox">
            <input
              type="checkbox"
              checked={isAnonymous}
              onChange={(e) => setIsAnonymous(e.target.checked)}
            />
            <span>匿名评价</span>
          </label>
        </div>

        {/* 错误提示 */}
        {error && (
          <div className="review-create__error-message">
            {error}
          </div>
        )}

        {/* 操作按钮 */}
        <div className="review-create__actions">
          <button
            className="review-create__btn review-create__btn--cancel"
            onClick={handleCancel}
            disabled={loading}
          >
            取消
          </button>
          <button
            className="review-create__btn review-create__btn--submit"
            onClick={handleSubmit}
            disabled={loading}
          >
            {loading ? '提交中...' : '提交评价'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ReviewCreate;
