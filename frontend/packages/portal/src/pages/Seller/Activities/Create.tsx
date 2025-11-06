/**
 * 营销活动创建页面 - 创建促销活动！🎁
 * @author BaSui 😎
 * @description 限时折扣、满减优惠、秒杀活动创建表单
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@campus/shared/components';
import {
  marketingService,
  CampaignType,
  DiscountType,
  CreateCampaignRequest,
  CAMPAIGN_TYPE_CONFIG,
} from '@campus/shared/services';
import { useNotificationStore } from '../../../store';
import './Activities.css';

/**
 * 活动创建页面组件
 */
const CreateActivity: React.FC = () => {
  const navigate = useNavigate();
  const toast = useNotificationStore();

  // ==================== 状态管理 ====================

  const [formData, setFormData] = useState<CreateCampaignRequest>({
    campaignName: '',
    campaignType: CampaignType.DISCOUNT,
    startTime: '',
    endTime: '',
    discountConfig: {
      discountType: DiscountType.PERCENTAGE,
      discountValue: 0.8,
    },
    goodsIds: [],
    stockLimit: undefined,
  });

  const [goodsInput, setGoodsInput] = useState('');
  const [submitting, setSubmitting] = useState(false);

  // ==================== 表单处理 ====================

  /**
   * 更新表单字段
   */
  const updateField = (field: keyof CreateCampaignRequest, value: any) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  /**
   * 更新折扣配置
   */
  const updateDiscountConfig = (field: string, value: any) => {
    setFormData(prev => ({
      ...prev,
      discountConfig: {
        ...prev.discountConfig,
        [field]: value,
      },
    }));
  };

  /**
   * 添加商品ID
   */
  const handleAddGoods = () => {
    const ids = goodsInput.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id));
    if (ids.length > 0) {
      setFormData(prev => ({
        ...prev,
        goodsIds: [...new Set([...prev.goodsIds, ...ids])],
      }));
      setGoodsInput('');
    }
  };

  /**
   * 移除商品ID
   */
  const handleRemoveGoods = (id: number) => {
    setFormData(prev => ({
      ...prev,
      goodsIds: prev.goodsIds.filter(gid => gid !== id),
    }));
  };

  /**
   * 表单验证
   */
  const validateForm = (): boolean => {
    if (!formData.campaignName.trim()) {
      toast.error('请输入活动名称！');
      return false;
    }

    if (!formData.startTime || !formData.endTime) {
      toast.error('请选择活动时间！');
      return false;
    }

    if (new Date(formData.startTime) >= new Date(formData.endTime)) {
      toast.error('结束时间必须晚于开始时间！');
      return false;
    }

    if (formData.goodsIds.length === 0) {
      toast.error('请至少添加一个商品！');
      return false;
    }

    if (formData.discountConfig.discountValue <= 0) {
      toast.error('请设置有效的折扣值！');
      return false;
    }

    if (formData.campaignType === CampaignType.FULL_REDUCTION && !formData.discountConfig.threshold) {
      toast.error('满减活动必须设置门槛金额！');
      return false;
    }

    if (formData.campaignType === CampaignType.FLASH_SALE && !formData.stockLimit) {
      toast.error('秒杀活动必须设置库存限制！');
      return false;
    }

    return true;
  };

  /**
   * 提交表单
   */
  const handleSubmit = async () => {
    if (!validateForm()) return;

    setSubmitting(true);

    try {
      await marketingService.createCampaign(formData);
      toast.success('活动创建成功！✅');
      navigate('/seller/activities');
    } catch (err: any) {
      console.error('创建活动失败:', err);
      toast.error(err.response?.data?.message || '创建活动失败!😭');
    } finally {
      setSubmitting(false);
    }
  };

  // ==================== 渲染 ====================

  return (
    <div className="activities-page">
      <div className="activities-container">
        {/* 头部 */}
        <div className="activities-header">
          <button className="back-btn" onClick={() => navigate('/seller/activities')}>
            ← 返回活动列表
          </button>
          <h1 className="activities-header__title">🎁 创建营销活动</h1>
          <p className="activities-header__subtitle">设置促销规则，吸引更多用户</p>
        </div>

        {/* 表单 */}
        <div className="activity-form">
          {/* 基本信息 */}
          <div className="form-section">
            <h2 className="form-section__title">基本信息</h2>
            
            <div className="form-group">
              <label className="form-label">活动名称 *</label>
              <input
                type="text"
                className="form-input"
                placeholder="例如：双11大促"
                value={formData.campaignName}
                onChange={(e) => updateField('campaignName', e.target.value)}
              />
            </div>

            <div className="form-group">
              <label className="form-label">活动类型 *</label>
              <div className="campaign-type-selector">
                {Object.entries(CAMPAIGN_TYPE_CONFIG).map(([type, config]) => (
                  <div
                    key={type}
                    className={`type-card ${formData.campaignType === type ? 'active' : ''}`}
                    onClick={() => updateField('campaignType', type as CampaignType)}
                  >
                    <div className="type-icon">{config.icon}</div>
                    <div className="type-name">{config.name}</div>
                    <div className="type-desc">{config.description}</div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* 活动时间 */}
          <div className="form-section">
            <h2 className="form-section__title">活动时间</h2>
            
            <div className="form-row">
              <div className="form-group">
                <label className="form-label">开始时间 *</label>
                <input
                  type="datetime-local"
                  className="form-input"
                  value={formData.startTime}
                  onChange={(e) => updateField('startTime', e.target.value)}
                />
              </div>

              <div className="form-group">
                <label className="form-label">结束时间 *</label>
                <input
                  type="datetime-local"
                  className="form-input"
                  value={formData.endTime}
                  onChange={(e) => updateField('endTime', e.target.value)}
                />
              </div>
            </div>
          </div>

          {/* 优惠规则 */}
          <div className="form-section">
            <h2 className="form-section__title">优惠规则</h2>
            
            <div className="form-group">
              <label className="form-label">折扣类型 *</label>
              <select
                className="form-select"
                value={formData.discountConfig.discountType}
                onChange={(e) => updateDiscountConfig('discountType', e.target.value)}
              >
                <option value={DiscountType.PERCENTAGE}>百分比折扣（如8折）</option>
                <option value={DiscountType.FIXED_AMOUNT}>固定金额减免（如减10元）</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">折扣值 *</label>
              {formData.discountConfig.discountType === DiscountType.PERCENTAGE ? (
                <input
                  type="number"
                  className="form-input"
                  placeholder="0.8 表示 8折"
                  min="0.1"
                  max="1"
                  step="0.1"
                  value={formData.discountConfig.discountValue}
                  onChange={(e) => updateDiscountConfig('discountValue', parseFloat(e.target.value))}
                />
              ) : (
                <input
                  type="number"
                  className="form-input"
                  placeholder="减免金额（元）"
                  min="1"
                  value={formData.discountConfig.discountValue}
                  onChange={(e) => updateDiscountConfig('discountValue', parseFloat(e.target.value))}
                />
              )}
            </div>

            {formData.campaignType === CampaignType.FULL_REDUCTION && (
              <div className="form-group">
                <label className="form-label">门槛金额 *</label>
                <input
                  type="number"
                  className="form-input"
                  placeholder="满多少元可用（例如：100）"
                  min="0"
                  value={formData.discountConfig.threshold || ''}
                  onChange={(e) => updateDiscountConfig('threshold', parseFloat(e.target.value))}
                />
              </div>
            )}

            {formData.campaignType === CampaignType.FLASH_SALE && (
              <div className="form-group">
                <label className="form-label">库存限制 *</label>
                <input
                  type="number"
                  className="form-input"
                  placeholder="秒杀库存数量"
                  min="1"
                  value={formData.stockLimit || ''}
                  onChange={(e) => updateField('stockLimit', parseInt(e.target.value))}
                />
              </div>
            )}
          </div>

          {/* 参与商品 */}
          <div className="form-section">
            <h2 className="form-section__title">参与商品</h2>
            
            <div className="form-group">
              <label className="form-label">商品ID *</label>
              <div className="goods-input-group">
                <input
                  type="text"
                  className="form-input"
                  placeholder="输入商品ID，多个用逗号分隔"
                  value={goodsInput}
                  onChange={(e) => setGoodsInput(e.target.value)}
                />
                <Button type="primary" onClick={handleAddGoods}>添加</Button>
              </div>
            </div>

            {formData.goodsIds.length > 0 && (
              <div className="goods-list">
                {formData.goodsIds.map(id => (
                  <div key={id} className="goods-tag">
                    商品 #{id}
                    <button className="goods-tag__remove" onClick={() => handleRemoveGoods(id)}>
                      ×
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* 提交按钮 */}
          <div className="form-actions">
            <Button type="default" size="large" onClick={() => navigate('/seller/activities')}>
              取消
            </Button>
            <Button 
              type="primary" 
              size="large" 
              onClick={handleSubmit}
              disabled={submitting}
            >
              {submitting ? '创建中...' : '创建活动'}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CreateActivity;
