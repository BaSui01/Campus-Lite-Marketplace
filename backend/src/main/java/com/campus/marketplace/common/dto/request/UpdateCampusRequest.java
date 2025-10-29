package com.campus.marketplace.common.dto.request;

import com.campus.marketplace.common.enums.CampusStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Update Campus Request
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Data
public class UpdateCampusRequest {
    @NotBlank
    private String name;

    @NotNull
    private CampusStatus status;
}
