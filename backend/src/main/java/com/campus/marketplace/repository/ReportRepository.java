package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Report;
import com.campus.marketplace.common.enums.ReportStatus;
import com.campus.marketplace.common.enums.ReportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 举报 Repository
 * 
 * @author BaSui
 * @date 2025-10-27
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    /**
     * 查询指定状态的举报列表
     */
    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    /**
     * 查询用户的举报记录
     */
    Page<Report> findByReporterId(Long reporterId, Pageable pageable);

    /**
     * 查询针对特定对象的举报
     */
    Page<Report> findByTargetTypeAndTargetId(ReportType targetType, Long targetId, Pageable pageable);

    /**
     * 统计用户今日举报次数
     */
    long countByReporterIdAndCreatedAtAfter(Long reporterId, java.time.LocalDateTime startTime);
}
