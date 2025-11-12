package com.campus.marketplace.common.dto.response;

import com.campus.marketplace.common.entity.BanLog;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 封禁记录响应 DTO
 *
 * @author BaSui 😎
 * @date 2025-11-08
 */
@Builder
@Schema(description = "封禁记录响应")
public record BanLogResponse(

        @Schema(description = "封禁记录ID", example = "1001")
        Long id,

        @Schema(description = "被封禁用户ID", example = "10002")
        Long userId,

        @Schema(description = "被封禁用户名", example = "user123")
        String username,

        @Schema(description = "管理员ID", example = "1")
        Long adminId,

        @Schema(description = "管理员用户名", example = "admin")
        String adminUsername,

        @Schema(description = "封禁原因", example = "违反社区规则")
        String reason,

        @Schema(description = "封禁天数（0表示永久）", example = "7")
        Integer days,

        @Schema(description = "解封时间（null表示永久封禁）", example = "2025-11-15T10:00:00")
        LocalDateTime unbanTime,

        @Schema(description = "是否已解封", example = "false")
        Boolean isUnbanned,

        @Schema(description = "创建时间", example = "2025-11-08T10:00:00")
        LocalDateTime createdAt
) {

    /**
     * 从 BanLog 实体转换为 BanLogResponse
     *
     * @param banLog 封禁记录实体
     * @return BanLogResponse
     */
    public static BanLogResponse from(BanLog banLog) {
        return BanLogResponse.builder()
                .id(banLog.getId())
                .userId(banLog.getUserId())
                .username(null) // 需要在 Service 层填充
                .adminId(banLog.getAdminId())
                .adminUsername(null) // 需要在 Service 层填充
                .reason(banLog.getReason())
                .days(banLog.getDays())
                .unbanTime(banLog.getUnbanTime())
                .isUnbanned(banLog.getIsUnbanned())
                .createdAt(banLog.getCreatedAt())
                .build();
    }

    /**
     * 从 BanLog 实体转换为 BanLogResponse（包含用户名）
     *
     * @param banLog 封禁记录实体
     * @param username 被封禁用户名
     * @param adminUsername 管理员用户名
     * @return BanLogResponse
     */
    public static BanLogResponse from(BanLog banLog, String username, String adminUsername) {
        return BanLogResponse.builder()
                .id(banLog.getId())
                .userId(banLog.getUserId())
                .username(username)
                .adminId(banLog.getAdminId())
                .adminUsername(adminUsername)
                .reason(banLog.getReason())
                .days(banLog.getDays())
                .unbanTime(banLog.getUnbanTime())
                .isUnbanned(banLog.getIsUnbanned())
                .createdAt(banLog.getCreatedAt())
                .build();
    }
}
