package com.campus.marketplace.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 登录响应 DTO
 * 
 * @author BaSui
 * @date 2025-10-25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * 访问令牌（Access Token）- 短期有效（15分钟）
     */
    private String accessToken;

    /**
     * 刷新令牌（Refresh Token）- 长期有效（7天）
     */
    private String refreshToken;

    /**
     * Token 类型
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * 访问令牌过期时间（毫秒）
     */
    private Long expiresIn;

    /**
     * 刷新令牌过期时间（毫秒）
     */
    private Long refreshExpiresIn;

    /**
     * 用户信息
     */
    private UserInfo userInfo;

    /**
     * 是否需要 2FA 验证
     */
    private Boolean requires2FA;

    /**
     * 临时 Token（用于 2FA 验证，仅在 requires2FA=true 时返回）
     */
    private String tempToken;

    /**
     * 用户信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String avatar;
        private Integer points;
        private List<String> roles;
        private List<String> permissions;
    }
}
