package com.campus.marketplace.common.schedule;

import com.campus.marketplace.common.entity.PrivacyRequest;
import com.campus.marketplace.common.entity.User;
import com.campus.marketplace.common.enums.PrivacyRequestStatus;
import com.campus.marketplace.common.enums.PrivacyRequestType;
import com.campus.marketplace.common.enums.UserStatus;
import com.campus.marketplace.repository.PrivacyRequestRepository;
import com.campus.marketplace.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.EntityManager;

/**
 * 隐私清理定时任务
 *
 * 1) 执行到期的“删除账号”请求：将用户状态置为 DELETED 并记录 deletedAt
 * 2) 对已注销超过30天的账号进行清理：优先物理删除；失败则匿名化敏感信息
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.scheduling.privacy.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class PrivacyCleanupScheduler {

    private final PrivacyRequestRepository privacyRequestRepository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    private static final int PURGE_AFTER_DAYS = 30;

    /**
     * 每小时巡检一次
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void processScheduledAccountDeletions() {
        LocalDateTime now = LocalDateTime.now();
        List<PrivacyRequest> due = privacyRequestRepository.findByTypeAndStatusInAndScheduledAtBefore(
                PrivacyRequestType.DELETE,
                List.of(PrivacyRequestStatus.PENDING, PrivacyRequestStatus.PROCESSING),
                now
        );
        for (PrivacyRequest pr : due) {
            userRepository.findById(pr.getUserId()).ifPresent(user -> {
                if (user.getStatus() != UserStatus.DELETED) {
                    user.softDelete();
                    userRepository.save(user);
                    log.info("用户已软删除 userId={}", user.getId());
                }
                pr.setStatus(PrivacyRequestStatus.COMPLETED);
                pr.setCompletedAt(now);
                privacyRequestRepository.save(pr);
            });
        }
        if (!due.isEmpty()) {
            log.info("处理到期隐私删除请求 count={} ", due.size());
        }
    }

    /**
     * 每天凌晨3点清理已注销超过30天的账号
     */
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void purgeDeletedAccounts() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(PURGE_AFTER_DAYS);
        // 简化：全量遍历筛选，规模可控时可接受；规模大时请改为派生查询
        var candidates = userRepository.findAll().stream()
                .filter(u -> u.getStatus() == UserStatus.DELETED && u.getDeletedAt() != null && u.getDeletedAt().isBefore(cutoff))
                .toList();
        int purged = 0;
        int anonymized = 0;
        for (User u : candidates) {
            try {
                // 先解除角色关系，尽量减少外键约束
                if (u.getRoles() != null) {
                    u.getRoles().clear();
                    userRepository.save(u);
                }
                // 使用 EntityManager 进行物理删除，绕过软删除仓库
                User managed = entityManager.contains(u) ? u : entityManager.merge(u);
                entityManager.remove(managed);
                purged++;
                log.info("物理删除已注销用户成功 userId={}", u.getId());
            } catch (Exception ex) {
                // 出现外键约束等问题，退化为匿名化处理
                anonymizeUser(u);
                anonymized++;
                log.warn("物理删除失败，已改为匿名化 userId={} err={}", u.getId(), ex.getMessage());
            }
        }
        if (purged > 0 || anonymized > 0) {
            log.info("账号清理完成 purged={} anonymized={} ", purged, anonymized);
        }
    }

    private void anonymizeUser(User u) {
        u.setUsername("deleted_" + u.getId());
        u.setEmail(null);
        u.setPhone(null);
        u.setAvatar(null);
        u.setStudentId(null);
        u.setNickname("已注销用户");
        u.setPassword("{noop}invalid");
        userRepository.save(u);
    }
}
