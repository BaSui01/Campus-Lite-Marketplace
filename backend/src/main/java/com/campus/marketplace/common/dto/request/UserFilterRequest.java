package com.campus.marketplace.common.dto.request;

import com.campus.marketplace.common.enums.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 用户筛选请求参数
 *
 * @author BaSui 😎
 * @date 2025-11-10
 * @description 用户列表查询的筛选参数，继承通用筛选基类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户筛选请求参数")
public class UserFilterRequest extends BaseFilterRequest {

    /**
     * 用户状态
     */
    @Schema(description = "用户状态（ACTIVE/BANNED/INACTIVE）", example = "ACTIVE")
    private UserStatus status;

    /**
     * 角色筛选
     */
    @Schema(description = "角色（USER/ADMIN/SUPER_ADMIN）", example = "USER")
    private String role;

    /**
     * 校区 ID
     */
    @Schema(description = "校区 ID", example = "1")
    private Long campusId;

    /**
     * 是否实名认证
     */
    @Schema(description = "是否实名认证", example = "true")
    private Boolean verified;
}
