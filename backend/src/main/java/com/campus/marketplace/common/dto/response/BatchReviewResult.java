package com.campus.marketplace.common.dto.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量审核结果DTO
 * 
 * @author BaSui
 * @date 2025-2025-11-02
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchReviewResult {

    /**
     * 总数量
     */
    private int totalCount;

    /**
     * 成功数量
     */
    private int successCount;

    /**
     * 失败数量  
     */
    private int failureCount;

    /**
     * 成功处理的申诉ID列表
     */
    @Builder.Default
    private List<Long> successIds = new ArrayList<>();

    /**
     * 失败详情列表
     */
    @Builder.Default
    private List<BatchError> errors = new ArrayList<>();

    /**
     * 批量操作ID
     */
    private String batchId;

    /**
     * 处理耗时（毫秒）
     */
    private long processingTimeMs;

    /**
     * 是否有部分成功
     */
    public boolean hasPartialSuccess() {
        return successCount > 0 && failureCount > 0;
    }

    /**
     * 是否全部成功
     */
    public boolean isAllSuccess() {
        return failureCount == 0 && successCount > 0;
    }

    /**
     * 是否有失败
     */
    public boolean hasFailures() {
        return failureCount > 0;
    }
}
