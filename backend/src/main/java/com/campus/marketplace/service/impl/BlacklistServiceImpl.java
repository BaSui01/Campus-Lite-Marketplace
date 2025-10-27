package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.dto.response.UserProfileResponse;
import com.campus.marketplace.common.entity.Blacklist;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.exception.BusinessException;
import com.campus.marketplace.common.exception.ErrorCode;
import com.campus.marketplace.common.utils.RedisUtil;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.BlacklistRepository;
import com.campus.marketplace.repository.UserRepository;
import com.campus.marketplace.service.BlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 黑名单服务实现类
 *
 * 实现拉黑、解除拉黑、查询黑名单等功能
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistServiceImpl implements BlacklistService {

    private final BlacklistRepository blacklistRepository;
    private final UserRepository userRepository;
    private final RedisUtil redisUtil;

    /**
     * Redis 键前缀
     */
    private static final String BLACKLIST_KEY_PREFIX = "blacklist:";

    /**
     * 添加到黑名单
     *
     * 🚫 拉黑用户后，对方无法给你发送消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addToBlacklist(Long blockedUserId, String reason) {
        log.debug("添加黑名单：blockedUserId={}, reason={}", blockedUserId, reason);

        // 1. 获取当前用户
        String username = SecurityUtil.getCurrentUsername();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 不能拉黑自己
        if (currentUser.getId().equals(blockedUserId)) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "不能拉黑自己");
        }

        // 3. 验证被拉黑用户存在
        User blockedUser = userRepository.findById(blockedUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在"));
        // 使用被拉黑人信息（用于审计/日志），避免未使用变量告警
        log.debug("校验被拉黑用户存在：id={}, username={}", blockedUser.getId(), blockedUser.getUsername());

        // 4. 检查是否已拉黑
        if (blacklistRepository.existsByUserIdAndBlockedUserId(currentUser.getId(), blockedUserId)) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "已在黑名单中");
        }

        // 5. 添加到黑名单
        Blacklist blacklist = Blacklist.builder()
                .userId(currentUser.getId())
                .blockedUserId(blockedUserId)
                .reason(reason)
                .build();
        blacklistRepository.save(blacklist);

        // 6. 更新 Redis 缓存
        String cacheKey = BLACKLIST_KEY_PREFIX + currentUser.getId();
        redisUtil.sAdd(cacheKey, blockedUserId);
        redisUtil.expire(cacheKey, 7, TimeUnit.DAYS);

        log.info("添加黑名单成功：userId={}, blockedUserId={}", currentUser.getId(), blockedUserId);
    }

    /**
     * 从黑名单移除
     *
     * ✅ 解除拉黑后，对方可以正常给你发送消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeFromBlacklist(Long blockedUserId) {
        log.debug("移除黑名单：blockedUserId={}", blockedUserId);

        // 1. 获取当前用户
        String username = SecurityUtil.getCurrentUsername();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 检查是否在黑名单中
        if (!blacklistRepository.existsByUserIdAndBlockedUserId(currentUser.getId(), blockedUserId)) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "不在黑名单中");
        }

        // 3. 从黑名单移除
        blacklistRepository.deleteByUserIdAndBlockedUserId(currentUser.getId(), blockedUserId);

        // 4. 更新 Redis 缓存
        String cacheKey = BLACKLIST_KEY_PREFIX + currentUser.getId();
        redisUtil.sRemove(cacheKey, blockedUserId);

        log.info("移除黑名单成功：userId={}, blockedUserId={}", currentUser.getId(), blockedUserId);
    }

    /**
     * 查询黑名单列表
     *
     * 📋 返回当前用户拉黑的所有用户
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UserProfileResponse> listBlacklist(int page, int size) {
        log.debug("查询黑名单列表：page={}, size={}", page, size);

        // 1. 获取当前用户
        String username = SecurityUtil.getCurrentUsername();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 分页查询黑名单
        Pageable pageable = PageRequest.of(page, size);
        Page<Blacklist> blacklistPage = blacklistRepository.findByUserIdOrderByCreatedAtDesc(
                currentUser.getId(), pageable);

        // 3. 转换为用户信息
        return blacklistPage.map(blacklist -> {
            User blockedUser = userRepository.findById(blacklist.getBlockedUserId()).orElse(null);
            if (blockedUser == null) {
                return null;
            }
            return UserProfileResponse.builder()
                    .id(blockedUser.getId())
                    .username(blockedUser.getUsername())
                    .avatar(blockedUser.getAvatar())
                    .build();
        });
    }

    /**
     * 检查是否拉黑了某人
     *
     * 💡 优先从 Redis 读取，提升性能
     */
    @Override
    public boolean isBlocked(Long blockedUserId) {
        // 1. 获取当前用户
        String username = SecurityUtil.getCurrentUsername();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 先从 Redis 查询
        String cacheKey = BLACKLIST_KEY_PREFIX + currentUser.getId();
        Boolean inCache = redisUtil.sIsMember(cacheKey, blockedUserId);
        if (inCache != null && inCache) {
            return true;
        }

        // 3. Redis 中没有，查询数据库
        boolean isBlocked = blacklistRepository.existsByUserIdAndBlockedUserId(
                currentUser.getId(), blockedUserId);

        // 4. 如果在黑名单中，更新 Redis
        if (isBlocked) {
            redisUtil.sAdd(cacheKey, blockedUserId);
            redisUtil.expire(cacheKey, 7, TimeUnit.DAYS);
        }

        return isBlocked;
    }

    /**
     * 检查两个用户之间是否存在拉黑关系
     *
     * 🔍 检查双向拉黑（任一方拉黑对方都算）
     */
    @Override
    public boolean isBlockedBetween(Long userId1, Long userId2) {
        return blacklistRepository.existsMutualBlock(userId1, userId2);
    }
}
