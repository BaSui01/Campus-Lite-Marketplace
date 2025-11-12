package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 活动报名数据访问接口
 * 
 * @author BaSui 😎
 * @date 2025-11-11
 */
@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    /**
     * 检查用户是否已报名
     */
    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    /**
     * 查询用户的活动报名记录
     */
    Optional<EventRegistration> findByEventIdAndUserId(Long eventId, Long userId);

    /**
     * 查询用户报名的所有活动
     */
    List<EventRegistration> findByUserId(Long userId);

    /**
     * 统计活动的报名人数
     */
    long countByEventId(Long eventId);
}
