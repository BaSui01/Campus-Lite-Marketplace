package com.campus.marketplace.service.batch;

import com.campus.marketplace.common.enums.BatchType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 批量处理器工厂
 * 策略模式实现，支持插件化扩展
 * 
 * @author BaSui
 * @date 2025-11-02
 */
@Slf4j
@Component
public class BatchProcessorFactory {

    private final Map<BatchType, BatchProcessor> processorMap = new ConcurrentHashMap<>();
    private final ApplicationContext applicationContext;

    public BatchProcessorFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        initProcessors();
    }

    /**
     * 初始化处理器（自动发现所有BatchProcessor实现）
     */
    private void initProcessors() {
        Map<String, BatchProcessor> processors = applicationContext.getBeansOfType(BatchProcessor.class);
        for (BatchProcessor processor : processors.values()) {
            BatchType type = processor.getSupportedType();
            processorMap.put(type, processor);
            log.info("注册批量处理器: {} -> {}", type, processor.getClass().getSimpleName());
        }
    }

    /**
     * 获取处理器
     * 
     * @param batchType 批量类型
     * @return 对应的处理器
     * @throws IllegalArgumentException 如果类型不支持
     */
    public BatchProcessor getProcessor(BatchType batchType) {
        BatchProcessor processor = processorMap.get(batchType);
        if (processor == null) {
            throw new IllegalArgumentException("不支持的批量操作类型: " + batchType);
        }
        return processor;
    }

    /**
     * 检查是否支持指定类型
     */
    public boolean supports(BatchType batchType) {
        return processorMap.containsKey(batchType);
    }

    /**
     * 获取所有支持的类型
     */
    public java.util.Set<BatchType> getSupportedTypes() {
        return processorMap.keySet();
    }
}
