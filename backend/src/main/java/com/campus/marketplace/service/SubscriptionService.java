package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.request.CreateSubscriptionRequest;
import com.campus.marketplace.common.dto.response.SubscriptionResponse;
import com.campus.marketplace.common.entity.Goods;

import java.util.List;

public interface SubscriptionService {

    Long subscribe(CreateSubscriptionRequest request);

    void unsubscribe(Long subscriptionId);

    List<SubscriptionResponse> listMySubscriptions();

    void notifySubscribersOnGoodsApproved(Goods goods);
}
