package com.campus.marketplace.revert.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 撤销执行结果
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevertExecutionResult {
    
    /**
     * 是否执行成功
     */
    private boolean success;
    
    /**
     * 结果消息
     */
    private String message;
    
    /**
     * 撤销的实体ID
     */
    private Long entityId;
    
    /**
     * 撤销的实体类型
     */
    private String entityType;
    
    /**
     * 执行耗时（毫秒）
     */
    private Long executionTime;
    
    /**
     * 附加数据
     */
    private Object additionalData;
    
    /**
     * 创建成功的执行结果
     */
    public static RevertExecutionResult success(String message, Long entityId) {
        return RevertExecutionResult.builder()
                .success(true)
                .message(message)
                .entityId(entityId)
                .build();
    }
    
    /**
     * 创建失败的执行结果
     */
    public static RevertExecutionResult failed(String message) {
        return RevertExecutionResult.builder()
                .success(false)
                .message(message)
                .build();
    }
    
    /**
     * 创建失败的执行结果（带实体ID）
     */
    public static RevertExecutionResult failed(String message, Long entityId) {
        return RevertExecutionResult.builder()
                .success(false)
                .message(message)
                .entityId(entityId)
                .build();
    }
}
