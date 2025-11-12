package com.campus.marketplace.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 优惠券用户排行响应 DTO
 *
 * @author BaSui 😎
 * @date 2025-11-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponUserRankingResponse {

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 优惠券代码
     */
    private String code;

    /**
     * 优惠券名称
     */
    private String name;

    /**
     * 用户排行列表
     */
    private List<UserRankingItem> userRanking;

    /**
     * 用户排行项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRankingItem {
        /**
         * 排名
         */
        private Integer rank;

        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 用户名
         */
        private String username;

        /**
         * 使用次数
         */
        private Integer useCount;

        /**
         * 总优惠金额
         */
        private BigDecimal totalDiscountAmount;
    }
}
