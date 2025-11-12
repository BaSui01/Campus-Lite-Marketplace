package com.campus.marketplace.controller;

import com.campus.marketplace.common.dto.response.ApiResponse;
import com.campus.marketplace.common.dto.response.PaymentRecordDTO;
import com.campus.marketplace.common.dto.response.PaymentStatisticsDTO;
import com.campus.marketplace.service.PaymentAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 支付管理控制器（管理员）
 * 
 * @author BaSui 😎
 * @date 2025-11-10
 */
@RestController
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@Tag(name = "支付管理（管理员）", description = "支付记录查询、支付统计等接口")
@SecurityRequirement(name = "bearerAuth")
public class PaymentAdminController {

    private final PaymentAdminService paymentAdminService;

    /**
     * 查询支付记录列表
     */
    @GetMapping
    @Operation(summary = "查询支付记录列表", description = "管理员查询支付记录列表，支持关键词搜索、状态筛选、支付方式筛选、时间范围筛选")
    public ApiResponse<Page<PaymentRecordDTO>> listPayments(
            @Parameter(description = "关键词（订单号/用户名/商品名）") @RequestParam(required = false) String keyword,
            @Parameter(description = "订单状态（逗号分隔，如：PAID,COMPLETED,REFUNDED）") @RequestParam(required = false) String status,
            @Parameter(description = "支付方式（WECHAT/ALIPAY）") @RequestParam(required = false) String paymentMethod,
            @Parameter(description = "开始日期（格式：yyyy-MM-dd）") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期（格式：yyyy-MM-dd）") @RequestParam(required = false) String endDate,
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size
    ) {
        Page<PaymentRecordDTO> result = paymentAdminService.listPayments(
                keyword, status, paymentMethod, startDate, endDate, page, size
        );
        return ApiResponse.success(result);
    }

    /**
     * 查询支付详情
     */
    @GetMapping("/{orderNo}")
    @Operation(summary = "查询支付详情", description = "根据订单号查询支付详情")
    public ApiResponse<PaymentRecordDTO> getPaymentDetail(
            @Parameter(description = "订单号") @PathVariable String orderNo
    ) {
        PaymentRecordDTO result = paymentAdminService.getPaymentDetail(orderNo);
        return ApiResponse.success(result);
    }

    /**
     * 查询支付统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "查询支付统计", description = "查询支付统计数据（总金额、总次数、成功率、按支付方式统计等）")
    public ApiResponse<PaymentStatisticsDTO> getStatistics(
            @Parameter(description = "开始日期（格式：yyyy-MM-dd）") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期（格式：yyyy-MM-dd）") @RequestParam(required = false) String endDate
    ) {
        PaymentStatisticsDTO result = paymentAdminService.getStatistics(startDate, endDate);
        return ApiResponse.success(result);
    }
}
