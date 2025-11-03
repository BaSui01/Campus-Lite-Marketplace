package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.entity.UserFollow;
import com.campus.marketplace.common.enums.NotificationType;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.repository.UserFollowRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.NotificationService;
import com.campus.marketplace.service.UserFollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户关注服务实现类
 * 
 * 实现用户关注功能：关注、取消关注、获取关注列表
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserFollowServiceImpl implements UserFollowService {

    private final UserFollowRepository userFollowRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void followUser(Long followerId, Long followingId) {
        log.info("关注用户: followerId={}, followingId={}", followerId, followingId);

        // 不能关注自己
        if (followerId.equals(followingId)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "不能关注自己");
        }

        // 检查被关注者是否存在
        userRepository.findById(followingId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 检查是否已关注
        if (userFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "已经关注过了");
        }

        // 创建关注记录
        UserFollow userFollow = UserFollow.builder()
            .followerId(followerId)
            .followingId(followingId)
            .build();
        userFollowRepository.save(userFollow);

        // 发送通知给被关注者
        try {
            User follower = userRepository.findById(followerId).orElseThrow();
            notificationService.sendNotification(
                followingId,
                NotificationType.SYSTEM_ANNOUNCEMENT,
                "新增粉丝",
                "用户 " + follower.getUsername() + " 关注了你",
                followerId,
                "USER",
                "/users/" + followerId
            );
        } catch (Exception e) {
            log.error("发送关注通知失败: followerId={}, followingId={}", followerId, followingId, e);
        }

        log.info("关注用户成功: followerId={}, followingId={}", followerId, followingId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollowUser(Long followerId, Long followingId) {
        log.info("取消关注用户: followerId={}, followingId={}", followerId, followingId);

        // 查找关注记录
        UserFollow userFollow = userFollowRepository.findByFollowerIdAndFollowingId(followerId, followingId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "关注记录不存在"));

        // 删除关注记录
        userFollowRepository.delete(userFollow);

        log.info("取消关注用户成功: followerId={}, followingId={}", followerId, followingId);
    }

    @Override
    public List<User> getFollowingList(Long userId) {
        log.info("获取我关注的用户列表: userId={}", userId);
        List<UserFollow> follows = userFollowRepository.findByFollowerId(userId);
        return follows.stream()
            .map(UserFollow::getFollowingId)
            .map(id -> userRepository.findById(id).orElse(null))
            .filter(user -> user != null)
            .collect(Collectors.toList());
    }

    @Override
    public List<User> getFollowerList(Long userId) {
        log.info("获取我的粉丝列表: userId={}", userId);
        List<UserFollow> follows = userFollowRepository.findByFollowingId(userId);
        return follows.stream()
            .map(UserFollow::getFollowerId)
            .map(id -> userRepository.findById(id).orElse(null))
            .filter(user -> user != null)
            .collect(Collectors.toList());
    }

    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        return userFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Override
    public long getFollowingCount(Long userId) {
        return userFollowRepository.countByFollowerId(userId);
    }

    @Override
    public long getFollowerCount(Long userId) {
        return userFollowRepository.countByFollowingId(userId);
    }
}
