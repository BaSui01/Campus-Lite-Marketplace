package com.campus.marketplace.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 行为分析结果响应 DTO
 *
 * @author BaSui 😎
 * @date 2025-11-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BehaviorAnalysisResponse {

    /**
     * 是否为机器人
     */
    private Boolean isBot;

    /**
     * 机器人概率（0-1）
     */
    private Double botProbability;

    /**
     * 风险等级（LOW、MEDIUM、HIGH）
     */
    private RiskLevel riskLevel;

    /**
     * 分析详情（各项指标得分）
     */
    private Map<String, Object> details;

    /**
     * 建议操作（ALLOW、CHALLENGE、BLOCK）
     */
    private Action suggestedAction;

    /**
     * 风险等级枚举
     */
    public enum RiskLevel {
        LOW,      // 低风险（正常用户）
        MEDIUM,   // 中风险（可疑行为）
        HIGH      // 高风险（疑似机器人）
    }

    /**
     * 建议操作枚举
     */
    public enum Action {
        ALLOW,     // 允许通过
        CHALLENGE, // 需要额外验证（如验证码）
        BLOCK      // 直接拒绝
    }
}
