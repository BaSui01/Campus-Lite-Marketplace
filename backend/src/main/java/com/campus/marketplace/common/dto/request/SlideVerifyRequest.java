package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 滑块验证请求 DTO
 *
 * @author BaSui 😎
 * @date 2025-11-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlideVerifyRequest {

    /**
     * 滑块验证码ID
     */
    @NotBlank(message = "滑块ID不能为空")
    private String slideId;

    /**
     * 用户滑动的X轴位置
     */
    @NotNull(message = "滑动位置不能为空")
    private Integer xPosition;

    /**
     * 滑动轨迹（可选，用于防作弊）
     * 格式：[{x: 0, y: 0, t: 0}, {x: 10, y: 0, t: 100}, ...]
     */
    private List<TrackPoint> track;

    /**
     * 滑动轨迹点
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackPoint {
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
}
