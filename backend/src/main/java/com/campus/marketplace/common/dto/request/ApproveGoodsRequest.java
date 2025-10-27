package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 审核物品请求 DTO
 * 
 * @author BaSui
 * @date 2025-10-27
 */
public record ApproveGoodsRequest(
        
        /**
         * 是否通过审核
         */
        boolean approved,
        
        /**
         * 拒绝原因（审核不通过时必填）
         */
        @Size(max = 200, message = "拒绝原因长度不能超过 200 个字符")
        String rejectReason
) {
}
