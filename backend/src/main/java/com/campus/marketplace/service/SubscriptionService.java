package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateSubscriptionRequest;
import com.campus.marketplace.common.dto.response.SubscriptionResponse;
import com.campus.marketplace.common.entity.Goods;

import java.util.List;

/**
 * 订阅服务接口
 *
 * 提供关键词订阅/取消与订阅列表查询、触发通知
 *
 * @author BaSui
 * @date 2025-10-27
 */
public interface SubscriptionService {

    Long subscribe(CreateSubscriptionRequest request);

    void unsubscribe(Long subscriptionId);

    List<SubscriptionResponse> listMySubscriptions();

    void notifySubscribersOnGoodsApproved(Goods goods);
}
