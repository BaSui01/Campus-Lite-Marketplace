/**
 * 订单创建页 🛒
 * @author BaSui 😎
 * @description 确认商品信息、填写收货地址、创建订单
 */

import React, { useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation } from '@tanstack/react-query';
import { Input, Button, Skeleton, Empty } from '@campus/shared/components';
import { goodsService, orderService } from '@campus/shared/services';;
import { UpdateOrderDeliveryRequestDeliveryMethodEnum as DeliveryMethodEnum } from '@campus/shared/api/models';
import './OrderCreate.css';

interface AddressInfo {
  receiverName: string;
  receiverPhone: string;
  receiverAddress: string;
  note?: string;
}

export const OrderCreate: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const goodsId = Number(searchParams.get('goodsId'));

  const [addressInfo, setAddressInfo] = useState<AddressInfo>({
    receiverName: '',
    receiverPhone: '',
    receiverAddress: '',
    note: '',
  });
  const [deliveryMethod, setDeliveryMethod] = useState<DeliveryMethodEnum>(DeliveryMethodEnum.FaceToFace);

  const [errors, setErrors] = useState<Record<string, string>>({});

  // 获取商品信息
  const { data: goods, isLoading } = useQuery({
    queryKey: ['goods', 'detail', goodsId],
    queryFn: async () => {
      const response = await goodsService.getGoodsDetail(goodsId);
      return response;
    },
    enabled: !!goodsId,
  });

  // 创建订单
  const createOrderMutation = useMutation({
    mutationFn: async () => {
      // 🔧 BaSui: 只传递后端需要的字段（goodsId 和 couponId）
      const response = await orderService.createOrder({
        goodsId,
        // couponId 如需支持优惠券，在此传递
      });
      // 同步保存配送/收货信息（面交也记录方式与备注）
      if (response) {
        try {
          await orderService.updateOrderDelivery(response as string, {
            deliveryMethod,
            receiverName: addressInfo.receiverName || undefined,
            receiverPhone: addressInfo.receiverPhone || undefined,
            receiverAddress: addressInfo.receiverAddress || undefined,
            note: addressInfo.note || undefined,
          });
        } catch (e) {
          console.warn('更新配送信息失败', e);
        }
      }
      return response;
    },
    onSuccess: (orderNo) => {
      // 🎯 直接跳转到支付页面（订单号在响应中）
      navigate(`/payment?orderNo=${orderNo}`);
    },
    onError: (error: any) => {
      const msg = error?.response?.data?.message || error?.message || '创建订单失败，请重试';
      setErrors({ submit: msg });
    },
  });

  // 表单验证
  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (deliveryMethod === DeliveryMethodEnum.Express) {
      if (!addressInfo.receiverName.trim()) {
        newErrors.receiverName = '请输入收货人姓名';
      }
      if (!addressInfo.receiverPhone.trim()) {
        newErrors.receiverPhone = '请输入收货人手机号';
      } else if (!/^1[3-9]\d{9}$/.test(addressInfo.receiverPhone)) {
        newErrors.receiverPhone = '请输入有效的手机号';
      }
      if (!addressInfo.receiverAddress.trim()) {
        newErrors.receiverAddress = '请输入收货地址';
      }
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // 提交订单
  const handleSubmit = () => {
    if (!validateForm()) {
      return;
    }

    createOrderMutation.mutate();
  };

  // Loading状态
  if (isLoading) {
    return (
      <div className="order-create-page">
        <div className="order-create-container">
          <Skeleton type="card" />
        </div>
      </div>
    );
  }

  // 商品不存在
  if (!goods) {
    return (
      <div className="order-create-page">
        <div className="order-create-container">
          <Empty
            icon="❌"
            title="商品不存在"
            description="该商品可能已被删除或下架"
            action={
              <button onClick={() => navigate('/goods')}>
                返回商品列表
              </button>
            }
          />
        </div>
      </div>
    );
  }

  return (
    <div className="order-create-page">
      <div className="order-create-container">
        <h1 className="order-create-title">确认订单</h1>

        {/* 商品信息 */}
        <div className="order-goods-info">
          <h2 className="section-title">商品信息</h2>
          <div className="order-goods-card">
            <img
              src={goods.images?.[0] || '/placeholder.jpg'}
              alt={goods.title}
              className="order-goods-card__image"
            />
            <div className="order-goods-card__info">
              <h3 className="order-goods-card__title">{goods.title}</h3>
              <p className="order-goods-card__desc">{goods.description}</p>
              <div className="order-goods-card__price">¥{goods.price?.toFixed(2)}</div>
            </div>
          </div>
        </div>

        {/* 收货信息 */}
        <div className="order-address-form">
          <h2 className="section-title">收货信息</h2>
          {/* 配送方式 */}
          <div className="form-field">
            <label className="form-label">配送方式</label>
            <div style={{ display: 'flex', gap: 16 }}>
              <label><input type="radio" name="deliveryMethod" checked={deliveryMethod===DeliveryMethodEnum.FaceToFace} onChange={() => setDeliveryMethod(DeliveryMethodEnum.FaceToFace)} /> 面交</label>
              <label><input type="radio" name="deliveryMethod" checked={deliveryMethod===DeliveryMethodEnum.Express} onChange={() => setDeliveryMethod(DeliveryMethodEnum.Express)} /> 快递</label>
            </div>
          </div>
          
          <div className="order-form">
            <div className="form-field">
              <label className="form-label">
                收货人 {deliveryMethod === DeliveryMethodEnum.Express && <span className="form-required">*</span>}
              </label>
              <Input
                size="large"
                placeholder="请输入收货人姓名"
                value={addressInfo.receiverName}
                onChange={(e) => setAddressInfo({ ...addressInfo, receiverName: e.target.value })}
                disabled={deliveryMethod === DeliveryMethodEnum.FaceToFace}
              />
              {errors.receiverName && (
                <div className="form-error">{errors.receiverName}</div>
              )}
            </div>

            <div className="form-field">
              <label className="form-label">
                手机号 {deliveryMethod === DeliveryMethodEnum.Express && <span className="form-required">*</span>}
              </label>
              <Input
                size="large"
                placeholder="请输入收货人手机号"
                value={addressInfo.receiverPhone}
                onChange={(e) => setAddressInfo({ ...addressInfo, receiverPhone: e.target.value })}
                disabled={deliveryMethod === DeliveryMethodEnum.FaceToFace}
              />
              {errors.receiverPhone && (
                <div className="form-error">{errors.receiverPhone}</div>
              )}
            </div>

            <div className="form-field">
              <label className="form-label">
                收货地址 {deliveryMethod === DeliveryMethodEnum.Express && <span className="form-required">*</span>}
              </label>
              <Input
                size="large"
                placeholder="请输入详细的收货地址"
                value={addressInfo.receiverAddress}
                onChange={(e) => setAddressInfo({ ...addressInfo, receiverAddress: e.target.value })}
                disabled={deliveryMethod === DeliveryMethodEnum.FaceToFace}
              />
              {errors.receiverAddress && (
                <div className="form-error">{errors.receiverAddress}</div>
              )}
            </div>

            <div className="form-field">
              <label className="form-label">
                备注 <span className="form-optional">选填</span>
              </label>
              <textarea
                className="form-textarea"
                placeholder="如有特殊需求，请在此说明"
                value={addressInfo.note}
                onChange={(e) => setAddressInfo({ ...addressInfo, note: e.target.value })}
                rows={3}
              />
            </div>
          </div>
        </div>

        {/* 订单摘要 */}
        <div className="order-summary">
          <h2 className="section-title">订单摘要</h2>
          <div className="order-summary-content">
            <div className="order-summary-item">
              <span>商品金额</span>
              <span className="order-summary-value">¥{goods.price?.toFixed(2)}</span>
            </div>
            <div className="order-summary-item order-summary-total">
              <span>应付总额</span>
              <span className="order-summary-total-value">¥{goods.price?.toFixed(2)}</span>
            </div>
          </div>
        </div>

        {/* 提交错误 */}
        {errors.submit && (
          <div className="order-create-error">{errors.submit}</div>
        )}

        {/* 操作按钮 */}
        <div className="order-create-actions">
          <Button
            size="large"
            onClick={() => navigate(-1)}
          >
            取消
          </Button>
          <Button
            type="primary"
            size="large"
            onClick={handleSubmit}
            loading={createOrderMutation.isPending}
          >
            {createOrderMutation.isPending ? '创建中...' : '提交订单'}
          </Button>
        </div>
      </div>
    </div>
  );
};

export default OrderCreate;
