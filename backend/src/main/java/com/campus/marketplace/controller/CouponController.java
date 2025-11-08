package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.request.CreateCouponRequest;
import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.CouponResponse;
import com.campus.marketplace.common.entity.Coupon;
import com.campus.marketplace.common.entity.CouponUserRelation;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 优惠券控制器
 * <p>
 * 提供优惠券的创建、领取、使用、查询等API接口。
 * </p>
 *
 * @author BaSui 😎
 * @date 2025-11-09
 */
@Slf4j
@RestController
@RequestMapping("/coupons")
@RequiredArgsConstructor
@Tag(name = "优惠券管理", description = "优惠券创建、领取、使用、查询等接口")
public class CouponController {

    private final CouponService couponService;

    /**
     * 创建优惠券（管理员）
     * <p>
     * 管理员创建优惠券，支持满减券、折扣券、包邮券三种类型。
     * </p>
     *
     * POST /api/coupons/create
     *
     * @param request 创建优惠券请求
     * @return 优惠券实体
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "创建优惠券",
            description = "管理员创建优惠券，支持满减券、折扣券、包邮券三种类型"
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "创建优惠券请求体",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CreateCouponRequest.class),
                    examples = {
                            @ExampleObject(
                                    name = "满减券示例",
                                    value = """
                                            {
                                              "code": "SAVE10",
                                              "name": "满100减10",
                                              "type": "FIXED",
                                              "discountAmount": 10.00,
                                              "minAmount": 100.00,
                                              "totalCount": 1000,
                                              "limitPerUser": 1,
                                              "startTime": "2025-11-09T00:00:00",
                                              "endTime": "2025-12-31T23:59:59",
                                              "description": "新用户专享优惠券"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "折扣券示例",
                                    value = """
                                            {
                                              "code": "DISCOUNT20",
                                              "name": "全场8折",
                                              "type": "PERCENT",
                                              "discountRate": 0.80,
                                              "minAmount": 50.00,
                                              "totalCount": 500,
                                              "limitPerUser": 2,
                                              "startTime": "2025-11-09T00:00:00",
                                              "endTime": "2025-12-31T23:59:59",
                                              "description": "双十一特惠"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "包邮券示例",
                                    value = """
                                            {
                                              "code": "FREESHIP",
                                              "name": "全场包邮",
                                              "type": "FREE_SHIPPING",
                                              "minAmount": 0.00,
                                              "totalCount": 2000,
                                              "limitPerUser": 3,
                                              "startTime": "2025-11-09T00:00:00",
                                              "endTime": "2025-12-31T23:59:59",
                                              "description": "全场包邮券"
                                            }
                                            """
                            )
                    }
            )
    )
    public ApiResponse<Coupon> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        log.info("创建优惠券: code={}, name={}, type={}", request.code(), request.name(), request.type());

        Coupon coupon = couponService.createCoupon(
                request.code(),
                request.name(),
                request.type(),
                request.discountAmount(),
                request.discountRate(),
                request.minAmount(),
                request.totalCount(),
                request.limitPerUser(),
                request.startTime(),
                request.endTime(),
                request.description()
        );

        return ApiResponse.success(coupon);
    }

    /**
     * 查询可用优惠券列表（公开）
     * <p>
     * 查询当前可领取的优惠券列表，支持分页。
     * </p>
     *
     * GET /api/coupons/available
     *
     * @param page 页码（从 0 开始）
     * @param size 每页数量
     * @return 优惠券列表（分页）
     */
    @GetMapping("/available")
    @Operation(
            summary = "查询可用优惠券列表",
            description = "查询当前可领取的优惠券列表，支持分页"
    )
    public ApiResponse<Page<CouponResponse>> listAvailableCoupons(
            @Parameter(description = "页码（从 0 开始）", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "每页数量", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        log.info("查询可用优惠券列表: page={}, size={}", page, size);

        Page<CouponResponse> coupons = couponService.listAvailableCoupons(page, size);
        return ApiResponse.success(coupons);
    }

    /**
     * 领取优惠券
     * <p>
     * 用户领取优惠券，需要登录。
     * </p>
     *
     * POST /api/coupons/{couponId}/receive
     *
     * @param couponId 优惠券ID
     * @return 用户优惠券关联记录
     */
    @PostMapping("/{couponId}/receive")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "领取优惠券",
            description = "用户领取优惠券，需要登录"
    )
    public ApiResponse<CouponUserRelation> receiveCoupon(
            @Parameter(description = "优惠券ID", required = true, example = "1")
            @PathVariable Long couponId
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("领取优惠券: userId={}, couponId={}", userId, couponId);

        CouponUserRelation relation = couponService.receiveCoupon(userId, couponId);
        return ApiResponse.success(relation);
    }

