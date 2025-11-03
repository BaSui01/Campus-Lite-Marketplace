package com.campus.marketplace.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 材料统计信息响应
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialStatistics {
    
    /**
     * 申诉ID
     */
    private String appealId;
    
    /**
     * 总数量
     */
    private int totalCount;
    
    /**
     * 已上传数量
     */
    private int uploadedCount;
    
    /**
     * 已处理数量
     */
    private int processedCount;
    
    /**
     * 失败数量
     */
    private int failedCount;
    
    /**
     * 已删除数量
     */
    private int deletedCount;
    
    /**
     * 其他状态数量
     */
    private int otherCount;
}
