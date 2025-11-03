package com.campus.marketplace.common.enums;

import lombok.Getter;

/**
 * 标签类型枚举
 *
 * 用于评价标签的分类（与三维评分对应）
 *
 * @author BaSui 😎 - 五大标签分类，涵盖评价各个维度！
 * @since 2025-11-03
 */
@Getter
public enum TagType {

    /**
     * 物品质量标签
     * 如：质量好、做工精细、有瑕疵等
     */
    QUALITY("物品质量"),

    /**
     * 服务态度标签
     * 如：服务好、态度差、响应快等
     */
    SERVICE("服务态度"),

    /**
     * 物流速度标签
     * 如：发货快、配送慢、包装好等
     */
    DELIVERY("物流速度"),

    /**
     * 性价比标签
     * 如：划算、性价比高、价格贵等
     */
    PRICE("性价比"),

    /**
     * 其他标签
     * 无法归类到以上四种的标签
     */
    OTHER("其他");

    private final String description;

    TagType(String description) {
        this.description = description;
    }
}
