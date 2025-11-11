package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Event;
import com.campus.marketplace.common.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 活动数据访问接口
 * 
 * @author BaSui 😎
 * @date 2025-11-11
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * 根据状态查询活动列表
     */
    Page<Event> findByStatus(EventStatus status, Pageable pageable);

    /**
     * 根据校区ID查询活动列表
     */
    Page<Event> findByCampusId(Long campusId, Pageable pageable);

    /**
     * 根据状态和校区ID查询活动列表
     */
    Page<Event> findByStatusAndCampusId(EventStatus status, Long campusId, Pageable pageable);
}
