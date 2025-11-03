package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 信用等级枚举
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Getter
public enum CreditLevel {
    
    /**
     * 新手（0-10单）
     */
    NEWBIE("新手", 0, 10),
    
    /**
     * 铜牌（11-50单）
     */
    BRONZE("铜牌", 11, 50),
    
    /**
     * 银牌（51-100单）
     */
    SILVER("银牌", 51, 100),
    
    /**
     * 金牌（101-200单）
     */
    GOLD("金牌", 101, 200),
    
    /**
     * 钻石（201+单）
     */
    DIAMOND("钻石", 201, Integer.MAX_VALUE);

    private final String description;
    private final int minOrders;
    private final int maxOrders;

    CreditLevel(String description, int minOrders, int maxOrders) {
        this.description = description;
        this.minOrders = minOrders;
        this.maxOrders = maxOrders;
    }

    /**
     * 根据订单数量计算信用等级
     */
    public static CreditLevel fromOrderCount(long orderCount) {
        for (CreditLevel level : values()) {
            if (orderCount >= level.minOrders && orderCount <= level.maxOrders) {
                return level;
            }
        }
        return NEWBIE;
    }

    /**
     * 根据订单数量和好评率计算信用等级（加权）
     */
    public static CreditLevel fromOrderCountAndRate(long orderCount, double positiveRate) {
        CreditLevel baseLevel = fromOrderCount(orderCount);
        
        // 好评率 < 80% 降级
        if (positiveRate < 0.8 && baseLevel.ordinal() > 0) {
            return values()[baseLevel.ordinal() - 1];
        }
        
        // 好评率 >= 95% 且接近下一级，可以升级
        if (positiveRate >= 0.95 && orderCount >= baseLevel.maxOrders * 0.8) {
            if (baseLevel.ordinal() < values().length - 1) {
                return values()[baseLevel.ordinal() + 1];
            }
        }
        
        return baseLevel;
    }
}
