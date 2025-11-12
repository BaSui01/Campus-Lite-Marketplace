/**
 * 发布商品页 📝
 * @author BaSui 😎
 * @description 多步骤表单发布商品
 */

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMutation } from '@tanstack/react-query';
import { goodsService } from '@campus/shared/services';;
import BasicInfoStep from './BasicInfoStep';
import ImageUploadStep from './ImageUploadStep';
import ConfirmStep from './ConfirmStep';
import './Publish.css';

// 商品表单数据类型
export interface GoodsFormData {
  title: string;
  description: string;
  price: number;
  originalPrice?: number;
  categoryId?: number;
  condition: string;
  deliveryMethod: string[];
  images: string[];
  tags: string[];
}

const STEPS = [
  { id: 1, title: '基本信息', icon: '📝' },
  { id: 2, title: '上传图片', icon: '📷' },
  { id: 3, title: '确认发布', icon: '✅' },
];

export const Publish: React.FC = () => {
  const navigate = useNavigate();
  const [currentStep, setCurrentStep] = useState(1);
  const [formData, setFormData] = useState<GoodsFormData>({
    title: '',
    description: '',
    price: 0,
    condition: 'LIGHTLY_USED',
    deliveryMethod: ['MEET'],
    images: [],
    tags: [],
  });

  // 创建商品Mutation
  const createGoodsMutation = useMutation({
    mutationFn: async (data: GoodsFormData) => {
      const response = await goodsService.createGoods({
        title: data.title,
        description: data.description,
        price: data.price,
        originalPrice: data.originalPrice,
        categoryId: data.categoryId,
        condition: data.condition,
        deliveryMethod: data.deliveryMethod.join(','),
        coverImage: data.images[0],
        images: data.images,
        tags: data.tags,
      });
      return response;
    },
    onSuccess: (data) => {
      // 发布成功，跳转到商品详情页
      navigate(`/goods/${data.id}`);
    },
    onError: (error: any) => {
      alert(error?.message || '发布失败，请重试');
    },
  });

  // 更新表单数据
  const handleUpdateFormData = (updates: Partial<GoodsFormData>) => {
    setFormData((prev) => ({ ...prev, ...updates }));
  };

  // 下一步
  const handleNext = () => {
    if (currentStep < STEPS.length) {
      setCurrentStep(currentStep + 1);
    }
  };

  // 上一步
  const handlePrev = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
    }
  };

  // 提交发布
  const handleSubmit = () => {
    // 验证必填字段
    if (!formData.title.trim()) {
      alert('请输入商品标题');
      return;
    }
    if (!formData.description.trim()) {
      alert('请输入商品描述');
      return;
    }
    if (formData.price <= 0) {
      alert('请输入有效的价格');
      return;
    }
    if (formData.images.length === 0) {
      alert('请至少上传一张商品图片');
      return;
    }

    createGoodsMutation.mutate(formData);
  };

  // 取消发布
  const handleCancel = () => {
    if (window.confirm('确定要取消发布吗？已填写的信息将不会保存。')) {
      navigate(-1);
    }
  };

  return (
    <div className="publish-page">
      <div className="publish-container">
        {/* 步骤指示器 */}
        <div className="publish-steps">
          {STEPS.map((step, index) => (
            <div
              key={step.id}
              className={`publish-step ${currentStep === step.id ? 'active' : ''} ${currentStep > step.id ? 'completed' : ''}`}
            >
              <div className="publish-step__number">
                {currentStep > step.id ? '✓' : step.icon}
              </div>
              <div className="publish-step__title">{step.title}</div>
              {index < STEPS.length - 1 && (
                <div className="publish-step__line" />
              )}
            </div>
          ))}
        </div>

        {/* 表单内容 */}
        <div className="publish-content">
          {currentStep === 1 && (
            <BasicInfoStep
              formData={formData}
              onUpdate={handleUpdateFormData}
              onNext={handleNext}
              onCancel={handleCancel}
            />
          )}

          {currentStep === 2 && (
            <ImageUploadStep
              formData={formData}
              onUpdate={handleUpdateFormData}
              onNext={handleNext}
              onPrev={handlePrev}
            />
          )}

          {currentStep === 3 && (
            <ConfirmStep
              formData={formData}
              onSubmit={handleSubmit}
              onPrev={handlePrev}
              isSubmitting={createGoodsMutation.isPending}
            />
          )}
        </div>
      </div>
    </div>
  );
};

export default Publish;
