package com.campus.marketplace.common.dto.response;

import com.campus.marketplace.common.enums.CreditLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 用户信用信息响应 DTO
 * 
 * @author BaSui 😎
 * @date 2025-11-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreditInfoResponse {
    
    private Long userId;
    private String username;
    private String avatar;
    
    // 信用分（暂时用等级代替，未来可扩展）
    private Integer creditScore;
    private CreditLevel creditLevel;
    
    // 信用评分明细
    private Long orderCount;
    private Double positiveRate;
    private Integer avgResponseTime;
    
    // 信用等级进度
    private CreditLevelInfo currentLevelInfo;
    private CreditLevelInfo nextLevelInfo;
    private Double progressToNextLevel;
    
    /**
     * 信用等级信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreditLevelInfo {
        private String level;
        private String name;
        private String color;
        private Integer minOrders;
        private Integer maxOrders;
    }
}
