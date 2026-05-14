package com.wiimy.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductImageRequest(
        @NotBlank @Size(max = 500) String imageUrl,
        @Size(max = 200) String altText,
        String description,
        int sortOrder
) {}
