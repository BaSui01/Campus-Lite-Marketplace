package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.LogisticsDTO;
import com.campus.marketplace.common.dto.LogisticsStatisticsDTO;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.enums.LogisticsCompany;
import com.campus.marketplace.service.LogisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 物流控制器
 * <p>
 * 提供物流信息的查询、创建、同步等API接口。
 * </p>
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Slf4j
@RestController
@RequestMapping("/api/logistics")
@RequiredArgsConstructor
@Tag(name = "物流管理", description = "物流信息查询、创建、同步等接口")
public class LogisticsController {

    private final LogisticsService logisticsService;

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
    @PostMapping("/create")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "创建物流信息", description = "卖家发货时调用，创建物流记录并关联订单")
    public ApiResponse<LogisticsDTO> createLogistics(
            @Parameter(description = "订单ID", required = true)
            @RequestParam Long orderId,

            @Parameter(description = "快递单号", required = true)
            @RequestParam String trackingNumber,

            @Parameter(description = "快递公司", required = true)
            @RequestParam LogisticsCompany company
    ) {
        log.info("创建物流信息: orderId={}, trackingNumber={}, company={}", orderId, trackingNumber, company);

        LogisticsDTO logistics = logisticsService.createLogistics(orderId, trackingNumber, company);
        return ApiResponse.success(logistics);
    }

    /**
     * 根据订单ID查询物流信息
     * <p>
     * 买家查看订单物流时调用。
     * </p>
     *
     * @param orderId 订单ID
     * @return 物流信息DTO
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "根据订单ID查询物流信息", description = "买家查看订单物流时调用")
    public ApiResponse<LogisticsDTO> getLogisticsByOrderId(
            @Parameter(description = "订单ID", required = true)
            @PathVariable Long orderId
    ) {
        log.info("查询物流信息: orderId={}", orderId);

        LogisticsDTO logistics = logisticsService.getLogisticsByOrderId(orderId);
        return ApiResponse.success(logistics);
    }

    /**
     * 根据快递单号查询物流信息
     * <p>
     * 支持通过快递单号直接查询物流。
     * </p>
     *
     * @param trackingNumber 快递单号
     * @return 物流信息DTO
     */
    @GetMapping("/tracking/{trackingNumber}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "根据快递单号查询物流信息", description = "支持通过快递单号直接查询物流")
    public ApiResponse<LogisticsDTO> getLogisticsByTrackingNumber(
            @Parameter(description = "快递单号", required = true)
            @PathVariable String trackingNumber
    ) {
        log.info("查询物流信息: trackingNumber={}", trackingNumber);

        LogisticsDTO logistics = logisticsService.getLogisticsByTrackingNumber(trackingNumber);
        return ApiResponse.success(logistics);
    }

    /**
     * 手动同步物流信息
     * <p>
     * 用户点击"刷新物流"按钮时调用。
     * </p>
     *
     * @param orderId 订单ID
     * @return 更新后的物流信息DTO
     */
    @PostMapping("/sync/{orderId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "手动同步物流信息", description = "用户点击刷新物流按钮时调用")
    public ApiResponse<LogisticsDTO> syncLogistics(
            @Parameter(description = "订单ID", required = true)
            @PathVariable Long orderId
    ) {
        log.info("手动同步物流信息: orderId={}", orderId);

        LogisticsDTO logistics = logisticsService.syncLogistics(orderId);
        return ApiResponse.success(logistics);
    }

    /**
     * 获取物流统计数据
     * <p>
     * 管理员查看物流统计时调用，用于评估快递公司服务质量。
     * </p>
     *
     * @param startDate 开始时间（格式：yyyy-MM-dd HH:mm:ss）
     * @param endDate   结束时间（格式：yyyy-MM-dd HH:mm:ss）
     * @return 物流统计DTO
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取物流统计数据", description = "管理员查看物流统计时调用，用于评估快递公司服务质量")
    public ApiResponse<LogisticsStatisticsDTO> getLogisticsStatistics(
            @Parameter(description = "开始时间", required = true, example = "2025-01-01 00:00:00")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,

            @Parameter(description = "结束时间", required = true, example = "2025-12-31 23:59:59")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate
    ) {
        log.info("获取物流统计数据: startDate={}, endDate={}", startDate, endDate);

        LogisticsStatisticsDTO statistics = logisticsService.getLogisticsStatistics(startDate, endDate);
        return ApiResponse.success(statistics);
    }

    /**
     * 批量同步物流信息（定时任务调用）
     * <p>
     * 仅管理员可调用，用于手动触发批量同步。
     * </p>
     *
     * @return 同步成功的数量
     */
    @PostMapping("/batch-sync")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "批量同步物流信息", description = "仅管理员可调用，用于手动触发批量同步")
    public ApiResponse<Integer> batchSyncLogistics() {
        log.info("批量同步物流信息");

        int successCount = logisticsService.batchSyncLogistics();
        return ApiResponse.success(successCount);
    }

    /**
     * 检查并标记超时物流（定时任务调用）
     * <p>
     * 仅管理员可调用，用于手动触发超时检查。
     * </p>
     *
     * @return 标记超时的数量
     */
    @PostMapping("/mark-overtime")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "检查并标记超时物流", description = "仅管理员可调用，用于手动触发超时检查")
    public ApiResponse<Integer> markOvertimeLogistics() {
        log.info("检查并标记超时物流");

        int overtimeCount = logisticsService.markOvertimeLogistics();
        return ApiResponse.success(overtimeCount);
    }
}
