package com.campus.marketplace.service.impl;

import com.campus.marketplace.common.entity.Appeal;
import com.campus.marketplace.common.enums.AppealStatus;
import com.campus.marketplace.repository.AppealRepository;
import com.campus.marketplace.service.AppealPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

/**
 * 申诉权限验证服务实现类
 * 
 * 实现申诉相关的权限验证逻辑
 * 
 * @author BaSui 😎
 * @date 2025-11-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppealPermissionServiceImpl implements AppealPermissionService {

    private final AppealRepository appealRepository;

    // 可编辑的申诉状态
    private static final Set<AppealStatus> EDITABLE_STATUSES = Set.of(
        AppealStatus.PENDING
    );

    // 可取消的申诉状态
    private static final Set<AppealStatus> CANCELLABLE_STATUSES = Set.of(
        AppealStatus.PENDING
    );

    // 可处理的申诉状态
    private static final Set<AppealStatus> HANDLEABLE_STATUSES = Set.of(
        AppealStatus.PENDING,
        AppealStatus.REVIEWING
    );

    @Override
    public boolean canViewAppeal(Long userId, Long appealId) {
        if (userId == null || appealId == null) {
            log.warn("用户ID或申诉ID为空，拒绝访问");
            return false;
        }

        Optional<Appeal> appealOpt = appealRepository.findById(appealId);
        
        if (appealOpt.isEmpty()) {
            log.warn("申诉不存在: {}", appealId);
            return false;
        }

        Appeal appeal = appealOpt.get();
        
        // 用户只能查看自己的申诉
        boolean canView = userId.equals(appeal.getUserId());
        
        if (!canView) {
            log.warn("用户{}尝试查看他人申诉: {}", userId, appealId);
        }
        
        return canView;
    }

    @Override
    public boolean canViewAppealAsAdmin(Long adminUserId, Long appealId) {
        if (adminUserId == null || appealId == null) {
            log.warn("管理员ID或申诉ID为空，拒绝访问");
            return false;
        }

        Optional<Appeal> appealOpt = appealRepository.findById(appealId);
        
        if (appealOpt.isEmpty()) {
            log.warn("申诉不存在: {}", appealId);
            return false;
        }

        // 简化实现：假设调用此方法的用户已经是管理员
        // 实际应用中应该检查用户是否有管理员权限
        log.debug("管理员{}查看申诉: {}", adminUserId, appealId);
        return true;
    }

    @Override
    public boolean canEditAppeal(Long userId, Long appealId) {
        if (userId == null || appealId == null) {
            log.warn("用户ID或申诉ID为空，拒绝编辑");
            return false;
        }

        Optional<Appeal> appealOpt = appealRepository.findById(appealId);
        
        if (appealOpt.isEmpty()) {
            log.warn("申诉不存在: {}", appealId);
            return false;
        }

        Appeal appeal = appealOpt.get();
        
        // 验证所有权
        if (!userId.equals(appeal.getUserId())) {
            log.warn("用户{}尝试编辑他人申诉: {}", userId, appealId);
            return false;
        }

        // 验证状态
        if (!EDITABLE_STATUSES.contains(appeal.getStatus())) {
            log.warn("申诉{}状态为{}，不可编辑", appealId, appeal.getStatus());
            return false;
        }

        return true;
    }

    @Override
    public boolean canCancelAppeal(Long userId, Long appealId) {
        if (userId == null || appealId == null) {
            log.warn("用户ID或申诉ID为空，拒绝取消");
            return false;
        }

        Optional<Appeal> appealOpt = appealRepository.findById(appealId);
        
        if (appealOpt.isEmpty()) {
            log.warn("申诉不存在: {}", appealId);
            return false;
        }

        Appeal appeal = appealOpt.get();
        
        // 验证所有权
        if (!userId.equals(appeal.getUserId())) {
            log.warn("用户{}尝试取消他人申诉: {}", userId, appealId);
            return false;
        }

        // 验证状态
        if (!CANCELLABLE_STATUSES.contains(appeal.getStatus())) {
            log.warn("申诉{}状态为{}，不可取消", appealId, appeal.getStatus());
            return false;
        }

        return true;
    }

    @Override
    public boolean canHandleAppeal(Long adminUserId, Long appealId) {
        if (adminUserId == null || appealId == null) {
            log.warn("管理员ID或申诉ID为空，拒绝处理");
            return false;
        }

        Optional<Appeal> appealOpt = appealRepository.findById(appealId);
        
        if (appealOpt.isEmpty()) {
            log.warn("申诉不存在: {}", appealId);
            return false;
        }

        Appeal appeal = appealOpt.get();
        
        // 验证状态
        if (!HANDLEABLE_STATUSES.contains(appeal.getStatus())) {
            log.warn("申诉{}状态为{}，不可处理", appealId, appeal.getStatus());
            return false;
        }

        // 简化实现：假设调用此方法的用户已经是管理员
        log.debug("管理员{}可以处理申诉: {}", adminUserId, appealId);
        return true;
    }

    @Override
    public boolean hasAppealPermission(Long userId) {
        if (userId == null) {
            log.warn("用户ID为空，无申诉权限");
            return false;
        }

        // 简化实现：默认所有用户都有申诉权限
        // 实际应用中应该检查用户状态、是否被限制等
        return true;
    }

    @Override
    public boolean isWithinAppealLimit(Long userId, int maxCount) {
        if (userId == null) {
            log.warn("用户ID为空，拒绝检查申诉次数");
            return false;
        }

        long currentCount = getUserAppealCount(userId);
        boolean withinLimit = currentCount < maxCount;
        
        if (!withinLimit) {
            log.warn("用户{}本月申诉次数{}已达到或超过限制{}", userId, currentCount, maxCount);
        }
        
        return withinLimit;
    }

    @Override
    public long getUserAppealCount(Long userId) {
        if (userId == null) {
            return 0;
        }

        // 获取本月的申诉次数
        LocalDateTime startOfMonth = LocalDateTime.now()
            .withDayOfMonth(1)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);

        long count = appealRepository.countByUserIdAndCreatedAtAfter(userId, startOfMonth);
        log.debug("用户{}本月申诉次数: {}", userId, count);
        
        return count;
    }
}
