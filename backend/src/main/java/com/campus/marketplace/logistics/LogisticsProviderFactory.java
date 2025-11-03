package com.campus.marketplace.logistics;

import com.campus.marketplace.common.enums.LogisticsCompany;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 物流服务提供商工厂
 * <p>
 * 使用策略模式 + 工厂模式，根据快递公司枚举自动选择对应的实现类。
 * Spring会自动注入所有 LogisticsProvider 实现类，无需手动注册。
 * </p>
 *
 * @author BaSui 😎
 * @since 2025-11-03
 */
@Slf4j
@Component
public class LogisticsProviderFactory {

    /**
     * 物流服务提供商映射表
     * <p>
     * Key: 快递公司枚举
     * Value: 对应的实现类
     * </p>
     */
    private final Map<LogisticsCompany, LogisticsProvider> providerMap;

    /**
     * 构造函数（Spring自动注入所有实现类）
     *
     * @param providers 所有 LogisticsProvider 实现类
     */
    public LogisticsProviderFactory(List<LogisticsProvider> providers) {
        this.providerMap = providers.stream()
                .collect(Collectors.toMap(
                        LogisticsProvider::getSupportedCompany,
                        Function.identity()
                ));

        log.info("物流服务提供商工厂初始化完成，已注册 {} 个快递公司", providerMap.size());
        providerMap.keySet().forEach(company ->
                log.info("  - {}: {}", company.getDisplayName(), company.getCode())
        );
    }

    /**
     * 获取物流服务提供商
     *
     * @param company 快递公司枚举
     * @return 对应的实现类
     * @throws IllegalArgumentException 当快递公司不支持时抛出异常
     */
    public LogisticsProvider getProvider(LogisticsCompany company) {
        LogisticsProvider provider = providerMap.get(company);
        if (provider == null) {
            throw new IllegalArgumentException(
                    String.format("不支持的快递公司: %s (%s)", company.getDisplayName(), company.getCode())
            );
        }
        return provider;
    }

    /**
     * 检查是否支持指定快递公司
     *
     * @param company 快递公司枚举
     * @return true=支持，false=不支持
     */
    public boolean isSupported(LogisticsCompany company) {
        return providerMap.containsKey(company);
    }

    /**
     * 获取所有支持的快递公司
     *
     * @return 快递公司列表
     */
    public List<LogisticsCompany> getSupportedCompanies() {
        return List.copyOf(providerMap.keySet());
    }
}
