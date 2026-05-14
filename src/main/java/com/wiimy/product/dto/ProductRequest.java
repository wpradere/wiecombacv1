package com.wiimy.product.dto;

import com.wiimy.product.entity.Section;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 200, message = "Name must not exceed 200 characters")
        String name,

        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @Digits(integer = 10, fraction = 2, message = "Precio inválido: máximo 10 dígitos enteros y 2 decimales")
        BigDecimal price,

        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        String imageUrl,

        @NotNull(message = "Category is required")
        UUID categoryId,

        @NotNull(message = "Stock is required")
        @Min(value = 0, message = "Stock cannot be negative")
        Integer stock,

        @Size(max = 20, message = "Edition label must not exceed 20 characters")
        String edition,

        Boolean featured,

        Boolean active,

        Section section
) {}
