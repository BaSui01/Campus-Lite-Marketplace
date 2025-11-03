package com.campus.marketplace.service.batch.processor;

import com.campus.marketplace.common.dto.request.InventoryBatchRequest;
import com.campus.marketplace.common.entity.BatchTaskItem;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.enums.BatchType;
import com.campus.marketplace.common.enums.GoodsStatus;
import com.campus.marketplace.common.utils.SecurityUtil;
import com.campus.marketplace.repository.GoodsRepository;
import com.campus.marketplace.service.AuditLogService;
import com.campus.marketplace.service.CacheService;
import com.campus.marketplace.service.batch.BatchProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 库存批量更新处理器（二手商品场景简化版）
 * 对于二手商品，库存设置为0时标记为已售罄
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryBatchProcessor implements BatchProcessor {

    private final GoodsRepository goodsRepository;
    private final AuditLogService auditLogService;
    private final CacheService cacheService;
    private final ObjectMapper objectMapper;

    @Override
    public BatchType getSupportedType() {
        return BatchType.INVENTORY_BATCH;
    }

    @Override
    @Transactional
    public BatchItemResult processItem(BatchTaskItem item) {
        try {
            // 解析请求数据
            InventoryBatchRequest request = objectMapper.readValue(
                item.getInputData(), 
                InventoryBatchRequest.class
            );

            // 获取商品
            Optional<Goods> goodsOpt = goodsRepository.findById(item.getTargetId());
            if (goodsOpt.isEmpty()) {
                return new BatchItemResult(false, "商品不存在: " + item.getTargetId(), null);
            }

            Goods goods = goodsOpt.get();

            // 获取库存数量
            Integer inventoryCount = request.getInventoryData().get(item.getTargetId());
            if (inventoryCount == null) {
                return new BatchItemResult(false, "未提供库存数据", null);
            }

            // 验证库存数量
            if (inventoryCount < 0) {
                return new BatchItemResult(false, "库存数量不能为负数", null);
            }

            GoodsStatus oldStatus = goods.getStatus();

            // 对于二手商品，库存为0时标记为已售罄
            if (inventoryCount == 0) {
                goods.setStatus(GoodsStatus.SOLD);
            } else if (goods.getStatus() == GoodsStatus.SOLD) {
                // 如果商品已售出但库存大于0，恢复为上架状态
                goods.setStatus(GoodsStatus.APPROVED);
            }

            goodsRepository.save(goods);

            // 清除缓存
            cacheService.delete("goods:" + goods.getId());

            // 记录审计日志
            auditLogService.logEntityChange(
                SecurityUtil.getCurrentUserId(),
                SecurityUtil.getCurrentUsername(),
                com.campus.marketplace.common.enums.AuditActionType.GOODS_APPROVE,
                "Goods",
                goods.getId(),
                Map.of("status", oldStatus, "inventory", "previous"),
                Map.of("status", goods.getStatus(), "inventory", inventoryCount)
            );

            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("goodsId", goods.getId());
            result.put("oldStatus", oldStatus);
            result.put("newStatus", goods.getStatus());
            result.put("inventoryCount", inventoryCount);

            return new BatchItemResult(true, "库存更新成功", result);

        } catch (Exception e) {
            log.error("处理库存批量更新失败: itemId={}", item.getId(), e);
            return new BatchItemResult(false, "处理失败: " + e.getMessage(), null);
        }
    }
}
