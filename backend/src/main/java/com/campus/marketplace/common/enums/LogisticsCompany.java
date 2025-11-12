package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 物流公司枚举
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Getter
public enum LogisticsCompany {

    /**
     * 顺丰速运
     */
    SHUNFENG("顺丰速运", "SF"),

    /**
     * 中通快递
     */
    ZHONGTONG("中通快递", "ZTO"),

    /**
     * 圆通速递
     */
    YUANTONG("圆通速递", "YTO"),

    /**
     * 韵达快递
     */
    YUNDA("韵达快递", "YD"),

    /**
     * 邮政EMS
     */
    EMS("邮政EMS", "EMS"),

    /**
     * 京东物流
     */
    JINGDONG("京东物流", "JD"),

    /**
     * 德邦物流
     */
    DEBANG("德邦物流", "DBL"),

    /**
     * 申通快递
     */
    SHENTONG("申通快递", "STO");

    private final String displayName;
    private final String code;

    /**
     * 枚举构造器（私有）
     */
    LogisticsCompany(String displayName, String code) {
        this.displayName = displayName;
        this.code = code;
    }

    /**
     * 根据代码查找物流公司（不区分大小写）
     *
     * @param code 快递公司代码
     * @return 物流公司枚举
     * @throws IllegalArgumentException 当代码无效时抛出异常
     */
    public static LogisticsCompany fromCode(String code) {
        for (LogisticsCompany company : values()) {
            if (company.code.equalsIgnoreCase(code)) {
                return company;
            }
        }
        throw new IllegalArgumentException("Unknown logistics company code: " + code);
    }
}
