package com.campus.marketplace.service.batch.processor;

import com.campus.marketplace.common.dto.request.PriceBatchRequest;
import com.campus.marketplace.common.entity.BatchTaskItem;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.enums.BatchType;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 价格批量调整处理器
 * 处理商品价格批量调整
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PriceBatchProcessor implements BatchProcessor {

    private final GoodsRepository goodsRepository;
    private final AuditLogService auditLogService;
    private final CacheService cacheService;
    private final ObjectMapper objectMapper;

    @Override
    public BatchType getSupportedType() {
        return BatchType.PRICE_BATCH;
    }

    @Override
    @Transactional
    public BatchItemResult processItem(BatchTaskItem item) {
        try {
            // 解析请求数据
            PriceBatchRequest request = objectMapper.readValue(
                item.getInputData(), 
                PriceBatchRequest.class
            );

            // 获取商品
            Optional<Goods> goodsOpt = goodsRepository.findById(item.getTargetId());
            if (goodsOpt.isEmpty()) {
                return new BatchItemResult(false, "商品不存在: " + item.getTargetId(), null);
            }

            Goods goods = goodsOpt.get();

            // 计算新价格
            BigDecimal newPrice = calculateNewPrice(goods.getPrice(), request);

            // 验证价格调整幅度（不超过200%）
            BigDecimal changePercent = newPrice.subtract(goods.getPrice())
                .divide(goods.getPrice(), 4, RoundingMode.HALF_UP)
                .abs()
                .multiply(BigDecimal.valueOf(100));

            if (changePercent.compareTo(BigDecimal.valueOf(200)) > 0) {
                return new BatchItemResult(false, "价格调整幅度超过200%，当前幅度: " + changePercent + "%", null);
            }

            // 验证新价格必须大于0
            if (newPrice.compareTo(BigDecimal.ZERO) <= 0) {
                return new BatchItemResult(false, "调整后价格必须大于0", null);
            }

            BigDecimal oldPrice = goods.getPrice();

            // 更新商品价格
            goods.setPrice(newPrice);
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
                Map.of("price", oldPrice),
                Map.of("price", newPrice)
            );

            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("goodsId", goods.getId());
            result.put("oldPrice", oldPrice);
            result.put("newPrice", newPrice);
            result.put("changePercent", changePercent);

            return new BatchItemResult(true, "价格调整成功", result);

        } catch (Exception e) {
            log.error("处理价格批量调整失败: itemId={}", item.getId(), e);
            return new BatchItemResult(false, "处理失败: " + e.getMessage(), null);
        }
    }

    /**
     * 计算新价格
     */
    private BigDecimal calculateNewPrice(BigDecimal oldPrice, PriceBatchRequest request) {
        if (request.getAdjustType() == PriceBatchRequest.PriceAdjustType.PERCENTAGE) {
            // 百分比调整
            BigDecimal changeAmount = oldPrice.multiply(request.getAdjustValue())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            return oldPrice.add(changeAmount);
        } else {
            // 固定金额调整
            return oldPrice.add(request.getAdjustValue());
        }
    }
}
