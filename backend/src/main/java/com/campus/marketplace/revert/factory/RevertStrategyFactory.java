package com.campus.marketplace.revert.factory;

import com.campus.marketplace.common.entity.AuditLog;
import com.campus.marketplace.common.enums.AuditEntityType;
import com.campus.marketplace.revert.strategy.RevertStrategy;
import com.campus.marketplace.revert.strategy.impl.BatchRevertStrategy;
import com.campus.marketplace.revert.strategy.impl.GoodsRevertStrategy;
import com.campus.marketplace.revert.strategy.impl.OrderRevertStrategy;
import com.campus.marketplace.revert.strategy.impl.UserRevertStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 撤销策略工厂 - 策略模式核心
 * 
 * 功能说明：
 * 1. 管理所有撤销策略的注册和获取
 * 2. 根据实体类型自动匹配对应策略
 * 3. 支持插件化扩展新策略
 * 4. 提供策略列表查询功能
 * 
 * @author BaSui
 * @date 2025-11-03
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevertStrategyFactory {
    
    private final Map<String, RevertStrategy> strategyMap = new HashMap<>();
    private final List<RevertStrategy> strategies;
    
    /**
     * 构造后初始化 - 自动注册所有Spring管理的策略Bean
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        for (RevertStrategy strategy : strategies) {
            String entityType = strategy.getSupportedEntityType();
            strategyMap.put(entityType, strategy);
            log.info("注册撤销策略: {}", entityType);
        }
        log.info("撤销策略注册完成: 共注册 {} 个策略", strategyMap.size());
    }
    
    /**
     * 根据审计日志获取对应的撤销策略
     * 
     * @param auditLog 审计日志
     * @return 撤销策略
     * @throws IllegalArgumentException 如果实体类型不支持撤销
     */
    public RevertStrategy getStrategy(AuditLog auditLog) {
        if (auditLog == null || auditLog.getEntityType() == null) {
            throw new IllegalArgumentException("审计日志或实体类型不能为空");
        }
        
        String entityType = auditLog.getEntityType().name();
        RevertStrategy strategy = strategyMap.get(entityType);
        
        if (strategy == null) {
            throw new IllegalArgumentException(
                String.format("不支持的撤销实体类型: %s", entityType)
            );
        }
        
        return strategy;
    }
    
    /**
     * 检查实体类型是否支持撤销
     * 
     * @param entityType 实体类型
     * @return 是否支持
     */
    public boolean isSupported(AuditEntityType entityType) {
        if (entityType == null) {
            return false;
        }
        return strategyMap.containsKey(entityType.name());
    }
    
    /**
     * 获取所有支持的实体类型
     * 
     * @return 实体类型集合
     */
    public Set<String> getSupportedEntityTypes() {
        return Collections.unmodifiableSet(strategyMap.keySet());
    }
    
    /**
     * 获取策略总数
     * 
     * @return 策略数量
     */
    public int getStrategyCount() {
        return strategyMap.size();
    }
}
