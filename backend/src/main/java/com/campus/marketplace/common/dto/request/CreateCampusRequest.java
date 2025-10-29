package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Create Campus Request
 *
 * @author BaSui
 * @date 2025-10-29
 */

@Data
public class CreateCampusRequest {
    @NotBlank
    private String code;

    @NotBlank
    private String name;
}
