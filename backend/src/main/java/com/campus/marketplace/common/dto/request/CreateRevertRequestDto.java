package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建撤销请求DTO
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRevertRequestDto {
    
    /**
     * 撤销原因
     */
    @NotBlank(message = "撤销原因不能为空")
    @Size(max = 500, message = "撤销原因不能超过500字符")
    private String reason;
}
