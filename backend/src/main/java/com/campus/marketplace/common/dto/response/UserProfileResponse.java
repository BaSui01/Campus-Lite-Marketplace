package com.campus.marketplace.common.dto.response;

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
public class UserProfileResponse {

    /**
     * 用户 ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱（脱敏）
     */
    private String email;

    /**
     * 手机号（脱敏）
     */
    private String phone;

    /**
     * 学号
     */
    private String studentId;

    /**
     * 头像 URL
     */
    private String avatar;

    /**
     * 用户状态
     */
    private String status;

    /**
     * 积分
     */
    private Integer points;

    /**
     * 角色列表
     */
    private List<String> roles;

    /**
     * 注册时间
     */
    private LocalDateTime createdAt;
}
