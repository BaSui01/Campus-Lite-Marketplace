package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.response.FollowResponse;
import com.campus.marketplace.common.entity.Follow;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.NotificationType;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.FollowRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.FollowService;
import com.campus.marketplace.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 关注服务实现
 *
 * 提供关注/取消关注、关注列表查询及商品上架通知
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void followSeller(Long sellerId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        if (Objects.equals(currentUserId, sellerId)) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "不能关注自己");
        }
        userRepository.findById(sellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        followRepository.findByFollowerIdAndSellerId(currentUserId, sellerId)
                .ifPresent(follow -> {
                    throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "已关注该卖家");
                });
        Follow follow = Follow.builder()
                .followerId(currentUserId)
                .sellerId(sellerId)
                .build();
        followRepository.save(follow);
        log.info("关注卖家成功 followerId={}, sellerId={}", currentUserId, sellerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollowSeller(Long sellerId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Follow follow = followRepository.findByFollowerIdAndSellerId(currentUserId, sellerId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FOLLOW_NOT_FOUND));
        followRepository.delete(follow);
        log.info("取消关注成功 followerId={}, sellerId={}", currentUserId, sellerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FollowResponse> listFollowings() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        List<Follow> follows = followRepository.findByFollowerId(currentUserId);
        return follows.stream()
                .map(follow -> {
                    User seller = userRepository.findById(follow.getSellerId())
                            .orElse(null);
                    return FollowResponse.builder()
                            .sellerId(follow.getSellerId())
                            .sellerName(seller != null ? seller.getUsername() : null)
                            .sellerAvatar(seller != null ? seller.getAvatar() : null)
                            .followedAt(follow.getCreatedAt())
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public void notifyFollowersOnGoodsApproved(Goods goods) {
        List<Follow> followers = followRepository.findBySellerId(goods.getSellerId());
        if (followers.isEmpty()) {
            return;
        }
        String title = "你关注的卖家发布了新商品";
        String content = String.format("卖家上架了《%s》，快去看看吧～", goods.getTitle());
        followers.forEach(follow -> notificationService.sendNotification(
                follow.getFollowerId(),
                NotificationType.FOLLOW_SELLER_NEW_GOODS,
                title,
                content,
                goods.getId(),
                "GOODS",
                "/goods/" + goods.getId()
        ));
        log.info("通知关注者成功 goodsId={}, followers={}", goods.getId(), followers.size());
    }
}
