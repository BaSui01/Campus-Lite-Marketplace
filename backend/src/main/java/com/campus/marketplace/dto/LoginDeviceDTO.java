package com.campus.marketplace.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 登录设备 DTO
 *
 * @author BaSui 😎
 * @date 2025-11-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录设备信息")
public class LoginDeviceDTO {

    @Schema(description = "设备ID")
    private Long id;

    @Schema(description = "设备名称", example = "Windows 11 - Chrome")
    private String deviceName;

    @Schema(description = "设备类型", example = "desktop", allowableValues = {"mobile", "desktop", "tablet"})
    private String deviceType;

    @Schema(description = "操作系统", example = "Windows 11")
    private String os;

    @Schema(description = "浏览器", example = "Chrome 120")
    private String browser;

    @Schema(description = "IP 地址", example = "192.168.1.100")
    private String ip;

    @Schema(description = "地理位置", example = "中国 北京")
    private String location;

    @Schema(description = "最后活跃时间")
    private LocalDateTime lastActiveAt;

    @Schema(description = "是否当前设备")
    private Boolean isCurrent;
}
