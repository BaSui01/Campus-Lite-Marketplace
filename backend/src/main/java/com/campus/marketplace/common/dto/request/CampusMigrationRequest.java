package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Campus Migration Request
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Data
public class CampusMigrationRequest {
    @NotNull
    private Long fromCampusId;
    @NotNull
    private Long toCampusId;
}
