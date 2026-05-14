package com.wiimy.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductAttributeRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 500) String value,
        int sortOrder
) {}
