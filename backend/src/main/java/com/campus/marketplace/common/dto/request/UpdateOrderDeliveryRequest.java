package com.campus.marketplace.common.dto.request;

import com.campus.marketplace.common.enums.DeliveryMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 更新订单配送信息请求
 */
public record UpdateOrderDeliveryRequest(
        @NotNull(message = "配送方式不能为空")
        DeliveryMethod deliveryMethod,

        @Size(max = 50, message = "收货人姓名长度不能超过50")
        String receiverName,

        @Size(max = 20, message = "手机号长度不能超过20")
        String receiverPhone,

        @Size(max = 255, message = "收货地址长度不能超过255")
        String receiverAddress,

        @Size(max = 500, message = "备注长度不能超过500")
        String note
) {}
