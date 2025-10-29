package com.campus.marketplace.common.dto;

import lombok.Data;

import java.util.List;

/**
 * Feature Flag Rules
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Data
public class FeatureFlagRules {
    private List<String> allowEnvs;
    private List<Long> allowUserIds;
    private List<Long> allowCampusIds;
}
