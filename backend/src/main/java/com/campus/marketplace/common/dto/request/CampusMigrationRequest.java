package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CampusMigrationRequest {
    @NotNull
    private Long fromCampusId;
    @NotNull
    private Long toCampusId;
}
