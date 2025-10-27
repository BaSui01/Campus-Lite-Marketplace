package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.FollowResponse;
import com.campus.marketplace.common.entity.Goods;

import java.util.List;

public interface FollowService {

    void followSeller(Long sellerId);

    void unfollowSeller(Long sellerId);

    List<FollowResponse> listFollowings();

    void notifyFollowersOnGoodsApproved(Goods goods);
}
