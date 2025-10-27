package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.PointsLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 积分流水数据访问接口
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Repository
public interface PointsLogRepository extends JpaRepository<PointsLog, Long> {

    /**
     * 查询用户积分流水
     */
    Page<PointsLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
