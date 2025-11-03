package com.campus.marketplace.common.dto.request;

import com.campus.marketplace.common.enums.BatchOperationType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商品批量操作请求
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsBatchRequest {

    /**
     * 操作类型
     */
    @NotNull(message = "操作类型不能为空")
    private BatchOperationType operation;

    /**
     * 目标商品ID列表
     */
    @NotEmpty(message = "商品ID列表不能为空")
    private List<Long> targetIds;

    /**
     * 操作原因（可选）
     */
    private String reason;
}
