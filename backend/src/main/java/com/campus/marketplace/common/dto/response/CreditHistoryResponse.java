package com.campus.marketplace.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 信用历史记录响应 DTO
 * 
 * @author BaSui 😎
 * @date 2025-11-11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditHistoryResponse {
    
    private Long id;
    private String changeType;
    private Integer changeValue;
    private String reason;
    private LocalDateTime createdAt;
}
