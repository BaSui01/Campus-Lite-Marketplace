package com.campus.marketplace.common.dto.request;

import com.campus.marketplace.common.enums.LogisticsCompany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 卖家发货请求
 */
public record ShipOrderRequest(
        /**
         * 物流单号
         */
        @NotBlank(message = "物流单号不能为空")
        @Size(max = 50, message = "物流单号长度不能超过50")
        String trackingNumber,

        /**
         * 物流公司
         */
        @NotNull(message = "物流公司不能为空")
        LogisticsCompany company
) {}
