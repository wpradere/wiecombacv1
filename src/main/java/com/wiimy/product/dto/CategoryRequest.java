package com.wiimy.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 50, message = "Name must not exceed 50 characters")
        String name,

        @Size(max = 100, message = "Label must not exceed 100 characters")
        String label,

        Boolean active
) {}