    /**
     * 查询我的优惠券列表
     * <p>
     * 查询当前用户的优惠券列表，支持分页。
     * </p>
     *
     * GET /api/coupons/my
     *
     * @param page 页码（从 0 开始）
     * @param size 每页数量
     * @return 用户优惠券列表（分页）
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "查询我的优惠券列表",
            description = "查询当前用户的优惠券列表，支持分页"
    )
    public ApiResponse<Page<CouponUserRelation>> listMyCoupons(
            @Parameter(description = "页码（从 0 开始）", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "每页数量", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        log.info("查询我的优惠券列表: userId={}, page={}, size={}", userId, page, size);

        Page<CouponUserRelation> coupons = couponService.listUserCoupons(userId, page, size);
        return ApiResponse.success(coupons);
    }

    /**
     * 计算优惠金额
     * <p>
     * 根据优惠券ID和订单金额，计算优惠金额。
     * </p>
     *
     * GET /api/coupons/{couponId}/calculate
     *
     * @param couponId       优惠券ID
     * @param originalAmount 订单原价
     * @return 优惠金额
     */
    @GetMapping("/{couponId}/calculate")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "计算优惠金额",
            description = "根据优惠券ID和订单金额，计算优惠金额"
    )
    public ApiResponse<BigDecimal> calculateDiscount(
            @Parameter(description = "优惠券ID", required = true, example = "1")
            @PathVariable Long couponId,

            @Parameter(description = "订单原价", required = true, example = "100.00")
            @RequestParam BigDecimal originalAmount
    ) {
        log.info("计算优惠金额: couponId={}, originalAmount={}", couponId, originalAmount);

        BigDecimal discount = couponService.calculateDiscount(couponId, originalAmount);
        return ApiResponse.success(discount);
    }

    /**
     * 查询优惠券详情
     * <p>
     * 根据优惠券ID查询详情。
     * </p>
     *
     * GET /api/coupons/{couponId}
     *
     * @param couponId 优惠券ID
     * @return 优惠券详情
     */
    @GetMapping("/{couponId}")
    @Operation(
            summary = "查询优惠券详情",
            description = "根据优惠券ID查询详情"
    )
    public ApiResponse<Coupon> getCouponDetail(
            @Parameter(description = "优惠券ID", required = true, example = "1")
            @PathVariable Long couponId
    ) {
        log.info("查询优惠券详情: couponId={}", couponId);

        Coupon coupon = couponService.getCouponById(couponId);
        return ApiResponse.success(coupon);
    }

    /**
     * 停用优惠券（管理员）
     * <p>
     * 管理员停用优惠券，停用后用户无法领取。
     * </p>
     *
     * POST /api/coupons/{couponId}/deactivate
     *
     * @param couponId 优惠券ID
     * @return 操作结果
     */
    @PostMapping("/{couponId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "停用优惠券",
            description = "管理员停用优惠券，停用后用户无法领取"
    )
    public ApiResponse<Void> deactivateCoupon(
            @Parameter(description = "优惠券ID", required = true, example = "1")
            @PathVariable Long couponId
    ) {
        log.info("停用优惠券: couponId={}", couponId);

        couponService.deactivateCoupon(couponId);
        return ApiResponse.success(null);
    }

    /**
     * 启用优惠券（管理员）
     * <p>
     * 管理员启用优惠券，启用后用户可以领取。
     * </p>
     *
     * POST /api/coupons/{couponId}/activate
     *
     * @param couponId 优惠券ID
     * @return 操作结果
     */
    @PostMapping("/{couponId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "启用优惠券",
            description = "管理员启用优惠券，启用后用户可以领取"
    )
    public ApiResponse<Void> activateCoupon(
            @Parameter(description = "优惠券ID", required = true, example = "1")
            @PathVariable Long couponId
    ) {
        log.info("启用优惠券: couponId={}", couponId);

        couponService.activateCoupon(couponId);
        return ApiResponse.success(null);
    }

    /**
     * 获取优惠券统计信息（管理员）
     * <p>
     * 管理员查看优惠券的使用统计，包括领取率、使用率、优惠金额等。
     * </p>
     *
     * GET /api/coupons/{couponId}/statistics
     *
     * @param couponId 优惠券ID
     * @return 优惠券统计信息
     */
    @GetMapping("/{couponId}/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "获取优惠券统计信息",
            description = "管理员查看优惠券的使用统计，包括领取率、使用率、优惠金额等"
    )
    public ApiResponse<com.campus.marketplace.common.dto.response.CouponStatisticsResponse> getCouponStatistics(
            @Parameter(description = "优惠券ID", required = true, example = "1")
            @PathVariable Long couponId
    ) {
        log.info("获取优惠券统计信息: couponId={}", couponId);

        com.campus.marketplace.common.dto.response.CouponStatisticsResponse statistics =
                couponService.getCouponStatistics(couponId);
        return ApiResponse.success(statistics);
    }

    /**
     * 获取所有优惠券统计列表（管理员）
     * <p>
     * 管理员查看所有优惠券的使用统计列表。
     * </p>
     *
     * GET /api/coupons/statistics
     *
     * @return 优惠券统计列表
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "获取所有优惠券统计列表",
            description = "管理员查看所有优惠券的使用统计列表"
    )
    public ApiResponse<java.util.List<com.campus.marketplace.common.dto.response.CouponStatisticsResponse>> getAllCouponStatistics() {
        log.info("获取所有优惠券统计列表");

        java.util.List<com.campus.marketplace.common.dto.response.CouponStatisticsResponse> statistics =
                couponService.getAllCouponStatistics();
        return ApiResponse.success(statistics);
    }

    /**
     * 获取优惠券趋势统计（管理员）
     * <p>
     * 管理员查看优惠券的时间维度统计，支持日/周/月维度。
     * </p>
     *
     * GET /api/coupons/{couponId}/trend
     *
     * @param couponId   优惠券ID
     * @param periodType 周期类型（DAILY/WEEKLY/MONTHLY）
     * @param days       统计天数
     * @return 优惠券趋势统计
     */
    @GetMapping("/{couponId}/trend")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "获取优惠券趋势统计",
            description = "管理员查看优惠券的时间维度统计，支持日/周/月维度"
    )
    public ApiResponse<com.campus.marketplace.common.dto.response.CouponTrendStatisticsResponse> getCouponTrendStatistics(
            @Parameter(description = "优惠券ID", required = true, example = "1")
            @PathVariable Long couponId,

            @Parameter(description = "周期类型", example = "DAILY")
            @RequestParam(defaultValue = "DAILY") String periodType,

            @Parameter(description = "统计天数", example = "30")
            @RequestParam(defaultValue = "30") int days
    ) {
        log.info("获取优惠券趋势统计: couponId={}, periodType={}, days={}", couponId, periodType, days);

        com.campus.marketplace.common.dto.response.CouponTrendStatisticsResponse trendStatistics =
                couponService.getCouponTrendStatistics(couponId, periodType, days);
        return ApiResponse.success(trendStatistics);
    }

    /**
     * 获取优惠券用户排行（管理员）
     * <p>
     * 管理员查看优惠券的用户维度统计，返回使用次数最多的前N名用户。
     * </p>
     *
     * GET /api/coupons/{couponId}/user-ranking
     *
     * @param couponId 优惠券ID
     * @param topN     返回前N名用户
     * @return 优惠券用户排行
     */
    @GetMapping("/{couponId}/user-ranking")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "获取优惠券用户排行",
            description = "管理员查看优惠券的用户维度统计，返回使用次数最多的前N名用户"
    )
    public ApiResponse<com.campus.marketplace.common.dto.response.CouponUserRankingResponse> getCouponUserRanking(
            @Parameter(description = "优惠券ID", required = true, example = "1")
            @PathVariable Long couponId,

            @Parameter(description = "返回前N名用户", example = "10")
            @RequestParam(defaultValue = "10") int topN
    ) {
        log.info("获取优惠券用户排行: couponId={}, topN={}", couponId, topN);

        com.campus.marketplace.common.dto.response.CouponUserRankingResponse userRanking =
                couponService.getCouponUserRanking(couponId, topN);
        return ApiResponse.success(userRanking);
    }

    /**
     * 导出优惠券统计（管理员）
     * <p>
     * 管理员导出优惠券统计数据，支持 Excel 和 CSV 格式。
     * 导出任务异步执行，返回任务ID，可通过任务ID查询导出进度。
     * </p>
     *
     * POST /api/coupons/export
     *
     * @param couponId 优惠券ID（可选，为空则导出所有）
     * @param format   导出格式（EXCEL/CSV）
     * @return 导出任务响应
     */
    @PostMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "导出优惠券统计",
            description = "管理员导出优惠券统计数据，支持 Excel 和 CSV 格式，异步执行"
    )
    public ApiResponse<com.campus.marketplace.common.dto.response.ExportTaskResponse> exportCouponStatistics(
            @Parameter(description = "优惠券ID（可选）", example = "1")
            @RequestParam(required = false) Long couponId,

            @Parameter(description = "导出格式", example = "EXCEL")
            @RequestParam(defaultValue = "EXCEL") String format
    ) {
        log.info("导出优惠券统计: couponId={}, format={}", couponId, format);

        String taskId = couponService.exportCouponStatistics(couponId, format);

        com.campus.marketplace.common.dto.response.ExportTaskResponse response =
                com.campus.marketplace.common.dto.response.ExportTaskResponse.builder()
                        .taskId(taskId)
                        .taskType("COUPON_STATISTICS")
                        .format(format)
                        .status("PENDING")
                        .progress(0)
                        .createdAt(java.time.LocalDateTime.now())
                        .build();

        return ApiResponse.success(response);
    }

    /**
     * 获取导出任务状态（管理员）
     * <p>
     * 管理员查询导出任务的执行状态和进度。
     * </p>
     *
     * GET /api/coupons/export/{taskId}
     *
     * @param taskId 任务ID
     * @return 导出任务响应
     */
    @GetMapping("/export/{taskId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "获取导出任务状态",
            description = "管理员查询导出任务的执行状态和进度"
    )
    public ApiResponse<com.campus.marketplace.common.dto.response.ExportTaskResponse> getExportTaskStatus(
            @Parameter(description = "任务ID", required = true, example = "task_123456")
            @PathVariable String taskId
    ) {
        log.info("获取导出任务状态: taskId={}", taskId);

        com.campus.marketplace.common.dto.response.ExportTaskResponse response =
                couponService.getExportTaskStatus(taskId);
        return ApiResponse.success(response);
    }
}
