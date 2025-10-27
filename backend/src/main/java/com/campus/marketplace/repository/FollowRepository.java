package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerIdAndSellerId(Long followerId, Long sellerId);

    long countBySellerId(Long sellerId);

    List<Follow> findByFollowerId(Long followerId);

    List<Follow> findBySellerId(Long sellerId);
}
