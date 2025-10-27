package com.campus.marketplace.common.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCampusRequest {
    @NotBlank
    private String code;

    @NotBlank
    private String name;
}
