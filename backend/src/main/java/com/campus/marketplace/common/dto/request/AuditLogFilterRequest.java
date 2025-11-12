package com.campus.marketplace.common.dto.request;

import com.campus.marketplace.common.enums.AuditActionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 审计日志筛选请求参数
 *
 * @author BaSui 😎
 * @date 2025-11-10
 * @description 审计日志列表查询的筛选参数，继承通用筛选基类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "审计日志筛选请求参数")
public class AuditLogFilterRequest extends BaseFilterRequest {

    /**
     * 操作人ID
     */
    @Schema(description = "操作人ID", example = "10001")
    private Long operatorId;

    /**
     * 操作类型
     */
    @Schema(description = "操作类型（CREATE/UPDATE/DELETE/APPROVE等）", example = "DELETE")
    private AuditActionType actionType;
}
