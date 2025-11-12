package com.campus.marketplace.common.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户行为数据请求 DTO
 *
 * @author BaSui 😎
 * @date 2025-11-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorDataRequest {

    /**
     * 会话ID（用于关联同一次登录的多个行为）
     */
    private String sessionId;

    /**
     * 鼠标轨迹（从进入页面到点击登录按钮）
     */
    private List<MousePoint> mouseTrack;

    /**
     * 点击事件列表
     */
    private List<ClickEvent> clicks;

    /**
     * 键盘输入事件列表
     */
    private List<KeyboardEvent> keyboardEvents;

    /**
     * 页面停留时间（毫秒）
     */
    private Long pageStayTime;

    /**
     * 浏览器指纹
     */
    private BrowserFingerprint fingerprint;

    /**
     * 鼠标轨迹点
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MousePoint {
        /**
         * X轴位置
         */
        private Integer x;

        /**
         * Y轴位置
         */
        private Integer y;

        /**
         * 时间戳（毫秒）
         */
        private Long t;
    }

    /**
     * 点击事件
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClickEvent {
        /**
         * 点击的元素类型（button、input、link等）
         */
        private String elementType;

        /**
         * 点击位置X
         */
        private Integer x;

        /**
         * 点击位置Y
         */
        private Integer y;

        /**
         * 时间戳（毫秒）
         */
        private Long t;
    }

    /**
     * 键盘输入事件
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeyboardEvent {
        /**
         * 输入的字段（username、password等）
         */
        private String field;

        /**
         * 输入字符数
         */
        private Integer length;

        /**
         * 输入时间间隔（毫秒）
         */
        private Long interval;

        /**
         * 时间戳（毫秒）
         */
        private Long t;
    }

    /**
     * 浏览器指纹
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrowserFingerprint {
        /**
         * User-Agent
         */
        private String userAgent;

        /**
         * 屏幕分辨率
         */
        private String screenResolution;

        /**
         * 时区
         */
        private String timezone;

        /**
         * 语言
         */
        private String language;

        /**
         * Canvas指纹
         */
        private String canvasFingerprint;
    }
}
