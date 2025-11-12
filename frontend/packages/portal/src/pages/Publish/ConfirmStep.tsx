/**
 * 确认发布步骤 ✅
 * @author BaSui 😎
 * @description 预览商品信息并确认发布
 */

import React from 'react';
import type { GoodsFormData } from './index';
import './ConfirmStep.css';

interface ConfirmStepProps {
  formData: GoodsFormData;
  onSubmit: () => void;
  onPrev: () => void;
  isSubmitting: boolean;
}

const CONDITION_MAP: Record<string, string> = {
  'BRAND_NEW': '全新',
  'LIKE_NEW': '几乎全新',
  'LIGHTLY_USED': '轻微使用痕迹',
  'WELL_USED': '明显使用痕迹',
  'HEAVILY_USED': '重度使用痕迹',
};

const DELIVERY_MAP: Record<string, string> = {
  'MEET': '校园面交',
  'MAIL': '快递邮寄',
};

export const ConfirmStep: React.FC<ConfirmStepProps> = ({
  formData,
  onSubmit,
  onPrev,
  isSubmitting,
}) => {
  return (
    <div className="confirm-step">
      <h2 className="confirm-step__title">✅ 确认发布</h2>
      
      <div className="confirm-step__preview">
        {/* 商品图片 */}
        <div className="confirm-preview__section">
          <h3 className="confirm-preview__section-title">商品图片</h3>
          <div className="confirm-preview__images">
            {formData.images.map((image, index) => (
              <div key={index} className="confirm-preview__image">
                <img src={image} alt={`商品图片${index + 1}`} />
                {index === 0 && (
                  <div className="confirm-preview__image-badge">封面</div>
                )}
              </div>
            ))}
          </div>
        </div>

        {/* 基本信息 */}
        <div className="confirm-preview__section">
          <h3 className="confirm-preview__section-title">基本信息</h3>
          <div className="confirm-preview__info">
            <div className="confirm-preview__item">
              <div className="confirm-preview__label">商品标题</div>
              <div className="confirm-preview__value">{formData.title}</div>
            </div>

            <div className="confirm-preview__item">
              <div className="confirm-preview__label">商品描述</div>
              <div className="confirm-preview__value confirm-preview__value--multiline">
                {formData.description}
              </div>
            </div>

            <div className="confirm-preview__item">
              <div className="confirm-preview__label">出售价格</div>
              <div className="confirm-preview__value confirm-preview__value--price">
                ¥{formData.price.toFixed(2)}
              </div>
            </div>

            {formData.originalPrice && formData.originalPrice > 0 && (
              <div className="confirm-preview__item">
                <div className="confirm-preview__label">原价</div>
                <div className="confirm-preview__value">
                  ¥{formData.originalPrice.toFixed(2)}
                </div>
              </div>
            )}

            <div className="confirm-preview__item">
              <div className="confirm-preview__label">商品成色</div>
              <div className="confirm-preview__value">
                {CONDITION_MAP[formData.condition] || formData.condition}
              </div>
            </div>

            <div className="confirm-preview__item">
              <div className="confirm-preview__label">交易方式</div>
              <div className="confirm-preview__value">
                {formData.deliveryMethod.map(m => DELIVERY_MAP[m] || m).join('、')}
              </div>
            </div>

            {formData.tags.length > 0 && (
              <div className="confirm-preview__item">
                <div className="confirm-preview__label">商品标签</div>
                <div className="confirm-preview__value">
                  <div className="confirm-preview__tags">
                    {formData.tags.map(tag => (
                      <span key={tag} className="confirm-preview__tag">
                        #{tag}
                      </span>
                    ))}
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* 温馨提示 */}
        <div className="confirm-preview__tips">
          <h4 className="confirm-preview__tips-title">📌 温馨提示</h4>
          <ul className="confirm-preview__tips-list">
            <li>发布后，商品将进入审核流程，通常在24小时内完成</li>
            <li>请确保商品信息真实准确，禁止发布违规内容</li>
            <li>商品发布后可以随时编辑或下架</li>
            <li>保持良好的交易态度，诚信交易</li>
          </ul>
        </div>
      </div>

      {/* 操作按钮 */}
      <div className="confirm-step__actions">
        <button
          type="button"
          className="confirm-step__btn confirm-step__btn--prev"
          onClick={onPrev}
          disabled={isSubmitting}
        >
          ← 上一步
        </button>
        <button
          type="button"
          className="confirm-step__btn confirm-step__btn--submit"
          onClick={onSubmit}
          disabled={isSubmitting}
        >
          {isSubmitting ? '发布中...' : '✨ 确认发布'}
        </button>
      </div>
    </div>
  );
};

export default ConfirmStep;
