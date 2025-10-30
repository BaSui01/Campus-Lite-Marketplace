package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.response.FollowResponse;
import com.campus.marketplace.common.entity.Goods;

import java.util.List;

/**
 * 关注服务接口
 *
 * 提供关注/取消关注与关注列表查询
 *
 * @author BaSui
 * @date 2025-10-27
 */
public interface FollowService {

    void followSeller(Long sellerId);

    void unfollowSeller(Long sellerId);

    List<FollowResponse> listFollowings();

    void notifyFollowersOnGoodsApproved(Goods goods);
}
