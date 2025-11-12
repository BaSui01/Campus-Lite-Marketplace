package com.campus.marketplace.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 推荐配置DTO
 *
 * @author BaSui 😎
 * @date 2025-11-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendConfigDTO {

    /**
     * 是否启用推荐功能
     */
    private Boolean enabled;

    /**
     * 推荐算法类型
     */
    private String algorithm;

    /**
     * 算法权重配置
     */
    private Map<String, Double> weights;

    /**
     * 推荐参数配置
     */
    private Map<String, Object> params;
}
