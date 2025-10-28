package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 关键词订阅仓库
 *
 * 按用户/关键词/校区维度存储订阅关系
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUserIdAndKeywordIgnoreCaseAndCampusId(Long userId, String keyword, Long campusId);

    List<Subscription> findByUserId(Long userId);
}
