package com.campus.marketplace.common.dto.request;

import com.campus.marketplace.common.enums.AppealTargetType;
import com.campus.marketplace.common.enums.AppealType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

/**
 * 创建申诉请求DTO
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAppealRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "目标类型不能为空")
    private AppealTargetType targetType;

    @NotNull(message = "目标ID不能为空")  
    @Min(value = 1, message = "目标ID必须大于0")
    private Long targetId;

    @NotNull(message = "申诉类型不能为空")
    private AppealType appealType;

    @NotBlank(message = "申诉原因不能为空")
    @Size(min = 10, max = 1000, message = "申诉原因长度必须在10-1000字符之间")
    private String reason;

    /**
     * 附件列表（可选）
     */
    private List<String> attachments;

    /**
     * 备注（可选）
     */
    private String notes;
}
