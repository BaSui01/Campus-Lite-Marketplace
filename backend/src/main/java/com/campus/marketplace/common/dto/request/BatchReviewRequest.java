package com.campus.marketplace.common.dto.request;

import com.campus.marketplace.common.enums.AppealStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

/**
 * 批量审核请求DTO
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchReviewRequest {

    /**
     * 要审核的申诉ID列表
     */
    @NotEmpty(message = "申诉ID列表不能为空")
    private List<Long> appealIds;

    /**
     * 审核后状态
     */
    @NotNull(message = "审核状态不能为空")
    private AppealStatus status;

    /**
     * 审核意见
     */
    @NotBlank(message = "审核意见不能为空")
    @Size(min = 5, max = 500, message = "审核意见长度必须在5-500字符之间")
    private String reviewComment;

    @NotNull(message = "审核人ID不能为空")
    @Min(value = 1, message = "审核人ID必须大于0")
    private Long reviewerId;

    /**
     * 审核人名称
     */
    @NotNull(message = "审核人名称不能为空")
    @Size(min = 2, max = 50, message = "审核人名称长度必须在2-50字符之间")
    private String reviewerName;
}
