package com.campus.marketplace.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 卖家信息 DTO
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerInfoDTO {

    /**
     * 卖家 ID
     */
    private Long sellerId;

    /**
     * 卖家用户名
     */
    private String username;

    /**
     * 卖家头像
     */
    private String avatar;

    /**
     * 信用等级
     */
    private String creditLevel;

    /**
     * 是否在线
     */
    private Boolean isOnline;

    /**
     * 平均响应时间（分钟）
     */
    private Integer avgResponseTime;

    /**
     * 商品总数
     */
    private Long totalGoodsCount;

    /**
     * 好评率
     */
    private Double positiveRate;
}
