package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.MerchantDashboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 商家数据看板数据访问接口
 *
 * @author BaSui 😎
 * @since 2025-11-04
 */
@Repository
public interface MerchantDashboardRepository extends JpaRepository<MerchantDashboard, Long> {

    /**
     * 查询指定商家指定日期的数据
     */
    Optional<MerchantDashboard> findByMerchantIdAndStatDate(Long merchantId, LocalDate statDate);

    /**
     * 查询指定商家时间范围内的数据
     */
    @Query("SELECT md FROM MerchantDashboard md WHERE md.merchantId = :merchantId " +
           "AND md.statDate BETWEEN :startDate AND :endDate " +
           "ORDER BY md.statDate ASC")
    List<MerchantDashboard> findByMerchantIdAndDateRange(
            @Param("merchantId") Long merchantId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 判断指定商家指定日期的数据是否存在
     */
    boolean existsByMerchantIdAndStatDate(Long merchantId, LocalDate statDate);

    /**
     * 删除指定商家的所有数据
     */
    void deleteByMerchantId(Long merchantId);
}
