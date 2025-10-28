package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.request.CreateSubscriptionRequest;
import com.campus.marketplace.common.dto.response.SubscriptionResponse;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.entity.Subscription;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.NotificationType;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.SubscriptionRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.NotificationService;
import com.campus.marketplace.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * 订阅服务实现
 *
 * 提供关键词订阅/取消与匹配触发通知
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long subscribe(CreateSubscriptionRequest request) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String keyword = normalize(request.keyword());
        Long campusId = request.campusId() != null ? request.campusId() : user.getCampusId();

        return subscriptionRepository.findByUserIdAndKeywordIgnoreCaseAndCampusId(currentUserId, keyword, campusId)
                .map(existing -> {
                    if (Boolean.TRUE.equals(existing.getActive())) {
                        throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "已订阅该关键词");
                    }
                    existing.setActive(true);
                    Subscription saved = subscriptionRepository.save(existing);
                    log.info("重新激活订阅 userId={}, subscriptionId={}", currentUserId, saved.getId());
                    return saved.getId();
                })
                .orElseGet(() -> {
                    Subscription subscription = Subscription.builder()
                            .userId(currentUserId)
                            .keyword(keyword)
                            .campusId(campusId)
                            .active(true)
                            .build();
                    subscriptionRepository.save(subscription);
                    log.info("创建订阅成功 userId={}, keyword={}, campusId={}", currentUserId, keyword, campusId);
                    return subscription.getId();
                });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unsubscribe(Long subscriptionId) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SUBSCRIPTION_NOT_FOUND));
        if (!subscription.getUserId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        subscriptionRepository.delete(subscription);
        log.info("取消订阅成功 userId={}, subscriptionId={}", currentUserId, subscriptionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> listMySubscriptions() {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        return subscriptionRepository.findByUserId(currentUserId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public void notifySubscribersOnGoodsApproved(Goods goods) {
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        if (subscriptions.isEmpty()) {
            return;
        }
        String lowerTitle = goods.getTitle().toLowerCase(Locale.ROOT);
        String lowerDescription = goods.getDescription() != null
                ? goods.getDescription().toLowerCase(Locale.ROOT)
                : "";
        subscriptions.stream()
                .filter(Subscription::getActive)
                .filter(sub -> sub.getCampusId() == null || sub.getCampusId().equals(goods.getCampusId()))
                .filter(sub -> lowerTitle.contains(sub.getKeyword()) || lowerDescription.contains(sub.getKeyword()))
                .forEach(sub -> notificationService.sendNotification(
                        sub.getUserId(),
                        NotificationType.SUBSCRIPTION_MATCHED_GOODS,
                        "有新的商品符合你的订阅",
                        String.format("《%s》符合你订阅的关键词“%s”", goods.getTitle(), sub.getKeyword()),
                        goods.getId(),
                        "GOODS",
                        "/goods/" + goods.getId()
                ));
    }

    private String normalize(String keyword) {
        return keyword.trim().toLowerCase(Locale.ROOT);
    }

    private SubscriptionResponse toResponse(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .keyword(subscription.getKeyword())
                .campusId(subscription.getCampusId())
                .active(subscription.getActive())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }
}
