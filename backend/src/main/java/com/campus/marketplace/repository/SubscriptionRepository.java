package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUserIdAndKeywordIgnoreCaseAndCampusId(Long userId, String keyword, Long campusId);

    List<Subscription> findByUserId(Long userId);
}
