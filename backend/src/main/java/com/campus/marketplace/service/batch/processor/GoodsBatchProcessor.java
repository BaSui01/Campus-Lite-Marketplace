package com.campus.marketplace.service.batch.processor;

import com.campus.marketplace.common.dto.request.GoodsBatchRequest;
import com.campus.marketplace.common.entity.BatchTaskItem;
import com.campus.marketplace.common.entity.Goods;
import com.campus.marketplace.common.enums.BatchOperationType;
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
 * 商品批量处理器
 * 处理商品批量上下架、删除等操作
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoodsBatchProcessor implements BatchProcessor {

    private final GoodsRepository goodsRepository;
    private final AuditLogService auditLogService;
    private final CacheService cacheService;
    private final ObjectMapper objectMapper;

    @Override
    public BatchType getSupportedType() {
        return BatchType.GOODS_BATCH;
    }

    @Override
    @Transactional
    public BatchItemResult processItem(BatchTaskItem item) {
        try {
            // 解析请求数据
            GoodsBatchRequest request = objectMapper.readValue(
                item.getInputData(), 
                GoodsBatchRequest.class
            );

            // 获取商品
            Optional<Goods> goodsOpt = goodsRepository.findById(item.getTargetId());
            if (goodsOpt.isEmpty()) {
                return new BatchItemResult(false, "商品不存在: " + item.getTargetId(), null);
            }

            Goods goods = goodsOpt.get();

            // 根据操作类型执行
            return switch (request.getOperation()) {
                case BATCH_ONLINE -> batchOnline(goods, request);
                case BATCH_OFFLINE -> batchOffline(goods, request);
                case BATCH_DELETE -> batchDelete(goods, request);
                default -> new BatchItemResult(false, "不支持的操作类型", null);
            };

        } catch (Exception e) {
            log.error("处理商品批量操作失败: itemId={}", item.getId(), e);
            return new BatchItemResult(false, "处理失败: " + e.getMessage(), null);
        }
    }

    /**
     * 批量上架
     */
    private BatchItemResult batchOnline(Goods goods, GoodsBatchRequest request) {
        // 验证商品状态
        if (goods.getStatus() == GoodsStatus.APPROVED) {
            return new BatchItemResult(false, "商品已上架", null);
        }

        if (goods.getStatus() == GoodsStatus.REJECTED) {
            return new BatchItemResult(false, "商品已被拒绝，无法上架", null);
        }

        // 验证商品完整性
        if (goods.getPrice() == null || goods.getPrice().signum() <= 0) {
            return new BatchItemResult(false, "商品价格无效", null);
        }

        GoodsStatus oldStatus = goods.getStatus();

        // 更新商品状态
        goods.setStatus(GoodsStatus.APPROVED);
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
            Map.of("status", oldStatus),
            Map.of("status", GoodsStatus.APPROVED)
        );

        Map<String, Object> result = new HashMap<>();
        result.put("goodsId", goods.getId());
        result.put("oldStatus", oldStatus);
        result.put("newStatus", GoodsStatus.APPROVED);

        return new BatchItemResult(true, "商品上架成功", result);
    }

    /**
     * 批量下架
     */
    private BatchItemResult batchOffline(Goods goods, GoodsBatchRequest request) {
        // 验证商品状态
        if (goods.getStatus() == GoodsStatus.OFFLINE) {
            return new BatchItemResult(false, "商品已下架", null);
        }

        if (goods.getStatus() == GoodsStatus.SOLD) {
            return new BatchItemResult(false, "商品已售出，无法下架", null);
        }

        GoodsStatus oldStatus = goods.getStatus();

        // 更新商品状态
        goods.setStatus(GoodsStatus.OFFLINE);
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
            Map.of("status", oldStatus),
            Map.of("status", GoodsStatus.OFFLINE)
        );

        Map<String, Object> result = new HashMap<>();
        result.put("goodsId", goods.getId());
        result.put("oldStatus", oldStatus);
        result.put("newStatus", GoodsStatus.OFFLINE);

        return new BatchItemResult(true, "商品下架成功", result);
    }

    /**
     * 批量删除（软删除）
     */
    private BatchItemResult batchDelete(Goods goods, GoodsBatchRequest request) {
        // 验证商品状态
        if (goods.getStatus() == GoodsStatus.SOLD) {
            return new BatchItemResult(false, "已售出商品无法删除", null);
        }

        if (goods.isDeleted()) {
            return new BatchItemResult(false, "商品已删除", null);
        }

        // 软删除
        goods.markDeleted();
        goodsRepository.save(goods);

        // 清除缓存
        cacheService.delete("goods:" + goods.getId());

        // 记录审计日志
        auditLogService.logEntityChange(
            SecurityUtil.getCurrentUserId(),
            SecurityUtil.getCurrentUsername(),
            com.campus.marketplace.common.enums.AuditActionType.GOODS_DELETE,
            "Goods",
            goods.getId(),
            null,
            null
        );

        Map<String, Object> result = new HashMap<>();
        result.put("goodsId", goods.getId());
        result.put("deleted", true);

        return new BatchItemResult(true, "商品删除成功", result);
    }
}
