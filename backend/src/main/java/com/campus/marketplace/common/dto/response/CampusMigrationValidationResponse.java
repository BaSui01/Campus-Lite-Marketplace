package com.campus.marketplace.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Campus Migration Validation Response
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampusMigrationValidationResponse {
    private Long fromCampusId;
    private Long toCampusId;
    private long goodsCount;
    private long postCount;
    private long orderCount;
    private long userCount;
    private boolean allowed;
    private String reason;
}
