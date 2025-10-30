package com.campus.marketplace.common.dto.request;

import com.campus.marketplace.common.enums.CouponType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建优惠券请求 DTO
 * 
 * @author BaSui
 * @date 2025-10-27
 */
public record CreateCouponRequest(
        @NotBlank(message = "优惠券代码不能为空")
        @Size(max = 50, message = "优惠券代码长度不能超过50")
        String code,

        @NotBlank(message = "优惠券名称不能为空")
        @Size(max = 100, message = "优惠券名称长度不能超过100")
        String name,

        @NotNull(message = "优惠券类型不能为空")
        CouponType type,

        BigDecimal discountAmount,

        BigDecimal discountRate,

        BigDecimal minAmount,

        @NotNull(message = "发行数量不能为空")
        @Min(value = 1, message = "发行数量至少为1")
        Integer totalCount,

        @Min(value = 1, message = "每人限领数量至少为1")
        Integer limitPerUser,

        @NotNull(message = "开始时间不能为空")
        LocalDateTime startTime,

        @NotNull(message = "结束时间不能为空")
        LocalDateTime endTime,

        @Size(max = 1000, message = "描述长度不能超过1000")
        String description
) {
}
