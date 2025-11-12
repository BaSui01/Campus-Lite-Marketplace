package com.campus.marketplace.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 🎯 BaSui 的推荐配置 - 从 application.yml 读取！😎
 *
 * 配置项：
 * - enabled: 是否启用推荐功能
 * - algorithm: 推荐算法（HYBRID/CF/HOT等）
 * - weights: 各算法权重
 * - params: 推荐参数
 *
 * @author BaSui 😎
 * @date 2025-11-08
 */
@Component
@ConfigurationProperties(prefix = "recommend")
@Data
public class RecommendConfigProperties {

    /**
     * 是否启用推荐功能
     */
    private Boolean enabled = true;

    /**
     * 推荐算法类型
     * - HYBRID: 混合推荐（协同过滤+内容+热门+个性化）
     * - CF: 协同过滤
     * - HOT: 热门榜单
     * - PERSONALIZED: 个性化推荐
     */
    private String algorithm = "HYBRID";

    /**
     * 算法权重配置
     */
    private WeightsConfig weights = new WeightsConfig();

    /**
     * 推荐参数配置
     */
    private ParamsConfig params = new ParamsConfig();

    /**
     * 算法权重配置
     */
    @Data
    public static class WeightsConfig {
        /**
         * 协同过滤权重
         */
        private Double collaborative = 0.4;

        /**
         * 内容推荐权重
         */
        private Double content = 0.3;

        /**
         * 热门榜单权重
         */
        private Double hot = 0.2;

        /**
         * 个性化推荐权重
         */
        private Double personalized = 0.1;
    }

    /**
     * 推荐参数配置
     */
    @Data
    public static class ParamsConfig {
        /**
         * 最大推荐数量
         */
        private Integer maxRecommendations = 20;

        /**
         * 最低推荐分数阈值
         */
        private Double minScore = 0.3;

        /**
         * 刷新间隔（秒）
         */
        private Integer refreshInterval = 3600;
    }
}
