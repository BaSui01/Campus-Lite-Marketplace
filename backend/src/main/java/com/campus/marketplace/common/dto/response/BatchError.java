package com.campus.marketplace.common.dto.response;

import lombok.*;

/**
 * 批量操作错误项DTO
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchError {

    /**
     * 错误的申诉ID
     */
    private Long appealId;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 错误详情（可选）
     */
    private String details;
}
