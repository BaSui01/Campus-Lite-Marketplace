package com.campus.marketplace.repository;

import com.campus.marketplace.common.entity.Logistics;
import com.campus.marketplace.common.enums.LogisticsCompany;
import com.campus.marketplace.common.enums.LogisticsStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 物流信息Repository
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Repository
public interface LogisticsRepository extends JpaRepository<Logistics, Long> {

    /**
     * 根据订单ID查找物流信息
     *
     * @param orderId 订单ID
     * @return 物流信息（可选）
     */
    Optional<Logistics> findByOrderId(Long orderId);

    /**
     * 根据快递单号查找物流信息
     *
     * @param trackingNumber 快递单号
     * @return 物流信息（可选）
     */
    Optional<Logistics> findByTrackingNumber(String trackingNumber);

    /**
     * 查找待同步的物流信息
     * <p>
     * 查询条件：最后同步时间早于指定阈值，且状态为指定状态列表
     * </p>
     *
     * @param threshold 时间阈值（早于此时间的需要同步）
     * @param statuses  物流状态列表
     * @return 待同步的物流信息列表
     */
    @Query("SELECT l FROM Logistics l WHERE l.lastSyncTime < :threshold " +
            "AND l.status IN :statuses")
    List<Logistics> findPendingLogistics(
            @Param("threshold") LocalDateTime threshold,
            @Param("statuses") List<LogisticsStatus> statuses
    );

    /**
     * 查找超时的物流信息
     * <p>
     * 查询条件：已标记为超时，且状态不在排除列表中
     * </p>
     *
     * @param excludeStatuses 排除的状态列表（如已签收、已拒签）
     * @return 超时的物流信息列表
     */
    @Query("SELECT l FROM Logistics l WHERE l.isOvertime = true " +
            "AND l.status NOT IN :excludeStatuses")
    List<Logistics> findOvertimeLogistics(
            @Param("excludeStatuses") List<LogisticsStatus> excludeStatuses
    );

    /**
     * 统计指定快递公司在时间范围内的订单数
     *
     * @param company   快递公司
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return 订单数量
     */
    @Query("SELECT COUNT(l) FROM Logistics l WHERE l.logisticsCompany = :company " +
            "AND l.actualDeliveryTime BETWEEN :startDate AND :endDate")
    long countByCompanyAndDateRange(
            @Param("company") LogisticsCompany company,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 统计指定快递公司的超时订单数
     *
     * @param company   快递公司
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return 超时订单数量
     */
    @Query("SELECT COUNT(l) FROM Logistics l WHERE l.logisticsCompany = :company " +
            "AND l.isOvertime = true " +
            "AND l.createdAt BETWEEN :startDate AND :endDate")
    long countOvertimeByCompanyAndDateRange(
            @Param("company") LogisticsCompany company,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * 查询指定快递公司的平均送达时长（小时）
     *
     * 注意：此方法使用 EXTRACT(EPOCH FROM ...) 函数，仅在 PostgreSQL 中有效。
     * H2 数据库不支持此语法，因此暂时注释。
     * TODO: 后续在 Service 层实现时，使用 Java 代码计算平均送达时长。
     *
     * @param company   快递公司
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return 平均送达时长（小时），如果无数据则返回null
     */
    // @Query("SELECT AVG(EXTRACT(EPOCH FROM (l.actualDeliveryTime - l.createdAt)) / 3600.0) " +
    //         "FROM Logistics l WHERE l.logisticsCompany = :company " +
    //         "AND l.actualDeliveryTime IS NOT NULL " +
    //         "AND l.createdAt BETWEEN :startDate AND :endDate")
    // Double calculateAverageDeliveryTimeInHours(
    //         @Param("company") LogisticsCompany company,
    //         @Param("startDate") LocalDateTime startDate,
    //         @Param("endDate") LocalDateTime endDate
    // );
}
