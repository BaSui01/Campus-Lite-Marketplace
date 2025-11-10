package com.campus.marketplace.common.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户资料响应 DTO
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户资料响应数据")
public class UserProfileResponse {

    /**
     * 用户 ID
     */
    @Schema(description = "用户唯一标识", example = "10001")
    private Long id;

    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "alice")
    private String username;

    /**
     * 昵称
     */
    @Schema(description = "昵称", example = "小爱同学")
    private String nickname;

    /**
     * 邮箱（脱敏）
     */
    @Schema(description = "邮箱（脱敏后）", example = "a***@example.com")
    private String email;

    /**
     * 手机号（脱敏）
     */
    @Schema(description = "手机号（脱敏后）", example = "138****1234")
    private String phone;

    /**
     * 学号
     */
    @Schema(description = "学号", example = "20240001")
    private String studentId;

    /**
     * 头像 URL
     */
    @Schema(description = "头像地址", example = "https://cdn.campus.com/avatar/u001.png")
    private String avatar;

    /**
     * 用户状态
     */
    @Schema(description = "用户状态", example = "ACTIVE")
    private String status;

    /**
     * 积分
     */
    @Schema(description = "当前积分", example = "1200")
    private Integer points;

    /**
     * 校区 ID
     */
    @Schema(description = "校区ID", example = "1")
    private Long campusId;

    /**
     * 校区名称
     */
    @Schema(description = "校区名称", example = "北京校区")
    private String campusName;

    /**
     * 封禁原因
     */
    @Schema(description = "封禁原因（仅封禁状态时有值）", example = "违规发布信息")
    private String banReason;

    /**
     * 角色列表
     */
    @Schema(description = "角色列表", example = "[\"ROLE_USER\",\"ROLE_SELLER\"]")
    private List<String> roles;

    /**
     * 注册时间
     */
    @Schema(description = "注册时间", example = "2024-03-01T12:00:00")
    private LocalDateTime createdAt;

    /**
     * 邮箱是否已验证
     */
    @Schema(description = "邮箱是否已验证", example = "true")
    private Boolean emailVerified;

    /**
     * 手机号是否已验证
     */
    @Schema(description = "手机号是否已验证", example = "false")
    private Boolean phoneVerified;

    /**
     * 是否启用两步验证（2FA）
     */
    @Schema(description = "是否启用两步验证", example = "false")
    private Boolean twoFactorEnabled;
}
