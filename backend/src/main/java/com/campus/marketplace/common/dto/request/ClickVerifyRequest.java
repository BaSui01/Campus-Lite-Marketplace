package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 点选验证请求 DTO
 *
 * @author BaSui 😎
 * @date 2025-11-10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClickVerifyRequest {

    /**
     * 点选验证码ID
     */
    @NotBlank(message = "点选验证码ID不能为空")
    private String clickId;

    /**
     * 用户点击的坐标列表
     */
    @NotEmpty(message = "点击坐标不能为空")
    private List<ClickPoint> clickPoints;

    /**
     * 点击坐标点
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClickPoint {
        /**
         * X轴坐标
         */
        private Integer x;

        /**
         * Y轴坐标
         */
        private Integer y;
    }
}
