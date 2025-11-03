package com.campus.marketplace.service;

import com.campus.marketplace.common.dto.LogisticsDTO;
import com.campus.marketplace.common.dto.LogisticsStatisticsDTO;
import com.campus.marketplace.common.enums.LogisticsCompany;

import java.time.LocalDateTime;

/**
 * 物流服务接口
 * <p>
 * 提供物流信息的查询、更新、统计等功能。
 * </p>
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
public interface LogisticsService {

    /**
     * 创建物流信息
     * <p>
     * 卖家发货时调用，创建物流记录并关联订单。
     * </p>
     *
     * @param orderId        订单ID
     * @param trackingNumber 快递单号
     * @param company        快递公司
     * @return 物流信息DTO
     */
    LogisticsDTO createLogistics(Long orderId, String trackingNumber, LogisticsCompany company);

    /**
     * 根据订单ID查询物流信息
     * <p>
     * 买家查看订单物流时调用。
     * </p>
     *
     * @param orderId 订单ID
     * @return 物流信息DTO
     */
    LogisticsDTO getLogisticsByOrderId(Long orderId);

    /**
     * 根据快递单号查询物流信息
     * <p>
     * 支持通过快递单号直接查询物流。
     * </p>
     *
     * @param trackingNumber 快递单号
     * @return 物流信息DTO
     */
    LogisticsDTO getLogisticsByTrackingNumber(String trackingNumber);

    /**
     * 同步物流信息
     * <p>
     * 调用快递公司API，更新物流轨迹和状态。
     * 支持手动同步和定时任务自动同步。
     * </p>
     *
     * @param orderId 订单ID
     * @return 更新后的物流信息DTO
     */
    LogisticsDTO syncLogistics(Long orderId);

    /**
     * 批量同步物流信息
     * <p>
     * 定时任务调用，批量同步待更新的物流信息。
     * 查询条件：最后同步时间早于2小时，且状态为运输中/派送中。
     * </p>
     *
     * @return 同步成功的数量
     */
    int batchSyncLogistics();

    /**
     * 检查并标记超时物流
     * <p>
     * 定时任务调用，检查预计送达时间已过但未签收的物流，标记为超时。
     * </p>
     *
     * @return 标记超时的数量
     */
    int markOvertimeLogistics();

    /**
     * 获取物流统计数据
     * <p>
     * 管理员查看物流统计时调用，用于评估快递公司服务质量。
     * </p>
     *
     * @param startDate 开始时间
     * @param endDate   结束时间
     * @return 物流统计DTO
     */
    LogisticsStatisticsDTO getLogisticsStatistics(LocalDateTime startDate, LocalDateTime endDate);
}
