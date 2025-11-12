package com.campus.marketplace.common.dto.request;

import com.campus.marketplace.common.enums.LogisticsStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 物流筛选请求参数
 *
 * @author BaSui 😎
 * @date 2025-11-10
 * @description 物流列表查询的筛选参数，继承通用筛选基类
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "物流筛选请求参数")
public class LogisticsFilterRequest extends BaseFilterRequest {

    /**
     * 物流状态
     */
    @Schema(description = "物流状态（PENDING/IN_TRANSIT/DELIVERED/CANCELLED）", example = "IN_TRANSIT")
    private LogisticsStatus status;
}
