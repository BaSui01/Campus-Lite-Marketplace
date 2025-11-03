package com.campus.marketplace.revert.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 撤销验证结果
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevertValidationResult {
    
    /**
     * 是否通过验证
     */
    private boolean valid;
    
    /**
     * 验证消息
     */
    private String message;
    
    /**
     * 验证级别（SUCCESS/WARNING/ERROR）
     */
    @Builder.Default
    private ValidationLevel level = ValidationLevel.SUCCESS;
    
    /**
     * 创建成功的验证结果
     */
    public static RevertValidationResult success() {
        return RevertValidationResult.builder()
                .valid(true)
                .message("验证通过")
                .level(ValidationLevel.SUCCESS)
                .build();
    }
    
    /**
     * 创建成功的验证结果（带消息）
     */
    public static RevertValidationResult success(String message) {
        return RevertValidationResult.builder()
                .valid(true)
                .message(message)
                .level(ValidationLevel.SUCCESS)
                .build();
    }
    
    /**
     * 创建警告的验证结果（可以继续但需注意）
     */
    public static RevertValidationResult warning(String message) {
        return RevertValidationResult.builder()
                .valid(true)
                .message(message)
                .level(ValidationLevel.WARNING)
                .build();
    }
    
    /**
     * 创建失败的验证结果
     */
    public static RevertValidationResult failed(String message) {
        return RevertValidationResult.builder()
                .valid(false)
                .message(message)
                .level(ValidationLevel.ERROR)
                .build();
    }
    
    /**
     * 验证级别枚举
     */
    public enum ValidationLevel {
        SUCCESS,
        WARNING,
        ERROR
    }
}
