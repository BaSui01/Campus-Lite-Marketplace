package com.campus.marketplace.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class FeatureFlagRules {
    private List<String> allowEnvs;
    private List<Long> allowUserIds;
    private List<Long> allowCampusIds;
}
