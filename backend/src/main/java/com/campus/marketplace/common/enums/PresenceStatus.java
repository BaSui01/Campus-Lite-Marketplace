package com.campus.marketplace.common.enums;

/**
 * 用户在线状态枚举
 *
 * 用于表示用户的实时在线状态，支持多种状态展示
 *
 * 状态说明：
 * - ONLINE：在线（绿点）- 用户当前活跃在线
 * - BUSY：忙碌（红点）- 用户在线但忙碌中
 * - AWAY：离开（黄点）- 用户暂时离开但保持连接
 * - OFFLINE：离线（灰点）- 用户已断开连接
 *
 * 使用场景：
 * 1. 聊天界面显示对方在线状态
 * 2. 用户列表显示在线状态图标
 * 3. WebSocket 连接状态管理
 * 4. 在线状态统计和分析
 *
 * @author BaSui
 * @date 2025-11-03
 */
public enum PresenceStatus {

    /**
     * 在线 - 用户当前活跃在线
     */
    ONLINE("在线"),

    /**
     * 忙碌 - 用户在线但忙碌中
     */
    BUSY("忙碌"),

    /**
     * 离开 - 用户暂时离开但保持连接
     */
    AWAY("离开"),

    /**
     * 离线 - 用户已断开连接
     */
    OFFLINE("离线");

    /**
     * 显示名称（中文）
     */
    private final String displayName;

    /**
     * 构造函数
     *
     * @param displayName 显示名称
     */
    PresenceStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * 获取显示名称
     *
     * @return 显示名称
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 判断是否为在线状态（ONLINE 或 BUSY）
     *
     * @return true=在线，false=离线或离开
     */
    public boolean isOnline() {
        return this == ONLINE || this == BUSY;
    }

    /**
     * 判断是否为离线状态
     *
     * @return true=离线，false=其他状态
     */
    public boolean isOffline() {
        return this == OFFLINE;
    }
}
