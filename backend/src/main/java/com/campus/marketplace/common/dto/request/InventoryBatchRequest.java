package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * 库存批量更新请求
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryBatchRequest {

    /**
     * 商品ID和库存数量映射
     * Key: 商品ID, Value: 库存数量
     */
    @NotEmpty(message = "库存数据不能为空")
    private Map<Long, Integer> inventoryData;

    /**
     * 更新原因
     */
    private String updateReason;
}
