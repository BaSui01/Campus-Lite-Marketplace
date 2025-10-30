package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 关注关系仓库
 *
 * 提供关注/取关与统计相关的持久化操作
 *
 * @author BaSui
 * @date 2025-10-27
 */
@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerIdAndSellerId(Long followerId, Long sellerId);

    long countBySellerId(Long sellerId);

    List<Follow> findByFollowerId(Long followerId);

    List<Follow> findBySellerId(Long sellerId);
}
